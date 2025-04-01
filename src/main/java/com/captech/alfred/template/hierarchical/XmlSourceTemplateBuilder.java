/*
 * Copyright 2018 CapTech Ventures, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.captech.alfred.template.hierarchical;

import com.captech.alfred.dataConnections.DataStoreService;
import com.captech.alfred.exceptions.AppInternalError;
import com.captech.alfred.exceptions.RequiredInfoMissing;
import com.captech.alfred.template.Field;
import com.captech.alfred.template.Template;
import com.captech.alfred.template.XMLFile;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

@Component("xmlSource")
public class XmlSourceTemplateBuilder implements SourceTemplateBuilder {
    private static final Logger logger = LoggerFactory.getLogger(XmlSourceTemplateBuilder.class);

    @Autowired
    DataStoreService dataConnection;

    @Override
    public Template preFill(String sampleFile, Template template) {
        Document document = parseInput(sampleFile);
        if (document == null) {
            // TODO: make this an HTTP error
            logger.error("no sample data - document is null");
            throw new RequiredInfoMissing();
        }
        NodeList nodes = document.getChildNodes();
        if (nodes == null) {
            logger.error("no sample data - nodes missing");
            throw new RequiredInfoMissing();
        }
        Boolean hasDefaultNamespaces = null;
        Map<String, Field> fieldMap = new TreeMap<>();
        for (int item = 0; item < nodes.getLength(); item++) {
            Node node = nodes.item(item);
            fieldMap.putAll(walk(node, fieldMap, new ArrayList<String>()));
            if (hasDefaultNamespaces == null) {
                hasDefaultNamespaces = checkForDefaultNamespace(node);
            }
        }
        ((XMLFile) template.getFile().getTechnical()).setIgnoreNamespace(false);
        if (hasDefaultNamespaces != null && hasDefaultNamespaces) {
            ((XMLFile) template.getFile().getTechnical()).setIgnoreNamespace(true);
        }

        // at this point, we've put all the possible Fields in the fieldMap

        // Grab all the fields marked as PK
        ArrayList<Integer> pk = new ArrayList<>();
        for (Field field : fieldMap.values()) {
            if (field.getPk() != null && field.getPk()) {
                pk.add(field.getPosition());
            }
        }

        // TODO For all the other fields that are below a PK's parent, update
        // the XPath to include the PK selector
        // TODO need to work through the finer points of this with Brian

        for (Field field : fieldMap.values()) {
            // we are passing references, so updates to existing fields are
            // already in the Template
            if (pk.contains(field.getParent())) {
                // if the root element is the parent, we do not want it in a
                // struct, we want a separate column
                field.setParent(null);
            }
            if (!template.getFields().contains(field)) {
                String name = field.getName();
                if (field.getName().startsWith(field.getNamespace() + "_")) {
                    field.setName(name.substring(field.getNamespace().length() + 1));
                }
                template.addField(field);
            }
        }
        return template;
    }

    private Boolean checkForDefaultNamespace(Node node) {
        Boolean returnVal = null;
        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            for (int item = 0; item < attributes.getLength(); item++) {
                Attr attr = (Attr) attributes.item(item);
                if (attr.getName().startsWith("xmlns")) {
                    // do not return yet. if there are other namespaces, we want
                    // to return false
                    returnVal = true;
                    if (attr.getName().startsWith("xmlns:")) {
                        // don't need to keep checking
                        return false;
                    }

                }
            }
        }
        return returnVal;
    }

    Map<String, Field> walk(Node node, Map<String, Field> fieldMap, ArrayList<String> namespaces) {
        // check to see if node is element. If it is, what kind of children does
        // it have?
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element elemNode = (Element) node;
            Field field = new Field();
            String path = XPaths.getSimplePath(node);
            if (fieldMap.containsKey(path)) {
                field = fieldMap.get(path);
            } else {
                field.setName(XPaths.scrubInvalidNameChars(path));
                field.setSourceXpath(path);
                field.setPosition(fieldMap.size() + 1);
                if (node.getParentNode() != null && node.getParentNode().getNodeType() == Node.ELEMENT_NODE
                        && fieldMap.containsKey(XPaths.getSimplePath(node.getParentNode()) + "/"
                        + ((Element) node.getParentNode()).getTagName())) {
                    field.setParent(fieldMap.get(XPaths.getSimplePath(node.getParentNode()) + "/"
                            + ((Element) node.getParentNode()).getTagName()).getPosition());
                } else if (node.getParentNode() != null
                        && fieldMap.containsKey(XPaths.getSimplePath(node.getParentNode()))) {
                    field.setParent(fieldMap.get(XPaths.getSimplePath(node.getParentNode())).getPosition());
                }
            }
            if (node.hasChildNodes()) {
                for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                    Node childNode = node.getChildNodes().item(i);
                    // if Text node, this is a simple type and should be the
                    // only node on the element node
                    if (childNode.getNodeType() == Node.TEXT_NODE) {
                        Text text = (Text) childNode;
                        if (!StringUtils.isBlank(childNode.getNodeValue())) {
                            if (!fieldMap.containsKey(path)) {
                                field.setDatatype(DataTypes.guessDataType(text.getNodeValue()));
                                // get outta here
                                break;
                            } else {
                                String currentGuess = DataTypes.guessDataType(text.getNodeValue());
                                String lastGuess = fieldMap.get(path).getDatatype();
                                if (DataTypes.getRankOrder(currentGuess) > DataTypes.getRankOrder(lastGuess)) {
                                    fieldMap.get(path).setDatatype(currentGuess);
                                }
                            }
                        }
                    }
                    if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                        // this is a complex type; find out if struct or array
                        // if we don't already know
                        // compare all the child names. If any of them are
                        // different, it is a struct. If all are the same, it is
                        // an array
                        // UPDATE: because we cannot tell if something is an
                        // array or not for certain, everything is now an array.
                        if (StringUtils.isBlank(field.getDatatype())) {
                            field.setDatatype("array");
                        }

                        // if an element node has child elements, I need an
                        // extra array field that points to the contents.
                        // Example: I have <elementA
                        // attr="value"><elementB>value</elementB></elementA>
                        // I need fields with the following xpaths: /elementA;
                        // /elementA/attr; /elementA/elementB;
                        // elementA/elementB/elementB
                        // while the 3rd is not a valid xpath, it is to get into
                        // the value of that with the SerDe
                        Field subField = new Field();
                        if (!fieldMap.containsKey(field.getSourceXpath() + "/" + elemNode.getTagName())) {
                            subField.setPosition(field.getPosition() + 1);
                            subField.setParent(field.getPosition());
                            subField.setName(field.getName() + "_" + elemNode.getTagName());
                            subField.setSourceXpath(field.getSourceXpath() + "/" + elemNode.getTagName());
                            subField.setDatatype(field.getDatatype());
                            fieldMap.put(subField.getSourceXpath(), subField);
                        }
                    } // ignore everything else like comment nodes

                }
                // end if node has child nodes
            }
            fieldMap.put(path, field);
        }
        // get the attributes
        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            Field owner = null;
            for (int item = 0; item < attributes.getLength(); item++) {

                Field field = new Field();

                Attr attr = (Attr) attributes.item(item);
                if (owner == null) {
                    owner = fieldMap.get(XPaths.getSimplePath(attr.getOwnerElement()));
                }
                // if nothing up top caught it, it is most likely empty with
                // just attributes
                if (owner != null && StringUtils.isEmpty(owner.getDatatype())) {
                    owner.setDatatype("struct");
                }
                // ignore default ns
                if (attr.getName().startsWith("xmlns")) {
                    // don't add a field for namespace declarations, but save it
                    // so we can filter later
                    if (attr.getName().startsWith("xmlns:")) {
                        String ns = attr.getName().split(":")[1];
                        namespaces.add(ns);
                    }

                } else {
                    String attrValue = attr.getValue();
                    String path = XPaths.getSimplePath(attr);
                    String namespace = null;
                    for (String ns : namespaces) {
                        if (path.contains(ns + ":")) {
                            namespace = ns;
                            path.replace(ns + ":", "");
                        }
                    }
                    if (!fieldMap.containsKey(path)) {
                        if (namespace != null) {
                            field.setNamespace(namespace);
                        }
                        if (attr.getOwnerElement() != null
                                && fieldMap.containsKey(XPaths.getSimplePath(attr.getOwnerElement()))) {
                            Field ownerField = fieldMap.get(XPaths.getSimplePath(attr.getOwnerElement()));

                            if (ownerField.getParent() == null) {
                                field.setParent(ownerField.getPosition());
                            } else {
                                field.setParent(ownerField.getParent());
                            }
                        }
                        field.setName(XPaths.scrubInvalidNameChars(path));
                        field.setSourceXpath(path);
                        field.setDatatype(DataTypes.guessDataType(attrValue));
                        field.setPosition(fieldMap.size() + 1);
                        fieldMap.put(path, field);
                    } else {
                        // See if we have a better guess on data type
                        String currentGuess = DataTypes.guessDataType(attrValue);
                        String lastGuess = fieldMap.get(path).getDatatype();
                        if (DataTypes.getRankOrder(currentGuess) > DataTypes.getRankOrder(lastGuess)) {
                            fieldMap.get(path).setDatatype(currentGuess);
                        }
                    }
                }
            }
        }

        // are there children
        // TODO: turn these all into addAlls instead
        if (node.hasChildNodes()) {
            for (int item = 0; item < node.getChildNodes().getLength(); item++) {
                fieldMap.putAll(walk(node.getChildNodes().item(item), fieldMap, namespaces));
            }
        }
        return fieldMap;
    }

    Document parseInput(String rawXml) {
        InputStream inputStream = new ByteArrayInputStream(rawXml.getBytes());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        try {
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(inputStream);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            logger.error(e.getMessage(), e);
            throw new AppInternalError("Error parsing sample XML");
        }

    }

}
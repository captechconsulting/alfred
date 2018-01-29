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

import com.captech.alfred.template.Field;
import com.captech.alfred.template.Template;
import com.captech.alfred.template.XMLFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class XmlHelperUtils {

    public static final String ARRAY = "array";
    public static final String STRUCT = "struct";

    public static Template doPutLogic(Template metadata, Template oldData) {
        XMLFile technical = ((XMLFile) metadata.getFile().getTechnical());
        if (oldData != null && technical.getIgnoreNamespace() == null) {
            technical.setIgnoreNamespace(((XMLFile) oldData.getFile().getTechnical()).getIgnoreNamespace());
        }
        Integer PK = null;
        // Position->Field
        HashMap<Integer, Field> fieldMap = new HashMap<>();
        // Parent->list of fields with parent
        HashMap<Integer, ArrayList<Field>> parentFieldMap = new HashMap<>();
        // Position->Parent of the doubled container -> potential single
        // container
        HashMap<Integer, Integer> squaredParentMap = new HashMap<>();
        // find arrays and structs whose last 2 of the xpath are the same.
        for (Field field : metadata.getFields()) {
            if (PK == null && field.getPk() != null && field.getPk()) {
                PK = field.getPosition();
            }
            // map position to field
            fieldMap.put(field.getPosition(), field);
            // map parent to list of fields
            if (field.getParent() != null) {
                if (!parentFieldMap.containsKey(field.getParent())) {
                    parentFieldMap.put(field.getParent(), new ArrayList<Field>());
                }
                parentFieldMap.get(field.getParent()).add(field);
            }
            // map array doubles to their parents to find singles -- going
            // to need to pull deleted single's parent and set it.
            if (ARRAY.equalsIgnoreCase(field.getDatatype()) || STRUCT.equalsIgnoreCase(field.getDatatype())) {
                String[] xpath = field.getSourceXpath().split("/");
                if (xpath.length > 1 && xpath[xpath.length - 1].equalsIgnoreCase(xpath[xpath.length - 2])) {
                    // I have the double.
                    squaredParentMap.put(field.getPosition(), field.getParent());
                }
            }

        }
        // get the pk out of the parent field map
        if (PK != null) {
            HashMap<String, Field> arrays = new HashMap<>();
            List<Field> attrs = new ArrayList<>();
            ArrayList<Field> pkchilds = parentFieldMap.get(PK);
            if (pkchilds != null && !pkchilds.isEmpty()) {
                for (Field pkchild : pkchilds) {
                    if (ARRAY.equalsIgnoreCase(pkchild.getDatatype())
                            || STRUCT.equalsIgnoreCase(pkchild.getDatatype())) {
                        arrays.put(pkchild.getSourceXpath(), pkchild);
                    } else {
                        attrs.add(pkchild);
                    }
                }
            }
            // if we have arrays or structs, any other child will be attrs.
            if (!arrays.isEmpty()) {
                for (Field attr : attrs) {
                    String parentxpath = attr.getSourceXpath().substring(0, attr.getSourceXpath().lastIndexOf('/'));
                    if (arrays.containsKey(parentxpath)) {
                        attr.setParent(arrays.get(parentxpath).getPosition());
                    }
                }
            }
        }
        // for each squaredParentMap item, roll through
        for (Entry<Integer, Integer> squaredParent : squaredParentMap.entrySet()) {
            // if parent is still in the main map
            if (fieldMap.get(squaredParent.getValue()) != null) {
                Field doublefield = fieldMap.get(squaredParent.getKey());
                int index = doublefield.getSourceXpath().lastIndexOf('/');
                String singlePath = doublefield.getSourceXpath().substring(0, index);
                Field singleField = fieldMap.get(squaredParent.getValue());
                // this is the single!
                // move any field whose xpath -1 matches this field's
                // xpath with parent as double's position to this
                // field's position instead.

                // all fields whose parent is double
                if (singleField.getSourceXpath().equalsIgnoreCase(singlePath) && parentFieldMap.containsKey(squaredParent.getKey())) {
                    for (Field childfield : parentFieldMap.get(squaredParent.getValue())) {
                        // get xpath
                        // if xpath - fieldname (1) matches
                        // singleField.getSourceXpath()
                        String parentXpath = childfield.getSourceXpath().substring(0,
                                childfield.getSourceXpath().lastIndexOf('/'));
                        if (singleField.getSourceXpath().equalsIgnoreCase(parentXpath)) {
                            childfield.setParent(singleField.getPosition());
                        }
                    }
                }
                // parent was deleted, go get the deleted from the old data
            } else {
                List<Field> deletedFields = oldData.getFields();
                if (deletedFields != null && !deletedFields.isEmpty()) {
                    deletedFields.removeAll(metadata.getFields());
                    for (Field deletedfield : deletedFields) {
                        if (squaredParent.getValue() == deletedfield.getPosition() && deletedfield.getParent() != null) {
                            fieldMap.get(squaredParent.getKey()).setParent(deletedfield.getParent());
                        }

                    }
                }
            }
        }
        return metadata;
    }

}

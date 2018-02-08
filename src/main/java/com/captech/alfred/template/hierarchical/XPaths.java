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

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Stack;

public class XPaths {

    private static final String INVALID_NAME_CHARS = "!@#$%^&*()[]{}+=/|?';:.,<>`~\\\" ";

    /**
     * Make the constructor private so no one can instantiate this
     */
    private XPaths() {
    }

    public static String getSimplePath(Node n) {
        // if its null, nothing to do
        if (null == n)
            return null;

        Stack<Node> hierarchy = new Stack<>();
        StringBuilder sb = new StringBuilder();

        // push the given element on stack
        hierarchy.push(n);

        Node parent = null;
        if (n.getNodeType() == Node.ATTRIBUTE_NODE) {
            parent = ((Attr) n).getOwnerElement();
        } else {
            parent = n.getParentNode();
        }

        // Collect the node hierarchy on the stack
        while (null != parent && parent.getNodeType() != Node.DOCUMENT_NODE) {
            hierarchy.push(parent);

            // next parent up
            parent = parent.getParentNode();
        }

        // construct xpath
        Object obj = null;
        while (!hierarchy.isEmpty() && null != (obj = hierarchy.pop())) {
            Node node = (Node) obj;

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                // is this the root element?
                if (sb.length() == 0) {
                    sb.append(node.getNodeName());
                } else {
                    // child element
                    sb.append("/");
                    sb.append(node.getNodeName());

                }
            } else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
                sb.append("/@");
                sb.append(node.getNodeName());
            } else if (node.getNodeType() == Node.TEXT_NODE) {
                // text element - do  nothing since we address the text by parent node name
            }
        }
        return sb.toString();
    }

    public static String scrubInvalidNameChars(String in) {
        StringBuilder sb = new StringBuilder();
        boolean leading = true;
        boolean lastValid = false;
        int pointer = 0;
        while (pointer < in.length()) {
            char c = in.charAt(pointer);
            if (leading) {
                // throw away any leading invalid characters
                if (!isValid(c)) {
                    pointer++;
                } else {
                    leading = false;
                    lastValid = true;
                }
            } else if (lastValid) {
                if (isValid(c)) {
                    sb.append(c);
                } else {
                    lastValid = false;
                }
                pointer++;
            } else {
                if (isValid(c)) {
                    lastValid = true;
                    sb.append("_");
                    sb.append(c);
                }
                pointer++;
            }
        }
        return sb.toString();
    }

    private static boolean isValid(char c) {
        return !INVALID_NAME_CHARS.contains(String.valueOf(c));
    }

    /**
     * Keeping this for reference for now, but probably will not use it in this form.
     *
     * @param n
     * @return
     */
    public static String getFullyQualifiedXPath(Node n) {
        // abort early
        if (null == n)
            return null;

        Stack<Node> hierarchy = new Stack<>();
        StringBuilder sb = new StringBuilder();

        // push element on stack
        hierarchy.push(n);

        Node parent = null;
        if (n.getNodeType() == Node.ATTRIBUTE_NODE) {
            parent = ((Attr) n).getOwnerElement();
        } else {
            parent = n.getParentNode();
        }

        while (null != parent && parent.getNodeType() != Node.DOCUMENT_NODE) {
            // push on stack
            hierarchy.push(parent);

            // get parent of parent
            parent = parent.getParentNode();
        }

        // construct xpath
        Object obj = null;
        while (!hierarchy.isEmpty() && null != (obj = hierarchy.pop())) {
            Node node = (Node) obj;
            boolean handled = false;

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) node;

                // is this the root element?
                if (sb.length() == 0) {
                    // root element - simply append element name
                    sb.append(node.getNodeName());
                } else {
                    // child element - append slash and element name
                    sb.append("/");
                    sb.append(node.getNodeName());

                    if (node.hasAttributes()) {
                        // see if the element has a name or id attribute
                        if (e.hasAttribute("id")) {
                            // id attribute found - use that
                            sb.append("[@id='" + e.getAttribute("id") + "']");
                            handled = true;
                        } else if (e.hasAttribute("name")) {
                            // name attribute found - use that
                            sb.append("[@name='" + e.getAttribute("name") + "']");
                            handled = true;
                        }
                    }

                    if (!handled) {
                        // no known attribute we could use - get sibling index
                        int prev_siblings = 1;
                        Node prev_sibling = node.getPreviousSibling();
                        while (null != prev_sibling) {
                            if (prev_sibling.getNodeType() == node.getNodeType() &&
                                    prev_sibling.getNodeName().equalsIgnoreCase(node.getNodeName())) {
                                prev_siblings++;
                            }
                            prev_sibling = prev_sibling.getPreviousSibling();
                        }
                        sb.append("[" + prev_siblings + "]");
                    }
                }
            } else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
                sb.append("/@");
                sb.append(node.getNodeName());
            }
        }
        // return buffer
        return sb.toString();
    }
}

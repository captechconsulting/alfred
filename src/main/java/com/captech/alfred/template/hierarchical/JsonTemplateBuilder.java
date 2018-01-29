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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 */

@Component("jsonSource")
public class JsonTemplateBuilder implements SourceTemplateBuilder {
    private static final Logger logger = LoggerFactory.getLogger(JsonTemplateBuilder.class);

    /**
     * Pass in any valid JSON object and a Hive schema will be returned for it.
     * You should avoid having null values in the JSON document, however.
     * <p>
     * The Hive schema columns will be printed in alphabetical order - overall
     * and within subsections.
     *
     * @param json
     * @return string Hive schema
     * @throws JSONException if the JSON does not parse correctly
     */
    @Override
    public Template preFill(String json, Template template) {

        try {
            JSONObject jsonObject = new JSONObject(json);

            @SuppressWarnings("unchecked")
            Iterator<String> keys = jsonObject.keys();
            keys = new OrderedIterator(keys);

            ArrayList<Field> fields = new ArrayList<>();
            int posn = 1;
            while (keys.hasNext()) {
                String key = keys.next();
                Field field = new Field();
                field.setName(key);
                field.setSourceXpath("/" + key);
                field.setPosition(posn);
                posn++;
                fields.addAll(valueToDatatype(jsonObject.opt(key), field, posn));
                posn = fields.size() + 1;
            }
            template.getFields().addAll(fields);
            return template;
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            logger.error(e.getMessage(), e);
            return template;
        }
    }

    private ArrayList<Field> valueToDatatype(Object o, Field field, int posn) throws JSONException {
        ArrayList<Field> fields = new ArrayList<>();
        fields.add(field);
        if (isScalar(o)) {
            field.setDatatype(scalarType(o));
            return fields;
        }
        if (o instanceof JSONObject) {
            field.setDatatype("struct");
            ArrayList<Field> somefields = toHiveSchema((JSONObject) o, field, posn);
            return somefields;
        }
        if (o instanceof JSONArray) {
            ArrayList<Field> somefields = toHiveSchema((JSONArray) o, field, posn);
            return somefields;
        } else {
            field.setDatatype("string");
        }
        throw new IllegalArgumentException("unknown type: " + o.getClass());
    }

    private ArrayList<Field> toHiveSchema(JSONObject o, Field field, Integer posn) throws JSONException {
        @SuppressWarnings("unchecked")
        Iterator<String> keys = o.keys();
        keys = new OrderedIterator(keys);
        ArrayList<Field> fields = new ArrayList<>();
        fields.add(field);
        while (keys.hasNext()) {
            String k = keys.next();
            Field newfield = new Field();
            newfield.setSourceXpath(field.getSourceXpath() + "/" + k);
            newfield.setParent(field.getPosition());
            newfield.setPosition(posn);
            newfield.setName(field.getName() + "_" + k);
            posn = posn + 1;
            ArrayList<Field> newFields = valueToDatatype(o.opt(k), newfield, posn);
            fields.addAll(newFields);
            posn = posn + newFields.size() - 1;
        }
        return fields;
    }

    private ArrayList<Field> toHiveSchema(JSONArray a, Field field, int posn) throws JSONException {
        field.setDatatype("array");
        ArrayList<Field> fields = new ArrayList<>();
        if (a.length() == 0) {
            // nothing to compare, assuming string
            fields.add(field);
            return fields;
        }
        Object entry0 = a.get(0);
        if (isScalar(entry0)) {
            fields.add(field);
            return fields;
        }
        if (entry0 instanceof JSONObject) {
            return toHiveSchema((JSONObject) entry0, field, posn);
        }
        if (entry0 instanceof JSONArray) {
            return toHiveSchema((JSONArray) entry0, field, posn);
        }
        throw new IllegalArgumentException("unknown type: " + a.getClass());
    }

    private String scalarType(Object o) {
        if (o == null || o instanceof String) {
            return "string";
        }
        if (o instanceof Number) {
            String s = o.toString();
            if (s.indexOf('.') >= 0) {
                return "double";
            }
            return "int";
        }
        if (o instanceof Boolean) {
            return "boolean";
        }
        return "string";
    }

    private boolean isScalar(Object o) {
        return o instanceof String || o instanceof Number || o instanceof Boolean || o == JSONObject.NULL;
    }

    static class OrderedIterator implements Iterator<String> {

        Iterator<String> it;

        public OrderedIterator(Iterator<String> iter) {
            SortedSet<String> keys = new TreeSet<>();
            while (iter.hasNext()) {
                keys.add(iter.next());
            }
            it = keys.iterator();
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public String next() {
            return it.next();
        }

        @Override
        public void remove() {
            it.remove();
        }
    }
}
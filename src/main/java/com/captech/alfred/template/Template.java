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

package com.captech.alfred.template;

import com.captech.alfred.Constants;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import org.springframework.util.StringUtils;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Template {

    @Valid
    private File file;
    @Valid
    private ArrayList<Field> fields;
    private String version;
    private String stage;
    private String eid;
    private HashMap<String, String> properties = new HashMap<>();

    public Template() {
    }

    /**
     * @return the file
     */
    public File getFile() {
        if (file == null) {
            return new File();
        }
        return file;
    }

    /**
     * @param file
     *            the file to set
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * @return the fields
     */
    public List<Field> getFields() {
        if (fields == null) {
            setFields(new ArrayList<Field>());
        }
        //TODO:order; possibly make a map with position as key?
        return fields;
    }

    /**
     * @param fields
     *            the fields to set
     */
    public void setFields(ArrayList<Field> fields) {
        this.fields = fields;
    }

    public void addField(Field field) {
        this.getFields().add(field);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    /**
     * @return the eid
     */
    public String getEid() {
        return eid;
    }

    /**
     * @param eid
     *            the eid to set
     */
    public void setEid(String eid) {
        this.eid = eid;
    }

    public static Template checkFieldNames(Template metadata) {
        for (Field field : metadata.getFields()) {
            if (Constants.HIVE_RESERVED.contains(field.getName().toUpperCase())) {
                field.setName(field.getName() + Constants.HIVE_ADDENDUM);
            }
            field.setName(field.getName().replace('.', '_'));
        }
        return metadata;
    }

    public static Template checkTableName(Template metadata) {
        if (StringUtils.isEmpty(metadata.getFile().getTechnical().getTableName())) {
            metadata.getFile().getTechnical().setTableName(metadata.getFile().getKey());
        }
        metadata.getFile().getTechnical()
                .setTableName(metadata.getFile().getTechnical().getTableName().replace('.', '_'));
        return metadata;
    }

    /**
     * @return the properties
     */
    @JsonAnyGetter
    public HashMap<String, String> getProperties() {
        return properties;
    }

    /**
     * @param key
     *            the properties to set
     */
    @JsonAnySetter
    public void setProperties(String key, String value) {
        properties.put(key, value);
    }

}

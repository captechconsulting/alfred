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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class File {


    private String key;
    private UUID guid;
    private String dataPartition;
    private String subjectArea;
    @Valid private BusinessFile business;
    @Valid
    private TechnicalFile technical;
    private Map<String, String> properties = new HashMap<>();

    public String getSubjectArea() {
        return subjectArea;
    }

    public void setSubjectArea(String subjectArea) {
        this.subjectArea = subjectArea;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String Key) {
        this.key = Key;
    }

    /**
     * @return the dataPartition
     */
    public String getDataPartition() {
        return dataPartition;
    }

    /**
     * @param dataPartition
     *            the dataPartition to set
     */
    public void setDataPartition(String dataPartition) {
        this.dataPartition = dataPartition;
    }

    /**
     * @return the business
     */
    public BusinessFile getBusiness() {
        return business;
    }

    /**
     * @param business the business to set
     */
    public void setBusiness(BusinessFile business) {
        this.business = business;
    }

    /**
     * @return the technical
     */
    public TechnicalFile getTechnical() {
        return technical;
    }

    /**
     * @param technical the technical to set
     */
    public void setTechnical(TechnicalFile technical) {
        this.technical = technical;
    }

    /**
     * @return the guid
     */

    public UUID getGuid() {
        return guid;
    }

    /**
     * @param guid the guid to set
     */

    public void setGuid(UUID guid) {
        this.guid = guid;
    }

    /**
     * @return the properties
     */
    @JsonAnyGetter
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * @param key the properties to set
     */
    @JsonAnySetter
    public void setProperties(String key, String value) {
        properties.put(key, value);
    }

}

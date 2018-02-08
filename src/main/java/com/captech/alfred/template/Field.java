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

import java.util.ArrayList;
import java.util.List;

public class Field {

    private String name;
    private Integer position;
    private String businessName;
    private String description;
    private String datatype;
    private String format;
    private Boolean nullable;
    private String precision;
    private Boolean pk;
    private Integer partitionPosition;
    //TODO: create refined field extended
    private Quality quality;
    private Boolean derived;
    private String formula;
    private ArrayList<String> namesOfSourceFields;
    //TODO: create XML field extended
    private String sourceXpath;
    private String namespace;
    private Integer parent;


    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public Boolean getNullable() {
        return nullable;
    }

    public void setNullable(Boolean nullable) {
        this.nullable = nullable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrecision() {
        return precision;
    }

    public void setPrecision(String precision) {
        this.precision = precision;
    }

    public Boolean getPk() {
        return pk;
    }

    public void setPk(Boolean pk) {
        this.pk = pk;
    }

    public Integer getPartitionPosition() {
        return partitionPosition;
    }

    public void setPartitionPosition(Integer partitionPosition) {
        this.partitionPosition = partitionPosition;
    }

    public Quality getQuality() {
        return quality;
    }

    public void setQuality(Quality quality) {
        this.quality = quality;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public Boolean getDerived() {
        return derived;
    }

    public void setDerived(Boolean derived) {
        this.derived = derived;
    }

    public List<String> getNamesOfSourceFields() {
        return namesOfSourceFields;
    }

    public void setNamesOfSourceFields(ArrayList<String> namesOfSourceFields) {
        this.namesOfSourceFields = namesOfSourceFields;
    }

    public String getSourceXpath() {
        return sourceXpath;
    }

    public void setSourceXpath(String sourceXpath) {
        this.sourceXpath = sourceXpath;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Integer getParent() {
        return parent;
    }

    public void setParent(Integer integer) {
        this.parent = integer;
    }

}

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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "format", defaultImpl = ExtendedFile.class, visible = true)
@JsonSubTypes({ @JsonSubTypes.Type(value = TabularFile.class, name = "tabular"),
        @JsonSubTypes.Type(value = ExtendedFile.class, name = "other"),
        @JsonSubTypes.Type(value = XMLFile.class, name = "xml"),
        @JsonSubTypes.Type(value = ExtendedFile.class, name = "json")})
public class TechnicalFile {

    private String format;

    private String tableName;

    private String compression;

    private String fileUpdateType;

    /**
     * @return the format
     */
    public String getFormat() {
        return format;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * @return the tableName
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @param tableName the tableName to set
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getCompression() {
        return compression;
    }

    public void setCompression(String compression) {
        this.compression = compression;
    }

    /**
     * @return the fileUpdateType
     */
    public String getFileUpdateType() {
        return fileUpdateType;
    }

    /**
     * @param fileUpdateType the fileUpdateType to set
     */
    public void setFileUpdateType(String fileUpdateType) {
        this.fileUpdateType = fileUpdateType;
    }

    @Override
    public String toString() {
        return "TechnicalFile [format=" + format + ", tableName=" + tableName + ", compression=" + compression
                + ", fileUpdateType=" + fileUpdateType + "]";
    }

}

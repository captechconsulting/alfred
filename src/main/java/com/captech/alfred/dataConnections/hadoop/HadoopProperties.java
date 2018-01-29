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

package com.captech.alfred.dataConnections.hadoop;

import com.captech.alfred.dataConnections.LocationProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Paths;

@ConfigurationProperties("hadoop")
public class HadoopProperties extends LocationProperties {

    private String defaultPath = "alfred/";
    private String userDir = "/user/" + System.getProperty("user.name") + "/";
    private String databaseName = "dev_alfred";
    private String sampleDir = "data/samples";

    @Override
    public String getDefaultPath() {
        return defaultPath;
    }

    public void setDefaultPath(String defaultPath) {
        this.defaultPath = defaultPath;
    }

    public String getFullSampleDir() {
        return Paths.get(getUserDir(), getSampleDir()).toString();
    }

    @Override
    public String getUserDir() {
        return userDir;
    }

    public void setUserDir(String userDir) {
        this.userDir = userDir;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getSampleDir() {
        return sampleDir;
    }

    public void setSampleDir(String sampleDir) {
        this.sampleDir = sampleDir;
    }
}
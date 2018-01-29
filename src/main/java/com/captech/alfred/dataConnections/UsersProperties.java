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

package com.captech.alfred.dataConnections;

import com.captech.alfred.dataConnections.hadoop.HadoopProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Paths;

@ConfigurationProperties("authentication")
public class UsersProperties {
    private String homePath = System.getProperty("user.home") + "/";
    private String defaultPath = "alfred/";
    private String authentication = "authentication/";
    private String oldAuth = "oldAuth/";
    private String authorities = "authorities/";


    protected String getHomePath() {
        return homePath;
    }

    public void setHomePath(String homePath) {
        this.homePath = homePath;
    }

    protected String getDefaultPath() {
        return defaultPath;
    }

    public void setDefaultPath(String defaultPath) {
        this.defaultPath = defaultPath;
    }

    protected String getAuthentication() {
        return authentication;
    }

    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    protected String getOldAuth() {
        return oldAuth;
    }

    public void setOldAuth(String oldAuth) {
        this.oldAuth = oldAuth;
    }

    protected String getAuthorities() {
        return authorities;
    }

    public void setAuthorities(String authorities) {
        this.authorities = authorities;
    }

    public String getAuthPath() {
        return Paths.get(getDefaultPath(), getAuthentication()).toString();
    }

    public String getOldAuthPath() {
        return Paths.get(getDefaultPath(), getOldAuth()).toString();
    }

    public String getAuthoritiesPath() {
        return Paths.get(getDefaultPath(), getAuthorities()).toString();
    }


    public String getfullAuthPath() {
        return Paths.get((new HadoopProperties()).getUserDir(), getDefaultPath(), getAuthentication()).toString();
    }

    public String getTextfullAuthPath() {
        return Paths.get(getHomePath(), getDefaultPath(), getAuthentication()).toString();
    }

    public String getfullOldAuthPath() {
        return Paths.get((new HadoopProperties()).getUserDir(), getDefaultPath(), getOldAuth()).toString();
    }

    public String getTextFullOldAuthPath() {
        return Paths.get(getHomePath(), getDefaultPath(), getOldAuth()).toString();
    }

    public String getFullAuthoritiesPath() {
        return Paths.get((new HadoopProperties()).getUserDir(), getDefaultPath(), getAuthorities()).toString();
    }

    public String getFullTextAuthoritiesPath() {
        return Paths.get(getHomePath(), getDefaultPath(), getAuthorities()).toString();
    }


}

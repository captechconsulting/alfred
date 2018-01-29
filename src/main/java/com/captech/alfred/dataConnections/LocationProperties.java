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

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Paths;

@ConfigurationProperties
public abstract class LocationProperties {

    private String metadataLocation = "template/";
    private String current = "current/";
    private String versions = "versions/";
    private String logLocation = "instance/";
    private String draftLocation = "draft/";
    private String refined = "refined/";
    private String sandbox = "sandbox/";


    public abstract String getUserDir();

    public abstract String getDefaultPath();

    protected String getRelativeMetadataPath() {
        return metadataLocation;
    }

    protected String getRelativeCurrent() {
        return current;
    }

    public void setCurrent(String currentMd) {
        this.current = currentMd;
    }

    protected String getRelativeVersioned() {
        return versions;
    }

    public void setVersioned(String location) {
        this.versions = location;
    }

    protected String getRelativeDraft() {
        return draftLocation;
    }

    public void setDraftLocation(String draftLocation) {
        this.draftLocation = draftLocation;
    }

    protected String getRelativeRefinedPath() {
        return refined;
    }

    public void setRefined(String refined) {
        this.refined = refined;
    }

    protected String getRelativeLogLocation() {
        return logLocation;
    }

    protected String getRelativeSandbox() {
        return sandbox;
    }

    public void setSandbox(String sandbox) {
        this.sandbox = sandbox;
    }

    public String getMetadataLocation() {
        String defaultPath = getDefaultPath();
        String relativePath = getRelativeMetadataPath();
        return Paths.get(defaultPath, relativePath).toString();
    }

    public void setMetadataLocation(String location) {
        this.metadataLocation = location;
    }

    public String getRefinedLocation() {
        return Paths.get(getDefaultPath(), getRelativeRefinedPath()).toString();
    }

    public String getSandboxLocation() {
        return Paths.get(getDefaultPath(), getRelativeSandbox()).toString();
    }

    public String getCurrentMd() {
        return Paths.get(getMetadataLocation(), getRelativeCurrent()).toString();
    }

    public String getVersionedMd() {
        return Paths.get(getMetadataLocation(), getRelativeVersioned()).toString();
    }

    public String getDraftMd() {
        return Paths.get(getMetadataLocation(), getRelativeDraft()).toString();
    }

    public String getCurrentRefined() {
        return Paths.get(getRefinedLocation(), getRelativeCurrent()).toString();
    }

    public String getVersionedRefined() {
        return Paths.get(getRefinedLocation(), getRelativeVersioned()).toString();
    }

    public String getDraftRefined() {
        return Paths.get(getRefinedLocation(), getRelativeDraft()).toString();
    }

    public String getCurrentSandbox() {
        return Paths.get(getSandboxLocation(), getRelativeCurrent()).toString();
    }

    public String getVersionedSandbox() {
        return Paths.get(getSandboxLocation(), getRelativeVersioned()).toString();
    }

    public String getFullCurrentMetadataPath() {
        return Paths.get(getUserDir(), getCurrentMd()).toString();
    }

    public String getFullVersionedMetadataPath() {
        return Paths.get(getUserDir(), getVersionedMd()).toString();
    }

    public String getFullDraftLocation() {
        return Paths.get(getUserDir(), getDraftMd()).toString();
    }

    public String getFullCurrentRefined() {
        return Paths.get(getUserDir(), getCurrentRefined()).toString();
    }

    public String getFullVersionedRefined() {
        return Paths.get(getUserDir(), getVersionedRefined()).toString();
    }

    public String getFullDraftRefined() {
        return Paths.get(getUserDir(), getDraftRefined()).toString();
    }

    public String getFullCurrentSandbox() {
        return Paths.get(getUserDir(), getCurrentSandbox()).toString();
    }

    public String getFullVersionedSandbox() {
        return Paths.get(getUserDir(), getVersionedSandbox()).toString();
    }

    public String getLogLocation() {
        return Paths.get(getDefaultPath(), getRelativeLogLocation()).toString();
    }

    public void setLogLocation(String logLocation) {
        this.logLocation = logLocation;
    }

    public String getFullInstancePath() {
        return Paths.get(getUserDir(), getLogLocation()).toString();
    }
}

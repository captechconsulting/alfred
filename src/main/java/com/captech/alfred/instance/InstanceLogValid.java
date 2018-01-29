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

package com.captech.alfred.instance;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Map;

@JsonTypeName("valid")
public class InstanceLogValid extends InstanceLog {
    private Boolean valid;
    private Map<String, String> reasons;
    private String validQuery;


    public Boolean isValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public Map<String, String> getReasons() {
        return reasons;
    }

    public void setReasons(Map<String, String> reasons) {
        this.reasons = reasons;
    }

    public String getValidQuery() {
        return validQuery;
    }

    public void setValidQuery(String validQuery) {
        this.validQuery = validQuery;
    }

}

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

public class SummaryMinTemplates {

    private long count;
    private List<Object> summary;
    private String env;

    public long getCount() {
        return count;
    }

    public void setCount(long l) {
        this.count = l;
    }

    public List<Object> getSummary() {
        return summary;
    }

    public void setSummary(ArrayList<Object> summary) {
        this.summary = summary;
    }

    public void addToSummary(Object template) {
        if (this.summary == null) {
            this.summary = new ArrayList<>();
        }
        this.summary.add(template);
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

}

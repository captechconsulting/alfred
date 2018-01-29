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

package com.captech.alfred.template.refined;

import com.captech.alfred.template.Template;

import java.util.ArrayList;
import java.util.List;

public class Refined {

    private Script script;
    private List<Template> sourceTemplates;
    private Template refinedDataset;
    private String stage;


    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }

    public List<Template> getSourceTemplates() {
        if (sourceTemplates == null) {
            sourceTemplates = new ArrayList<>();
        }
        return sourceTemplates;
    }

    public void setSourceTemplates(ArrayList<Template> sourceTemplates) {
        this.sourceTemplates = sourceTemplates;
    }

    public Template getRefinedDataset() {
        return refinedDataset;
    }

    public void setRefinedDataset(Template refinedDataset) {
        this.refinedDataset = refinedDataset;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }
}

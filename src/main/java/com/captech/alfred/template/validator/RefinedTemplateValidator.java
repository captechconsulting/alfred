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

package com.captech.alfred.template.validator;

import com.captech.alfred.Constants;
import com.captech.alfred.template.Template;
import com.captech.alfred.template.refined.Refined;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class RefinedTemplateValidator implements Validator {

    private static final Logger log = LoggerFactory.getLogger(TemplateValidator.class);

    @Autowired
    private TemplateValidator templateValidator;

    @Override
    public boolean supports(Class<?> clazz) {
        return Refined.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmpty(errors, "refinedDataset", "empty", "refinedDataset must be provided.");
        Refined refined = (Refined) target;
        if (refined.getRefinedDataset() == null) {
            return;
        }
        if (!Constants.DRAFT.equals(refined.getRefinedDataset().getStage()) && !Constants.SANDBOX.equals(refined.getRefinedDataset().getStage())) {
            ValidationUtils.rejectIfEmpty(errors, "script", "empty", "must contain script information");
            ValidationUtils.rejectIfEmpty(errors, "sourceTemplates", "empty",
                    "must provide at least one source dataset");
            if (refined.getScript() == null || refined.getSourceTemplates().isEmpty()) {
                return;
            }
            try {
                errors.pushNestedPath("script");
                ValidationUtils.rejectIfEmpty(errors, "path", "script.empty", "script path cannot be empty");
                ValidationUtils.rejectIfEmpty(errors, "name", "script.empty", "script name cannot be empty");
            } finally {
                errors.popNestedPath();
            }

            for (int i = 0; i < refined.getSourceTemplates().size(); i++) {
                Template template = refined.getSourceTemplates().get(i);
                errors.pushNestedPath("sourceTemplates[" + i + "]");
                if (template.getFile() == null
                        || (template.getFile().getGuid() == null && template.getFile().getKey() == null)) {
                    errors.rejectValue("file", "source.empty", "source template key or guid must be provided");
                }
                ValidationUtils.rejectIfEmpty(errors, "version", "source.empty", "Source template version must be provided.");
                if (template.getFields().isEmpty()) {
                    errors.rejectValue("fields", "source.empty", "source field(s) must be provided.");
                    errors.popNestedPath();
                    break;
                }
                for (int j = 0; j < template.getFields().size(); j++) {
                    errors.popNestedPath();
                    errors.pushNestedPath("sourceTemplates[" + i + "]." + "fields[" + j + "]");
                    ValidationUtils.rejectIfEmpty(errors, "name", "fields.empty", "source field name(s) must be provided.");
                }
                errors.popNestedPath();
            }
            try {
                errors.pushNestedPath("refinedDataset");
                ValidationUtils.invokeValidator(templateValidator, refined.getRefinedDataset(), errors);
            } finally {
                try {
                    errors.popNestedPath();
                } catch (IllegalStateException e) {
                    log.debug("RefinedTemplateValidator unable to pop - nowhere to pop back to.");
                }
            }
        }
    }

}

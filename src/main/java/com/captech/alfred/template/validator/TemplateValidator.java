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
import com.captech.alfred.template.Field;
import com.captech.alfred.template.TabularFile;
import com.captech.alfred.template.Template;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class TemplateValidator implements Validator {

    private static final Logger log = LoggerFactory.getLogger(TemplateValidator.class);
    private static final List<String> REQD_PRECISION_DATATYPES = Arrays.asList("CHAR", "VARCHAR");
    private static final List<String> NO_PRECISION_DATATYPES = Arrays.asList("DOUBLE", "FLOAT");

    @Override
    public boolean supports(Class<?> clazz) {
        return Template.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (!(target instanceof Template)) {
            errors.reject("json object is not a template type");
            return;
        }
        ValidationUtils.rejectIfEmpty(errors, "file", "file.empty", "file information is required");
        ValidationUtils.rejectIfEmpty(errors, "stage", "stage.empty", "stage needed. Is this a draft?");
        Template md = (Template) target;

        if (StringUtils.isBlank(md.getStage())) {
            return;
        }
        try {
            errors.pushNestedPath("file");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "key", "all.key.notnull", "Key is required!");
            if (!md.getFile().getKey().matches("[a-zA-Z0-9 \\_\\-]+")) {
                errors.reject("all.key.invalid",
                        "key cannot contain certain characters. Consider only using alphanumeric characters and underscores if possible.");
                return;
            }
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "dataPartition", "all.required",
                    "data partition is required!");
            if (!Constants.DRAFT.equalsIgnoreCase(md.getStage())) {
                if (Constants.FINAL_STAGE.equalsIgnoreCase(md.getStage())) {
                    validateFinal(md, errors);
                    if (md.getFields().isEmpty()) {
                        errors.reject("Fields are required");
                    }
                }
                if (!md.getFields().isEmpty()) {
                    validateFields(md, errors);
                }
                try {
                    errors.popNestedPath();
                } catch (IllegalStateException e) {
                    log.warn("TemplateValidator unable to pop - nowhere to pop back to!");
                }
                if (!Constants.REFINED.equalsIgnoreCase(md.getStage()) && md.getFile() == null || md.getFile().getTechnical() == null
                        && !md.getFields().isEmpty()) {
                    errors.rejectValue("file.technical", "technical.required",
                            "Technical information is required to be able to parse file correctly when fields are given.");
                    return;
                }
                if (!Constants.REFINED.equalsIgnoreCase(md.getStage()) && md.getFile().getTechnical() instanceof TabularFile) {
                    TabularFile tfile = (TabularFile) md.getFile().getTechnical();
                    if (StringUtils.isBlank(tfile.getFieldDelimiter())
                            && (!md.getFields().isEmpty() || tfile.getContainsHeaderRow())) {
                        errors.rejectValue("file.technical.fieldDelimiter", "tabular.reqired",
                                "Field Delimiter is required when (1) format is tabular and (2) either fields are added or header row is selected");
                        return;
                    }
                }
            }
        } finally {
            try {
                errors.popNestedPath();
            } catch (IllegalStateException e) {
                log.warn("TemplateValidator unable to pop - nowhere to pop back to!");
            }
        }

    }

    private void validateFinal(Template md, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "subjectArea", "final.required",
                "We need to know the subject area so we can store the files effectively");
        errors.popNestedPath();
        if (md.getFile() != null && md.getFile().getTechnical() != null) {
            errors.pushNestedPath("file.technical");
            if (!"refined".equalsIgnoreCase(md.getStage()) && md.getFile().getTechnical().getFormat() != null
                    && "tabular".equalsIgnoreCase(md.getFile().getTechnical().getFormat())) {
                ValidationUtils.rejectIfEmptyOrWhitespace(errors, "rowFormat", "final.tabular.required",
                        "row format is required");
                if ("delimited".equalsIgnoreCase(md.getFile().getTechnical().getFormat())) {
                    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "fieldDelimiter", "final.tabular.reqired",
                            "Field Delimiter is required when row format is delimited");
                }
                ValidationUtils.rejectIfEmpty(errors, "lineTerminator", "final.tabular.required",
                        "We need to know the new line key");
                ValidationUtils.rejectIfEmptyOrWhitespace(errors, "containsHeaderRow", "final.tabular.required",
                        "Is there a header row?");
            } // if tabular
            if (!StringUtils.isEmpty(md.getFile().getTechnical().getTableName()) &&
                    !md.getFile().getTechnical().getTableName().matches("[a-zA-Z0-9 \\_\\-]+")) {
                errors.reject("tablename.invalid",
                        "tablename can only contain alphanumeric characters, underscores, and dashes.");
            }
        }
    }

    private void validateFields(Template md, Errors errors) {
        Set<Integer> positions = new HashSet<>();
        Set<Integer> partitions = new HashSet<>();
        for (int i = 0; i < md.getFields().size(); i++) {
            errors.popNestedPath();
            Field field = md.getFields().get(i);
            String fieldString = "fields[" + i + "]";
            errors.pushNestedPath(fieldString);
            // TODO:make conditional based on technical format (not required for semi structured; required for tabular)
            // ValidationUtils.rejectIfEmpty(errors, "position",
            // "field.required",
            // "position field is required and must be unique across fields.");
            if (field.getPosition() != null && !positions.add(field.getPosition())) {
                errors.rejectValue("position", "fields.duplicatePosition",
                        "Position must be unique. Field with invalid position is " + field.getName());
            }
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "datatype", "field.required",
                    "datatype is required for each field. Field with invalid datatype is " + field.getName());
            if (field.getPartitionPosition() != null && !partitions.add(field.getPartitionPosition())) {
                errors.rejectValue("partitionPosition", "fields.duplicatePartition",
                        "Partition Position must be unique when provided. Field with invalid partition is "
                                + field.getName());
            }
            if (!StringUtils.isBlank(field.getFormat())) {
                try {
                    new SimpleDateFormat(field.getFormat());
                } catch (IllegalArgumentException e) {
                    errors.rejectValue("format", "field.format.invalid",
                            "format must be a valid datetime format. Field with invalid format is " + field.getName());
                }
            }

            if (REQD_PRECISION_DATATYPES.contains(StringUtils.upperCase(field.getDatatype()))) {
                ValidationUtils.rejectIfEmpty(errors, "precision", "field.precision.invalid",
                        "precision must be included for chars and varchars. Field with invalid precision is "
                                + field.getName());
            }
            if (NO_PRECISION_DATATYPES.contains(StringUtils.upperCase(field.getDatatype()))) {
                if (StringUtils.isNotEmpty(field.getPrecision())) {
                    errors.rejectValue("precision", "field.precision.invalid",
                            "precision cannot be included for doubles and floats. This will cause an error in hive. Field with invalid precision is "
                                    + field.getName());
                }
            }

            if (!StringUtils.isBlank(field.getPrecision())) {
                String[] precisions = field.getPrecision().split(",");
                if (precisions.length > 2) {
                    errors.rejectValue("precision", "field.precision.invalid",
                            "precision must be in either 'x' format or 'x,y' format, where x and y are numbers. X is length of entire field, and y is the number of digits after the decimal (if applicable). Field with invalid precision is "
                                    + field.getName());
                }
                for (String precision : precisions) {
                    try {
                        Integer.parseInt(precision);
                    } catch (NumberFormatException e) {
                        errors.rejectValue("precision", "field.precision.invalid",
                                "precision must be in 'x,y' format, where x and y are numbers. Field with invalid precision is "
                                        + field.getName());
                    }
                }
            } // precision
        } // for each field
    }
}

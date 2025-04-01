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

package com.captech.alfred;

import com.captech.alfred.dataConnections.DataStoreService;
import com.captech.alfred.exceptions.AppInternalError;
import com.captech.alfred.template.Template;
import com.captech.alfred.template.refined.Refined;
import com.captech.alfred.template.validator.RefinedTemplateValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@RestController
@EnableAutoConfiguration
@RequestMapping(value = "/refinedDatasets")
public class RefinedController {
    public static final String DRAFT = "draft";
    private static final Logger logger = LoggerFactory.getLogger(MetadataController.class);

    @Autowired
    DataStoreService dataConnection;

    @Autowired
    private RefinedTemplateValidator refinedTemplateValidator;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(refinedTemplateValidator);
    }

    public RefinedController(DataStoreService dataConnection) {
        logger.debug("initialized MetadataController with dataConnection class "
                + dataConnection.getClass().getCanonicalName());
        this.dataConnection = dataConnection;
    }

    @PostMapping(consumes = "application/json")
    @PreAuthorize("hasRole('ADMIN') or hasAnyAuthority('PERM_ADD')")
    public ResponseEntity<?> registerNewRefined(@RequestBody @Valid Refined refined, Errors errors) {
        refined.getRefinedDataset().getFile().setGuid(UUID.randomUUID());
        refined.getRefinedDataset().setVersion(new SimpleDateFormat(Constants.VERSION_FORMAT).format(new Date()));
        refined.setStage("refined");
        if (errors.hasErrors()) {
            ValidationErrors retErrors = new ValidationErrors();
            for (ObjectError error : errors.getAllErrors()) {
                retErrors.getErrors().add(error.getDefaultMessage());
            }

            return ResponseEntity.badRequest().body(retErrors);
        }
        refined.setRefinedDataset(Template.checkFieldNames(refined.getRefinedDataset()));
        refined.setRefinedDataset(Template.checkTableName(refined.getRefinedDataset()));

        return ResponseEntity.ok(dataConnection.addNewRefined(refined));
    }

    @PutMapping(value = "{key:.+}", consumes = "application/json")
    @PreAuthorize("hasRole('ADMIN') or hasAnyAuthority('PERM_EDIT')")
    public ResponseEntity<?> updateRefined(@PathVariable String key, @RequestBody @Valid Refined refined,
                                           Errors errors) {
        if (errors.hasErrors()) {
            ValidationErrors retErrors = new ValidationErrors();
            for (ObjectError error : errors.getAllErrors()) {
                retErrors.getErrors().add(error.getDefaultMessage());
            }

            return ResponseEntity.badRequest().body(retErrors);
        }
        logger.debug("update metadata called for key: " + key);
        refined.getRefinedDataset().setVersion(new SimpleDateFormat(Constants.VERSION_FORMAT).format(new Date()));
        if (!DRAFT.equalsIgnoreCase(refined.getRefinedDataset().getStage())
                && refined.getRefinedDataset().getFile().getGuid() == null) {
            logger.debug("setting guid");
            Refined oldData = dataConnection.getRefined(key);
            if (oldData != null && oldData.getRefinedDataset().getFile().getGuid() != null) {
                refined.getRefinedDataset().getFile().setGuid(oldData.getRefinedDataset().getFile().getGuid());
            } else {
                refined.getRefinedDataset().getFile().setGuid(UUID.randomUUID());
            }
        }

        refined.setRefinedDataset(Template.checkTableName(refined.getRefinedDataset()));
        refined.setRefinedDataset(Template.checkFieldNames(refined.getRefinedDataset()));
        refined.setStage("refined");
        String returnedKey = dataConnection.updateRefined(key, refined);
        return ResponseEntity.ok(dataConnection.getRefined(returnedKey));
    }

    @DeleteMapping(value = "{key:.+}")
    @PreAuthorize("hasRole('ADMIN') or hasAnyAuthority('PERM_DELETE')")
    public void deleteRefined(@PathVariable String key) {
        if (key.contains("..") || key.contains("/") || key.contains("\\")) {
            throw new IllegalArgumentException("Invalid key");
        }
        logger.debug("Soft delete metadata called for key " + key);
        String deletedLocation = dataConnection.deleteRefined(key);
        if (deletedLocation == null) {
            throw new AppInternalError("unable to delete refined dataset");
        }
    }

    @GetMapping(value = "{key:.+}")
    @PreAuthorize("hasAnyRole('APP','ADMIN') or hasAnyAuthority('PERM_VIEW')")
    public Refined getRefined(@PathVariable String key) {
        logger.debug("get metadata for key: " + key);
        return dataConnection.getRefined(key);
    }

}

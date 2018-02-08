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

import com.captech.alfred.authentication.User;
import com.captech.alfred.dataConnections.DataStoreService;
import com.captech.alfred.dataConnections.hadoop.HadoopDatastoreService;
import com.captech.alfred.exceptions.AppInternalError;
import com.captech.alfred.exceptions.NoDataFound;
import com.captech.alfred.exceptions.RequiredInfoMissing;
import com.captech.alfred.exceptions.Unauthorized;
import com.captech.alfred.template.*;
import com.captech.alfred.template.hierarchical.SourceTemplateBuilder;
import com.captech.alfred.template.hierarchical.XmlHelperUtils;
import com.captech.alfred.template.refined.Refined;
import com.captech.alfred.template.validator.TemplateValidator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.data.hadoop.store.output.TextFileWriter;
import org.springframework.data.hadoop.store.strategy.naming.StaticFileNamingStrategy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@EnableAutoConfiguration
@RequestMapping(value = "/fileMetadata")
public class MetadataController {

    public static final String ROLE_SANDBOX = "ROLE_SANDBOX";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String PERM_ADD = "PERM_ADD";
    public static final String SANDBOX = "sandbox";
    public static final String ANY = "any";
    public static final String ROLE_APP = "ROLE_APP";
    public static final String FINAL_STAGE = "final";
    public static final String DRAFT = "draft";
    public static final String JSON = "json";
    public static final String XML = "xml";
    private static final Logger logger = LoggerFactory.getLogger(MetadataController.class);
    private static final Logger upload_logger = LoggerFactory.getLogger("file_upload");
    @Autowired
    DataStoreService dataConnection;
    @Autowired
    @Qualifier("jsonSource")
    SourceTemplateBuilder jsonSourceBuilder;
    @Autowired
    @Qualifier("xmlSource")
    SourceTemplateBuilder xmlSourceBuilder;
    @Autowired
    private TemplateValidator templateValidator;
    @Autowired
    private Environment env;
    @Autowired
    private TextFileWriter sampleWriter;

    public MetadataController(DataStoreService dataConnection) {
        //logger.debug("initialized MetadataController with dataConnection class "
        //        + dataConnection.getClass().getCanonicalName());
        this.dataConnection = dataConnection;
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(templateValidator);
    }

    /**
     * @param name: the name of the file to match to the template
     * @return Template of the metadata if found; a blank template object if no
     * match
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('APP', 'ADMIN', 'SANDBOX')or hasAnyAuthority('PERM_SEARCH')")
    public Template findFileMetadata(@RequestParam(value = "name") String name,
                                     @RequestParam(value = "stage", required = false) String stage, Authentication auth) {
        logger.debug("find called for file " + name);
        Template md;
        String sentStage = null;
        if (StringUtils.isNotEmpty(stage) && !ANY.equalsIgnoreCase(stage)) {
            sentStage = stage;
        }
        String key = dataConnection.findMetaDataByFileName(name, sentStage);
        if (key == null) {
            logger.debug("no dataset found for file: " + name);
            return new Template();
        }
        logger.debug("key matched: " + key);
        try {
            md = getMetadata(key, auth);
            List<String> auths = new ArrayList<>();
            for (GrantedAuthority authority : auth.getAuthorities()) {
                auths.add(authority.getAuthority());
            }
            if (md == null || !(auths.contains(ROLE_APP) || auths.contains(ROLE_ADMIN)
                    || User.getAllowedAccessGroups(auth.getAuthorities())
                    .contains(md.getFile().getDataPartition()))) {
                return new Template();
            }
            if (!StringUtils.isEmpty(stage)) {

                if (ANY.equalsIgnoreCase(stage)) {
                    return md;
                }
                if (!stage.equalsIgnoreCase(md.getStage())) {
                    return new Template();
                }
            } else {
                if (!FINAL_STAGE.equalsIgnoreCase(md.getStage())) {
                    return new Template();
                }
            }
        } catch (NoDataFound e) {
            logger.warn("no dataset found for file: " + name);
            return new Template();
        }
        return md;
    }

    @GetMapping(value = "keys")
    @PreAuthorize("hasAnyRole('APP', 'ADMIN', 'SANDBOX')or hasAnyAuthority('PERM_VIEW')")
    public RegisteredKeys getKeys() {
        logger.debug("get keys");
        RegisteredKeys keys = new RegisteredKeys();
        keys.setRegisteredKeys(dataConnection.getAllKeys());
        return keys;
    }

    @GetMapping(value = {"{key:.+}"})
    @PreAuthorize("hasAnyRole('APP', 'ADMIN', 'SANDBOX')or hasAnyAuthority('PERM_VIEW')")
    public Template getMetadata(@PathVariable String key, Authentication auth) {
        logger.debug("get metadata for key: " + key);
        Template filedata;
        // findCurrent - if none, findDraft - if none, 404
        filedata = dataConnection.getCurrentMetadata(key);
        if (filedata == null) {
            filedata = dataConnection.getDraftMetadata(key);
        }
        if (filedata == null) {
            filedata = dataConnection.getSandboxMetadata(key);
        }
        if (filedata == null) {
            throw new NoDataFound();
        }
        List<String> auths = new ArrayList<>();
        for (GrantedAuthority authority : auth.getAuthorities()) {
            auths.add(authority.getAuthority());
        }
        if (!(auths.contains(ROLE_APP) || auths.contains(ROLE_ADMIN)
                || User.getAllowedAccessGroups(auth.getAuthorities())
                .contains(filedata.getFile().getDataPartition()))) {
            throw new NoDataFound();
        }
        return filedata;
    }

    @PostMapping(consumes = "application/json")
    @PreAuthorize("hasAnyRole('ADMIN', 'SANDBOX') or hasAnyAuthority('PERM_ADD')")
    public ResponseEntity<?> registerNewMetadata(@RequestBody @Valid Template metadata, Errors errors,
                                                 Authentication auth) {
        if (metadata != null && metadata.getFile() != null) {
            logger.debug("register new metadata called for key: " + metadata.getFile().getKey());
        } else {
            logger.debug("register new metadata called without key");
        }
        List<String> auths = new ArrayList<>();
        for (GrantedAuthority authority : auth.getAuthorities()) {
            auths.add(authority.getAuthority());
        }
        if (metadata == null || !(auths.contains(ROLE_ADMIN) || User.getAllowedAccessGroups(auth.getAuthorities())
                .contains(metadata.getFile().getDataPartition()))) {
            throw new Unauthorized("User not permitted to data group");
        }
        // if only sandbox, and this isn't sandbox
        if (!(auths.contains(ROLE_ADMIN) || auths.contains(PERM_ADD)) && auths.contains(ROLE_SANDBOX)
                && !StringUtils.equals(metadata.getStage(), SANDBOX)) {
            throw new Unauthorized("sandbox only role");
        }
        if (errors.hasErrors()) {
            ValidationErrors retErrors = new ValidationErrors();
            for (ObjectError error : errors.getAllErrors()) {
                retErrors.getErrors().add(error.getCode() + " - " + error.getDefaultMessage());
                logger.debug(error.getCode() + ": " + error.getDefaultMessage());
            }

            return ResponseEntity.badRequest().body(retErrors);
        }
        metadata.getFile().setGuid(UUID.randomUUID());
        metadata = setDefaultsAndCheckData(metadata, auth.getName());
        // TODO: check if draft; delete existing draft

        return ResponseEntity.ok(dataConnection.addNewMetadata(metadata));
    }

    /**
     * Helper method for update and add new to make sure field and table names
     * are hive compliant, and any defaults are set appropriately
     *
     * @param metadata
     */
    private Template setDefaultsAndCheckData(Template metadata, String user) {

        metadata.setVersion(new SimpleDateFormat(Constants.VERSION_FORMAT).format(new Date()));

        if (metadata.getStage() == null) {
            metadata.setStage(FINAL_STAGE);
        }
        if (metadata.getEid() == null) {
            metadata.setEid(user);
        }
        metadata = Template.checkFieldNames(metadata);
        metadata = Template.checkTableName(metadata);
        if (StringUtils.isEmpty(metadata.getFile().getTechnical().getCompression())) {
            metadata.getFile().getTechnical().setCompression("parquet");
        }
        for (Field field : metadata.getFields()) {
            if (field.getParent() != null) {
                Field parent = null;
                for (Field parentfield : metadata.getFields()) {
                    if (parentfield != null && parentfield.getPosition().equals(field.getParent())) {
                        parent = parentfield;
                        break;
                    }
                }
                if (parent == null) {
                    // assume the user removed the parent, and clear the parent
                    // field parameter
                    field.setParent(null);
                } else {
                    // if parent is the root element, we want that in the same
                    // row, not in a struct
                    if (parent.getPk() != null && parent.getPk()) {
                        field.setParent(null);
                    }
                }
            }
        }

        return metadata;
    }

    @PutMapping(value = "{key:.+}", consumes = "application/json")
    @PreAuthorize("hasAnyRole('ADMIN','SANDBOX') or hasAnyAuthority('PERM_EDIT')")
    public ResponseEntity<?> updateMetadata(@PathVariable String key, @RequestBody @Valid Template metadata,
                                            Errors errors, Authentication auth) {

        List<String> auths = new ArrayList<>();
        for (GrantedAuthority authority : auth.getAuthorities()) {
            auths.add(authority.getAuthority());
        }
        if (metadata == null || !(auths.contains(ROLE_ADMIN) || User.getAllowedAccessGroups(auth.getAuthorities())
                .contains(metadata.getFile().getDataPartition()))) {
            throw new Unauthorized(null);
        }
        // if only sandbox, and this isn't sandbox
        if (!(auths.contains(ROLE_ADMIN) || auths.contains("PERM_EDIT")) && auths.contains(ROLE_SANDBOX)
                && !StringUtils.equals(metadata.getStage(), SANDBOX)) {
            throw new Unauthorized("sandbox only role");
        }
        logger.debug("update metadata called for key: " + key);
        if (metadata.getStage() == null) {
            metadata.setStage(FINAL_STAGE);
        }
        if (errors != null && errors.hasErrors()) {
            ValidationErrors retErrors = new ValidationErrors();
            for (ObjectError error : errors.getAllErrors()) {
                retErrors.getErrors().add(error.getCode() + " - " + error.getDefaultMessage());
                logger.debug(error.getCode());
            }
            return ResponseEntity.badRequest().body(retErrors);
        }

        Template oldData = getMetadata(key, auth);
        if (!DRAFT.equalsIgnoreCase(metadata.getStage()) && metadata.getFile().getGuid() == null) {
            logger.debug("setting guid");
            if (oldData != null && oldData.getFile().getGuid() != null) {
                metadata.getFile().setGuid(oldData.getFile().getGuid());
            } else {
                metadata.getFile().setGuid(UUID.randomUUID());
            }
        }
        String user = auth.getName();
        if (oldData != null && !StringUtils.isBlank(oldData.getEid())) {
            user = oldData.getEid();
        }
        if (metadata.getFile().getTechnical() instanceof XMLFile) {
            metadata = XmlHelperUtils.doPutLogic(metadata, oldData);
        }
        metadata = setDefaultsAndCheckData(metadata, user);
        String returnedKey = dataConnection.updateMetadata(key, metadata);
        if (returnedKey == null) {
            throw new AppInternalError("unable to update metadata");
        }
        return ResponseEntity.ok(getMetadata(returnedKey, auth));
    }

    @DeleteMapping(value = "{key:.+}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_DELETE')")
    public void deleteMetadata(@PathVariable String key) {
        // TODO:verify user has authority to do this
        logger.debug("Soft delete metadata called for key " + key);
        String deletedLocation = dataConnection.deleteMetadata(key);
        if (deletedLocation == null) {
            throw new AppInternalError("unable to delete");
        }
    }

    @GetMapping("count")
    public TemplateCount getCount() {
        TemplateCount count = new TemplateCount();
        count.setCount(dataConnection.getCount());
        return count;
    }

    @GetMapping("summary")
    @PreAuthorize("hasRole('ADMIN') or hasAnyAuthority('PERM_VIEW')")
    public SummaryMinTemplates getSummary(@RequestParam(value = "begin") String begin,
                                          @RequestParam(value = "end", required = false) String end, Authentication auth) {
        SummaryMinTemplates summary = new SummaryMinTemplates();
        summary.setEnv("unknown");
        if (env.getActiveProfiles().length > 0) {
            summary.setEnv(env.getActiveProfiles()[0]);
        }
        ArrayList<String> allowedAccessGroups = User.getAllowedAccessGroups(auth.getAuthorities());
        summary.setCount(getCount().getCount());
        List<String> keys = getKeys().getRegisteredKeys();
        List<String> auths = new ArrayList<>();
        for (GrantedAuthority authority : auth.getAuthorities()) {
            auths.add(authority.getAuthority());
        }
        for (String key : keys) {
            try {
                Template template = getMetadata(key, auth);
                if (template != null && (auths.contains(ROLE_ADMIN)
                        || allowedAccessGroups.contains(template.getFile().getDataPartition()))) {
                    summary.addToSummary(template);
                }
            } catch (NoDataFound e) {
                // just skip it
                logger.warn("data not found for key " + key + ".");
            }
        }
        List<String> refinedKeys = dataConnection.getRefinedKeys();
        for (String key : refinedKeys) {
            Refined refined = dataConnection.getRefined(key);
            try {
                if (refined != null && (auths.contains(ROLE_ADMIN) || allowedAccessGroups
                        .contains(refined.getRefinedDataset().getFile().getDataPartition()))) {
                    summary.addToSummary(refined);
                }
            } catch (NoDataFound e) {
                // just skip it
                logger.warn("data not found for key " + key + ".");
            }
        }
        return summary;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('SANDBOX','ADMIN') or hasAnyAuthority('SANDBOX', 'UPLOAD')")
    public void handleFileUpload(@RequestParam("file") MultipartFile file, Authentication auth) {

        upload_logger.info(file.getOriginalFilename() + " with an unknown key file uploaded by " + auth.getName());
        if (file.isEmpty()) {
            throw new RequiredInfoMissing();
        }
        // TODO:check to see if key exists as a sandbox template; fail otherwise
        dataConnection.uploadSandbox(file, null);
    }

    @PostMapping("/{key:.+}/upload")
    @PreAuthorize("hasAnyRole('SANDBOX','ADMIN') or hasAnyAuthority('SANDBOX', 'UPLOAD')")
    public void handleFileUploadWithKey(@PathVariable String key, @RequestParam("file") MultipartFile file,
                                        Authentication auth) {

        upload_logger.info(file.getOriginalFilename() + " with '" + key + "' key file uploaded by " + auth.getName());
        if (file.isEmpty()) {
            throw new RequiredInfoMissing();
        }
        dataConnection.uploadSandbox(file, key);
    }

    @PutMapping(value = "{key:.+}/sample", produces = "application/json")
    public ResponseEntity<?> addSampleXml(@PathVariable String key, @RequestParam MultipartFile file,
                                          Authentication auth) {

        Template template = getMetadata(key, auth);
        if (template == null) {
            throw new NoDataFound();
        }
        try {
            if (template.getFields() != null && !template.getFields().isEmpty()) {
                throw new AppInternalError("Template already has fields. Cannot extrapolate");
            }
            String sampleData = new String(file.getBytes());
            if (dataConnection instanceof HadoopDatastoreService && sampleWriter != null) {
                // write the sample file to hadoop
                try {
                    String formattedDate = new SimpleDateFormat(Constants.HADOOP_VERS_FORMAT).format(new Date());
                    sampleWriter.setFileNamingStrategy(new StaticFileNamingStrategy(file.getOriginalFilename() + "_" + formattedDate));
                    sampleWriter.write(sampleData);
                } catch (Exception e) {
                    //dont fail over this
                    logger.warn("could not save sample data to hadoop");
                }
            }
            Template newTemplate;
            if (JSON.equalsIgnoreCase(template.getFile().getTechnical().getFormat())) {
                logger.debug("json: " + file.getOriginalFilename());
                newTemplate = jsonSourceBuilder.preFill(sampleData, template);
            } else if (XML.equalsIgnoreCase(template.getFile().getTechnical().getFormat())) {
                logger.debug("xml: " + file.getOriginalFilename());
                newTemplate = xmlSourceBuilder.preFill(sampleData, template);
            } else {
                throw new AppInternalError("unrecognized type for sample upload");
            }
            if (DRAFT.equalsIgnoreCase(newTemplate.getStage())) {
                newTemplate.setStage(FINAL_STAGE);
            }
            return updateMetadata(key, newTemplate, null, auth);

        } catch (IOException e) {
            // TODO:throw an error back that works
            logger.error("addSampleXml: " + e.getMessage(), e);
            throw new AppInternalError("Error reading sample XML file");
        }
    }

    // @GetMapping
    // public ArrayList<String> findKeyByStage(@RequestParam(value = "stage")
    // String stage) {
    // // TODO: parse key - if stage is null, get file, check stage, and rename
    // // if stage matches, add to list
    // return new ArrayList<String>();
    // }
    //
    // @GetMapping
    // public ArrayList<String> findKeyByOwner(@RequestParam(value = "owner")
    // String owner) {
    // // TODO: parse key - if owner is null, get file, check owner, and rename
    // // if stage matches, add to list
    // return new ArrayList<String>();
    // }
}
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

import com.captech.alfred.Constants;
import com.captech.alfred.MetadataController;
import com.captech.alfred.dataConnections.textFiles.TextFileProperties;
import com.captech.alfred.exceptions.AppInternalError;
import com.captech.alfred.exceptions.RequiredInfoMissing;
import com.captech.alfred.instance.Guid;
import com.captech.alfred.instance.InstanceLog;
import com.captech.alfred.template.Template;
import com.captech.alfred.template.refined.Refined;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPOutputStream;

@Service
@EnableConfigurationProperties(TextFileProperties.class)
public abstract class DataStoreService {

    private static final Logger logger = LoggerFactory.getLogger(MetadataController.class);

    @Autowired
    TextFileProperties properties;

    /**
     * Returns filename if found, null if not
     */
    public abstract String findMetaDataByFileName(String filename, String stage);

    /**
     * @param key
     * @return gets the template in current templates matching key
     */
    public abstract Template getCurrentMetadata(String key);

    /**
     * @param key
     * @return gets the template in draft templates matching key
     */
    public abstract Template getDraftMetadata(String key);

    public abstract String updateMetadata(String key, Template input);

    public abstract String addNewMetadata(Template metadata);

    public abstract List<InstanceLog> getInstanceLog(String guid);

    public abstract Guid writeInstanceLog(InstanceLog log, String guid);

    public abstract String deleteMetadata(String key);

    public abstract String addNewRefined(Refined refined);

    public abstract Refined getRefined(String key);

    public abstract String updateRefined(String key, Refined refined);

    public abstract String deleteRefined(String key);

    /**
     * @return a list of all keys registered with the system
     */
    public abstract List<String> getAllKeys();

    public abstract long getCount();

    public void uploadSandbox(MultipartFile file, String key) {
        String landingZone = properties.getLandingZone();
        GZIPOutputStream gzos = null;
        try {
            // Validate the key to prevent path traversal
            if (key != null && (key.contains("..") || key.contains("/") || key.contains("\\"))) {
                throw new IllegalArgumentException("Invalid key");
            }
            // Get the file and save it somewhere
            byte[] bytes = file.getBytes();
            String prepend = "sbx_";
            if (!StringUtils.isEmpty(key)) {
                prepend = prepend + key + "_";
            }
            String filename = prepend + file.getOriginalFilename();
            if (file.getOriginalFilename().toLowerCase().endsWith(".xml")) {
                File uploadedFile = new File(Paths.get(landingZone, filename).toString());
                file.transferTo(uploadedFile);
            } else {
                gzos = new GZIPOutputStream(new FileOutputStream(
                        Paths.get(landingZone, (filename + ".gz")).toString()));
                gzos.write(bytes);

            }
        } catch (IOException e) {
            logger.error("unable to upload file: " + e.getMessage());
            logger.error(e.toString());
            throw new AppInternalError("unable to upload file");
        } finally {
            if (gzos != null) {
                try {
                    gzos.finish();
                    gzos.close();
                } catch (IOException e) {
                    logger.error("unable to close gzipOutputStream - " + e.getMessage());
                    logger.error(e.toString());
                }
            }
        }

    }

    protected boolean matchesPattern(String fileInstance, String template) {
        return (fileInstance.startsWith(template));
    }

    protected String stripExtension(String name) {
        if (name.lastIndexOf('.') != -1) {
            return name.substring(0, name.lastIndexOf('.'));
        }
        return name;
    }

    protected boolean hasRequiredFields(Template metadata) {
        if (metadata.getFile() == null) {
            throw new RequiredInfoMissing();
        }
        if (metadata.getFile().getKey() == null) {
            throw new RequiredInfoMissing();
        }
        return true;
    }

    /**
     * Parses storage name for HDFS and TextFile into Stage, owner, and key This
     * is for easier searching capabilities
     *
     * @param storedName
     * @return
     */
    protected HashMap<String, String> parseKey(String storedName) {
        HashMap<String, String> result = new HashMap<>();
        result.put(Constants.OWNER_PREFIX, null);
        String key = storedName;
        if (storedName.contains(Constants.OWNER_PREFIX)) {
            String[] split = storedName.split(Constants.OWNER_PREFIX, 2);
            result.put(Constants.OWNER_PREFIX, split[1]);
            key = split[0];
        }
        result.put(Constants.TEMPLATE_KEY, key);
        return result;
    }

    protected String createKey(Template template) {
        String key = template.getFile().getKey() + Constants.OWNER_PREFIX;
        String owner = template.getFile().getBusiness().getOwner();
        key = key + (owner == null ? "" : Constants.EID_SPLIT + owner);
        String dataSteward = template.getFile().getBusiness().getDataSteward();
        key = key + (dataSteward == null ? "" : Constants.EID_SPLIT + dataSteward);
        String creator = template.getEid();
        key = key + (creator == null ? "" : Constants.EID_SPLIT + creator);
        return key;
    }

    public abstract Template getSandboxMetadata(String key);

    public abstract List<String> getRefinedKeys();
}

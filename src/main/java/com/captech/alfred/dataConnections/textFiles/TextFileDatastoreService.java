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

package com.captech.alfred.dataConnections.textFiles;

import com.captech.alfred.Constants;
import com.captech.alfred.dataConnections.DataStoreService;
import com.captech.alfred.exceptions.AppInternalError;
import com.captech.alfred.exceptions.Forbidden;
import com.captech.alfred.exceptions.KeyExistsException;
import com.captech.alfred.exceptions.NoDataFound;
import com.captech.alfred.instance.Guid;
import com.captech.alfred.instance.InstanceLog;
import com.captech.alfred.template.Template;
import com.captech.alfred.template.refined.Refined;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@EnableConfigurationProperties(TextFileProperties.class)

private void validateGuid(String guid) {
    if (guid.contains("..") || guid.contains("/") || guid.contains("\\")) {
        throw new IllegalArgumentException("Invalid guid");
    }
}

private void validateKey(String key) {
        if (key.contains("..")) {
            throw new IllegalArgumentException("Invalid key");
        }
    }
public class TextFileDatastoreService extends DataStoreService {

    private static final Logger logger = LoggerFactory.getLogger(TextFileDatastoreService.class);

    @Autowired
    TextFileProperties textFileProperties;

    @Override
    public Template getCurrentMetadata(String key) {
        Object current = getData(key, Paths.get(textFileProperties.getCurrentMd()).toString(), Template.class);
        if (current != null && current instanceof Template) {
            return (Template) current;
        }
        return null;
    }

    @Override
    public Template getDraftMetadata(String key) {

        Object draft = getData(key, textFileProperties.getDraftMd(), Template.class);
        if (draft != null && draft instanceof Template) {
            return (Template) draft;
        }
        return null;
    }

    @Override
    public Template getSandboxMetadata(String key) {
        logger.info(textFileProperties.getCurrentSandbox());
        Object draft = getData(key, textFileProperties.getCurrentSandbox(), Template.class);
        if (draft != null && draft instanceof Template) {
            return (Template) draft;
        }
        return null;
    }

    public Template getMetadata(String key) {
        validateKey(key);
        Template template = getCurrentMetadata(key);
        if (template != null) {
            return template;
        }
        template = getSandboxMetadata(key);
        if (template != null) {
            return template;
        }
        return getDraftMetadata(key);
    }

    /**
     * Assumes that the name of the file matches the name of the metadata file.
     *
     * @param fileKey - name of file found
     * @return name of metadata file
     */
    @Override
    public String findMetaDataByFileName(final String fileKey, String stage) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            logger.info("Sleep interrupted");
            Thread.currentThread().interrupt();
        }
        File folder = new File(Paths.get(textFileProperties.getCurrentMd()).toString());
        File sbfolder = new File(Paths.get(textFileProperties.getCurrentSandbox()).toString());
        FilenameFilter matchesKey = new FilenameFilter() {
            @Override
            public boolean accept(File directory, String filename) {
                return matchesPattern(fileKey, stripExtension(filename).split(Constants.OWNER_PREFIX)[0]);
            }
        };
        File file = null;
        if (stage == null || (stage != null && Constants.FINAL_STAGE.equalsIgnoreCase(stage))) {
            logger.info(folder.getAbsolutePath());
            File[] fileList = folder.listFiles(matchesKey);
            if (fileList.length > 0) {
                // most specific
                file = fileList[0];
                if (fileList.length > 1) {
                    for (int i = 1; i < fileList.length; i++) {
                        if (fileList[i].length() > file.length()) {
                            file = fileList[i];
                        }
                    }
                }
            }
        }
        if (stage == null || (stage != null && Constants.SANDBOX.equalsIgnoreCase(stage))) {
            File[] sbfileList = sbfolder.listFiles(matchesKey);
            if (sbfileList.length > 0) {
                if (file == null) {
                    file = sbfileList[0];
                }
                for (int i = 0; i < sbfileList.length; i++) {
                    if (sbfileList[i].length() > file.length()) {
                        file = sbfileList[i];
                    }
                }
            }
        }

        if (file == null || !file.isFile() || !file.canRead()) {
            logger.debug("file not found.");
            return null;
        }
        return parseKey(file.getName()).get(Constants.TEMPLATE_KEY);
    }

    @Override
    public String updateMetadata(String filename, Template newMetadata) {
        logger.debug("deleting current");
        deleteMetadata(filename);
        return addNewMetadata(newMetadata);
    }

    @Override
    public String addNewMetadata(Template metadata) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            logger.info("Sleep interrupted");
            Thread.currentThread().interrupt();
        }
        String path = Paths.get(textFileProperties.getCurrentMd()).toString();
        if (Constants.DRAFT.equalsIgnoreCase(metadata.getStage())) {
            path = Paths.get(textFileProperties.getDraftMd()).toString();
        }
        if (Constants.SANDBOX.equalsIgnoreCase(metadata.getStage())) {
            path = Paths.get(textFileProperties.getCurrentSandbox()).toString();
        }
        if (getCurrentMetadata(metadata.getFile().getKey()) != null) {
            throw new KeyExistsException();
        }
        logger.debug("Path of file: " + path);
        File file = new File(Paths.get(path, createKey(metadata)).toString());
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            if (!file.createNewFile()) {
                throw new KeyExistsException();
            }
            fw = new FileWriter(file, false);
            bw = new BufferedWriter(fw);
            ObjectMapper om = new ObjectMapper();
            om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            om.writeValue(bw, metadata);
        } catch (IOException e) {
            throw new Forbidden("TextFile: " + e.getMessage());
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException e) {
                logger.error("cannot close file writer: " + e.getMessage());
            }
        }

        return metadata.getFile().getKey();
    }

    private String deleteDraft(String filename, String draftLocation) {
        logger.debug("file not found in current");
        String foundfile = findFileName(filename, draftLocation);
        if (foundfile == null) {
            throw new NoDataFound();
        }
        File file = new File(Paths.get(draftLocation, foundfile).toString());

        logger.debug("deleting draft at: " + file.getAbsolutePath());
        if (!file.isFile() || !file.canRead()) {
            throw new NoDataFound();
        }
        try {
            file.delete();
        } catch (SecurityException e) {
            logger.debug(e.toString());
            logger.debug(e.getMessage());
            throw new Forbidden("Text File: " + e.getMessage());
        }
        return "draft deleted";
    }

    /**
     * Soft deletes a metadata file by marking it as deleted and moving to a
     * different directory returns new location of soft deleted file.
     */
    @Override
    public String deleteMetadata(String filename) {
        File file = null;
        String versionLocation = null;
        // gets filedata and writes to deleted directory
        Template existingData = this.getMetadata(filename);
        if (existingData == null) {
            throw new NoDataFound();
        }
        SimpleDateFormat hadoop_format = new SimpleDateFormat(Constants.HADOOP_VERS_FORMAT);
        String formattedVersion = hadoop_format.format(new Date());

        if (!StringUtils.isEmpty(existingData.getVersion())) {
            try {
                formattedVersion = hadoop_format
                        .format(hadoop_format.parse(existingData.getVersion()));
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                logger.error(
                        "TextFileDataStoreService - unable to parse version from file. Using current date instead");
            }
        }

        String foundFilename = findFileName(filename, textFileProperties.getCurrentMd());
        if (foundFilename != null) {
            file = new File(Paths.get(textFileProperties.getCurrentMd(), foundFilename).toString());
            versionLocation = Paths.get(textFileProperties.getVersionedMd(), filename + "_" + formattedVersion).toString();
        }
        if (file == null || !file.isFile()) {
            foundFilename = findFileName(filename, textFileProperties.getCurrentSandbox());
            if (foundFilename != null) {
                file = new File(Paths.get(textFileProperties.getCurrentSandbox(), foundFilename).toString());

                versionLocation = Paths.get(textFileProperties.getVersionedSandbox(), filename + "_" + formattedVersion)
                        .toString();
            }
        }
        if (file == null || !file.isFile()) {
            return deleteDraft(filename, textFileProperties.getDraftMd());
        }
        if (file != null) {
            boolean renamed = file.renameTo(new File(versionLocation));
            if (!renamed) {
                logger.error("did not delete!");
            }
        }

        // check for it at draft

        return versionLocation;
    }

    @Override
    public List<InstanceLog> getInstanceLog(final String guid) {

        validateGuid(guid);
        List<InstanceLog> logs = new ArrayList<>();
        FileReader fr = null;
        File dir = new File(Paths.get(textFileProperties.getLogLocation()).toString());
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().startsWith(guid.toLowerCase());
            }
        });
        ObjectMapper mapper = new ObjectMapper();
        if (files.length <= 0) {
            throw new NoDataFound();
        }
        for (File file : files) {
            InstanceLog log = new InstanceLog();
            if (!file.isFile() || !file.canRead()) {
                throw new NoDataFound();
            }
            try {
                fr = new FileReader(file);
                try {
                    log = mapper.readValue(file, InstanceLog.class);
                    logs.add(log);
                } catch (JsonParseException e) {
                    logger.error("cannot parse json: " + e.getMessage());
                }
            } catch (IOException e) {
                logger.error("cannot read file: " + e.getMessage());
            } finally {
                try {
                    if (fr != null) {
                        fr.close();
                    }
                } catch (IOException ex) {
                    logger.error("cannot close file writer: " + ex.getMessage());
                }
            }
        }
        return logs;
    }

    @Override
    public Guid writeInstanceLog(InstanceLog log, String guid) {
        validateGuid(guid);
        UUID guidUUID = UUID.randomUUID();
        if (guid == null) {
            guid = guidUUID.toString();
            log.setGuid(guidUUID);
        } else {
            guidUUID = UUID.fromString(guid);
            log.setGuid(guidUUID);
        }
        if (log.getStage() == null) {
            log.setStage("unknown");
        }
        File file = new File(Paths.get(textFileProperties.getLogLocation(), guid + "_" + log.getStage()).toString());
        logger.debug("instance log location: " + file.getAbsolutePath());
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            if (!file.createNewFile()) {
                throw new KeyExistsException();
            }
            fw = new FileWriter(file, false);
            bw = new BufferedWriter(fw);
            ObjectMapper om = new ObjectMapper();
            om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
            om.writeValue(bw, log);
        } catch (IOException e) {
            throw new Forbidden("Text file: " + e.getMessage());
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException e) {
                logger.error("cannot close file writer: " + e.getMessage());
            }
        }

        return new Guid(guidUUID);
    }

    @Override
    public String addNewRefined(Refined refined) {
        String path = Paths.get(textFileProperties.getCurrentRefined()).toString();
        if (Constants.DRAFT.equalsIgnoreCase(refined.getRefinedDataset().getStage())) {
            path = Paths.get(textFileProperties.getDraftRefined()).toString();
        }
        logger.debug("Path of file: " + path);
        String filename = createKey(refined.getRefinedDataset());
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IllegalArgumentException("Invalid filename");
        }
        File file = new File(Paths.get(path, filename).toString());
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            if (!file.createNewFile()) {
                throw new KeyExistsException();
            }
            fw = new FileWriter(file, false);
            bw = new BufferedWriter(fw);
            ObjectMapper om = new ObjectMapper();
            om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
            om.writeValue(bw, refined);
        } catch (IOException e) {
            throw new Forbidden("Text file: " + e.getMessage());
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException e) {
                logger.error("cannot close file writer: " + e.getMessage());
            }
        }

        return refined.getRefinedDataset().getFile().getKey();
    }

    @Override
    public Refined getRefined(String key) {
        logger.debug("get refined metadata: " + key);
        Object current = getData(key, textFileProperties.getCurrentRefined(), Refined.class);
        if (current != null && current instanceof Refined) {
            return (Refined) current;
        }
        Object draft = getData(key, textFileProperties.getDraftRefined(), Refined.class);
        if (draft != null && draft instanceof Refined) {
            return (Refined) draft;
        }
        throw new NoDataFound();
    }

    public String findFileName(final String key, String location) {
        File dir = new File(Paths.get(location).toString());
        FilenameFilter matchesKey = new FilenameFilter() {
            @Override
            public boolean accept(File directory, String filename) {
                return StringUtils.equals(filename.split(Constants.OWNER_PREFIX)[0], key);
            }
        };

        File[] files = dir.listFiles(matchesKey);
        if (files.length > 1) {
            throw new AppInternalError("found more than one file matching the key");
        }
        if (files.length < 1) {
            return null;
        }
        File file = files[0];
        if (!file.isFile() || !file.canRead()) {
            logger.debug("file not found.");
            return null;
        }
        return file.getName();
    }

    @Override
    public String updateRefined(String key, Refined refined) {
        logger.debug("deleting old");
        deleteRefined(key);
        return addNewRefined(refined);
    }

    @Override
    public String deleteRefined(String filename) {
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IllegalArgumentException("Invalid filename");
        }
        File file = new File(
                Paths.get(textFileProperties.getCurrentRefined(), findFileName(filename, textFileProperties.getCurrentRefined()))
                        .toString());
        if (!file.isFile()) {
            return deleteDraft(filename, textFileProperties.getDraftRefined());
        }

        // gets filedata and writes to deleted directory
        Refined existingData = this.getRefined(filename);
        if (existingData == null) {
            throw new NoDataFound();
        }
        SimpleDateFormat hadoop_format = new SimpleDateFormat(Constants.HADOOP_VERS_FORMAT);
        String formattedVersion = hadoop_format.format(new Date());
        if (!StringUtils.isEmpty(existingData.getRefinedDataset().getVersion())) {
            try {
                formattedVersion = hadoop_format
                        .format(hadoop_format.parse(existingData.getRefinedDataset().getVersion()));
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                logger.error(
                        "TextFileDataStoreService - unable to parse version from file. Using current date instead");
            }
        }
        String versionLocation = Paths.get(textFileProperties.getVersionedRefined(), filename + "_" + formattedVersion)
                .toString();
        file.renameTo(new File(versionLocation));
        // check for it at draft

        return versionLocation;
    }

    @Override
    public List<String> getAllKeys() {
        ArrayList<String> keys = new ArrayList<>();
        keys.addAll(getKeysFromFilename(textFileProperties.getCurrentMd()));
        keys.addAll(getKeysFromFilename(textFileProperties.getCurrentSandbox()));
        return keys;
    }

    private List<String> getKeysFromFilename(String path) {
        ArrayList<String> keys = new ArrayList<>();
        File folder = new File(Paths.get(path).toString());
        logger.debug(folder.getAbsolutePath());
        File[] fileList = folder.listFiles();
        if (fileList != null) {
            for (File file : fileList) {
                String key = parseKey(file.getName()).get(Constants.TEMPLATE_KEY);
                keys.add(key);
            }
        }
        return keys;
    }

    @Override
    public List<String> getRefinedKeys() {
        ArrayList<String> keys = new ArrayList<>();
        keys.addAll(getKeysFromFilename(textFileProperties.getCurrentRefined()));
        return keys;
    }

    private Object getData(final String key, String location, Class<?> type) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            logger.info("Sleep interrupted");
            Thread.currentThread().interrupt();
        }
        logger.info("get data: " + Paths.get(location, key).toString() + " of type " + type.getCanonicalName());
        File dir = new File(Paths.get(location).toString());
        FilenameFilter matchesKey = new FilenameFilter() {
            @Override
            public boolean accept(File directory, String filename) {
                return StringUtils.equals(filename.split(Constants.OWNER_PREFIX)[0], key);
            }
        };

        File[] files = dir.listFiles(matchesKey);
        if (files == null) {
            return null;
        }
        if (files.length > 1) {
            throw new AppInternalError("found more than one file matching the key");
        }
        if (files.length < 1) {
            return null;
        }
        File file = files[0];
        if (!file.isFile() || !file.canRead()) {
            logger.debug("file not found.");
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readValue(file, type);
        } catch (IOException e) {
            logger.error("IOEXception: ", e);
            throw new AppInternalError("Unable to read data - " + e.getMessage());
        }
    }

    @Override
    public long getCount() {
        long count = 0;
        File file = new File(Paths.get(textFileProperties.getCurrentMd()).toString());
        if (file.listFiles() != null) {
            count = count + file.listFiles().length;
        }
        file = new File(Paths.get(textFileProperties.getCurrentSandbox()).toString());
        if (file.listFiles() != null) {
            count = count + file.listFiles().length;
        }
        file = new File(Paths.get(textFileProperties.getCurrentRefined()).toString());
        if (file.listFiles() != null) {
            count = count + file.listFiles().length;
        }
        return count;

    }

}

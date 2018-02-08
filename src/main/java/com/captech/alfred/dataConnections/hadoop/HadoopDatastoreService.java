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

package com.captech.alfred.dataConnections.hadoop;

import com.captech.alfred.Constants;
import com.captech.alfred.dataConnections.DataStoreService;
import com.captech.alfred.exceptions.AppInternalError;
import com.captech.alfred.exceptions.KeyExistsException;
import com.captech.alfred.exceptions.NoDataFound;
import com.captech.alfred.instance.Guid;
import com.captech.alfred.instance.InstanceLog;
import com.captech.alfred.template.Template;
import com.captech.alfred.template.refined.Refined;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.ContentSummary;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.hadoop.fs.FsShell;
import org.springframework.data.hadoop.store.output.TextFileWriter;
import org.springframework.data.hadoop.store.strategy.naming.StaticFileNamingStrategy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@EnableAutoConfiguration
@EnableConfigurationProperties(HadoopProperties.class)
public class HadoopDatastoreService extends DataStoreService {

    private static final Logger logger = LoggerFactory.getLogger(HadoopDatastoreService.class);

    @Autowired
    HadoopProperties properties;

    @Autowired
    private FsShell shell;

    @Autowired
    private TextFileWriter templateWriter;

    @Autowired
    private TextFileWriter instanceWriter;

    @Autowired
    private TextFileWriter refinedWriter;

    @Autowired
    private TextFileWriter refinedDraftWriter;

    @Autowired
    private TextFileWriter draftWriter;

    @Autowired
    private TextFileWriter sandboxWriter;

    @Override
    public Template getCurrentMetadata(String key) {
        return getMetadata(properties.getCurrentMd(), key);
    }

    @Override
    public Template getDraftMetadata(String key) {
        return getMetadata(properties.getDraftMd(), key);
    }

    @Override
    public Template getSandboxMetadata(String key) {
        return getMetadata(properties.getCurrentSandbox(), key);
    }

    public Template getMetadata(String path, String key) {
        logger.debug("get metadata: " + key);
        Collection<String> fileCollection = null;
        String filename = findData(path, key);
        if (filename != null) {
            fileCollection = shell.text(Paths.get(path, filename).toString());
            if (!fileCollection.isEmpty()) {
                return convertToTemplate(fileCollection.iterator().next());
            }
        }
        return null;
    }

    private Template convertToTemplate(String data) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(data, Template.class);
        } catch (IOException e) {
            logger.error("Unable to read data - " + e.getMessage());
            logger.error(e.toString());
            throw new AppInternalError("Unable to read data - " + e.getMessage());
        }
    }

    private Refined convertToRefined(String data) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(data, Refined.class);
        } catch (IOException e) {
            logger.error("Unable to read data - " + e.getMessage());
            logger.error(e.toString());
            throw new AppInternalError("Unable to read data - " + e.getMessage());
        }
    }

    @Override
    public String findMetaDataByFileName(String filename, String stage) {
        String key = null;
        if (StringUtils.isEmpty(stage) || (stage != null && Constants.FINAL_STAGE.equals(stage))) {
            key = findDataFromLS(shell.ls(Paths.get(properties.getCurrentMd()).toString()), filename, null);
        }

        if (StringUtils.isEmpty(stage) || (stage != null && Constants.SANDBOX.equals(stage))) {
            key = findDataFromLS(shell.ls(Paths.get(properties.getCurrentSandbox()).toString()), filename, key);
        }

        return key;
    }

    private String findDataFromLS(Collection<FileStatus> fileStatuses, String filename, String lastfound) {
        String key = lastfound;
        for (FileStatus s : fileStatuses) {
            if (s.isFile()) {
                String name = s.getPath().getName().split(Constants.OWNER_PREFIX)[0];
                if (matchesPattern(filename, name)) {
                    // make sure it's the most specific match
                    String tempKey = parseKey(name).get(Constants.TEMPLATE_KEY);
                    if (key == null || tempKey.length() > key.length()) {
                        key = tempKey;
                    }
                }
            }
        }
        return key;
    }

    @Override
    public String updateMetadata(String key, Template input) {
        if (Constants.DRAFT.equalsIgnoreCase(input.getStage())) {
            deleteDraft(key);
        } else {
            deleteMetadata(key);
        }
        return addNewMetadata(input);
    }

    @Override
    public String addNewMetadata(Template metadata) {
        if (!hasRequiredFields(metadata)) {
            return null;
        }
        if (!Constants.DRAFT.equalsIgnoreCase(metadata.getStage()) && getCurrentMetadata(metadata.getFile().getKey()) != null) {
            throw new KeyExistsException();
        }
        if (Constants.SANDBOX.equalsIgnoreCase(metadata.getStage())
                && getSandboxMetadata(metadata.getFile().getKey()) != null) {
            throw new KeyExistsException();
        }
        if (Constants.REFINED.equalsIgnoreCase(metadata.getStage()) && getRefined(metadata.getFile().getKey()) != null) {
            throw new KeyExistsException();
        }

        TextFileWriter writer = templateWriter;
        if (Constants.DRAFT.equalsIgnoreCase(metadata.getStage())) {
            writer = draftWriter;
            deleteDraft(metadata.getFile().getKey());
        }
        if (Constants.SANDBOX.equalsIgnoreCase(metadata.getStage())) {
            writer = sandboxWriter;
        }
        writer.setFileNamingStrategy(new StaticFileNamingStrategy(createKey(metadata)));
        ObjectMapper mapper = new ObjectMapper();
        try {
            String dataAsString = mapper.writeValueAsString(metadata);
            writer.write(dataAsString);
        } catch (IOException e) {
            logger.error("unable to write data to Hadoop: " + e.getMessage());
            logger.error(e.toString());
            throw new AppInternalError("unable to write data to Hadoop: " + e.getMessage());
        } finally {
            try {
                writer.flush();
                writer.close();
            } catch (IOException e) {
                logger.error("unable to write data to Hadoop: " + e.getMessage());
                logger.error(e.toString());
            }
        }
        return metadata.getFile().getKey();
    }

    @Override
    public String deleteMetadata(String key) {

        Template data = getCurrentMetadata(key);
        if (data == null) {
            String returnVal = deleteSandboxMetadata(key);
            if (StringUtils.isEmpty(returnVal) && data == null) {
                if (!deleteDraft(key)) {
                    throw new NoDataFound();
                }
                return "draft deleted";
            }
            return returnVal;
        }
        String version = data.getVersion();
        Date dateVersion = new Date();
        if (version != null) {
            try {
                dateVersion = new SimpleDateFormat(Constants.VERSION_FORMAT).parse(version);
            } catch (ParseException e) {
                logger.error("HadoopDatastoreService - unable to parse version from file. Using current date instead");
            }
        }

        String formattedVersion = new SimpleDateFormat(Constants.HADOOP_VERS_FORMAT).format(dateVersion);
        String currentFile = Paths.get(properties.getCurrentMd(), findData(properties.getCurrentMd(), key)).toString();
        shell.mv(currentFile, Paths.get(properties.getVersionedMd(), key + "_" + formattedVersion).toString());
        return properties.getVersionedMd() + key;
    }

    private String deleteSandboxMetadata(String key) {
        Template data = getSandboxMetadata(key);
        if (data == null) {
            return null;
        }
        String version = data.getVersion();
        Date dateVersion = new Date();
        if (version != null) {
            try {
                dateVersion = new SimpleDateFormat(Constants.VERSION_FORMAT).parse(version);
            } catch (ParseException e) {
                logger.error("HadoopDatastoreService - unable to parse version from file. Using current date instead");
            }
        }

        String formattedVersion = new SimpleDateFormat(Constants.HADOOP_VERS_FORMAT).format(dateVersion);
        String currentFile = Paths.get(properties.getCurrentSandbox(), findData(properties.getCurrentSandbox(), key))
                .toString();
        shell.mv(currentFile, Paths.get(properties.getVersionedSandbox(), (key + "_" + formattedVersion)).toString());
        return properties.getVersionedSandbox() + key;
    }

    @Override
    public List<InstanceLog> getInstanceLog(String guid) {
        List<InstanceLog> logs = new ArrayList<>();
        Collection<String> fileTexts = shell.text(Paths.get(properties.getLogLocation() + guid + "*").toString());
        if (fileTexts == null || fileTexts.isEmpty()) {
            throw new NoDataFound();
        }
        for (String s : fileTexts) {
            InstanceLog log = new InstanceLog();
            ObjectMapper mapper = new ObjectMapper();
            try {
                log = mapper.readValue(s, InstanceLog.class);
                logs.add(log);
            } catch (IOException e) {
                logger.error("Unable to read data - " + e.getMessage());
                logger.error(e.toString());
                throw new AppInternalError("Unable to read data - " + e.getMessage());
            }
        }
        return logs;
    }

    @Override
    public Guid writeInstanceLog(InstanceLog log, String guid) {
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
        String name = guid + "_" + log.getStage();
        for (FileStatus s : shell.ls(Paths.get(properties.getLogLocation()).toString())) {
            if (s.isFile() && stripExtension(s.getPath().getName()).equals(name)) {
                logger.error("Instance Log name: " + name + "already exists. Writing new name");
                name = UUID.randomUUID().toString() + "_" + log.getStage();
                logger.error("Instance log new name: " + name);
            }
        }

        instanceWriter.setFileNamingStrategy(new StaticFileNamingStrategy(name));
        ObjectMapper mapper = new ObjectMapper();
        try {
            String dataAsString = mapper.writeValueAsString(log);
            instanceWriter.write(dataAsString);
        } catch (IOException e) {
            logger.error("unable to write data to Hadoop: " + e.getMessage());
            logger.error(e.toString());
            throw new AppInternalError("unable to write data to Hadoop: " + e.getMessage());
        } finally {
            try {
                instanceWriter.flush();
                instanceWriter.close();
            } catch (IOException e) {
                logger.error("unable to write data to Hadoop: " + e.getMessage());
                logger.error(e.toString());
            }
        }
        return new Guid(guidUUID);
    }

    @Override
    public String addNewRefined(Refined refined) {
        if (!Constants.DRAFT.equalsIgnoreCase(refined.getRefinedDataset().getStage())
                && findCurrentRefined(refined.getRefinedDataset().getFile().getKey()) != null) {
            throw new KeyExistsException();
        }

        TextFileWriter writer = refinedWriter;
        if (Constants.DRAFT.equalsIgnoreCase(refined.getRefinedDataset().getStage())) {
            writer = refinedDraftWriter;
            deleteRefinedDraft(refined.getRefinedDataset().getFile().getKey());
        }

        writer.setFileNamingStrategy(new StaticFileNamingStrategy(createKey(refined.getRefinedDataset())));
        ObjectMapper mapper = new ObjectMapper();
        try {
            String dataAsString = mapper.writeValueAsString(refined);
            writer.write(dataAsString);
        } catch (IOException e) {
            logger.error("unable to write data to Hadoop: " + e.getMessage());
            logger.error(e.toString());
            throw new AppInternalError("unable to write data to Hadoop: " + e.getMessage());
        } finally {
            try {
                writer.flush();
                writer.close();
            } catch (IOException e) {
                logger.error("unable to write data to Hadoop: " + e.getMessage());
                logger.error(e.toString());
            }
        }
        return refined.getRefinedDataset().getFile().getKey();
    }

    public Refined getRefined(String path, String key) {
        logger.debug("get metadata: " + key);
        Collection<String> fileCollection = null;
        String filename = findData(path, key);
        if (filename != null) {
            fileCollection = shell.text(Paths.get(path, filename).toString());
            if (!fileCollection.isEmpty()) {
                return convertToRefined(fileCollection.iterator().next());
            }
        }
        return null;
    }

    @Override
    public Refined getRefined(String key) {
        Refined refined = getRefined(properties.getCurrentRefined(), key);
        if (refined != null) {
            return refined;
        }
        refined = getRefined(properties.getDraftRefined(), key);
        if (refined != null) {
            return refined;
        }
        throw new NoDataFound();
    }

    @Override
    public String updateRefined(String key, Refined refined) {
        if (Constants.DRAFT.equalsIgnoreCase(refined.getRefinedDataset().getStage())) {
            deleteRefinedDraft(key);
        } else {
            deleteRefined(key);
        }
        return addNewRefined(refined);
    }

    @Override
    public String deleteRefined(String key) {

        Refined data = getRefined(properties.getCurrentRefined(), key);
        if (data == null) {
            if (!deleteRefinedDraft(key)) {
                throw new NoDataFound();
            }
            return "draft deleted";
        }
        String version = data.getRefinedDataset().getVersion();
        Date dateVersion = new Date();
        if (version != null) {
            try {
                dateVersion = new SimpleDateFormat(Constants.VERSION_FORMAT).parse(version);
            } catch (ParseException e) {
                logger.error("HadoopDatastoreService - unable to parse version from file. Using current date instead");
            }
        }
        String formattedVersion = new SimpleDateFormat(Constants.HADOOP_VERS_FORMAT).format(dateVersion);
        String currentFile = Paths.get(properties.getCurrentRefined(), findData(properties.getCurrentRefined(), key))
                .toString();
        shell.mv(currentFile, properties.getVersionedRefined() + key + "_" + formattedVersion);
        return properties.getVersionedRefined() + key;
    }

    private String findData(String path, String key) {
        for (FileStatus s : shell.ls(Paths.get(path).toString())) {
            if (s.isFile() && StringUtils.equals(s.getPath().getName().split(Constants.OWNER_PREFIX)[0], key)) {
                return s.getPath().getName();
            }
        }
        return null;
    }

    private String findRefinedDraft(String key) {
        return findData(properties.getDraftRefined(), key);
    }

    private String findCurrentRefined(String key) {
        return findData(properties.getCurrentRefined(), key);
    }

    private boolean deleteDraft(String key) {
        String filename = findData(properties.getDraftMd(), key);
        if (filename == null) {
            return false;
        }
        shell.rm(Paths.get(properties.getDraftMd(), filename).toString());
        return true;
    }

    private boolean deleteRefinedDraft(String key) {
        String filename = findRefinedDraft(key);
        if (filename == null) {
            return false;
        }
        shell.rm(Paths.get(properties.getDraftRefined(), filename).toString());
        return true;
    }

    @Override
    public List<String> getAllKeys() {
        ArrayList<String> keys = new ArrayList<>();
        for (FileStatus s : shell.ls(Paths.get(properties.getCurrentMd()).toString())) {
            if (s.isFile()) {
                HashMap<String, String> map = parseKey(s.getPath().getName());
                String key = map.get(Constants.TEMPLATE_KEY);
                keys.add(key);
            }
        }
        for (FileStatus s : shell.ls(Paths.get(properties.getCurrentSandbox()).toString())) {
            if (s.isFile()) {
                HashMap<String, String> map = parseKey(s.getPath().getName());
                String key = map.get(Constants.TEMPLATE_KEY);
                keys.add(key);
            }
        }

        return keys;
    }

    @Override
    public long getCount() {
        // TODO: vary based on request of what to see and add drafts
        Map<Path, ContentSummary> currentCountSummary = shell.count(Paths.get(properties.getCurrentMd()).toString());
        long currentCount = 0;
        for (ContentSummary cs : currentCountSummary.values()) {
            currentCount += cs.getFileCount();
        }
        long refinedCount = 0;
        Map<Path, ContentSummary> refinedCountSummary = shell
                .count(Paths.get(properties.getCurrentRefined()).toString());
        for (ContentSummary cs : refinedCountSummary.values()) {
            refinedCount += cs.getFileCount();
        }
        long sandboxCount = 0;
        Map<Path, ContentSummary> sanboxCountSummary = shell
                .count(Paths.get(properties.getCurrentSandbox()).toString());
        for (ContentSummary cs : sanboxCountSummary.values()) {
            sandboxCount += cs.getFileCount();
        }
        return currentCount + refinedCount + sandboxCount;
    }

    @Override
    public List<String> getRefinedKeys() {
        ArrayList<String> keys = new ArrayList<>();
        for (FileStatus s : shell.ls(Paths.get(properties.getCurrentRefined()).toString())) {
            if (s.isFile()) {
                HashMap<String, String> map = parseKey(s.getPath().getName());
                String key = map.get(Constants.TEMPLATE_KEY);
                keys.add(key);
            }
        }
        return keys;
    }

    @Configuration
    @EnableConfigurationProperties(HadoopProperties.class)
    static class Config {

        @Autowired
        HadoopProperties properties;
        @Autowired
        private org.apache.hadoop.conf.Configuration hadoopConfiguration;

        @Bean
        TextFileWriter templateWriter() {
            TextFileWriter writer = new TextFileWriter(hadoopConfiguration, new Path(properties.getCurrentMd()), null);
            return writer;
        }

        @Bean
        TextFileWriter draftWriter() {
            TextFileWriter writer = new TextFileWriter(hadoopConfiguration, new Path(properties.getDraftMd()), null);
            return writer;
        }

        @Bean
        TextFileWriter instanceWriter() {
            TextFileWriter writer = new TextFileWriter(hadoopConfiguration, new Path(properties.getLogLocation()),
                    null);
            return writer;
        }

        @Bean
        TextFileWriter refinedWriter() {
            TextFileWriter writer = new TextFileWriter(hadoopConfiguration, new Path(properties.getCurrentRefined()),
                    null);
            return writer;
        }

        @Bean
        TextFileWriter refinedDraftWriter() {
            TextFileWriter writer = new TextFileWriter(hadoopConfiguration, new Path(properties.getDraftRefined()),
                    null);
            return writer;
        }

        @Bean
        TextFileWriter sandboxWriter() {
            TextFileWriter writer = new TextFileWriter(hadoopConfiguration, new Path(properties.getCurrentSandbox()),
                    null);
            return writer;
        }

    }

}

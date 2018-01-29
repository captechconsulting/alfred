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
//package com.captech.alfred.dataConnections;
//
//import static org.hamcrest.CoreMatchers.equalTo;
//import static org.hamcrest.CoreMatchers.is;
//import static org.hamcrest.CoreMatchers.not;
//import static org.hamcrest.CoreMatchers.nullValue;
//import static org.hamcrest.Matchers.empty;
//import static org.junit.Assert.assertThat;
//
//import java.io.File;
//import java.util.List;
//import java.util.UUID;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import com.captech.alfred.dataConnections.textFiles.TextFileProperties;
//import com.captech.alfred.exceptions.KeyExistsException;
//import com.captech.alfred.exceptions.NoDataFound;
//import com.captech.alfred.instance.Guid;
//import com.captech.alfred.instance.InstanceLog;
//import com.captech.alfred.template.Template;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//@RunWith(SpringRunner.class)
//@EnableAutoConfiguration
//@SpringBootTest
//public class DataStoreServiceTest {
//
//    @Autowired
//    TextFileProperties properties;
//
//    @Autowired
//    DataStoreService datastoreService;
//
//    private String testTemplate = "editorSample.json";
//    private String testKey = "editorSample";
//    private String testFile = "editorSample_Q1.csv";
//    private String noTemplate = "madeup-Q1.csv";
//    private String notATemplate = "madeupFile.json";
//    private String key = "householdElectricPowerConsumption";
//    private String guid = "beeffeed-cafe-face-deed-123456789abc";
//
//    @Test
//    public void getMetadataPass() throws Exception {
//        Template md = datastoreService.getMetadata(testTemplate);
//        assertThat(md.getFile().getKey(), is(equalTo(key)));
//    }
//
//    @Test(expected = NoDataFound.class)
//    public void getMetadataThrowsNoDataException() throws Exception {
//        datastoreService.getMetadata(notATemplate);
//    }
//
//    @Test
//    public void findMetadataPass() throws Exception {
//        String template = datastoreService.findMetaDataByFileName(testFile);
//        assertThat(template, is(equalTo(testTemplate)));
//    }
//
//    @Test
//    public void findMetadataEmpty() throws Exception {
//        String template = datastoreService.findMetaDataByFileName(noTemplate);
//        assertThat(template, is(nullValue()));
//    }
//
//    @Test
//    public void updateMetadataPass() throws Exception {
//        ObjectMapper mapper = new ObjectMapper();
//        File file = new File(properties.getMetadataLocation() + testTemplate);
//        Template input = mapper.readValue(file, Template.class);
//        String filereturn = datastoreService.updateMetadata(testTemplate, input);
//        assertThat(filereturn, is(equalTo(testTemplate)));
//    }
//
//    @Test(expected = NoDataFound.class)
//    public void updateMetadataNoFileToUpdate() throws Exception {
//        ObjectMapper mapper = new ObjectMapper();
//        File file = new File(properties.getMetadataLocation() + testTemplate);
//        Template input = mapper.readValue(file, Template.class);
//        String filereturn = datastoreService.updateMetadata(notATemplate, input);
//        // won't get here - prior throws NoDataFound
//        assertThat(filereturn, is(not(equalTo(testTemplate))));
//    }
//
//    @Test
//    public void addNewMetadataPass() throws Exception {
//        ObjectMapper mapper = new ObjectMapper();
//        File file = new File(properties.getMetadataLocation() + testTemplate);
//        Template input = mapper.readValue(file, Template.class);
//        input.getFile().setKey(key);
//        String keyreturn = datastoreService.addNewMetadata(input);
//        assertThat(keyreturn, is(equalTo(key)));
//        (new File(properties.getMetadataLocation() + keyreturn + ".json")).delete();
//    }
//
//    @Test(expected = KeyExistsException.class)
//    public void addNewMetadataKeyExists() throws Exception {
//        ObjectMapper mapper = new ObjectMapper();
//        File file = new File(properties.getMetadataLocation() + testTemplate);
//        Template input = mapper.readValue(file, Template.class);
//        input.getFile().setKey(testKey);
//        String keyreturn = datastoreService.addNewMetadata(input);
//        (new File(properties.getMetadataLocation() + keyreturn + ".json")).delete();
//    }
//
//    @Test
//    public void getInstanceLogsPass() throws Exception {
//        List<InstanceLog> output = datastoreService.getInstanceLog(guid);
//        assertThat(output, is(not(empty())));
//    }
//
//    @Test(expected = NoDataFound.class)
//    public void getInstanceLogsNoData() throws Exception {
//        datastoreService.getInstanceLog("noguid");
//    }
//
//    @Test
//    public void writeNewInstanceLogPass() throws Exception {
//        Guid newguid = new Guid(UUID.randomUUID());
//        try {
//            newguid = datastoreService.writeInstanceLog(new InstanceLog(), null);
//            assertThat(newguid.getGuid().toString(), is(not(nullValue())));
//        } catch (Exception e) {
//            throw e;
//        } finally {
//            (new File(properties.getLogLocation() + newguid.getGuid().toString() + "_unknown.json")).delete();
//        }
//    }
//
//    @Test(expected = KeyExistsException.class)
//    public void writeNewInstancelogKeyExists() throws Exception {
//        InstanceLog log = new InstanceLog();
//        log.setStage("raw");
//        datastoreService.writeInstanceLog(log, guid);
//        (new File(properties.getLogLocation() + guid.toString() + "_raw.json")).delete();
//    }
//
//}

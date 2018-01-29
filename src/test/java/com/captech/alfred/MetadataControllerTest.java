/*
 * Copyright 2018 CapTech Ventures, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http:www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.captech.alfred;

import com.captech.alfred.dataConnections.DataStoreService;
import com.captech.alfred.exceptions.KeyExistsException;
import com.captech.alfred.template.RegisteredKeys;
import com.captech.alfred.template.Template;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("local")
@WithMockUser(username = "admin", password = "admin", authorities = {
        "ROLE_ADMIN"})
public class MetadataControllerTest {


    @Autowired
    protected WebApplicationContext wac;

    //@MockBean(classes=TextFileDatastoreService.class)
    //@Qualifier("getConnector")
    @Autowired
    DataStoreService dataConnection;

    private MockMvc mockMvc;
    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private ObjectMapper mapper;
    private String testFilePath = "/fileMetadata/sampleFile.txt";
    private String testFile = "src/test/resources/editorSample";
    private String deletedTestFileLocation = "/opt/deleted/householdElectricPowerConsumption.json";
    private String key = "householdElectricPowerConsumption";
    private Template md = new Template();

    @Before
    public void setup() {
        Mockito.reset(dataConnection);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity(springSecurityFilterChain)).build();
        mapper = new ObjectMapper();
        FileReader fr = null;
        File file = new File(testFile);
        if (file.isFile() && file.canRead()) {
            try {
                fr = new FileReader(file);
                md = mapper.readValue(file, Template.class);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fr != null) {
                        fr.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    @Test
    public void updateMetadataPass() throws Exception {
        String data = mapper.writeValueAsString(md);
        when(dataConnection.updateMetadata(any(String.class), any(Template.class))).thenReturn(key);
        when(dataConnection.getCurrentMetadata(any(String.class))).thenReturn(md);
        this.mockMvc.perform(put("/fileMetadata/" + key).contentType(MediaType.APPLICATION_JSON).content(data))
                .andDo(print()).andExpect(status().isOk()).andExpect(content().json(data));
    }

    @Test
    public void findMetadataNoParamFail() throws Exception {
        this.mockMvc.perform(get("/fileMetadata")).andDo(print()).andExpect(status().isBadRequest());
    }

    @Test
    public void findMetadataPass() throws Exception {

        when(dataConnection.findMetaDataByFileName(any(String.class), any(String.class))).thenReturn(key);
        when(dataConnection.getCurrentMetadata(any(String.class))).thenReturn(md);

        this.mockMvc.perform(get("/fileMetadata").param("name", "editorSample.csv")).andDo(print())
                .andExpect(status().isOk()).andExpect(jsonPath("$.file.key").value(key)).andReturn();
    }

    @Test
    public void getKeysPass() throws Exception {
        RegisteredKeys keys = new RegisteredKeys();
        keys.setRegisteredKeys(Arrays.asList("sampleTest.json", key));

        this.mockMvc.perform(get("/fileMetadata/keys")).andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.registeredKeys").isArray()).andReturn();
    }

    @Test
    public void getMetadataPass() throws Exception {
        when(dataConnection.getCurrentMetadata(any(String.class))).thenReturn(md);
        this.mockMvc.perform(get("/fileMetadata/" + key)).andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.file.key").value(key)).andReturn();
    }

    @Test
    public void newMetadataPass() throws Exception {
        String input = mapper.writeValueAsString(md);

        when(dataConnection.addNewMetadata(any(Template.class))).thenReturn(key);
        this.mockMvc.perform(post("/fileMetadata/").contentType(MediaType.APPLICATION_JSON).content(input))
                .andDo(print()).andExpect(status().isOk()).andExpect(content().string(key));
    }

    @Test
    public void newMetadataConflict() throws Exception {

        String input = mapper.writeValueAsString(md);

        when(dataConnection.addNewMetadata(any(Template.class))).thenThrow(new KeyExistsException());
        this.mockMvc.perform(post("/fileMetadata/").contentType(MediaType.APPLICATION_JSON).content(input))
                .andDo(print()).andExpect(status().isConflict());
    }

    @Test
    public void newMetadataValidationError() throws Exception {
        Template newMd = new Template();
        newMd.setFile(new com.captech.alfred.template.File());
        newMd.getFile().setSubjectArea(md.getFile().getSubjectArea());
        newMd.getFile().setKey(md.getFile().getKey());
        String input = mapper.writeValueAsString(newMd);

        this.mockMvc.perform(post("/fileMetadata/").contentType(MediaType.APPLICATION_JSON).content(input))
                .andDo(print()).andExpect(status().isBadRequest());
    }

    @Test
    public void updateMetadataBadRequest() throws Exception {
        String input = mapper.writeValueAsString(md);
        when(dataConnection.updateMetadata(any(String.class), any(Template.class))).thenReturn(null);
        when(dataConnection.getCurrentMetadata(any(String.class))).thenReturn(md);
        this.mockMvc.perform(put(testFilePath).contentType(MediaType.APPLICATION_JSON).content(input)).andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @TestConfiguration
    public static class MetadataControllerTestConfiguration {

        // manually insert mocked primary service
        @Bean("getConnector")
        @Primary
        public static DataStoreService someService() {
            return Mockito.mock(DataStoreService.class);
        }
    }

    @Test
    public void putTestNoBody() throws Exception {
        this.mockMvc.perform(put(testFilePath)).andExpect(status().is4xxClientError());
    }

    @Test
    public void putTestBodyNotJSON() throws Exception {
        String input = "This is not a valid json string";
        this.mockMvc.perform(put(testFilePath).contentType(MediaType.APPLICATION_JSON).content(input))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void deleteMetadataPass() throws Exception {
        when(dataConnection.deleteMetadata(any(String.class))).thenReturn(deletedTestFileLocation);
        this.mockMvc.perform(delete("/fileMetadata/" + key).contentType(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isOk());
    }


}

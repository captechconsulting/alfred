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
//package com.captech.alfred;
//
//import static org.mockito.Matchers.any;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.UUID;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.web.servlet.MockMvc;
//
//import com.captech.alfred.dataConnections.DataStoreService;
//import com.captech.alfred.exceptions.KeyExistsException;
//import com.captech.alfred.exceptions.NoDataFound;
//import com.captech.alfred.instance.Guid;
//import com.captech.alfred.instance.InstanceLog;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//@RunWith(SpringRunner.class)
//@WebMvcTest(InstanceController.class)
//public class InstanceControllerTest {
//
//    @MockBean(name = "datastoreService")
//    @Qualifier("getConnector")
//    DataStoreService datastoreService;
//
//    @Autowired
//    private MockMvc mockMvc;
//    private ObjectMapper mapper;
//
//    private String guid = "beeffeed-cafe-face-deed-123456789abc";
//
//    @Before
//    public void setup() {
//        mapper = new ObjectMapper();
//    }
//
//    @Test
//    public void getLogsPass() throws Exception {
//        List<InstanceLog> list = new ArrayList<InstanceLog>();
//        InstanceLog log = new InstanceLog();
//        log.setGuid(UUID.fromString(guid));
//        log.setStage("raw");
//        log.setTimestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss").format(new Date()));
//        list.add(log);
//        when(datastoreService.getInstanceLog(any(String.class))).thenReturn(list);
//
//        this.mockMvc.perform(get("/instanceLog/" + guid)).andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].guid").value(guid)).andReturn();
//    }
//
//    @Test
//    public void getLogsNoLogs() throws Exception {
//        when(datastoreService.getInstanceLog(any(String.class))).thenThrow(new NoDataFound());
//
//        this.mockMvc.perform(get("/instanceLog/" + guid)).andExpect(status().isNotFound());
//
//    }
//
//    @Test
//    public void addNewLogPass() throws Exception {
//
//        String input = mapper.writeValueAsString(new InstanceLog());
//
//        when(datastoreService.writeInstanceLog(any(InstanceLog.class), any(String.class)))
//                .thenReturn(new Guid(UUID.fromString(guid)));
//
//        this.mockMvc.perform(post("/instanceLog").contentType(MediaType.APPLICATION_JSON).content(input))
//                .andExpect(status().isOk()).andExpect(jsonPath("$.guid").value(guid));
//
//    }
//
//    @Test
//    public void addAnotherLogPass() throws Exception {
//
//        String input = mapper.writeValueAsString(new InstanceLog());
//
//        when(datastoreService.writeInstanceLog(any(InstanceLog.class), any(String.class)))
//                .thenReturn(new Guid(UUID.fromString(guid)));
//
//        this.mockMvc.perform(put("/instanceLog/" + guid).contentType(MediaType.APPLICATION_JSON).content(input))
//                .andExpect(status().isOk()).andExpect(jsonPath("$.guid").value(guid));
//    }
//
//    @Test
//    public void addAnotherLogAlreadyExists() throws Exception {
//
//        String input = mapper.writeValueAsString(new InstanceLog());
//
//        when(datastoreService.writeInstanceLog(any(InstanceLog.class), any(String.class)))
//                .thenThrow(new KeyExistsException());
//
//        this.mockMvc.perform(put("/instanceLog/" + guid).contentType(MediaType.APPLICATION_JSON).content(input))
//                .andExpect(status().isConflict());
//    }
//
//}

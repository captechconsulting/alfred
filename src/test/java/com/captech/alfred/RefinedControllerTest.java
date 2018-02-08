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
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//import com.captech.alfred.dataConnections.DataStoreService;
//import com.captech.alfred.template.refined.Refined;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest
//@EnableAutoConfiguration
//public class RefinedControllerTest {
//
//    private MockMvc mockMvc;
//
//    @MockBean(name = "datastoreService")
//    @Qualifier("getHadoopConnector")
//    DataStoreService datastoreService;
//
//    private ObjectMapper mapper;
//    private String testFile = "src/test/resources/consumptionByWeather";
//    private String key = "consumptionByWeather";
//    private Refined refined = new Refined();
//
//    @Before
//    public void setup() {
//        mockMvc = MockMvcBuilders.standaloneSetup(new RefinedController(datastoreService)).build();
//        mapper = new ObjectMapper();
//        FileReader fr = null;
//        File file = new File(testFile);
//        if (file.isFile() && file.canRead()) {
//            try {
//                fr = new FileReader(file);
//                refined = mapper.readValue(file, Refined.class);
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                try {
//                    if (fr != null) {
//                        fr.close();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//    }
//
//    @Test
//    public void getRefinedNoParameterFail() throws Exception {
//        this.mockMvc.perform(get("/refinedDatasets")).andDo(print()).andExpect(status().isBadRequest());
//    }
//    
//    @Test
//    public void newRefinedPass() throws Exception {
//        String input = mapper.writeValueAsString(refined);
//
//        when(datastoreService.addNewRefined(any(Refined.class))).thenReturn(key);
//        this.mockMvc.perform(post("/refinedDatasets/").contentType(MediaType.APPLICATION_JSON).content(input))
//                .andDo(print()).andExpect(status().isOk()).andExpect(content().string(key));
//    }
//
//}

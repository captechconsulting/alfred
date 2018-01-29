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

package com.captech.alfred.template;
//package com.captech.alfred.template;
//
//import static org.junit.Assert.assertEquals;
//
//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
//import java.lang.annotation.Annotation;
//import java.util.Set;
//
//import javax.annotation.Resource;
//import javax.validation.ConstraintViolation;
//import javax.validation.Validator;
//import javax.validation.constraints.NotNull;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import com.captech.alfred.dataConnections.DataStoreService;
//import com.captech.alfred.dataConnections.textFiles.TextFileProperties;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//@RunWith(SpringRunner.class)
//@EnableAutoConfiguration
//@SpringBootTest
//public class MetadataTest {
//
//    @MockBean(name = "datastoreService")
//    @Qualifier("getConnector")
//    DataStoreService datastoreService;
//    
//    @Autowired
//    TextFileProperties properties;
//
//    private ObjectMapper mapper;
//    private String testFile = "editorSample.json";
//    private Template md = new Template();
//
//    @Resource
//    @Qualifier("getValidator")
//    private Validator validator;
//
//    @Before
//    public void setup() {
//        mapper = new ObjectMapper();
//        FileReader fr = null;
//        File file = new File(properties.getMetadataLocation() + testFile);
//        if (file.isFile() && file.canRead()) {
//            try {
//                fr = new FileReader(file);
//                md = mapper.readValue(file, Template.class);
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
//    }
//
//    @Test
//    public void invalidCodeOfConduct() throws Exception {
//        Template newMd = new Template();
//        newMd.setFile(new com.captech.alfred.template.File());
//        newMd.getFile().setSubjectArea(md.getFile().getSubjectArea());
//        newMd.getFile().setKey(md.getFile().getKey());
//
//        Set<ConstraintViolation<Template>> constraintViolations = validator.validate(newMd);
//        ConstraintViolation<Template> violation = constraintViolations.iterator().next();
//        Annotation annotation = violation.getConstraintDescriptor().getAnnotation();
//        assertEquals(NotNull.class, annotation.annotationType());
//
//    }
//}

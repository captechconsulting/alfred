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

package com.captech.alfred.template.hierarchical;
//package com.captech.alfred.template.hierarchical;
//
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertTrue;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//
//import org.apache.commons.io.IOUtils;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import com.captech.alfred.template.Field;
//import com.captech.alfred.template.Template;
//
//@RunWith(SpringRunner.class)
//@EnableAutoConfiguration
//@SpringBootTest
//public class XmlSourceTemplateBuilderTest {
//
//    @Autowired @Qualifier("xmlSource")
//    SourceTemplateBuilder xmlSourceBuilder;
//
//    @Test
//    public void testPreFill() throws IOException {
//        File sampleXmlFile = new File("src/test/resources/hierarchical/sample-schema-test.xml");
//        System.out.println(sampleXmlFile.getAbsolutePath());
//        InputStream sampleXmlStream = new FileInputStream(sampleXmlFile);
//        byte[] sampleXsd = IOUtils.toByteArray(sampleXmlStream);
//        
//        
//        Template template = xmlSourceBuilder.preFill(sampleXmlFile.toString(), null);
//        assertNotNull(template);
//        assertTrue(template.getFields().size() >= 5);
//        for (Field field : template.getFields()) {
//            System.out.println(field.getSourceXpath());
//        }
//    }
//
//}

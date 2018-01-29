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

import com.captech.alfred.dataConnections.DataStoreService;
import com.captech.alfred.dataConnections.DataUserStoreService;
import com.captech.alfred.dataConnections.hadoop.HadoopDatastoreService;
import com.captech.alfred.dataConnections.hadoop.HadoopProperties;
import com.captech.alfred.dataConnections.hadoop.HadoopUsers;
import com.captech.alfred.template.TabularFile;
import com.captech.alfred.template.TechnicalFile;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.data.hadoop.store.output.TextFileWriter;


@Configuration@Profile("!local")
@ComponentScan(value = { "com.captech.alfred" })
@EnableConfigurationProperties(HadoopProperties.class)
public class AppConfig {

    @Autowired
    HadoopProperties properties;
    @Autowired
    private org.apache.hadoop.conf.Configuration hadoopConfiguration;

    @Bean
    @Primary
    DataStoreService getHadoopConnector() {
        return new HadoopDatastoreService();
    }

    @Bean
    @Primary
    DataUserStoreService getUserHadoopConnector() {
        return new HadoopUsers();
    }

    @Bean
    @Primary
    TechnicalFile getFileType() {
        return new TabularFile();
    }

    @Bean
    TextFileWriter sampleWriter() {
        return new TextFileWriter(hadoopConfiguration, new Path(properties.getFullSampleDir()), null);
    }

}

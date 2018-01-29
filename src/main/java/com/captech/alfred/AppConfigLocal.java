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
import com.captech.alfred.dataConnections.textFiles.TextFileDatastoreService;
import com.captech.alfred.dataConnections.textFiles.TextUsers;
import com.captech.alfred.template.TabularFile;
import com.captech.alfred.template.TechnicalFile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.hadoop.store.output.TextFileWriter;

@Configuration
@ComponentScan(value = { "com.captech.alfred" })
//@Profile("local")
public class AppConfigLocal {

    @Bean@Primary
    DataStoreService getConnector() {
        return new TextFileDatastoreService();
    }

    @Bean@Primary
    DataUserStoreService getUserConnector() {
        return new TextUsers();
    }

    @Bean
    @Primary
    TechnicalFile getFileType() {
        return new TabularFile();
    }

    @Bean
    TextFileWriter sampleWriter() {
        return null;
    }

}

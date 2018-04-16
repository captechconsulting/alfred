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

import com.captech.alfred.authentication.EncryptionGenerator;
import com.captech.alfred.authentication.Role;
import com.captech.alfred.authentication.User;
import com.captech.alfred.dataConnections.DataStoreService;
import com.captech.alfred.dataConnections.DataUserStoreService;
import com.captech.alfred.dataConnections.UsersProperties;
import com.captech.alfred.dataConnections.hadoop.HadoopDatastoreService;
import com.captech.alfred.dataConnections.hadoop.HadoopProperties;
import com.captech.alfred.dataConnections.hadoop.HadoopUsers;
import com.captech.alfred.dataConnections.textFiles.TextFileDatastoreService;
import com.captech.alfred.dataConnections.textFiles.TextFileProperties;
import com.captech.alfred.dataConnections.textFiles.TextUsers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.hadoop.fs.FsShell;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

@Component
@EnableAutoConfiguration
@EnableConfigurationProperties(HadoopProperties.class)
public class Startup implements CommandLineRunner {

    @Autowired
    JdbcTemplate hive;

    @Autowired
    private FsShell shell;

    @Autowired
    HadoopProperties properties;

    @Autowired
    TextFileProperties textProperties;

    @Autowired
    UsersProperties userProperties;

    @Autowired
    UsersProperties authProperties;

    @Autowired
    DataStoreService dataConnection;

    @Autowired
    DataUserStoreService dataUserConn;

    @Value("${auth.firstuser.username:onetimeuser}")
    private String username;

    @Value("${auth.firstuser.password:auth}")
    private String password;

    String createDB;

    String currentDdl;

    String versionDdl;

    String instanceDdl;
    String currentRefinedDdl;

    String versionRefinedDdl;

    @Override
    public void run(String... strings) throws Exception {

        if (dataConnection instanceof TextFileDatastoreService) {
            Files.createDirectories(Paths.get(textProperties.getFullCurrentMetadataPath()));
            Files.createDirectories(Paths.get(textProperties.getFullVersionedMetadataPath()));
            Files.createDirectories(Paths.get(textProperties.getFullInstancePath()));
            Files.createDirectories(Paths.get(textProperties.getFullDraftLocation()));
            Files.createDirectories(Paths.get(textProperties.getFullDraftRefined()));
            Files.createDirectories(Paths.get(textProperties.getFullVersionedRefined()));
            Files.createDirectories(Paths.get(textProperties.getFullCurrentRefined()));
            Files.createDirectories(Paths.get(textProperties.getFullCurrentSandbox()));
            Files.createDirectories(Paths.get(textProperties.getFullVersionedSandbox()));
            Files.createDirectories(Paths.get(textProperties.getLandingZone()));
        }
        if (dataUserConn instanceof TextUsers) {
            Files.createDirectories(Paths.get(userProperties.getTextfullAuthPath()));
            Files.createDirectories(Paths.get(userProperties.getTextFullOldAuthPath()));
            Files.createDirectories(Paths.get(userProperties.getFullTextAuthoritiesPath()));
            if (dataUserConn.listUsers().isEmpty()) {
                User user = new User();
                user.addRole(new Role("ADMIN"));
                user.setPassword(EncryptionGenerator.generatePassword("admin"));
                user.setUsername("admin");
                ArrayList<String> permissions = new ArrayList<>();
                permissions.addAll(Arrays.asList("VIEW", "SEARCH", "ADD", "EDIT", "EDIT_FINAL", "DELETE"));
                user.setPermissions(permissions);
                dataUserConn.writeNewUser(user);
            }
        }

        createDB = "create database if not exists " + properties.getDatabaseName();

        currentDdl = "create external table if not exists " + properties.getDatabaseName() + ".template"
                + "( template_json string ) ROW FORMAT delimited " + "fields terminated by '\\;'"
                + "stored as textfile " + "LOCATION '" + properties.getFullCurrentMetadataPath() + "'";

        versionDdl = "create external table if not exists " + properties.getDatabaseName() + ".template_versions"
                + "( template_json string ) ROW FORMAT delimited " + "fields terminated by '\\;'"
                + "stored as textfile " + "LOCATION '" + properties.getFullVersionedMetadataPath() + "'";

        instanceDdl = "create external table if not exists " + properties.getDatabaseName() + ".instance"
                + "( instance_json string ) ROW FORMAT delimited " + "fields terminated by '\\;' stored as textfile "
                + "LOCATION '" + properties.getFullInstancePath() + "'";

        currentRefinedDdl = "create external table if not exists " + properties.getDatabaseName() + ".refined"
                + "( template_json string ) ROW FORMAT delimited " + "fields terminated by '\\;'"
                + "stored as textfile " + "LOCATION '" + properties.getFullCurrentRefined() + "'";

        versionRefinedDdl = "create external table if not exists " + properties.getDatabaseName() + ".refined_versions"
                + "( template_json string ) ROW FORMAT delimited " + "fields terminated by '\\;'"
                + "stored as textfile " + "LOCATION '" + properties.getFullVersionedRefined() + "'";

        if (dataConnection instanceof HadoopDatastoreService) {
            if (!shell.test(true, false, true, properties.getFullCurrentMetadataPath())) {
                shell.mkdir(properties.getFullCurrentMetadataPath());
            }
            if (!shell.test(true, false, true, properties.getFullVersionedMetadataPath())) {
                shell.mkdir(properties.getFullVersionedMetadataPath());
            }
            if (!shell.test(true, false, true, properties.getFullInstancePath())) {
                shell.mkdir(properties.getFullInstancePath());
            }
            if (!shell.test(true, false, true, properties.getFullDraftLocation())) {
                shell.mkdir(properties.getFullDraftLocation());
            }
            if (!shell.test(true, false, true, properties.getFullDraftRefined())) {
                shell.mkdir(properties.getFullDraftRefined());
            }
            if (!shell.test(true, false, true, properties.getFullVersionedRefined())) {
                shell.mkdir(properties.getFullVersionedRefined());
            }
            if (!shell.test(true, false, true, properties.getFullCurrentRefined())) {
                shell.mkdir(properties.getFullCurrentRefined());
            }
            if (!shell.test(true, false, true, properties.getFullCurrentSandbox())) {
                shell.mkdir(properties.getFullCurrentSandbox());
            }
            if (!shell.test(true, false, true, properties.getFullVersionedSandbox())) {
                shell.mkdir(properties.getFullVersionedSandbox());
            }
            if(!shell.test(true, false, true, properties.getFullSampleDir())){
                shell.mkdir(properties.getFullSampleDir());
            }
            hive.execute(createDB);
            hive.execute(currentDdl);
            hive.execute(versionDdl);
            hive.execute(instanceDdl);
            hive.execute(currentRefinedDdl);
            hive.execute(versionRefinedDdl);
        }
        if (dataUserConn instanceof HadoopUsers) {
            if (!shell.test(true, false, true, authProperties.getfullAuthPath())) {
                shell.mkdir(authProperties.getfullAuthPath());
                if (dataUserConn.listUsers().isEmpty()) {
                    User user = new User();
                    user.addRole(new Role("AUTH"));
                    user.addPermission("VIEW");
                    user.setPassword(EncryptionGenerator.generatePassword(password));
                    user.setUsername(username);
                    user.setPasswordExpiry(new Date());
                    dataUserConn.writeNewUser(user);
                }
            }
            if (!shell.test(true, false, true, authProperties.getfullOldAuthPath())) {
                shell.mkdir(authProperties.getfullOldAuthPath());
            }
            if (!shell.test(true, false, true, authProperties.getFullAuthoritiesPath())) {
                shell.mkdir(authProperties.getFullAuthoritiesPath());
            }
        }
    }
}

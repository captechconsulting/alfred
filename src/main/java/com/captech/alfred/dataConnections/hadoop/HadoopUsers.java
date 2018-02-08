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
import com.captech.alfred.authentication.Role;
import com.captech.alfred.authentication.User;
import com.captech.alfred.dataConnections.DataUserStoreService;
import com.captech.alfred.dataConnections.UsersProperties;
import com.captech.alfred.exceptions.AppInternalError;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
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
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@EnableAutoConfiguration
@EnableConfigurationProperties(UsersProperties.class)
public class HadoopUsers extends DataUserStoreService {

    private static final Logger logger = LoggerFactory.getLogger(HadoopUsers.class);
    @Autowired
    UsersProperties properties;
    @Autowired
    TextFileWriter authWriter;
    @Autowired
    TextFileWriter removedAuthWriter;
    @Autowired
    TextFileWriter authoritiesWriter;
    @Autowired
    private FsShell shell;

    @Override
    public User findByUsername(String username) {
        for (FileStatus s : shell.ls(Paths.get(properties.getAuthPath()).toString())) {
            if (s.isFile()) {
                String filename = s.getPath().getName();
                if (filename.startsWith(username)) {
                    Collection<String> fileCollection = shell.text(Paths.get(properties.getAuthPath(), filename).toString());
                    if (!fileCollection.isEmpty()) {

                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            return mapper.readValue(fileCollection.iterator().next(), User.class);
                        } catch (IOException e) {
                            logger.error("Unable to read data - " + e.getMessage());
                            logger.error(e.toString());
                            throw new AppInternalError("Unable to read data - " + e.getMessage());
                        }
                    }
                }
            }
        }
        return null;
    }

    public boolean verifyExistingUser(String username) {
        for (FileStatus s : shell.ls(Paths.get(properties.getAuthPath()).toString())) {
            if (s.isFile()) {
                String filename = s.getPath().getName();
                if (filename.startsWith(username)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String deleteUser(String username) {
        String movedFiles = "";
        if (!verifyExistingUser(username)) {
            return null;
        }
        for (FileStatus s : shell.ls(Paths.get(properties.getAuthPath()).toString())) {
            if (s.isFile()) {
                String filename = s.getPath().getName();
                if (filename.startsWith(username)) {
                    shell.mv(Paths.get(properties.getAuthPath(), filename).toString(),
                            Paths.get(properties.getOldAuthPath(), filename + "_"
                                    + new SimpleDateFormat(Constants.HADOOP_VERS_FORMAT).format(new Date())).toString());
                    if (!movedFiles.isEmpty()) {
                        movedFiles = movedFiles + ",";
                    } else {
                        movedFiles = movedFiles + filename;
                    }
                }
            }
        }
        return movedFiles;

    }

    @Override
    public void writeNewUser(User user) {


        authWriter.setFileNamingStrategy(new StaticFileNamingStrategy(generateFilename(user)));
        ObjectMapper mapper = new ObjectMapper();
        try {
            String dataAsString = mapper.writeValueAsString(user);
            authWriter.write(dataAsString);
        } catch (IOException e) {
            logger.error("unable to write data to Hadoop: " + e.getMessage());
            logger.error(e.toString());
            throw new AppInternalError("unable to write data to Hadoop: " + e.getMessage());
        } finally {
            try {
                authWriter.flush();
                authWriter.close();
            } catch (IOException e) {
                logger.error("unable to write data to Hadoop: " + e.getMessage());
                logger.error(e.toString());
            }
        }

    }

    @Override
    public void updateUser(User user) {
        if (verifyExistingUser(user.getUsername())) {
            deleteUser(user.getUsername());
        }
        writeNewUser(user);

    }

    @Override
    public List<Role> getRoles() {
        List<Role> roles = new ArrayList<>();
        for (FileStatus s : shell.ls(Paths.get(properties.getAuthoritiesPath()).toString())) {
            if (s.isFile() && !s.getPath().getName().startsWith("ALL_PERMS")) {
                Collection<String> fileCollection = shell.text(Paths.get(properties.getAuthPath(), s.getPath().getName()).toString());
                if (!fileCollection.isEmpty()) {
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        roles.add(mapper.readValue(fileCollection.iterator().next(), Role.class));
                    } catch (IOException e) {
                        logger.error("Unable to read data - " + e.getMessage());
                        logger.error(e.toString());
                        throw new AppInternalError("Unable to read data - " + e.getMessage());
                    }
                }
            }
        }
        return roles;
    }

    @Override
    public List<String> getPermissions() {
        List<String> permissions = new ArrayList<>();
        // permissions are stored as a comma delimited file called ALL_PERMS
        for (FileStatus s : shell.ls(Paths.get(properties.getAuthoritiesPath(), "ALL_PERMS").toString())) {
            if (s.isFile()) {
                Collection<String> fileCollection = shell.text(Paths.get(properties.getAuthoritiesPath(), s.getPath().getName()).toString());
                if (!fileCollection.isEmpty()) {
                    String text = fileCollection.iterator().next();
                    if (!StringUtils.isEmpty(text)) {
                        text = text.replaceAll("\n", "");
                        String[] values = text.split(",");
                        if (values.length > 0) {
                            permissions.addAll(Arrays.asList(values));
                        }
                    }
                }
            }
        }

        return permissions;
    }

    @Override
    public void addPermission(String permission) {
        authoritiesWriter.setFileNamingStrategy(new StaticFileNamingStrategy("ALL_PERMS"));
        authoritiesWriter.setAppendable(true);
        try {
            authoritiesWriter.write("," + StringUtils.upperCase(permission));
        } catch (IOException e) {
            logger.error("unable to write data to Hadoop: " + e.getMessage());
            logger.error(e.toString());
            throw new AppInternalError("unable to write data to Hadoop: " + e.getMessage());
        } finally {
            try {
                authoritiesWriter.flush();
                authoritiesWriter.close();
            } catch (IOException e) {
                logger.error("unable to write data to Hadoop: " + e.getMessage());
                logger.error(e.toString());
            }
        }
    }

    @Override
    public void addRole(Role role) {
        authoritiesWriter.setFileNamingStrategy(new StaticFileNamingStrategy("ROLE_" + role.getName()));
        ObjectMapper mapper = new ObjectMapper();
        try {
            String dataAsString = mapper.writeValueAsString(role);
            authoritiesWriter.write(dataAsString);
        } catch (IOException e) {
            logger.error("unable to write data to Hadoop: " + e.getMessage());
            logger.error(e.toString());
            throw new AppInternalError("unable to write data to Hadoop: " + e.getMessage());
        } finally {
            try {
                authoritiesWriter.flush();
                authoritiesWriter.close();
            } catch (IOException e) {
                logger.error("unable to write data to Hadoop: " + e.getMessage());
                logger.error(e.toString());
            }
        }

    }

    @Override
    public void editRole(Role role) {
        authoritiesWriter.setOverwrite(true);
        addRole(role);
        authoritiesWriter.setOverwrite(false);
    }

    public Role getRole(String name) {
        for (FileStatus s : shell.ls(Paths.get(properties.getAuthoritiesPath(), name).toString())) {
            if (s.isFile()) {
                Collection<String> fileCollection = shell.text(Paths.get(properties.getAuthoritiesPath(), s.getPath().getName()).toString());
                if (!fileCollection.isEmpty()) {
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        return (mapper.readValue(fileCollection.iterator().next(), Role.class));
                    } catch (IOException e) {
                        logger.error("Unable to read data - " + e.getMessage());
                        logger.error(e.toString());
                        throw new AppInternalError("Unable to read data - " + e.getMessage());
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Set<String> listUsers() {
        Set<String> users = new HashSet<>();
        for (FileStatus s : shell.ls(Paths.get(properties.getAuthPath()).toString())) {
            if (s.isFile()) {
                String username = s.getPath().getName().split("_roles_")[0];
                users.add(username);
            }
        }
        return users;
    }

    @Configuration
    @EnableConfigurationProperties(HadoopProperties.class)
    static class Config {

        @Autowired
        UsersProperties properties;
        @Autowired
        private org.apache.hadoop.conf.Configuration hadoopConfiguration;

        @Bean
        TextFileWriter authWriter() {
            TextFileWriter writer = new TextFileWriter(hadoopConfiguration, new Path(Paths.get(properties.getAuthPath()).toString()), null);
            return writer;
        }

        @Bean
        TextFileWriter removedAuthWriter() {
            TextFileWriter writer = new TextFileWriter(hadoopConfiguration, new Path(Paths.get(properties.getOldAuthPath()).toString()),
                    null);
            return writer;
        }

        @Bean
        TextFileWriter authoritiesWriter() {
            TextFileWriter writer = new TextFileWriter(hadoopConfiguration, new Path(Paths.get(properties.getAuthoritiesPath()).toString()),
                    null);
            return writer;
        }
    }
}

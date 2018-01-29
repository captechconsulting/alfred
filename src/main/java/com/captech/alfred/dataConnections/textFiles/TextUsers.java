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

package com.captech.alfred.dataConnections.textFiles;

import com.captech.alfred.Constants;
import com.captech.alfred.authentication.Role;
import com.captech.alfred.authentication.User;
import com.captech.alfred.dataConnections.DataUserStoreService;
import com.captech.alfred.dataConnections.UsersProperties;
import com.captech.alfred.exceptions.AppInternalError;
import com.captech.alfred.exceptions.Forbidden;
import com.captech.alfred.exceptions.KeyExistsException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TextUsers extends DataUserStoreService {

    private static final Logger logger = LoggerFactory.getLogger(TextUsers.class);

    @Autowired
    UsersProperties properties;

    @Override
    public void writeNewUser(User user) {
        File file = new File(Paths.get(properties.getTextfullAuthPath(), generateFilename(user)).toString());
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            if (!file.createNewFile()) {
                throw new KeyExistsException();
            }
            fw = new FileWriter(file, false);
            bw = new BufferedWriter(fw);
            ObjectMapper om = new ObjectMapper();
            om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            om.writeValue(bw, user);
        } catch (IOException e) {
            throw new Forbidden("Text File User: " + e.getMessage());
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException e) {
                logger.error("cannot close file writer: " + e.getMessage());
            }
        }
    }

    @Override
    public User findByUsername(final String username) {
        File dir = new File(Paths.get(properties.getTextfullAuthPath()).toString());
        FilenameFilter matchesUser = new FilenameFilter() {
            @Override
            public boolean accept(File directory, String filename) {
                return StringUtils.equals(filename.split("_roles_")[0], username);
            }
        };

        File[] files = dir.listFiles(matchesUser);

        if (files.length < 1) {
            return null;
        }
        File file = files[0];
        ObjectMapper mapper = new ObjectMapper();
        User user = null;
        try {
            user = mapper.readValue(file, User.class);
        } catch (IOException e) {
            logger.error("IOEXception: ", e);
            throw new AppInternalError("Unable to read data - " + e.getMessage());
        }
        return user;
    }

    @Override
    public void updateUser(User user) {
        deleteUser(user.getUsername());
        writeNewUser(user);

    }

    @Override
    public String deleteUser(final String username) {
        File dir = new File(Paths.get(properties.getTextfullAuthPath()).toString());
        FilenameFilter matchesUser = new FilenameFilter() {
            @Override
            public boolean accept(File directory, String filename) {
                return StringUtils.equals(filename.split("_roles_")[0], username);
            }
        };

        File[] files = dir.listFiles(matchesUser);

        if (files.length < 1) {
            return null;
        }
        String movedFiles = "";
        for (File file : files) {
            if (!file.isFile() || !file.canRead()) {
                logger.debug("file not found.");
                return null;
            }
            String versionLocation = Paths.get(properties.getTextFullOldAuthPath(), username + "_"
                    + new SimpleDateFormat(Constants.VERSION_FORMAT).format(new Date())).toString();
            file.renameTo(new File(versionLocation));
            if (!movedFiles.isEmpty()) {
                movedFiles = movedFiles + ",";
            }
            movedFiles = movedFiles + versionLocation;
        }
        return movedFiles;
    }

    @Override
    public List<Role> getRoles() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getPermissions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addRole(Role role) {
        // TODO Auto-generated method stub

    }

    @Override
    public void editRole(Role role) {
        // TODO Auto-generated method stub

    }

    @Override
    public Set<String> listUsers() {
        HashSet<String> users = new HashSet<>();
        File dir = new File(Paths.get(properties.getTextfullAuthPath()).toString());
        File[] files = dir.listFiles();
        for (File file : files) {
            users.add(file.getName().split("_roles_")[0]);
        }
        return users;
    }

    @Override
    public void addPermission(String permission) {
        // TODO Auto-generated method stub

    }

}

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

package com.captech.alfred.dataConnections;

import com.captech.alfred.authentication.Role;
import com.captech.alfred.authentication.User;

import java.util.List;
import java.util.Set;

public abstract class DataUserStoreService {

    public abstract String deleteUser(String username);

    public abstract void writeNewUser(User user);

    public abstract User findByUsername(String username);

    public abstract void updateUser(User user);

    public abstract Set<String> listUsers();

    public abstract List<Role> getRoles();

    public abstract List<String> getPermissions();

    public abstract void addRole(Role role);

    public abstract void editRole(Role rolename);

    protected String generateFilename(User user) {
        String file = user.getUsername() + "_roles_";
        for (Role role : user.getRoles()) {
            file = file + "." + role.getName();
        }
        return file;
    }

    public abstract void addPermission(String permission);

}

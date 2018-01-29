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

package com.captech.alfred.authentication;

import java.util.Collection;
import java.util.HashSet;

public class Role {
    private String name;
    private Collection<String> permissions;

    public Role() {
        super();
    }

    public Role(String name) {
        setName(name);
    }

    public Role(String name, Collection<String> permissions) {
        setName(name);
        setPermissions(permissions);
    }

    public Role(String name, String permissions) {
        setName(name);
        addPermission(permissions);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<String> getPermissions() {
        if (permissions == null) {
            return new HashSet<>();
        }
        return permissions;
    }

    public void setPermissions(Collection<String> permissions) {
        this.permissions = permissions;
    }

    public void addPermission(String permission) {
        getPermissions().add(permission);
    }
}

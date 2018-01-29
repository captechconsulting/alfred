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

import com.captech.alfred.Constants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class User {
    private String password;
    private String username;
    private Collection<Role> roles;
    private Collection<String> permissions;
    private String passwordExpiry;

    public User() {

    }

    public User(String name, String password, Collection<Role> roles, Collection<String> permissions) {
        super();
        this.username = name;
        this.password = password;
        this.roles = roles;
        this.permissions = permissions;
    }

    public User(String name, String password) {
        this(name, password, new ArrayList<>(Arrays.asList(new Role("USER"))), null);
    }

    public User(String name, String password, Role role) {
        this(name, password, new ArrayList<>(Arrays.asList(role)), null);
    }

    //cannot do name, password, collection permissions - conflicts with this guy.
    //Permissions should be sent as name, password, null, permissions
    public User(String name, String password, Collection<Role> roles) {
        this(name, password, roles, null);
    }

    public User(String name, String password, String permission) {
        this(name, password, new ArrayList<>(Arrays.asList(new Role("USER"))),
                new ArrayList<>(Arrays.asList(permission)));
    }

    public User(String name, String password, Role role, String permission) {
        this(name, password, new ArrayList<>(Arrays.asList(role)), new ArrayList<String>(Arrays.asList(permission)));
    }

    public User(String name, String password, Role role, Collection<String> permissions) {
        this(name, password, new ArrayList<>(Arrays.asList(role)), permissions);
    }

    public static ArrayList<String> getAllowedAccessGroups(Collection<? extends GrantedAuthority> collection) {
        ArrayList<String> acccessGroups = new ArrayList<>();
        for (GrantedAuthority oneAuth : collection) {
            if (oneAuth.getAuthority().toLowerCase().contains("access_group")) {
                acccessGroups.add(oneAuth.getAuthority().replace("PERM_", ""));
            }
        }
        return acccessGroups;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Collection<Role> getRoles() {
        if (roles == null) {
            roles = new ArrayList<>();
        }
        return roles;
    }

    public void setRole(Collection<Role> roles) {
        this.roles = roles;
    }

    public void addRole(Role role) {
        getRoles().add(role);
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

    public String getPasswordExpiry() {
        return passwordExpiry;
    }

    public void setPasswordExpiry(String passwordExpiry) {
        this.passwordExpiry = passwordExpiry;
    }

    public void setPasswordExpiry(Date passwordExpiry) {
        this.passwordExpiry = new SimpleDateFormat(Constants.VERSION_FORMAT).format(passwordExpiry);
    }

    @JsonIgnore
    public Date getPasswordExpiryAsDate() {
        try {
            return new SimpleDateFormat(Constants.VERSION_FORMAT).parse(passwordExpiry);
        } catch (ParseException e) {
            return null;
        }
    }


}

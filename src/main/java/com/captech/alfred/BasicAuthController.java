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
import com.captech.alfred.authentication.Password;
import com.captech.alfred.authentication.Role;
import com.captech.alfred.authentication.User;
import com.captech.alfred.dataConnections.DataUserStoreService;
import com.captech.alfred.exceptions.KeyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

@RestController
@EnableAutoConfiguration
@RequestMapping(value = "/authentication")
public class BasicAuthController {

    @Autowired
    DataUserStoreService dataConnection;

    @Value("${auth.passwordExpiryDays:90}")
    private int pwdExpiry;

    @PostMapping("user")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTH')or hasAnyAuthority('PERM_CREATE_USER')")
    public void createUser(@RequestBody User user) {
        if (getSpecificUserAuth(user.getUsername()) != null) {
            throw new KeyExistsException();
        }
        user.setPasswordExpiry(getPwdExpiry(pwdExpiry));
        user.setPassword(EncryptionGenerator.generatePassword(user.getPassword()));
        dataConnection.writeNewUser(user);
    }

    @GetMapping("user/allusers")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTH')or hasAnyAuthority('PERM_VIEW_USERS')")
    public Set<String> getAllUsers() {
        return dataConnection.listUsers();
    }

    @GetMapping("user")
    public User getCurrentUserAuth(Authentication auth) {
        return getSpecificUserAuth(auth.getName());
    }

    @GetMapping("user/{user}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTH')or hasAnyAuthority('PERM_VIEW_USERS')")
    public User getUserAuth(@PathVariable String user) {

        return getSpecificUserAuth(user);
    }

    // created to allow authorization on getUserAuth that isn't the same as
    // getCurrentUserAuth
    private User getSpecificUserAuth(String user) {
        User foundUser = dataConnection.findByUsername(user);
        if (foundUser != null) {
            foundUser.setPassword("");
        }
        return foundUser;
    }

    @PutMapping("user/password")
    public User updatePassword(@RequestBody Password password, Authentication auth) {
        User user = getSpecificUserAuth(auth.getName());
        if (password == null || StringUtils.isEmpty(password.getNewPassword())) {
            password = new Password();
            password.setNewPassword("");
        }
        user.setPassword(EncryptionGenerator.generatePassword(password.getNewPassword()));
        user.setPasswordExpiry(getPwdExpiry(pwdExpiry));
        dataConnection.updateUser(user);
        user.setPassword("");
        return user;
    }

    @PutMapping("user/{username}/password")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTH')or hasAnyAuthority('PERM_PASSWORD')")
    public User updatePassword(@RequestBody Password password, @PathVariable String username, Authentication auth) {
        User user = getSpecificUserAuth(username);
        if (password == null || StringUtils.isEmpty(password.getNewPassword())) {
            password = new Password();
            password.setNewPassword("");
        }
        user.setPassword(EncryptionGenerator.generatePassword(password.getNewPassword()));
        user.setPasswordExpiry(getPwdExpiry(0));
        dataConnection.updateUser(user);
        user.setPassword("");
        return user;
    }

    @PutMapping("user/{username}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTH')or hasAnyAuthority('PERM_CREATE_USER')")
    public void updateUserPermissions(@RequestBody User user, @PathVariable String username, Authentication auth) {

        if (user.getPassword() == null) {
            User oldUser = dataConnection.findByUsername(username);
            //already encrypted, do not re-encrypt
            user.setPassword(oldUser.getPassword());
        }
        dataConnection.updateUser(user);
    }

    @DeleteMapping("user/{user}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTH')or hasAnyAuthority('PERM_DELETE_USERS')")
    public ResponseEntity<String> deleteUser(@PathVariable String user, Authentication auth) {
        if (auth.getName().equals(user)) {
            return ResponseEntity.badRequest().body("You cannot delete your own user account");
        }
        dataConnection.deleteUser(user);
        return ResponseEntity.ok(null);
    }

    @GetMapping("roles")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTH')or hasAnyAuthority('PERM_CREATE_USER','PERM_VIEW_ROLES')")
    public List<Role> getAllRoles() {
        return dataConnection.getRoles();

    }

    @PostMapping("roles")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTH')or hasAnyAuthority('PERM_CREATE_USER','PERM_ADD_ROLES')")
    public void addNewRole(@RequestBody Role role) {
        dataConnection.addRole(role);
    }

    @PutMapping("roles/{rolename}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTH')or hasAnyAuthority('PERM_CREATE_USER','PERM_ADD_ROLES')")
    public void updateRole(@PathVariable String rolename, @RequestBody Role role) {
        dataConnection.editRole(role);
    }

    @GetMapping("permissions")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTH')or hasAnyAuthority('PERM_CREATE_USER','PERM_VIEW_PERMS')")
    public List<String> getAllPermissions() {
        return dataConnection.getPermissions();
    }

    @PostMapping("permissions")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTH')or hasAnyAuthority('PERM_CREATE_USER','PERM_ADD_PERMS')")
    public void addNewPermission(@PathVariable String newPermission) {
        dataConnection.addPermission(newPermission);
    }

    private Date getPwdExpiry(int passwordExpiryDays) {
        if (passwordExpiryDays == -1) {
            return null;
        }
        Date date = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, passwordExpiryDays);
        date = c.getTime();
        return date;
    }
}

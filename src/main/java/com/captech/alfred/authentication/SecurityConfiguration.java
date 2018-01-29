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

import com.captech.alfred.dataConnections.DataUserStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Configuration
class SecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

    @Autowired
    DataUserStoreService userStore;

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService()).passwordEncoder(encoder());
    }

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                User user = userStore.findByUsername(username);
                if (user == null || user.getUsername() == null) {
                    throw new UsernameNotFoundException("could not find the user '" + username + "'");
                }
                Set<String> rolesAndPermissions = new HashSet<>();
                for (Role role : user.getRoles()) {
                    rolesAndPermissions.add("ROLE_" + role.getName());
                    for (String perm : role.getPermissions()) {
                        rolesAndPermissions.add("PERM_" + perm);
                    }
                }
                for (String perm : user.getPermissions()) {
                    rolesAndPermissions.add("PERM_" + perm);
                }
                boolean expired = false;
                if (user.getPasswordExpiry() != null && (new Date()).compareTo(user.getPasswordExpiryAsDate()) >= 0) {
                    expired = true;
                }
                /**
                 * public User(String username, String password, boolean
                 * enabled, boolean accountNonExpired, boolean
                 * credentialsNonExpired, boolean accountNonLocked, Collection<?
                 * extends GrantedAuthority> authorities)
                 */

                return new ExpiringUser(user.getUsername(), user.getPassword(), AuthorityUtils.createAuthorityList(
                        rolesAndPermissions.toArray(new String[rolesAndPermissions.size()])), expired);
            }
        };
    }
}

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override

    protected void configure(HttpSecurity http) throws Exception {
        http.addFilterAfter(new ChangePasswordFilter(),
                SwitchUserFilter.class)
                .authorizeRequests().antMatchers("/", "/js/**", "/css/**", "/webjars/**").permitAll()
                .anyRequest().fullyAuthenticated().and().httpBasic().realmName("Alfred").and()
                .csrf().disable().logout().logoutSuccessUrl("/").and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}

@Configuration
@Order(1)
class WebSecurityConfig1 extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/authentication/user/password").authorizeRequests()
                .antMatchers("/authentication/user/password").fullyAuthenticated().and()
                .httpBasic().realmName("Alfred").and().csrf().disable().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

    }
}

@Configuration
@Order(2)
class WebSecurityConfig2 extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.requestMatcher(new
                AntPathRequestMatcher("/authentication/user", "GET")).authorizeRequests()
                .antMatchers("/authentication/user").fullyAuthenticated().and()
                .httpBasic().realmName("Alfred").and().csrf().disable().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

    }
}
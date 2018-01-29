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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ChangePasswordFilter extends OncePerRequestFilter implements Filter, InitializingBean {
    private static final Logger pwFilterLogger = LoggerFactory.getLogger(ChangePasswordFilter.class);
    protected final String ERRORS_KEY = "errors";
    protected String changePasswordKey = "user.must.change.password";
    private String changePasswordUrl = null;

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws ServletException {
        Assert.notNull(changePasswordUrl, "changePasswordUrl must be set.");
        Assert.notNull(changePasswordKey, "changePasswordKey must be set.");
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
     * javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        ExpiringUser user = null;

        pwFilterLogger.debug("changepasswordfilter URL: " + request.getRequestURL());
        pwFilterLogger.debug("changepasswordfilter URI: " + request.getRequestURI());
        Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (obj instanceof ExpiringUser) {
            user = (ExpiringUser) obj;
        }

        if (user != null && user.isPasswordExpired()) {
            // send user to change password page
            pwFilterLogger.error(user.getUsername() + " credentials expired - sending to changepassword page.");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "CredentialsExpired.");
            response.setContentType("application/json");
            return;
        }

        chain.doFilter(request, response);
    }
}
/*
 * Copyright 2014 Amazon Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *    http://aws.amazon.com/apache2.0
 *
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and
 * limitations under the License.
 */
package com.amediamanager.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.amediamanager.domain.User;

@ControllerAdvice
public class AllControllers {
    @ModelAttribute("user")
    public User populateUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) { return null; }
        Authentication auth = context.getAuthentication();
        if (auth == null) { return null; }
        Object user = auth.getDetails();

        return (user != null && user instanceof User) ? (User) user : null;
    }
}

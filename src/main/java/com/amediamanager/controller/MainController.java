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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.amediamanager.dao.TagDao.TagCount;
import com.amediamanager.domain.NewUser;
import com.amediamanager.domain.Video;
import com.amediamanager.service.TagsService;
import com.amediamanager.service.UserService;
import com.amediamanager.service.VideoService;

@Controller
public class MainController {
	private static final Logger LOG = LoggerFactory.getLogger(MainController.class);
	
	@Autowired
	UserService userService;

	@Autowired
	VideoService videoService;
	
	@Autowired
	TagsService tagService;

	@RequestMapping(value = { "/", "/home", "/welcome" }, method = RequestMethod.GET)
	public String home(ModelMap model, HttpSession session) {
		Authentication auth = SecurityContextHolder.getContext()
				.getAuthentication();

		// If the user is not authenticated, show a different view
		if (auth instanceof AnonymousAuthenticationToken) {
            model.addAttribute("newUser", new NewUser());
			model.addAttribute("templateName", "welcome");
		} else {
			List<Video> videos = new ArrayList<Video>();
			List<TagCount> tags = new ArrayList<TagCount>();
			try {
				// Get user's videos and tags
				videos = videoService.findByUserId(auth.getName());
				tags = tagService.getTagsForUser(auth.getName());

				// Add expiring URLs (1 hour)
				videos = videoService.generateExpiringUrls(videos, 1000*60*60);
			} catch (Exception e) {
				LOG.error("Error loading videos: {}", e);
				return "redirect:/config";
			}
			model.addAttribute("tags", tags);
			model.addAttribute("videos", videos);
			model.addAttribute("templateName", "only_videos");
		}
		return "base";
	}


	@RequestMapping(value = "/error", method = RequestMethod.GET)
	public String error(ModelMap model) {
		return "base";
	}

	@RequestMapping(value = "/empty", method = RequestMethod.GET)
	public String empty(ModelMap model) {
		return "base";
	}

	@RequestMapping(value = "/not-found", method = RequestMethod.GET)
	public String notFound(ModelMap model) {
		model.addAttribute("error", "Resource not found");
		return "base";
	}

	@RequestMapping(value = "/login-failed", method = RequestMethod.GET)
	public String loginerror(ModelMap model, HttpSession session) {
		model.addAttribute("error", "Login failed.");
		return home(model, session);
	}

	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public String logout(ModelMap model, HttpSession session) {
		return home(model, session);
	}
}

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder;
import com.amazonaws.services.elastictranscoder.model.CreateJobOutput;
import com.amazonaws.services.elastictranscoder.model.CreateJobRequest;
import com.amazonaws.services.elastictranscoder.model.CreateJobResult;
import com.amazonaws.services.elastictranscoder.model.JobInput;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amediamanager.config.ConfigurationSettings;
import com.amediamanager.config.ConfigurationSettings.ConfigProps;
import com.amediamanager.dao.TagDao.TagCount;
import com.amediamanager.domain.ContentType;
import com.amediamanager.domain.Privacy;
import com.amediamanager.domain.Tag;
import com.amediamanager.domain.User;
import com.amediamanager.domain.Video;
import com.amediamanager.service.TagsService;
import com.amediamanager.service.VideoService;
import com.amediamanager.util.CommaDelimitedTagEditor;
import com.amediamanager.util.PrivacyEditor;
import com.amediamanager.util.VideoUploadFormSigner;

@Controller
public class VideoController {
	private static final Logger LOG = LoggerFactory
			.getLogger(VideoController.class);

	@Autowired
	com.amediamanager.service.VideoServiceImpl videoService;
	
	@Autowired
	TagsService tagService;

	@Autowired
	ConfigurationSettings config;

	@Autowired
	AmazonS3 s3Client;

	@ModelAttribute("allPrivacy")
	public List<Privacy> populatePrivacy() {
		return Arrays.asList(Privacy.ALL);
	}

	@ModelAttribute("allContentType")
	public List<ContentType> populateContentType() {
		return Arrays.asList(ContentType.ALL);
	}

	@RequestMapping(value = "/videos", method = RequestMethod.GET)
	public String videos(ModelMap model) {
		return "redirect:/";
	}

	@RequestMapping(value = "/tags/{tagId}", method = RequestMethod.GET)
	public String tags(ModelMap model, @PathVariable String tagId) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		List<Video> videos = new ArrayList<Video>();
		List<TagCount> tags = new ArrayList<TagCount>();
		try {
			// Get user's videos and tags
			videos = tagService.getVideosForUserByTag(auth.getName(), tagId);
			tags = tagService.getTagsForUser(auth.getName());

			// Add expiring URLs (1 hour)
			videos = videoService.generateExpiringUrls(videos, 1000*60*60);
		} catch (Exception e) {
			return "redirect:/config";
		}
		model.addAttribute("selectedTag", tagId);
		model.addAttribute("tags", tags);
		model.addAttribute("videos", videos);
		model.addAttribute("templateName", "only_videos");
		return "base";
	}
	
	@RequestMapping(value = "/video/{videoId}", method = RequestMethod.GET)
	public String videoGet(ModelMap model, @PathVariable String videoId,
			@RequestParam(value = "delete", required = false) String delete) {
		Video video = videoService.findById(videoId);

		if (null != delete) {
			videoService.delete(video);
			return videos(model);
		} else {
			video = videoService.generateExpiringUrl(video, 5000);
			model.addAttribute("video", video);
			model.addAttribute("templateName", "video_edit");

			return "base";
		}
	}

	@RequestMapping(value = "/video/{videoId}", method = RequestMethod.POST)
	public String videoEdit(@ModelAttribute Video video,
			@PathVariable String videoId, BindingResult result,
			RedirectAttributes attr, HttpSession session) {
		videoService.update(video);
		return "redirect:/";
	}

	@RequestMapping(value = "/video/upload", method = RequestMethod.GET)
	public String videoUpload(ModelMap model, HttpServletRequest request,
			@ModelAttribute User user) {
		// Video redirect URL
		String redirectUrl = request.getScheme() + "://"
				+ request.getServerName() + ":" + request.getServerPort()
				+ request.getContextPath() + "/video/ingest";

		// Prepare S3 form upload
		VideoUploadFormSigner formSigner = new VideoUploadFormSigner(
				config.getProperty(ConfigProps.S3_UPLOAD_BUCKET),
				config.getProperty(ConfigProps.S3_UPLOAD_PREFIX), user,
				config, redirectUrl);

		model.addAttribute("formSigner", formSigner);
		model.addAttribute("templateName", "video_upload");

		return "base";
	}

	@RequestMapping(value = "/video/ingest", method = RequestMethod.GET)
	public String videoIngest(ModelMap model,
			@RequestParam(value = "bucket") String bucket,
			@RequestParam(value = "key") String videoKey) throws ParseException {
		
		Video video = videoService.save(bucket, videoKey);

		// Kick off preview encoding
		videoService.createVideoPreview(video);

		return "redirect:/";
	}

	@InitBinder
	public void initDateBinder(final WebDataBinder dataBinder) {
		// Bind dates
		final String dateformat = "MM/dd/yyyy";
		final SimpleDateFormat sdf = new SimpleDateFormat(dateformat);
		sdf.setLenient(false);
		dataBinder.registerCustomEditor(Date.class, new CustomDateEditor(sdf,
				false));
	}

	@InitBinder
	public void initTagsBinder(final WebDataBinder dataBinder) {
		// Bind tags
		dataBinder.registerCustomEditor(Set.class,
				new CommaDelimitedTagEditor());
	}

	@InitBinder
	public void initPrivacyBinder(final WebDataBinder dataBinder) {
		// Bind tags
		dataBinder.registerCustomEditor(Privacy.class, new PrivacyEditor());
	}
}

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
package com.amediamanager.service;

import java.util.List;

import net.spy.memcached.MemcachedClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amediamanager.config.ConfigurationSettings;
import com.amediamanager.dao.TagDao;
import com.amediamanager.dao.TagCount;
import com.amediamanager.domain.Video;

@Service
public class TagsServiceImpl implements TagsService {

	protected static final Logger LOG = LoggerFactory
			.getLogger(TagsServiceImpl.class);
	
	@Autowired
	private TagDao tagDao; 

	@Autowired
	protected MemcachedClient memcachedClient;
	
	@Autowired
	protected ConfigurationSettings config;
	
	@SuppressWarnings("unchecked")
	@Override
	public List<TagCount> getTagsForUser(String user) {
		Object cached = null;
		List<TagCount> tags = null;
		
		if(cachingEnabled()) {
			cached = memcachedClient.get(getTagListKey(user));
		}
		
		if(cached != null) {
			tags = (List<TagCount>)cached;
			LOG.debug("CACHE HIT: Tag List");
		} else {
			tags = tagDao.getTagsForUser(user);
			if(cachingEnabled()) {
				LOG.info("CACHE MISS: Tag List");
				memcachedClient.set(getTagListKey(user), 3600, tags);
			}
		}
		return tags;
	}

	@Override
	public List<Video> getVideosForUserByTag(String user, String tagId) {
		return tagDao.getVideosForUserByTag(user, tagId);
	}
	
	@Override
	public void bustCacheForUser(String user) {
		if(cachingEnabled()) {
			memcachedClient.delete(getTagListKey(user));
			LOG.info("Busted tag list cache for " + getTagListKey(user));
		}
	}

	private String getTagListKey(String ownerId) {
		return ownerId + "-tags";
	}

	private Boolean cachingEnabled() {
		return Boolean.parseBoolean(config.getProperty(ConfigurationSettings.ConfigProps.CACHE_ENABLED));
	}
}

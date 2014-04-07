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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amediamanager.dao.TagDao;
import com.amediamanager.dao.TagDao.TagCount;
import com.amediamanager.domain.Video;

@Service
public class TagsServiceImpl implements TagsService {

	@Autowired private TagDao tagDao; 

	@Override
	public List<TagCount> getTagsForUser(String user) {
		return tagDao.getTagsForUser(user);
	}

	@Override
	public List<Video> getVideosForUserByTag(String user, String tagId) {
		return tagDao.getVideosForUserByTag(user, tagId);
	}

	
}

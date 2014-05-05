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
package com.amediamanager.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.amediamanager.domain.Tag;
import com.amediamanager.domain.Video;

@Repository
@Transactional
public class RDSTagDaoImpl implements TagDao {

	@Autowired
    private SessionFactory sessionFactory;
	
	public Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

	@Override
	public void save(Tag t) {
		sessionFactory.getCurrentSession().saveOrUpdate(t);
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<TagCount> getTagsForUser(String u) {
		Query query = sessionFactory.getCurrentSession().createQuery("select new com.amediamanager.dao.TagCount(tag.tagId, tag.name, count(video.id)) from Tag tag join tag.videos video where video.owner = :owner group by tag.tagId");
		query.setParameter("owner", u);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<Video> getVideosForUserByTag(String user, String tagId) {
		Query query = sessionFactory.getCurrentSession().createQuery("select video from Video video join video.tags tag where video.owner = :owner and tag.tagId = :tag");
		query.setParameter("owner", user);
		query.setParameter("tag", tagId);
		return query.list();
	}
}

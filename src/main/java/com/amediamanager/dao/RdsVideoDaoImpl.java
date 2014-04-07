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

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.amediamanager.domain.Video;

@Repository
@Transactional
public class RdsVideoDaoImpl implements VideoDao {

    @Autowired
    private SessionFactory sessionFactory;

    public Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public void save(Video video) {
        getCurrentSession().saveOrUpdate(video);
    }

    @Override
    public void update(Video video) {
        getCurrentSession().saveOrUpdate(video);
    }

    @Override
    public void delete(Video video) {
    	getCurrentSession().delete(video);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<Video> findByUserId(String userId) {
        List<Video> videos = getCurrentSession().createQuery(
                "from Video as video where video.owner = :owner")
                .setParameter("owner", userId)
                .list();

        return videos;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Video findByTranscodeJobId(String jobId) {
       List<Video> videos = getCurrentSession().createQuery(
                "from Video as video where video.transcodeJobId = :jobId")
                .setParameter("jobId", jobId)
                .list();
       return videos.isEmpty() ? null : videos.get(0);
    }

    @Override
    public Video findById(String id) {
        return (Video) getCurrentSession().get(Video.class, id);
    }
}

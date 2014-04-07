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

import java.text.ParseException;
import java.util.List;

import com.amediamanager.domain.Video;
import com.amediamanager.exceptions.DataSourceTableDoesNotExistException;

public interface VideoService {

    public void save(Video user) throws DataSourceTableDoesNotExistException;
    
    public Video save(String bucket, String videoKey) throws ParseException; 

    public void update(Video user) throws DataSourceTableDoesNotExistException;
    
    public void delete(Video video);

    public Video findById(String videoId) throws DataSourceTableDoesNotExistException;

    public List<Video> findByUserId(String email) throws DataSourceTableDoesNotExistException;

    public Video findByTranscodeJobId(String jobId) throws DataSourceTableDoesNotExistException;
    
    public void createVideoPreview(Video video);

    public List<Video> findAllPublic(int limit, int start, int end) throws DataSourceTableDoesNotExistException;

    public Video generateExpiringUrl(Video video, long expirationInMillis);
    
    public List<Video> generateExpiringUrls(List<Video> video, long expirationInMillis);

    public String getDefaultVideoPosterKey();
}

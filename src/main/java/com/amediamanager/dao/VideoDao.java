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

import com.amediamanager.domain.Video;
import com.amediamanager.exceptions.DataSourceTableDoesNotExistException;

public interface VideoDao {
    public void save(Video video) throws DataSourceTableDoesNotExistException;
    public void update(Video video) throws DataSourceTableDoesNotExistException;
    public void delete(Video video);
    public List<Video> findByUserId(String userId);
    public Video findByTranscodeJobId(String jobId);
    public Video findById(String id);
}

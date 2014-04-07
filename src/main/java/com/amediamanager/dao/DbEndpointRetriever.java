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

import com.amazonaws.services.rds.model.Endpoint;

public interface DbEndpointRetriever {
	/**
	 * Retrieve the DNS endpoint for the specified DB
	 * @param dbInstanceId
	 * @return
	 */
	public Endpoint getMasterDbEndpoint(String dbInstanceId);
	
	/**
	 * Retrieve a list of DNS endpoints for any Read Replicas associated with
	 * the specified DB
	 * @param dbInstanceId
	 * @return
	 */
	public List<Endpoint> getReadReplicaEndpoints(String dbInstanceId);
}

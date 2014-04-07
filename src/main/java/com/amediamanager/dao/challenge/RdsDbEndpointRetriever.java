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
package com.amediamanager.dao.challenge;

import java.util.List;

import org.springframework.stereotype.Component;

import com.amazonaws.services.rds.model.Endpoint;

@Component
public class RdsDbEndpointRetriever extends com.amediamanager.dao.RdsDbEndpointRetriever {
	/**
	 * Return the Endpoint for the dbInstanceId 
	 */
	@Override
	public Endpoint getMasterDbEndpoint(String dbInstanceId) {
		return super.getMasterDbEndpoint(dbInstanceId);
	}
	
	/**
	 * First locate the master DB for the provided dbInstanceId, then
	 * locate and return any Read Replicas it may have. Be sure the
	 * replicas are in the 'available' state before returning 
	 */
	@Override
	public List<Endpoint> getReadReplicaEndpoints(String dbInstanceId) {
		return super.getReadReplicaEndpoints(dbInstanceId);
	}
}

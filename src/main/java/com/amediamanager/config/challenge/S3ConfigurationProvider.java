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
package com.amediamanager.config.challenge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3ConfigurationProvider extends com.amediamanager.config.S3ConfigurationProvider {
	private static final Logger LOG = LoggerFactory.getLogger(S3ConfigurationProvider.class);
	
	@Override
	public void loadProperties() {
		/**
		 * ** CHALLENGE **
		 * - Create an AmazonS3Client (does not need creds or a region)
		 * - Retrieve the object stored in bucket super.getBucket() with key super.getKey()
		 * - Call super.setProperties with your result. 
		 */
		super.loadProperties();
	}
}

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
package com.amediamanager.config;

import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read S3 bucket and key from env
 */
public class S3EnvConfigurationProvider extends com.amediamanager.config.challenge.S3ConfigurationProvider {
	private static final Logger LOG = LoggerFactory.getLogger(S3EnvConfigurationProvider.class);
	public S3EnvConfigurationProvider() {
		try {
			LOG.debug("Attempting to create S3ConfigurationProvider with bucket and key from env");
			String bucket = System.getProperty("S3_CONFIG_BUCKET");
			String key = System.getProperty("S3_CONFIG_KEY");
			
			if(bucket == null || key == null) {
				StringBuilder sb = new StringBuilder();
		        Enumeration<?> e = System.getProperties().propertyNames();
		        while (e.hasMoreElements()){
		            String propertyKey = (String) e.nextElement();
		            sb.append(propertyKey);
		            sb.append("=");
		            sb.append(System.getProperties().getProperty(propertyKey));
		            sb.append("\n");
		        }

				LOG.debug("Bucket or key for S3ConfigurationProvider could not be found in system properties. Properties searched were: {}", sb.toString());
			} else {
				LOG.debug("Bucket and key for S3ConfigurationProvider were found in system properties");
				super.setBucket(bucket);
				super.setKey(key);
			}
		} catch (Exception ex) {
			LOG.debug("An error occurred looking for S3 config in env: {}", ex);
		}
	}
}

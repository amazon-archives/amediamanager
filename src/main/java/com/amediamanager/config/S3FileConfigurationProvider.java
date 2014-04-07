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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read S3 bucket and key from file
 */
public class S3FileConfigurationProvider extends com.amediamanager.config.challenge.S3ConfigurationProvider {
	private static final Logger LOG = LoggerFactory.getLogger(S3FileConfigurationProvider.class);
	private static final String S3_CONFIG_FILE = "/s3config.properties";
	public S3FileConfigurationProvider() {
		InputStream stream = getClass().getResourceAsStream(S3_CONFIG_FILE);
        try {
        	LOG.debug("Attempting to create S3ConfigurationProvider with bucket and key from file {}.", S3_CONFIG_FILE);
            Properties properties = new Properties();
            properties.load(stream);
            super.setBucket(properties.getProperty("S3_CONFIG_BUCKET"));
            super.setKey(properties.getProperty("S3_CONFIG_KEY"));
        } catch (Exception e) {
            LOG.debug("No S3 configuration information found in file {}.", S3_CONFIG_FILE);
        } finally {
            try {
            	if(stream != null) {
            		stream.close();
            	}
            } catch (IOException e) {
            	LOG.warn("Error closing stream to configuration file {}.", S3_CONFIG_FILE);
            }
        }
	}
}

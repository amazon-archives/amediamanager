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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amediamanager.config.ConfigurationSettings.ConfigProps;

public class ClassResourceConfigurationProvider extends ConfigurationProvider {
	private static final Logger LOG = LoggerFactory
			.getLogger(ClassResourceConfigurationProvider.class);
	private final String resourceFilePath;
	private Properties properties;

	public ClassResourceConfigurationProvider(String resourceFilePath) {
		this.resourceFilePath = resourceFilePath;
	}

	@Override
	public Properties getProperties() {
		return properties;
	}

	@Override
	public void loadProperties() {
		this.properties = null;
		InputStream stream = getClass().getResourceAsStream(this.resourceFilePath);
		try {
			this.properties = new Properties();
			this.properties.load(stream);
		} catch (IOException e) {
			this.properties = null;
			LOG.error("Failed to get properties.", e);
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				// Don't care
			}
		}
	}

	@Override
	public String getPrettyName() {
		return this.getClass().getSimpleName() + " (" + this.resourceFilePath
				+ ")";
	}

	@Override
	public void persistNewProperty(String key, String value) {
		this.properties.setProperty(key, value);
		
		try {
			URL url = getClass().getResource(this.resourceFilePath);
			this.properties.store(new FileOutputStream(new File(url.toURI())), null);
		} catch(Exception ioe) {
			LOG.error("Error persisting property", ioe);
		}
	}

	@Override
	public void persistNewProperty(ConfigProps property, String value) {
		persistNewProperty(property.name(), value);
	}

}

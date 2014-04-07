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

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amediamanager.config.ConfigurationSettings.ConfigProps;

public class ConfigurationProviderChain extends ConfigurationProvider {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationProviderChain.class);
	private Properties properties;
	private ConfigurationProvider theProvider;
	private final List<ConfigurationProvider> configurationProviders = new LinkedList<ConfigurationProvider>();

	public ConfigurationProviderChain(
			ConfigurationProvider... configurationProviders) {
		for (ConfigurationProvider configurationProvider : configurationProviders) {
			LOG.info("Initializing new ConfigurationProviderChain with provider {}", configurationProvider.getClass().getSimpleName());
			this.configurationProviders.add(configurationProvider);
		}
		loadProperties();
	}

	@Override
	public void loadProperties() {
		this.properties = null;
		this.theProvider = null;
		LOG.info("Loading properties from providers. The first provider with properties will be the ConfigurationProvider.");
		if (this.properties == null) {
			for (ConfigurationProvider provider : this.configurationProviders) {
				provider.loadProperties();
				this.properties = provider.getProperties();
				if (this.properties != null) {
					LOG.info("Selected provider {} as it had properties.", provider.getClass().getSimpleName());
					this.theProvider = provider;
					break;
				}
				LOG.info("Skipped provider {} as it had no properties.", provider.getClass().getSimpleName());
			}
			if (properties == null) {
				throw new RuntimeException(
						"Unable to load properties from any provider in the chain.");
			}
		}
	}
	
	@Override
	public Properties getProperties() {
		return properties;
	}
	

	@Override
	public String getPrettyName() {
		return this.getTheProvider().getPrettyName();
	}

	@Override
	public void persistNewProperty(String key, String value) {
		this.theProvider.persistNewProperty(key, value);
		this.loadProperties();
	}

	@Override
	public void persistNewProperty(ConfigProps property, String value) {
		persistNewProperty(property.name(), value);
	}

	public ConfigurationProvider getTheProvider() {
		return this.theProvider;
	}
}

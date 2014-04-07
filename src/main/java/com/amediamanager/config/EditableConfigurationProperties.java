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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import com.amediamanager.config.ConfigurationSettings.ConfigProps;

@Component
@Scope(WebApplicationContext.SCOPE_APPLICATION)
public class EditableConfigurationProperties {
	@Autowired
	ConfigurationSettings config;
	
	private List<EditableConfigurationProperty> configProps;
	
	@PostConstruct
	public void initialize() {
		configProps = new ArrayList<EditableConfigurationProperty>();
		configProps.add(new EditableConfigurationProperty(ConfigProps.CACHE_ENABLED, config.getProperty(ConfigProps.CACHE_ENABLED), "Enable Caching?"));
	}
	
	public List<EditableConfigurationProperty> getConfigProps() {
		return configProps;
	}
	
	public void setConfigProps(List<EditableConfigurationProperty> configProps) {
		this.configProps = configProps;
	}
}

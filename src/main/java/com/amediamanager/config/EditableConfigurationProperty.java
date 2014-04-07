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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

public class EditableConfigurationProperty {

	private ConfigurationSettings.ConfigProps propertyName;
	private String propertyValue;
	private String displayName;
	
	public EditableConfigurationProperty() {}
	
	public EditableConfigurationProperty(ConfigurationSettings.ConfigProps propertyName, String propertyValue, String displayName) {
		this.propertyName = propertyName;
		this.propertyValue = propertyValue;
		this.displayName = displayName;
	}
	
	public ConfigurationSettings.ConfigProps getPropertyName() {
		return this.propertyName;
	}
	
	public void setPropertyName(ConfigurationSettings.ConfigProps propertyName) {
		this.propertyName = propertyName;
	}

	public String getPropertyValue() {
		return this.propertyValue;
	}

	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}

	public String getDisplayName() {
		return this.displayName;
	}

}

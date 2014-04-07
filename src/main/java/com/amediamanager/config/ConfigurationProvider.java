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
import java.util.Properties;

import com.amediamanager.config.ConfigurationSettings.ConfigProps;

public abstract class ConfigurationProvider {

	public abstract Properties getProperties();
	public abstract String getPrettyName();
	public abstract void loadProperties();
	public abstract void persistNewProperty(String key, String value);
	public abstract void persistNewProperty(ConfigProps property, String value);

	public String propsToString(Properties properties) {
		StringBuilder sb = new StringBuilder();
		Enumeration<?> e = properties.propertyNames();
		while (e.hasMoreElements()){
			String key = (String) e.nextElement();
			sb.append(key);
			sb.append("=");
			sb.append(properties.getProperty(key));
			sb.append("\n");
		}

		return sb.toString();
	}
}

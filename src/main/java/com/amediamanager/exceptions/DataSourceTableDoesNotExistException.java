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

package com.amediamanager.exceptions;

/**
 * Exception thrown when a Data Source's table can not be found.
 *
 */
public class DataSourceTableDoesNotExistException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public String dataSourceName;
	
	public DataSourceTableDoesNotExistException(){}
	public DataSourceTableDoesNotExistException(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}
	public String getDataSourceName() {
		return dataSourceName;
	}
	@Override
	public String getMessage() {
		return "The data source " + this.dataSourceName + " does not exist.";
	}
}

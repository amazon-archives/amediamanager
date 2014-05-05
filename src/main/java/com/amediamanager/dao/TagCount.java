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
package com.amediamanager.dao;

import java.io.Serializable;

public class TagCount implements Serializable {
	private static final long serialVersionUID = -7637673029169203992L;
	private final String tagId;
	private final String name;
	private final long count;

	public TagCount() {
		this(null, null, 0);
	}

	public TagCount(String tagId, String name, long count) {
		this.tagId = tagId;
		this.name = name;
		this.count = count;
	}

	public String getTagId() {
		return tagId;
	}

	public String getName() {
		return name;
	}

	public long getCount() {
		return count;
	}

	public String toString() {
		return getClass().getName() + "[" + tagId + "|" + name + "|" + count
				+ "]";
	}
}
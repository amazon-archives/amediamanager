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
package com.amediamanager.domain;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name="tags")
public class Tag implements Serializable {
	private static final long serialVersionUID = -2103948648383738451L;
	private String tagId;
	private String name;
	private Set<Video> videos;
	
	public Tag() {}
	public Tag(String name) {
		this.tagId = name;
		this.name = name;
	}
	
	@Id
	@Column(name = "tagId", unique = true, nullable = false)
	public String getTagId() {
		return this.tagId;
	}
	
	@Column(name = "name")
	public String getName() {
		return this.name;
	}
	
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "tags")
	public Set<Video> getVideos() {
		return this.videos;
	}
	
	public void setTagId(String tagId) {
		this.tagId = tagId;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setVideos(Set<Video> videos) {
		this.videos = videos;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}

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

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

/**
 * DefaultUser is an implementation of the User interface. The password property
 * for this type of user should be MD5 hashed before setting the value. This
 * class provides a static method to perform the hash.
 */

public class User implements Serializable {
	private static final long serialVersionUID = -1210678236518532231L;
	
	private String id;
	
	@NotEmpty @Email
	private String email;
	
	@Size(min=2, max=30) 
	private String password;
	
	private String nickname;
	private String tagline;
	private String profilePicKey;
	private CommonsMultipartFile profilePicData;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNickname() {
		return this.nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	/**
	 * If the Tagline is null, return an empty string.
	 */
	public String getTagline() {
		return this.tagline == null ? "" : this.tagline;
	}

	public void setTagline(String tagline) {
		this.tagline = tagline;

	}

	public String getProfilePicKey() {
		return this.profilePicKey;
	}

	public void setProfilePicKey(String profilePicKey) {
		this.profilePicKey = profilePicKey;

	}
	
	public CommonsMultipartFile getprofilePicData()
	  {
	    return profilePicData;
	  }
	 
	  public void setprofilePicData(CommonsMultipartFile profilePicData)
	  {
	    this.profilePicData = profilePicData;
	  }
}

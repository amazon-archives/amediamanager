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
package com.amediamanager.dao.challenge;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.amediamanager.config.ConfigurationSettings;
import com.amediamanager.config.ConfigurationSettings.ConfigProps;
import com.amediamanager.domain.User;
import com.amediamanager.exceptions.DataSourceTableDoesNotExistException;
import com.amediamanager.exceptions.UserExistsException;

@Repository
public class DynamoDbUserDaoImpl extends com.amediamanager.dao.DynamoDbUserDaoImpl {
	/**
	 * Store the following User properties in DynamoDB:
	 * - email
	 * - password (already hashed by com.amediamanager.service.UserServiceImpl)
	 * - nickname
	 * - tagline
	 * - profilePicKey
	 * 
	 * Expected attribute names are available in super:
	 * - super.HASH_KEY_NAME
	 * - super.EMAIL_ATTR
	 * - super.PASSWORD_ATTR
	 * - super.NICKNAME_ATTR
	 * - super.TAGLINE_ATTR
	 * - super.PROFILE_PIC_KEY_ATTR
	 * 
	 * If you get stuck, you can convert a User to a Map with:
	 * - super.getMapFromUser
	 */
	@Override
	public void save(User user) throws DataSourceTableDoesNotExistException, UserExistsException {
		super.save(user);
	}
	
	/**
	 * Use config.getProperty(ConfigurationSettings.ConfigProps.DDB_USERS_TABLE)) to get the DDB table name
	 * Use super.HASH_KEY_NAME for name of table's hash key
	 * Return a User object. See super.getUserFromMap() for help converting an Item to a User 
	 */
	@Override
	public User find(String email) throws DataSourceTableDoesNotExistException {
		return super.find(email);
	}
	
	/**
	 * - Watch out for profile pics from different users with the same name (define a good naming scheme for the photos)
	 * - See super.config.getProperty(ConfigProps.S3_PROFILE_PIC_PREFIX) for a pre-defined key prefix that isolates profile pics in their own keyspace within your S3 bucket
	 * - The profile photo should have a public read ACL
	 * - Return the URL (i.e. http://) of the uploaded photo.
	 */
	@Override
	public String uploadFileToS3(CommonsMultipartFile profilePic) throws IOException {
		return super.uploadFileToS3(profilePic);
	}

}

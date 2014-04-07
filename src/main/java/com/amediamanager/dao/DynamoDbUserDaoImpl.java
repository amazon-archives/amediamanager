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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amediamanager.config.ConfigurationSettings;
import com.amediamanager.config.ConfigurationSettings.ConfigProps;
import com.amediamanager.domain.User;
import com.amediamanager.exceptions.DataSourceTableDoesNotExistException;
import com.amediamanager.exceptions.UserExistsException;

public class DynamoDbUserDaoImpl implements UserDao {

	private static final Logger LOG = LoggerFactory.getLogger(DynamoDbUserDaoImpl.class);

	@Autowired
	protected ConfigurationSettings config;

	@Autowired
	protected AmazonDynamoDB dynamoClient;

	@Autowired
	protected AmazonS3 s3Client;

	/** Attribute names **/
	public static final String HASH_KEY_NAME = "EMail";
	public static final String EMAIL_ATTR = HASH_KEY_NAME;
	public static final String PASSWORD_ATTR = "Password";
	public static final String NICKNAME_ATTR = "Nickname";
	public static final String TAGLINE_ATTR = "Tagline";
	public static final String PROFILE_PIC_KEY_ATTR = "ProfilePicKey";
	public static final String ALERT_ON_NEW_CONTENT_ATTR = "AlertOnNewContent";

	@PostConstruct
	public void init() {
	}

	@Override
	public void save(User user)
			throws UserExistsException, DataSourceTableDoesNotExistException {
		try {
			// See if User item exists
			User existing = this.find(user.getEmail());

			// If the user exists, throw an exception
			if(existing != null) {
				throw new UserExistsException();
			}

			// Convert the User object to a Map. The DynamoDB PutItemRequest object
			// requires the Item to be in the Map<String, AttributeValue> structure
			Map<String, AttributeValue> userItem = getMapFromUser(user);

			// Create a request to save and return the user
			PutItemRequest putItemRequest = new PutItemRequest()
												.withTableName(config.getProperty(ConfigurationSettings.ConfigProps.DDB_USERS_TABLE))
												.withItem(userItem);

			// Save user
			dynamoClient.putItem(putItemRequest);
		} catch (ResourceNotFoundException rnfe) {
			throw new DataSourceTableDoesNotExistException(config.getProperty(ConfigurationSettings.ConfigProps.DDB_USERS_TABLE));
		} catch (AmazonServiceException ase) {
			throw ase;
		}
	}

	@Override
	public void update(User user) throws DataSourceTableDoesNotExistException {
		try {
			// If the object includes a profile pic file, upload it to S3
			if(user.getprofilePicData() != null && user.getprofilePicData().getSize() > 0) {
				try {
					String profilePicUrl = this.uploadFileToS3(user.getprofilePicData());
					user.setProfilePicKey(profilePicUrl);
				} catch (IOException e) {
					LOG.error("Error uploading profile pic to S3", e);
				}
			}

			// Convert the User object to a Map
			Map<String, AttributeValue> userItem = getMapFromUser(user);

			// Create a request to save and return the user
			PutItemRequest putItemRequest = new PutItemRequest()
												.withItem(userItem)
												.withTableName(config.getProperty(ConfigurationSettings.ConfigProps.DDB_USERS_TABLE));

			// Save user
			dynamoClient.putItem(putItemRequest);
		} catch (ResourceNotFoundException rnfe) {
			throw new DataSourceTableDoesNotExistException(config.getProperty(ConfigurationSettings.ConfigProps.DDB_USERS_TABLE));
		} catch (AmazonServiceException ase) {
			throw ase;
		}

	}

	@Override
	public User find(String email) throws DataSourceTableDoesNotExistException {
		try {

			User user = null;

			// Create a request to find a User by email address
			GetItemRequest getItemRequest = new GetItemRequest()
					.withTableName(
							config.getProperty(ConfigurationSettings.ConfigProps.DDB_USERS_TABLE))
					.addKeyEntry(HASH_KEY_NAME, new AttributeValue(email));

			// Issue the request to find the User in DynamoDB
			GetItemResult getItemResult = dynamoClient.getItem(getItemRequest);

			// If an item was found
			if (getItemResult.getItem() != null) {
				// Marshal the Map<String, AttributeValue> structure returned in
				// the
				// GetItemResult to a User object
				user = getUserFromMap(getItemResult.getItem());
			}

			return user;

		} catch (ResourceNotFoundException rnfe) {

			// The ResourceNotFoundException method is thrown by the getItem()
			// method
			// if the DynamoDB table doesn't exist. This exception is re-thrown
			// as a
			// custom, more specific DataSourceTableDoesNotExistException that
			// users
			// of this DAO understand.
			throw new DataSourceTableDoesNotExistException(config.getProperty(ConfigurationSettings.ConfigProps.DDB_USERS_TABLE));
		}
	}

	/**
	 * Marshal a Map<String, AttributeValue> object (representing an Item
	 * retrieved from DynamoDB) to a User.
	 *
	 * @return
	 */
	public User getUserFromMap(Map<String, AttributeValue> userItem) {
		// Create a new user from the minimum required values. As the password
		// is stored
		// hashed, the last parameter is false to indicate.
		User user = new User();

		user.setEmail(userItem.get(EMAIL_ATTR).getS());
		user.setPassword(userItem.get(PASSWORD_ATTR).getS());
		user.setId(userItem.get(EMAIL_ATTR).getS());

		// Look for other optional attributes and set them for the object if
		// they exist.
		if (null != userItem.get(NICKNAME_ATTR))
			user.setNickname(userItem.get(NICKNAME_ATTR).getS());

		if (null != userItem.get(TAGLINE_ATTR))
			user.setTagline(userItem.get(TAGLINE_ATTR).getS());

		if (null != userItem.get(PROFILE_PIC_KEY_ATTR))
			user.setProfilePicKey((userItem.get(PROFILE_PIC_KEY_ATTR).getS()));

		return user;
	}

	/**
     * Marshal a User object to a Map<String, AttributeValue> suitable for inserting
     * as an item into a DynamoDB table.
     *
     * @param user
     * @return
     */
    public Map<String, AttributeValue> getMapFromUser(User user) {
		// Create a Map object from the User
		Map<String, AttributeValue> userItem = new HashMap<String, AttributeValue>();

		// Add items to the Map
		userItem.put(EMAIL_ATTR, new AttributeValue(user.getEmail()));
		userItem.put(PASSWORD_ATTR, new AttributeValue(user.getPassword()));

		// Ensure User properties are neither null nor empty strings
		if(null != user.getNickname() && true != user.getNickname().isEmpty())
			userItem.put(NICKNAME_ATTR, new AttributeValue(user.getNickname()));

		if(null != user.getTagline() && true != user.getTagline().isEmpty())
			userItem.put(TAGLINE_ATTR, new AttributeValue(user.getTagline()));

		if(null != user.getProfilePicKey() && true != user.getProfilePicKey().isEmpty())
			userItem.put(PROFILE_PIC_KEY_ATTR, new AttributeValue(user.getProfilePicKey()));

		return userItem;
    }

    /**
     * Upload the profile pic to S3 and return it's URL
     * @param profilePic
     * @return The fully-qualified URL of the photo in S3
     * @throws IOException
     */
    public String uploadFileToS3(CommonsMultipartFile profilePic) throws IOException {

		// Profile pic prefix
    	String prefix = config.getProperty(ConfigProps.S3_PROFILE_PIC_PREFIX);

		// Date string
		String dateString = new SimpleDateFormat("ddMMyyyy").format(new java.util.Date());
		String s3Key = prefix + "/" + dateString + "/" + UUID.randomUUID().toString() + "_" + profilePic.getOriginalFilename();

		// Get bucket
		String s3bucket = config.getProperty(ConfigProps.S3_UPLOAD_BUCKET);

		// Create a ObjectMetadata instance to set the ACL, content type and length
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType(profilePic.getContentType());
		metadata.setContentLength(profilePic.getSize());

		// Create a PutRequest to upload the image
		PutObjectRequest putObject = new PutObjectRequest(s3bucket, s3Key, profilePic.getInputStream(), metadata);

		// Put the image into S3
		s3Client.putObject(putObject);
		s3Client.setObjectAcl(s3bucket, s3Key, CannedAccessControlList.PublicRead);

		return "http://" + s3bucket + ".s3.amazonaws.com/" + s3Key;
    }
}

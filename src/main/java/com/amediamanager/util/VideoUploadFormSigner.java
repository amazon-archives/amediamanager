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
package com.amediamanager.util;

import java.util.UUID;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amediamanager.config.ConfigurationSettings;
import com.amediamanager.config.ConfigurationSettings.ConfigProps;
import com.amediamanager.domain.User;

/**
 * 
 * @author evbrown
 *
 */
public class VideoUploadFormSigner extends S3FormSigner {
	private String s3Bucket;
	private String objectKey;
	private String keyPrefix;
	private String successActionRedirect;
	private String encodedPolicy;
	private String signature;
	private String uuid;
	private User user;
	private AWSCredentialsProvider credsProvider;
	private ConfigurationSettings config;
	
	public VideoUploadFormSigner(String s3Bucket, String keyPrefix, User user, ConfigurationSettings config,
			String successActionRedirect) {
		this.s3Bucket = s3Bucket;
		this.keyPrefix = keyPrefix;
		this.successActionRedirect = successActionRedirect;
		this.user = user;
		this.credsProvider = config.getAWSCredentialsProvider();
		this.config = config;
		this.uuid =  UUID.randomUUID().toString();
		
		String policy = super.generateUploadPolicy(s3Bucket, keyPrefix, credsProvider, successActionRedirect);
		String[] policyAndSig = super.signRequest(credsProvider, policy);
		
		// Create object key
		generateVideoObjectKey(this.uuid);
		
		// Create policy
		this.encodedPolicy = policyAndSig[0];
		this.signature = policyAndSig[1];
	}
	
	/**
	 * Generate a unique object key for this upload
	 */
	private void generateVideoObjectKey(String uuid) {
		this.objectKey = this.keyPrefix + "/original/" + user.getId() + "/" + uuid;
	}
	public AWSCredentialsProvider getCredsProvider() {
		return credsProvider;
	}
	public String getS3Bucket() {
		return s3Bucket;
	}
	public String getS3BucketUrl() {
		String region = config.getProperty(ConfigProps.AWS_REGION); 
		if(region.equals("us-east-1")) {
			region = "external-1";
		}
		
		String prefix = "s3-" + region;
		
		return "https://" + s3Bucket + "." + prefix + ".amazonaws.com/";
	}
	public void setS3BucketUrl(String s3BucketUrl) {
		this.s3Bucket = s3BucketUrl;
	}
	public String getObjectKey() {
		return objectKey;
	}
	public void setObjectKey(String objectKey) {
		this.objectKey = objectKey;
	}
	public String getKeyPrefix() {
		return keyPrefix;
	}
	public void setKeyPrefix(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}
	public Boolean getIsToken() {
		return credsProvider.getCredentials() instanceof BasicSessionCredentials;
	}
	public String getSuccessActionRedirect() {
		return successActionRedirect;
	}
	public void setSuccessActionRedirect(String successActionRedirect) {
		this.successActionRedirect = successActionRedirect;
	}
	public String getEncodedPolicy() {
		return encodedPolicy;
	}
	public void setPolicy(String policy) {
		this.encodedPolicy = policy;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

}

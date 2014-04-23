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

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.amediamanager.config.ConfigurationSettings.ConfigProps;

public abstract class S3ConfigurationProvider extends ConfigurationProvider {
    private static final Logger LOG = LoggerFactory.getLogger(S3ConfigurationProvider.class);
    private String bucket;
    private String key;
    private Properties properties;

    @Override
    public void loadProperties() {
    	this.properties = null;
    	
    	// Load properties if there is a bucket and key
        if (bucket != null && key != null) {
          AWSCredentialsProvider creds = new AWSCredentialsProviderChain(
                      new InstanceProfileCredentialsProvider(),
                  		new EnvironmentVariableCredentialsProvider(),
                      new SystemPropertiesCredentialsProvider()
                          );
            AmazonS3 s3Client = new AmazonS3Client(creds);
            try {
                S3Object object = s3Client.getObject(this.bucket, this.key);
                if (object != null) {
                    this.properties = new Properties();
                    try {
                        this.properties.load(object.getObjectContent());
                    } catch (IOException e) {
                        this.properties = null;
                        LOG.warn("Found configuration file in S3 but failed to load properties (s3://{}/{})", new Object[]{this.bucket, this.key, e});
                    } finally {
                        try {
                            object.close();
                        } catch (IOException e) {
                            // Don't care
                        }
                    }
                }
            } catch (AmazonS3Exception ase) {
                LOG.error("Error loading config from s3://{}/{}", new Object[]{this.bucket, this.key, ase});
            }
        }
    }

    @Override
    public void persistNewProperty(String key, String value) {
        if (this.properties != null) {
            this.properties.put(key, value);
            AmazonS3 s3Client = new AmazonS3Client();
            try {
                s3Client.putObject(this.bucket, this.key,
                        IOUtils.toInputStream(this.propsToString(this.properties)), null);
            } catch (AmazonS3Exception ase) {
                LOG.error("Error persisting config from s3://{}/{}", new Object[]{this.bucket, this.key, ase});
            }
        } else {
        	LOG.error("Could not persist new property because this.properties is null.");
        }
    }

    @Override
    public void persistNewProperty(ConfigProps property, String value) {
        persistNewProperty(property.name(), value);
    }

    @Override
    public String getPrettyName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Properties getProperties() {
    	return properties;
    }
    
    /**
     * This setter is only exposed to make the coding challenge feasible. The challenge
     * subclass implements loadProperties and needs access to set this.properties.
     * @param properties
     */
    public void setProperties(Properties properties) {
    	this.properties = properties;
    }
    
    public String getBucket() {
    	return this.bucket;
    }
    
    public void setBucket(String bucket) {
    	this.bucket = bucket;
    }
    
    public String getKey() {
    	return this.key;
    }
    
    public void setKey(String key) {
    	this.key = key;
    }
}

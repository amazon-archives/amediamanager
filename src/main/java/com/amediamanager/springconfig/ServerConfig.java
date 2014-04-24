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
package com.amediamanager.springconfig;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import net.spy.memcached.MemcachedClient;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsyncClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder;
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoderClient;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amediamanager.config.ConfigurationSettings;

@Configuration
@EnableScheduling
@EnableAspectJAutoProxy
@EnableWebMvc
@EnableTransactionManagement
public class ServerConfig {

	@Bean
	@Scope(WebApplicationContext.SCOPE_APPLICATION)
	public MemcachedClient memcachedClient(final ConfigurationSettings settings) throws IOException {
		MemcachedClient client = null;
		if(settings.getProperty(ConfigurationSettings.ConfigProps.CACHE_ENABLED).equalsIgnoreCase("true")) {
			String configEndpoint = settings.getProperty(ConfigurationSettings.ConfigProps.CACHE_ENDPOINT);
	        Integer clusterPort = Integer.parseInt(settings.getProperty(ConfigurationSettings.ConfigProps.CACHE_PORT));
	        client = new MemcachedClient(new InetSocketAddress(configEndpoint, clusterPort));
		}
        return client;
	}
	
    @Bean
    @Scope(WebApplicationContext.SCOPE_APPLICATION)
    public AWSCredentialsProvider credentials() {
        return new AWSCredentialsProviderChain(
		            new InstanceProfileCredentialsProvider(),
		            new EnvironmentVariableCredentialsProvider(),
		            new SystemPropertiesCredentialsProvider()
                );
    }

    @Bean
    @Scope(WebApplicationContext.SCOPE_APPLICATION)
    public Region region(final ConfigurationSettings settings) {
        return Region.getRegion(Regions.fromName(settings.getProperty(ConfigurationSettings.ConfigProps.AWS_REGION)));

    }

    @Bean
    @Scope(WebApplicationContext.SCOPE_APPLICATION)
    public AmazonElasticTranscoder transcodeClient(final AWSCredentialsProvider creds,
                                                   final Region region) {
        return region.createClient(AmazonElasticTranscoderClient.class, creds, null);
    }

    @Bean
    @Scope(WebApplicationContext.SCOPE_APPLICATION)
    public AmazonS3 s3Client(final AWSCredentialsProvider creds,
                             final Region region) {
        return region.createClient(AmazonS3Client.class, creds, null);
    }

    @Bean
    @Scope(WebApplicationContext.SCOPE_APPLICATION)
    public AmazonRDS rdsClient(final AWSCredentialsProvider creds,
                               final Region region) {
        return region.createClient(AmazonRDSClient.class, creds, null);
    }

    @Bean
    @Scope(WebApplicationContext.SCOPE_APPLICATION)
    public AmazonDynamoDB dynamoClient(final AWSCredentialsProvider creds,
                               final Region region) {
        return region.createClient(AmazonDynamoDBClient.class, creds, null);
    }

    @Bean
    @Scope(WebApplicationContext.SCOPE_APPLICATION)
    public AmazonCloudWatchAsyncClient cloudwatchClient(final AWSCredentialsProvider creds,
                                                final Region region) {
        return region.createClient(AmazonCloudWatchAsyncClient.class, creds, null);
    }

    @Bean
    @Scope(WebApplicationContext.SCOPE_APPLICATION)
    public AmazonSQS sqsClient(final AWSCredentialsProvider creds, final Region region) {
        return region.createClient(AmazonSQSClient.class, creds, null);
    }
}

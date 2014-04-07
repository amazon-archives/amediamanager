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
package com.amediamanager.scheduled.challenge;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.amazonaws.services.sqs.model.Message;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ElasticTranscoderTasks extends com.amediamanager.scheduled.ElasticTranscoderTasks {
	
	/**
	 * - Call super.handleMessage for each message you receive from the queue.
	 * - Use super.config to find the queue to poll
	 * - User super.sqsClient to connect to the queue
	 */
	@Override
	@Scheduled(fixedDelay = 1)
    public void checkStatus() {
        super.checkStatus();
    }
	
	/**
	 * - Delete the given message from the SQS queue
	 */
	@Override
	public void deleteMessage(final Message message) {
		super.deleteMessage(message);
	}
}

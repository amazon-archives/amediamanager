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
package com.amediamanager.scheduled;

import java.io.IOException;

import org.joda.time.Instant;
import org.joda.time.Minutes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amediamanager.config.ConfigurationSettings;
import com.amediamanager.config.ConfigurationSettings.ConfigProps;
import com.amediamanager.domain.Video;
import com.amediamanager.service.VideoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Holds scheduled tasks related to our Elastic Transcoder
 *
 */
public class ElasticTranscoderTasks {
    protected static final Logger LOG = LoggerFactory.getLogger(ElasticTranscoderTasks.class);

    @Autowired
    protected ConfigurationSettings config;

    @Autowired
    protected VideoService videoService;

    @Autowired
    protected AmazonSQS sqsClient;

    protected final ObjectMapper mapper = new ObjectMapper();

    protected void checkStatus() {
        String sqsQueue = config.getProperty(ConfigProps.TRANSCODE_QUEUE);
        LOG.info("Polling transcode queue {} for changes.", sqsQueue);
        ReceiveMessageRequest request = new ReceiveMessageRequest(sqsQueue)
            .withMaxNumberOfMessages(3)
            .withWaitTimeSeconds(20);

        ReceiveMessageResult result = sqsClient.receiveMessage(request);

        for (Message msg : result.getMessages()) {
            handleMessage(msg);
        }
        LOG.info("Finished polling transcode queue {} and handled {} message(s).", sqsQueue, result.getMessages().size());
    }

    protected void deleteMessage(final Message message) {
        DeleteMessageRequest request = new DeleteMessageRequest()
            .withQueueUrl(config.getProperty(ConfigProps.TRANSCODE_QUEUE))
            .withReceiptHandle(message.getReceiptHandle());

        sqsClient.deleteMessage(request);
    }

    protected void handleMessage(final Message message) {
        try {
        	LOG.info("Handling message received from checkStatus");
            ObjectNode snsMessage = (ObjectNode) mapper.readTree(message.getBody());
            ObjectNode notification = (ObjectNode) mapper.readTree(snsMessage.get("Message").asText());
            String state = notification.get("state").asText();
            String jobId = notification.get("jobId").asText();
            String pipelineId = notification.get("pipelineId").asText();
            Video video = videoService.findByTranscodeJobId(jobId);
            if (video == null) {
                LOG.warn("Unable to process result for job {} because it does not exist.", jobId);
                Instant msgTime = Instant.parse(snsMessage.get("Timestamp").asText());
                if (Minutes.minutesBetween(msgTime, new Instant()).getMinutes() > 20) {
                    LOG.error("Job {} has not been found for over 20 minutes, deleting message from queue", jobId);
                    deleteMessage(message);
                }
                // Leave it on the queue for now.
                return;
            }
            if ("ERROR".equals(state)) {
                LOG.warn("Job {} for pipeline {} failed to complete. Body: \n{}", jobId, pipelineId, notification.get("messageDetails").asText());
                video.setThumbnailKey(videoService.getDefaultVideoPosterKey());
                videoService.save(video);
            } else {
                // Construct our url prefix: https://bucketname.s3.amazonaws.com/output/key/
                String prefix = notification.get("outputKeyPrefix").asText();
                if (!prefix.endsWith("/")) { prefix += "/"; }

                ObjectNode output = ((ObjectNode) ((ArrayNode) notification.get("outputs")).get(0));
                String previewFilename = prefix + output.get("key").asText();
                String thumbnailFilename = prefix + output.get("thumbnailPattern").asText().replaceAll("\\{count\\}", "00002") + ".png";
                video.setPreviewKey(previewFilename);
                video.setThumbnailKey(thumbnailFilename);
                videoService.save(video);
            }
            deleteMessage(message);
        } catch (JsonProcessingException e) {
            LOG.error("JSON exception handling notification: {}", message.getBody(), e);
        } catch (IOException e) {
            LOG.error("IOException handling notification: {}", message.getBody(), e);
        }
    }

}

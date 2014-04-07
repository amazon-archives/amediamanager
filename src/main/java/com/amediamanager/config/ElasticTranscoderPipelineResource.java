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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder;
import com.amazonaws.services.elastictranscoder.model.AudioParameters;
import com.amazonaws.services.elastictranscoder.model.CreatePipelineRequest;
import com.amazonaws.services.elastictranscoder.model.CreatePipelineResult;
import com.amazonaws.services.elastictranscoder.model.CreatePresetRequest;
import com.amazonaws.services.elastictranscoder.model.CreatePresetResult;
import com.amazonaws.services.elastictranscoder.model.Notifications;
import com.amazonaws.services.elastictranscoder.model.Thumbnails;
import com.amazonaws.services.elastictranscoder.model.VideoParameters;
import com.amediamanager.config.ConfigurationSettings.ConfigProps;

@Component
public class ElasticTranscoderPipelineResource implements ProvisionableResource {
    private static final Logger LOG = LoggerFactory.getLogger(ElasticTranscoderPipelineResource.class);
    private volatile ProvisionState state = ProvisionState.UNPROVISIONED;

    @Autowired
    private ConfigurationSettings config;

    @Autowired
    private AmazonElasticTranscoder transcoderClient;

    @PostConstruct
    public void init() {
        getState();
    }

    @Override
    public ProvisionState getState() {
    	if (getPipeline() != null && getPreset() != null) {
            state = ProvisionState.PROVISIONED;
        }
    	return state;
    }

    @Override
    public String getName() {
        return "Elastic Transcoder Pipeline";
    }

    private String getPipeline() {
        return config.getProperty(ConfigProps.TRANSCODE_PIPELINE);
    }

    private String getPreset() {
        return config.getProperty(ConfigProps.TRANSCODE_PRESET);
    }

    private String provisionPipeline() {
        String pipelineId = config.getProperty(ConfigProps.TRANSCODE_PIPELINE);

        if (pipelineId == null) {
        	LOG.info("Provisioning ETS Pipeline.");
            state = ProvisionState.PROVISIONING;
            Notifications notifications = new Notifications()
                .withError(config.getProperty(ConfigProps.TRANSCODE_TOPIC))
                .withCompleted(config.getProperty(ConfigProps.TRANSCODE_TOPIC))
                .withProgressing("")
                .withWarning("");

            CreatePipelineRequest pipelineRequest = new CreatePipelineRequest()
                .withName("amm-reinvent-pipeline-" + UUID.randomUUID().toString().replace("-", "").substring(0, 18).toUpperCase())
                .withRole(config.getProperty(ConfigProps.TRANSCODE_ROLE))
                .withInputBucket(config.getProperty(ConfigProps.S3_UPLOAD_BUCKET))
                .withOutputBucket(config.getProperty(ConfigProps.S3_UPLOAD_BUCKET))
                .withNotifications(notifications);

            try {
                CreatePipelineResult pipelineResult = transcoderClient.createPipeline(pipelineRequest);
                pipelineId = pipelineResult.getPipeline().getId();
                LOG.info("Pipeline {} created. Persisting to configuration provider.", pipelineId);
                config.getConfigurationProvider().persistNewProperty(ConfigProps.TRANSCODE_PIPELINE, pipelineId);
            } catch (AmazonServiceException e) {
                LOG.error("Failed creating pipeline {}", pipelineRequest.getName(), e);
                state = ProvisionState.UNPROVISIONED;
            }
        }
        return pipelineId;
    }

    private String provisionPreset() {
        String presetId = config.getProperty(ConfigProps.TRANSCODE_PRESET);

        if (presetId == null) {
        	LOG.info("Provisioning ETS Preset.");
            state = ProvisionState.PROVISIONING;
            Map<String, String> codecOptions = new HashMap<String, String>();
            codecOptions.put("Profile", "main");
            codecOptions.put("Level", "3.1");
            codecOptions.put("MaxReferenceFrames", "3");

            VideoParameters video = new VideoParameters()
                .withCodec("H.264")
                .withCodecOptions(codecOptions)
                .withKeyframesMaxDist("90")
                .withFixedGOP("false")
                .withBitRate("2200")
                .withFrameRate("30")
                .withMaxWidth("1280")
                .withMaxHeight("720")
                .withSizingPolicy("ShrinkToFit")
                .withPaddingPolicy("NoPad")
                .withDisplayAspectRatio("auto");

            AudioParameters audio = new AudioParameters()
                .withCodec("AAC")
                .withSampleRate("44100")
                .withBitRate("160")
                .withChannels("2");

            Thumbnails thumbnails = new Thumbnails()
                .withFormat("png")
                .withInterval("60")
                .withMaxWidth("500")
                .withMaxHeight("300")
                .withSizingPolicy("ShrinkToFit")
                .withPaddingPolicy("NoPad");

            CreatePresetRequest presetRequest = new CreatePresetRequest()
                .withName("amm-reinvent-preset-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase())
                .withDescription("Preset used by aMediaManager re:Invent 2013")
                .withContainer("mp4")
                .withVideo(video)
                .withAudio(audio)
                .withThumbnails(thumbnails);

            try {
                CreatePresetResult result = transcoderClient.createPreset(presetRequest);
                presetId = result.getPreset().getId();
                config.getConfigurationProvider().persistNewProperty(ConfigProps.TRANSCODE_PRESET, presetId);
                LOG.info("Preset {} created. Persisting to configuration provider.", presetId);
            } catch (AmazonServiceException e) {
                LOG.error("Failed creating transcoder preset {}", presetRequest.getName(), e);
                state = ProvisionState.UNPROVISIONED;
            }
        }
        return presetId;
    }

    @Override
    public synchronized void provision() {
        if (provisionPipeline() != null && provisionPreset() != null) {
            state = ProvisionState.PROVISIONED;
        }
    }
}

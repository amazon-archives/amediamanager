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
package com.amediamanager.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.spy.memcached.MemcachedClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder;
import com.amazonaws.services.elastictranscoder.model.CreateJobOutput;
import com.amazonaws.services.elastictranscoder.model.CreateJobRequest;
import com.amazonaws.services.elastictranscoder.model.CreateJobResult;
import com.amazonaws.services.elastictranscoder.model.JobInput;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amediamanager.config.ConfigurationSettings;
import com.amediamanager.config.ConfigurationSettings.ConfigProps;
import com.amediamanager.controller.VideoController;
import com.amediamanager.dao.VideoDao;
import com.amediamanager.domain.Privacy;
import com.amediamanager.domain.Tag;
import com.amediamanager.domain.Video;
import com.amediamanager.exceptions.DataSourceTableDoesNotExistException;

@Service
public class VideoServiceImpl implements VideoService {
	protected static final Logger LOG = LoggerFactory
			.getLogger(VideoServiceImpl.class);
	
	@Autowired
	protected VideoDao videoDao;

	@Autowired
	protected AWSCredentialsProvider credentials;

	@Autowired
	protected AmazonS3 s3Client;

	@Autowired
	protected ConfigurationSettings config;

	@Autowired
	protected AmazonElasticTranscoder transcoderClient;
	
	@Autowired
	protected MemcachedClient memcachedClient;

	@Override
	public void save(Video video) throws DataSourceTableDoesNotExistException {
		if(cachingEnabled()) {
			memcachedClient.delete(getVideoListKey(video.getOwner()));
			LOG.info("Busted video cache for " + getVideoListKey(video.getOwner()));
		}
		videoDao.save(video);
	}
	
	@Override
	public Video save(String bucket, String videoKey) throws ParseException {

		// From bucket and key, get metadata from video that was just uploaded
		GetObjectMetadataRequest metadataReq = new GetObjectMetadataRequest(
				bucket, videoKey);
		ObjectMetadata metadata = s3Client.getObjectMetadata(metadataReq);
		Map<String, String> userMetadata = metadata.getUserMetadata();

		Video video = new Video();

		video.setDescription(userMetadata.get("description"));
		video.setOwner(userMetadata.get("owner"));
		video.setId(userMetadata.get("uuid"));
		video.setTitle(userMetadata.get("title"));
		video.setPrivacy(Privacy.fromName(userMetadata.get("privacy")));
		video.setCreatedDate(new SimpleDateFormat("MM/dd/yyyy")
				.parse(userMetadata.get("createddate")));
		video.setOriginalKey(videoKey);
		video.setBucket(userMetadata.get("bucket"));
		video.setUploadedDate(new Date());

		Set<Tag> tags = new HashSet<Tag>();
		for (String tag : userMetadata.get("tags").split(",")) {
			tags.add(new Tag(tag.trim()));
		}
		video.setTags(tags);

		save(video);
		
		return video;
	}

	@Override
	public void update(Video video) throws DataSourceTableDoesNotExistException {
		if(cachingEnabled()) {
			memcachedClient.delete(getVideoListKey(video.getOwner()));
			LOG.info("Busted video cache for " + getVideoListKey(video.getOwner()));
		}
		videoDao.update(video);
	}

	@Override
	public void delete(Video video) {
		if(cachingEnabled()) {
			memcachedClient.delete(getVideoListKey(video.getOwner()));
			LOG.info("Busted video cache for " + getVideoListKey(video.getOwner()));
		}
		videoDao.delete(video);
	}
	
	@Override
	public Video findById(String videoId)
			throws DataSourceTableDoesNotExistException {
		return videoDao.findById(videoId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Video> findByUserId(String email) {
		Object cached = null;
		List<Video> videos = null;
		
		if(cachingEnabled()) {
			cached = memcachedClient.get(getVideoListKey(email));
		}
		
		if(cached != null) {
			videos = (List<Video>)cached;
			LOG.info("CACHE HIT: Video List");
		} else {
			videos = videoDao.findByUserId(email);
			if(cachingEnabled()) {
				LOG.info("CACHE MISS: Video List");
				memcachedClient.set(getVideoListKey(email), 3600, videos);
			}
		}
		return videos;
	}

	@Override
	public Video findByTranscodeJobId(final String jobId)
			throws DataSourceTableDoesNotExistException {
		return videoDao.findByTranscodeJobId(jobId);
	}

	@Override
	public List<Video> findAllPublic(int limit, int start, int end)
			throws DataSourceTableDoesNotExistException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createVideoPreview(Video video) {
		String pipelineId = config.getProperty(ConfigProps.TRANSCODE_PIPELINE);
		String presetId = config.getProperty(ConfigProps.TRANSCODE_PRESET);
		if (pipelineId == null || presetId == null) {
			return;
		}
		CreateJobRequest encodeJob = new CreateJobRequest()
				.withPipelineId(pipelineId)
				.withInput(
						new JobInput().withKey(video.getOriginalKey())
								.withAspectRatio("auto").withContainer("auto")
								.withFrameRate("auto").withInterlaced("auto")
								.withResolution("auto"))
				.withOutputKeyPrefix(
						"uploads/converted/" + video.getOwner() + "/")
				.withOutput(
						new CreateJobOutput()
								.withKey(UUID.randomUUID().toString())
								.withPresetId(presetId)
								.withThumbnailPattern(
										"thumbs/"
												+ UUID.randomUUID().toString()
												+ "-{count}"));

		try {
			CreateJobResult result = transcoderClient.createJob(encodeJob);
			video.setTranscodeJobId(result.getJob().getId());
			video.setThumbnailKey("static/img/in_progress_poster.png");
			save(video);
		} catch (AmazonServiceException e) {
			LOG.error("Failed creating transcode job for video {}",
					video.getId(), e);
		}
	}
	
	@Override
	public List<Video> generateExpiringUrls(List<Video> videos, long expirationInMillis) {
		List<Video> newVideos = null;
		if(null != videos) {
			newVideos = new ArrayList<Video>();
			for(Video video : videos) {
				newVideos.add(generateExpiringUrl(video, expirationInMillis));
			}
		}
		
		return newVideos;
	}
	
	@Override
	public Video generateExpiringUrl(Video video, long expirationInMillis) {

		Date expiration = new java.util.Date();
		long msec = expiration.getTime();
		msec += expirationInMillis;
		expiration.setTime(msec);

		// Expiring URL for original video
		GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(
				video.getBucket(), video.getOriginalKey());
		generatePresignedUrlRequest.setMethod(HttpMethod.GET);
		generatePresignedUrlRequest.setExpiration(expiration);
		video.setExpiringUrl(s3Client
				.generatePresignedUrl(generatePresignedUrlRequest));

		// Expiring URL for preview video
		if (video.getPreviewKey() != null) {
			generatePresignedUrlRequest = new GeneratePresignedUrlRequest(
					video.getBucket(), video.getPreviewKey());
			generatePresignedUrlRequest.setMethod(HttpMethod.GET);
			generatePresignedUrlRequest.setExpiration(expiration);
			video.setExpiringPreviewKey(s3Client
					.generatePresignedUrl(generatePresignedUrlRequest));
		}

		// Expiring URL for original video
		if (video.getThumbnailKey() != null) {
			generatePresignedUrlRequest = new GeneratePresignedUrlRequest(
					video.getBucket(), video.getThumbnailKey());
			generatePresignedUrlRequest.setMethod(HttpMethod.GET);
			generatePresignedUrlRequest.setExpiration(expiration);
			video.setExpiringThumbnailKey(s3Client
					.generatePresignedUrl(generatePresignedUrlRequest));
		}

		return video;
	}

	/**
	 * Default placeholder image for profile pic
	 * 
	 * @return
	 */
	@Override
	public String getDefaultVideoPosterKey() {
		return "https://"
				+ config.getProperty(ConfigurationSettings.ConfigProps.S3_UPLOAD_BUCKET)
				+ ".s3.amazonaws.com/"
				+ config.getProperty(ConfigurationSettings.ConfigProps.DEFAULT_VIDEO_POSTER_KEY);
	}
	
	private String getVideoListKey(String ownerId) {
		return ownerId + "-videos";
	}

	private Boolean cachingEnabled() {
		return Boolean.parseBoolean(config.getProperty(ConfigurationSettings.ConfigProps.CACHE_ENABLED));
	}
}

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
package com.amediamanager.metrics.challenge;

import java.util.Collection;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsync;
import com.amazonaws.services.cloudwatch.model.MetricDatum;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class MetricBatcher extends com.amediamanager.metrics.MetricBatcher {

    @Autowired
    public MetricBatcher(AmazonCloudWatchAsync cloudWatch) {
        super(cloudWatch);
    }

    /**
     * Send a batch of MetricDatums to CloudWatch
     * @param datums a map of metric namespace to datums
     */
    @Override
    protected void sendBatch(Map<String, Collection<MetricDatum>> datums) {
        super.sendBatch(datums);
    }
}

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

import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amediamanager.metrics.MetricBatcher;

@Aspect
@Component
public class MetricAspect extends com.amediamanager.metrics.MetricAspect {

    @Autowired
    public MetricAspect(MetricBatcher metricBatcher) {
        super(metricBatcher);
    }

    /**
     * Emit metrics for an operation performed against AWS
     * @param service the AWS service being called
     * @param operation the name of the AWS API
     * @param startTime the time the call began
     * @param exception the exception thrown (can be null)
     */
    @Override
    protected void emitMetrics(String service, String operation,
                               long startTime, Throwable exception) {
        super.emitMetrics(service, operation, startTime, exception);
    }

}

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
package com.amediamanager.metrics;

import java.util.Date;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;

import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

public class MetricAspect {

    protected final MetricBatcher metricBatcher;

    public MetricAspect(final MetricBatcher metricBatcher) {
        this.metricBatcher = metricBatcher;
    }

    @Pointcut("execution(public * com.amazonaws.services..*.*(..))")
    public final void sdkClients() {
    }

    @Around("sdkClients()")
    public final Object logMetrics(ProceedingJoinPoint pjp) throws Throwable {
        final String service = pjp.getSignature().getDeclaringType().getSimpleName();
        final String operation = pjp.getSignature().getName();
        final long startTime = System.currentTimeMillis();

        Throwable exception = null;

        try {
            return pjp.proceed();
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            emitMetrics(service, operation, startTime, exception);
        }
    }

    protected void emitMetrics(String service, String operation, long startTime, Throwable exception) {
        final MetricDatum latency = newDatum(service, operation, startTime)
                                        .withMetricName("Latency")
                                        .withUnit(StandardUnit.Milliseconds)
                                        .withValue((double)System.currentTimeMillis() - startTime);
        metricBatcher.addDatum("AMM", latency);

        final MetricDatum success = newDatum(service, operation, startTime)
                                        .withMetricName("Success")
                                        .withValue(exception == null ? 1.0 : 0.0)
                                        .withUnit(StandardUnit.Count);
        metricBatcher.addDatum("AMM", success);
    }

    protected MetricDatum newDatum(String service, String operation, long startTime) {
        return new MetricDatum().withDimensions(new Dimension().withName("Svc")
                                                               .withValue(service),
                                                new Dimension().withName("Operation")
                                                               .withValue(operation))
                                .withTimestamp(new Date(startTime));
    }

}

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
package com.amediamanager.dao;

import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.amazonaws.services.rds.model.Endpoint;
import com.amediamanager.config.ConfigurationSettings;

/**
 * This custom DriverManagerDataSource retrieves DB connection information from
 * the ConfigurationSettings class. Most im
 * @author evbrown
 *
 */
public class RdsDriverManagerDataSource extends DriverManagerDataSource {
    private static final Logger LOG = LoggerFactory.getLogger(RdsDriverManagerDataSource.class);

    @Autowired
    ConfigurationSettings config;

    @Autowired
    com.amediamanager.dao.challenge.RdsDbEndpointRetriever dbEndpointRetriever;

    @PostConstruct
    public void init() {
        initializeDataSource();
    }

    @Override
    public String getUsername() {
        return config.getProperty(ConfigurationSettings.ConfigProps.RDS_USERNAME);
    }

    @Override
    public String getPassword() {
        return config.getProperty(ConfigurationSettings.ConfigProps.RDS_PASSWORD);
    }

    private void initializeDataSource() {
        // Use the RDS DB and the dbEndpointRetriever to discover the URL of the
        // database. If there
        // are read replicas, set the correct driver and use them.
        final String masterId = config
                .getProperty(ConfigurationSettings.ConfigProps.RDS_INSTANCEID);

        try {
            Endpoint master = dbEndpointRetriever.getMasterDbEndpoint(masterId);
            List<Endpoint> replicas = dbEndpointRetriever
                    .getReadReplicaEndpoints(masterId);

            if (master != null) {
            	LOG.info("Detected RDS Master database");
                StringBuilder builder = new StringBuilder();
                builder.append("jdbc:mysql:");
                if (replicas != null) {
                    builder.append("replication:");
                    super.setDriverClassName("com.mysql.jdbc.ReplicationDriver");
                } else {
                    super.setDriverClassName("com.mysql.jdbc.Driver");
                }

                builder.append("//" + master.getAddress() + ":"
                        + master.getPort());
                if (replicas != null) {
                	LOG.info("Detected RDS Read Replicas");
                    for (Endpoint endpoint : replicas) {
                        builder.append("," + endpoint.getAddress() + ":"
                                + endpoint.getPort());
                    }
                } else {
                	LOG.info("No Read Replicas detected");
                }
                builder.append("/"
                        + config.getProperty(ConfigurationSettings.ConfigProps.RDS_DATABASE));
                String connectionString = builder.toString();
                LOG.info("MySQL Connection String: " + connectionString);
                super.setUrl(connectionString);
            } else {
            	LOG.warn("No RDS master database detected!");
            }
        } catch (Exception e) {
            LOG.warn("Failed to initialize datasource.", e);
        }
    }
}

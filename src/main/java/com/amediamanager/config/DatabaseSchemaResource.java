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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import com.amediamanager.dao.RdsDriverManagerDataSource;

@Component
@Scope(WebApplicationContext.SCOPE_APPLICATION)
public class DatabaseSchemaResource implements ProvisionableResource {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseSchemaResource.class);

    @Autowired
    private RdsDriverManagerDataSource dataSource;

    private static final String name = "RDS Database Schema";
    private ProvisionState provisionState;

    @PostConstruct
    public void checkProvisionedState() {
        try {
            if(this.doesDataSourceExist(VIDEO_TABLE_NAME) &&
                    this.doesDataSourceExist(TAGS_TABLE_NAME) &&
                    this.doesDataSourceExist(VIDEOS_TAGS_TABLE_NAME)) {
                provisionState = ProvisionableResource.ProvisionState.PROVISIONED;
            } else {
                provisionState = ProvisionableResource.ProvisionState.UNPROVISIONED;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error connecting to database");
        }
    }

    @Override
    public ProvisionState getState() {
        return provisionState;
    }

    @Override
    public String getName() {
        return DatabaseSchemaResource.name;
    }

    @Override
    public void provision() {
        this.provisionDataSource(VIDEO_DROP_TABLE, VIDEO_CREATE_TABLE);
        this.provisionDataSource(TAGS_DROP_TABLE, TAGS_CREATE_TABLE);
        this.provisionDataSource(VIDEOS_TAGS_DROP_TABLE, VIDEOS_TAGS_CREATE_TABLE);

        // Refresh provisioned state
        this.checkProvisionedState();
    }

    private Boolean doesDataSourceExist(final String tableName) throws Exception {
        boolean dataSourceExists = false;

        Connection connection = null;
        ResultSet results = null;
        DatabaseMetaData metadata;

        try {
            connection = dataSource.getConnection();
            metadata = connection.getMetaData();
            results = metadata.getTables(null, null, tableName, null);

            dataSourceExists = results.next();
        } catch (Exception e) {
            LOG.error("Failed to check datasource.", e);
            throw e;
        } finally {
            try {
                results.close();
                connection.close();
            } catch (Exception x) {
            }
        }

        return dataSourceExists;
    }

    private void provisionDataSource(final String dropTableQuery, final String createTableQuery) {

        Connection connection = null;
        Statement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();

            statement.executeUpdate(dropTableQuery);
            statement.executeUpdate(createTableQuery);

        } catch (SQLException e) {
            LOG.warn("Failed provisioning datasource", e);
        } finally {
            try {
                statement.close();
                connection.close();
            } catch (Exception x) {
            }
        }
    }

    /** Tags table**/
    public static final String TAGS_TABLE_NAME = "tags";

    private static final String TAGS_CREATE_TABLE = "CREATE TABLE `tags` (" +
            "`tagId` varchar(255) NOT NULL," +
            "`name` varchar(255) NOT NULL," +
              "PRIMARY KEY (`tagId`)," +
              "KEY `ix_tag` (`tagId`)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8";

    public static final String TAGS_DROP_TABLE = "DROP TABLE IF EXISTS " + TAGS_TABLE_NAME;


    /** Video table stuff**/
    public static final String VIDEO_TABLE_NAME = "videos";

    private static final String VIDEO_CREATE_TABLE = "CREATE TABLE `videos` (" +
              "`videoId` varchar(255) NOT NULL," +
              "`transcodeJobId` varchar(255) UNIQUE NULL," +
              "`originalKey` varchar(255) NOT NULL," +
              "`bucket` varchar(255) NOT NULL," +
              "`owner` varchar(255) NOT NULL," +
              "`uploadedDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
              "`privacy` varchar(8) NOT NULL DEFAULT 'PRIVATE'," +
              "`title` varchar(255) DEFAULT NULL," +
              "`description` varchar(255) DEFAULT NULL," +
              "`thumbnailKey` varchar(255) DEFAULT NULL," +
              "`previewKey` varchar(255) DEFAULT NULL," +
              "`createdDate` date DEFAULT NULL," +
              "PRIMARY KEY (`videoId`)," +
              "KEY `ix_tag` (`videoId`)," +
              "UNIQUE KEY `originalKey` (`originalKey`)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";

    public static final String VIDEO_DROP_TABLE = "DROP TABLE IF EXISTS " + VIDEO_TABLE_NAME;


    /** Videos_Tags join tablef**/
    public static final String VIDEOS_TAGS_TABLE_NAME = "videos_tags";

    private static final String VIDEOS_TAGS_CREATE_TABLE = "CREATE TABLE `videos_tags` (" +
              "`tagId` varchar(255) NOT NULL," +
              "`videoId` varchar(255) NOT NULL," +
              "PRIMARY KEY (`tagId`,`videoId`)," +
              "CONSTRAINT FOREIGN KEY (`tagId`) REFERENCES `tags` (`tagId`) ON DELETE CASCADE ON UPDATE CASCADE," +
              "CONSTRAINT FOREIGN KEY (`videoId`) REFERENCES `videos` (`videoId`) ON DELETE CASCADE ON UPDATE CASCADE" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";


    public static final String VIDEOS_TAGS_DROP_TABLE = "DROP TABLE IF EXISTS " + VIDEOS_TAGS_TABLE_NAME;
}

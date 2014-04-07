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
package com.amediamanager.domain;

public enum ContentType {
    
    MP4("video/mp4");

    public static final ContentType[] ALL = { MP4 };
        
    private final String contentType;

    public static ContentType fromName(final String contentType) {
        if (null == contentType) {
            throw new IllegalArgumentException("Null is not a valid value for ContentType");
        }
        if (contentType.toUpperCase().equals("MP4")) {
            return MP4;
        }
        throw new IllegalArgumentException("\"" + contentType + "\" is invalid");
    }
    
    private ContentType(final String contentType) {
        this.contentType = contentType;
    }
    
    public String getContentType() {
        return this.contentType;
    }
    
    @Override
    public String toString() {
        return getContentType();
    }
    
}

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

import java.io.Serializable;

public enum Privacy implements Serializable {
    
    PRIVATE("Private"), 
    PUBLIC("Public"), 
    SHARED("Shared");

    
    public static final Privacy[] ALL = { PRIVATE, PUBLIC, SHARED };
        
    private final String privacyLevel;

    public static Privacy fromName(final String privacyLevel) {
        if (null == privacyLevel) {
            throw new IllegalArgumentException("Null is not a valid value for Privacy");
        }
        if (privacyLevel.toUpperCase().equals("PRIVATE")) {
            return PRIVATE;
        } else if (privacyLevel.toUpperCase().equals("PUBLIC")) {
            return PUBLIC;
        } else if (privacyLevel.toUpperCase().equals("SHARED")) {
            return SHARED;
        }
        throw new IllegalArgumentException("\"" + privacyLevel + "\" is invalid");
    }
    
    private Privacy(final String privacyLevel) {
        this.privacyLevel = privacyLevel;
    }
    
    public String getPrivacyLevel() {
        return this.privacyLevel;
    }
    
    @Override
    public String toString() {
        return getPrivacyLevel();
    }
    
}

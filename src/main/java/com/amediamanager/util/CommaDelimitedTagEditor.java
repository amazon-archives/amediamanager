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
package com.amediamanager.util;

import java.beans.PropertyEditorSupport;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.amediamanager.domain.Tag;

public class CommaDelimitedTagEditor extends PropertyEditorSupport {

	// Convert a string to a Set of Tags
    public void setAsText(String text) {
        Set<Tag> tags = new HashSet<Tag>();
        String[] strings = text.split(",");
        for(int i =0; i < strings.length; i++) {
            String tag = strings[i].trim();
            tags.add(new Tag(tag));
        }
        setValue(tags);
    }
    
    // Convert a tag list to a comma-delimited string
    @SuppressWarnings("unchecked")
	public String getAsText() {
    	String concatenated = new String();
    	if(null != getValue()) {
    		concatenated = StringUtils.join((Set<Tag>)getValue(), ", ");
    	}
    	return concatenated;
    }
}
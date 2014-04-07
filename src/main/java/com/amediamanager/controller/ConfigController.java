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
package com.amediamanager.controller;

import java.beans.PropertyEditorSupport;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.WebApplicationContext;

import com.amazonaws.auth.BasicSessionCredentials;
import com.amediamanager.config.ConfigurationSettings;
import com.amediamanager.config.EditableConfigurationProperties;
import com.amediamanager.config.EditableConfigurationProperty;
import com.amediamanager.config.ProvisionableResource;
import com.amediamanager.config.S3ConfigurationProvider;
import com.amediamanager.config.ToggleableConfigurationProperty;
import com.amediamanager.domain.Privacy;
import com.amediamanager.util.PrivacyEditor;

@Controller
public class ConfigController {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private ConfigurationSettings config;
	
	@Autowired
	private EditableConfigurationProperties editableConfigurationProperties;

	@RequestMapping(value="/config", method = RequestMethod.GET)
	public String config(ModelMap model) {
		config.refreshConfigurationProvider();
		
		model.addAttribute("templateName", "config");
		model.addAttribute("configLoadedFrom", config.getConfigurationProvider().getPrettyName());
		model.addAttribute("appConfig", config.toString());
		model.addAttribute("accessKey", config.getAWSCredentialsProvider().getCredentials().getAWSAccessKeyId());
		model.addAttribute("isToken", config.getAWSCredentialsProvider().getCredentials() instanceof BasicSessionCredentials);

		// Inject info about S3 config
		if(config.getConfigurationProvider() instanceof S3ConfigurationProvider) {
			model.addAttribute("isS3Config", true);
			model.addAttribute("configBucket", ((S3ConfigurationProvider)config.getConfigurationProvider()).getBucket());
			model.addAttribute("configKey", ((S3ConfigurationProvider)config.getConfigurationProvider()).getKey());
		}
		
		// Handle ProvisionableResources
		Map<String, ProvisionableResource> provisionableResources = context.getBeansOfType(ProvisionableResource.class);

		// Let the view know if there are any unprovisioned resources
		Boolean allProvisioned = true;
		for (Map.Entry<String, ProvisionableResource> entry : provisionableResources.entrySet())
		{
		    if(entry.getValue().getState() != ProvisionableResource.ProvisionState.PROVISIONED) {
		    	allProvisioned = false;
		    	break;
		    }
		}
		
		// Set provisionables
		model.addAttribute("allProvisioned", allProvisioned);
		model.addAttribute("prs", provisionableResources);
		
		// Set EditableConfigurationProperties
		editableConfigurationProperties.initialize();
		model.addAttribute("ecp", editableConfigurationProperties);
		
		return "base";
	}

	@RequestMapping(value="/config/provision/{provisionableBeanName}", method=RequestMethod.GET)
	public String provision(ModelMap model, @PathVariable String provisionableBeanName) {
		ProvisionableResource pr = (ProvisionableResource)context.getBean(provisionableBeanName);
		pr.provision();
		return config(model);
	}
	
	@RequestMapping(value="/config/edit", method=RequestMethod.POST)
	public String configure(ModelMap model, @ModelAttribute EditableConfigurationProperties editableConfigurationProperties) {
		for(EditableConfigurationProperty ecp : editableConfigurationProperties.getConfigProps()) {
			config.getConfigurationProvider().persistNewProperty(ecp.getPropertyName(), ecp.getPropertyValue());
		}
		return "redirect:/config";
	}
	
	@InitBinder
	public void initPrivacyBinder(final WebDataBinder dataBinder) {
		// Bind tags
		dataBinder.registerCustomEditor(ConfigurationSettings.ConfigProps.class, new ConfigPropertyEditor());
	}
	
	class ConfigPropertyEditor extends PropertyEditorSupport {
	    public void setAsText(String text) {
	        setValue(ConfigurationSettings.ConfigProps.valueOf(text));
	    }
	}
}

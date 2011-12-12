/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package com.openshift.express.internal.client.request;

import java.net.MalformedURLException;
import java.net.URL;

import com.openshift.express.internal.client.utils.UrlBuilder;

/**
 * @author André Dietisheim
 */
public abstract class AbstractOpenShiftRequest implements IOpenShiftRequest {

	private String rhlogin;
	private boolean debug;
	private String[] optionals;

	public AbstractOpenShiftRequest(String username) {
		this(username, false);
	}

	public AbstractOpenShiftRequest(String username, boolean debug) {
		this.rhlogin = username;
		this.debug = debug;
	}
	
	public AbstractOpenShiftRequest(String username, boolean debug, String[] optionals) {
		this.rhlogin = username;
		this.debug = debug;
		this.optionals = optionals;
	}
	
	public String getRhLogin() {
		return rhlogin;
	}

	public boolean isDebug() {
		return debug;
	}
	
	public String[] getOptionals() {
		return optionals;
	}
	
	public URL getUrl(String baseUrl) throws MalformedURLException {
		return new UrlBuilder(baseUrl).path(getResourcePath()).toUrl();
	}
	
	public String getUrlString(String baseUrl) {
		return new UrlBuilder(baseUrl).path(getResourcePath()).toString();
	}

	protected abstract String getResourcePath();
	
}

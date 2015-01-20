/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package com.openshift3.client.images;

/**
 * ImageUri is an immutable representation of a full image tag in accordance with
 * with Docker conventions [REGISTRYHOST/][USERNAME/]NAME[:TAG]
 */
public class ImageUri {
	
	private static final String LATEST = "latest";
	private String registryHost;
	private String userName;
	private String name;
	private String tag;
	
	public ImageUri(String registryHost, String userName, String name){
		this.registryHost = registryHost;
		this.userName = userName;
		this.name = name;
		this.tag = LATEST;
	}
	public ImageUri(String tag){
		String[] segments = tag.split("/");
		switch (segments.length) {
		case 3:
			registryHost = segments[0];
			userName = segments[1];
			setNameAndTag(segments[2]);
			break;
		case 2:
			userName = segments[0];
			setNameAndTag(segments[1]);
			break;
		default:
			setNameAndTag(segments[0]);
			break;
		}
	}
	
	private void setNameAndTag(String nameAndTag){
		String [] nameTag = nameAndTag.split(":");
		if(nameTag.length == 2){
			name = nameTag[0];
			tag = nameTag[1];
		}
		else{
			name =nameTag[0];
			tag = LATEST;
		}
	}
	
	@Override
	public String toString() {
		return getAbsoluteUri();
	}

	public String getName() {
		return this.name;
	}

	public String getTag() {
		return this.tag;
	}

	public String getUserName() {
		return this.userName;
	}

	public String getRepositoryHost() {
		return this.registryHost;
	}

	public String getAbsoluteUri() {
		return buildUri(registryHost, userName, name, tag);
	}

	public String getBaseUri() {
		return buildUri(null, userName, name, tag);
	}

	public String getUriWithoutTag() {
		return buildUri(registryHost, userName, name, null);
	}
	
	public String getUriWithoutHost() {
		return buildUri(null, userName, name, tag);
	}
	
	private String buildUri(String host, String user, String name, String tag){
		StringBuilder b = new StringBuilder();
		if(host != null) b.append(host).append("/");
		if(user != null) b.append(user).append("/");
		b.append(name);
		if(tag != null)b.append(":").append(tag);
		return b.toString();
	}
	
}

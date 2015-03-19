/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package com.openshift3.client.model;

/**
 * OpenShift route to Service
 */
public interface IRoute extends IResource {

	/**
	 * Retrieves the externally available hostname that can be used to access
	 * service.
	 * 
	 * @return Route hostname.
	 */
	String getHost();

	/**
	 * Sets the externally available hostname that can be used to access
	 * service.
	 * 
	 * @param host
	 *            hostname to use
	 */
	void setHost(String host);

	/**
	 * Retrieves the name of the service this route leads to.
	 * 
	 * @return Name of the service for this route.
	 */
	String getServiceName();

	/**
	 * Sets the name of the service this route should lead to.
	 * 
	 * @param serviceName
	 *            Name of the service this route should lead to.
	 */
	void setServiceName(String serviceName);

}

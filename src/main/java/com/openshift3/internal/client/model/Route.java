/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package com.openshift3.internal.client.model;

import java.util.Map;

import org.jboss.dmr.ModelNode;

import com.openshift3.client.IClient;
import com.openshift3.client.model.IRoute;

public class Route extends KubernetesResource implements IRoute {
	public Route(ModelNode node, IClient client, Map<String, String[]> propertyKeys) {
		super(node, client, propertyKeys);
	}

	@Override
	public String getHost() {
		return asString(ROUTE_HOST);
	}

	@Override
	public void setHost(String host) {
		get(ROUTE_HOST).set(host);
	}

	@Override
	public String getServiceName() {
		return asString(ROUTE_SERVICE_NAME);
	}

	@Override
	public void setServiceName(String serviceName) {
		get(ROUTE_SERVICE_NAME).set(serviceName);
	}

}

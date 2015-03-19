/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package com.openshift3.internal.client.model.properties;

/**
 * Keys used to determine where a given property is for a given resource
 */
public interface ResourcePropertyKeys extends BuildConfigPropertyKeys{
	static final String ANNOTATIONS = "annotations";
	static final String APIVERSION  = "apiversion";
	static final String CREATION_TIMESTAMP = "creationTimestamp";
	static final String LABELS = "labels";
	static final String NAME = "name";
	static final String NAMESPACE = "namespace";
	
	static final String REPLICATION_CONTROLLER_CONTAINERS = "replicationcontroller.containers";
	static final String REPLICATION_CONTROLLER_REPLICA_COUNT = "replicationcontroller.replicacount";
	static final String REPLICATION_CONTROLLER_CURRENT_REPLICA_COUNT = "replicationcontroller.replicacount.current";
	static final String REPLICATION_CONTROLLER_REPLICA_SELECTOR = "replicationcontroller.selector";
	
	static final String SERVICE_CONTAINER_PORT = "service.containerport";
	static final String SERVICE_PORT = "service.port";
	static final String SERVICE_SELECTOR = "service.selector";
	static final String SERVICE_PORTALIP = "service.portalIP";
	
	static final String STATUS_MESSAGE = "status.message";
	
	static final String BUILD_STATUS	= "build.status";
	static final String BUILD_MESSAGE	= "build.message";
	static final String BUILD_PODNAME	= "build.podname";
	
	static final String DEPLOYMENTCONFIG_CONTAINERS = "deploymentconfig.containers";
	static final String DEPLOYMENTCONFIG_REPLICAS = "deploymentconfig.replicas";
	static final String DEPLOYMENTCONFIG_REPLICA_SELECTOR = "deploymentconfig.replica.selector";
	static final String DEPLOYMENTCONFIG_TRIGGERS = "deploymentconfig.triggers";
	static final String IMAGEREPO_DOCKER_IMAGE_REPO = "imagerepo.dockerimagerepo";
	static final String PROJECT_DISPLAY_NAME = "project.displayname";
	static final String ROUTE_HOST = "route.host";
	static final String ROUTE_SERVICE_NAME = "route.serviceName";
	
	static final String POD_CONTAINERS = "pod.containers";
	static final String POD_HOST = "pod.host";
	static final String POD_IP = "pod.ip";
	static final String POD_STATUS = "pod.status";
}

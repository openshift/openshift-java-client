/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package com.openshift.internal.client;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.openshift.internal.client.response.ResourceDTOFactoryTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	ConfigurationTest.class,
	HttpClientTest.class,
	RestServicePropertiesTest.class,
	RestServiceTest.class,
	ResourceDTOFactoryTest.class,
	DomainResourceTest.class,
	ApplicationResourceTest.class,
	CartridgesTest.class,
	EmbeddableCartridgeTest.class,
	LatestVersionSelectorTest.class,
	UserTest.class,
	SSHKeyTest.class,
	GearGroupsResourceTest.class,
	GearTest.class,
	OpenShiftExceptionTest.class
})

/**
 * @author André Dietisheim
 */
public class OpenShiftTestSuite {

}

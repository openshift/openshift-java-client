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
package com.openshift.express.internal.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import com.openshift.express.client.IApplication;
import com.openshift.express.client.ICartridge;
import com.openshift.express.client.IEmbeddableCartridge;
import com.openshift.express.client.IOpenShiftService;
import com.openshift.express.client.OpenShiftException;
import com.openshift.express.client.OpenShiftService;
import com.openshift.express.client.User;
import com.openshift.express.client.configuration.DefaultConfiguration;
import com.openshift.express.client.configuration.SystemConfiguration;
import com.openshift.express.client.configuration.UserConfiguration;
import com.openshift.express.internal.client.test.fakes.TestUser;
import com.openshift.express.internal.client.test.utils.ApplicationUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author André Dietisheim
 */
public class EmbedIntegrationTest {

	private IOpenShiftService service;
	private User user;
	private IApplication application;

	@Before
	public void setUp() throws OpenShiftException, IOException {
		UserConfiguration userConfiguration = new UserConfiguration(new SystemConfiguration(new DefaultConfiguration()));
		this.service = new OpenShiftService(TestUser.ID, userConfiguration.getLibraServer());
		this.user = new TestUser();
		this.application = service.createApplication(ApplicationUtils.createRandomApplicationName(), ICartridge.JBOSSAS_7, user);
	}

	@After
	public void tearDown() {
		ApplicationUtils.silentlyDestroyAS7Application(application.getName(), user, service);
	}
	
	@Test
	public void canEmbedMySQL() throws Exception {
		assertNull(application.getEmbeddedCartridge());
		IEmbeddableCartridge mysqlCartridge = IEmbeddableCartridge.MYSQL_51;
		IApplication returnedApplication = service.addEmbeddableCartridge(application, mysqlCartridge , user);
		assertNotNull(returnedApplication);
		assertEquals(application.getName(), returnedApplication.getName());
		assertEquals(mysqlCartridge, returnedApplication.getEmbeddedCartridge());
	}
}

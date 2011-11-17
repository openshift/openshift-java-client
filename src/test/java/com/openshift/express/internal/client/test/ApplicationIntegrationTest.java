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

import static com.openshift.express.internal.client.test.utils.ApplicationAsserts.assertAppliactionUrl;
import static com.openshift.express.internal.client.test.utils.ApplicationAsserts.assertGitUri;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;

import com.openshift.express.client.IApplication;
import com.openshift.express.client.ICartridge;
import com.openshift.express.client.IOpenShiftService;
import com.openshift.express.client.InvalidCredentialsOpenShiftException;
import com.openshift.express.client.OpenShiftException;
import com.openshift.express.client.OpenShiftService;
import com.openshift.express.client.User;
import com.openshift.express.client.configuration.DefaultConfiguration;
import com.openshift.express.client.configuration.SystemConfiguration;
import com.openshift.express.client.configuration.UserConfiguration;
import com.openshift.express.internal.client.ApplicationInfo;
import com.openshift.express.internal.client.UserInfo;
import com.openshift.express.internal.client.test.fakes.TestUser;
import com.openshift.express.internal.client.test.utils.ApplicationUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * @author André Dietisheim
 */
public class ApplicationIntegrationTest {

	private IOpenShiftService service;

	private User user;
	private User invalidUser;
	
	@Before
	public void setUp() throws OpenShiftException, IOException {
		UserConfiguration userConfiguration = new UserConfiguration(new SystemConfiguration(new DefaultConfiguration()));
		this.service = new OpenShiftService(TestUser.ID, userConfiguration.getLibraServer());
		this.user = new TestUser();
		this.invalidUser = new TestUser("bogusPassword");
	}

	@Test(expected = InvalidCredentialsOpenShiftException.class)
	public void createApplicationWithInvalidCredentialsThrowsException() throws Exception {
		service.createApplication(ApplicationUtils.createRandomApplicationName(), ICartridge.JBOSSAS_7, invalidUser);
	}

	@Test
	public void canCreateApplication() throws Exception {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			ICartridge cartridge = ICartridge.JBOSSAS_7;
			IApplication application = service.createApplication(applicationName, cartridge, user);
			assertNotNull(application);
			assertEquals(applicationName, application.getName());
			assertEquals(cartridge, application.getCartridge());
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, user, service);
		}
	}

	@Test
	public void canDestroyApplication() throws Exception {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		service.createApplication(applicationName, ICartridge.JBOSSAS_7, user);
		service.destroyApplication(applicationName, ICartridge.JBOSSAS_7, user);
	}

	@Test(expected = OpenShiftException.class)
	public void createDuplicateApplicationThrowsException() throws Exception {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			service.createApplication(applicationName, ICartridge.JBOSSAS_7, user);
			service.createApplication(applicationName, ICartridge.JBOSSAS_7, user);
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, user, service);
		}
	}

	@Test
	public void canStopApplication() throws Exception {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			service.createApplication(applicationName, ICartridge.JBOSSAS_7, user);
			service.stopApplication(applicationName, ICartridge.JBOSSAS_7, user);
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, user, service);
		}
	}

	@Test
	public void canStartStoppedApplication() throws Exception {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			service.createApplication(applicationName, ICartridge.JBOSSAS_7, user);
			service.stopApplication(applicationName, ICartridge.JBOSSAS_7, user);
			service.startApplication(applicationName, ICartridge.JBOSSAS_7, user);
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, user, service);
		}
	}

	@Test
	public void canStartStartedApplication() throws Exception {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			/**
			 * freshly created apps are started
			 * 
			 * @link 
			 *       https://github.com/openshift/os-client-tools/blob/master/express
			 *       /doc/API
			 */
			service.createApplication(applicationName, ICartridge.JBOSSAS_7, user);
			service.startApplication(applicationName, ICartridge.JBOSSAS_7, user);
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, user, service);
		}
	}

	@Test
	public void canStopStoppedApplication() throws Exception {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			/**
			 * freshly created apps are started
			 * 
			 * @link 
			 *       https://github.com/openshift/os-client-tools/blob/master/express
			 *       /doc/API
			 */
			service.createApplication(applicationName, ICartridge.JBOSSAS_7, user);
			service.stopApplication(applicationName, ICartridge.JBOSSAS_7, user);
			service.stopApplication(applicationName, ICartridge.JBOSSAS_7, user);
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, user, service);
		}
	}

	@Test
	public void canRestartApplication() throws Exception {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			/**
			 * freshly created apps are started
			 * 
			 * @link 
			 *       https://github.com/openshift/os-client-tools/blob/master/express
			 *       /doc/API
			 */
			service.createApplication(applicationName, ICartridge.JBOSSAS_7, user);
			service.restartApplication(applicationName, ICartridge.JBOSSAS_7, user);
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, user, service);
		}
	}

	@Test
	public void canGetStatus() throws Exception {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			IApplication application = service.createApplication(applicationName, ICartridge.JBOSSAS_7, user);
			String applicationStatus = service.getStatus(application.getName(), application.getCartridge(), user);
			assertNotNull(applicationStatus);
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, user, service);
		}
	}

	@Test
	public void returnsValidGitUri() throws Exception {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			IApplication application = service.createApplication(applicationName, ICartridge.JBOSSAS_7, user);
			String gitUri = application.getGitUri();
			assertNotNull(gitUri);
			assertGitUri(applicationName, gitUri);
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, user, service);
		}
	}

	@Test
	public void returnsValidApplicationUrl() throws Exception {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			IApplication application = service.createApplication(applicationName, ICartridge.JBOSSAS_7, user);
			String applicationUrl = application.getApplicationUrl();
			assertNotNull(applicationUrl);
			assertAppliactionUrl(applicationName, applicationUrl);
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, user, service);
		}
	}

	@Test
	public void returnsCreationTime() throws Exception {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			IApplication application = service.createApplication(applicationName, ICartridge.JBOSSAS_7, user);
			Date creationTime = application.getCreationTime();
			assertNotNull(creationTime);
			assertTrue(creationTime.compareTo(new Date()) == -1);
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, user, service);
		}
	}

	/**
	 * This tests checks if the creation time is returned in the 2nd
	 * application. The creation time is only available in the
	 * {@link ApplicationInfo} which is held by the {@link UserInfo}. The
	 * UserInfo is fetched when the 1st application is created and then stored.
	 * The 2nd application has therefore to force the user to refresh the user
	 * info.
	 * 
	 * @throws Exception
	 * 
	 * @see UserInfo
	 * @see ApplicationInfo
	 */
	@Test
	public void returnsCreationTimeOn2ndApplication() throws Exception {
		String applicationName = null;
		String applicationName2 = null;
		try {
			applicationName = ApplicationUtils.createRandomApplicationName();
			IApplication application = user.createApplication(applicationName, ICartridge.JBOSSAS_7);
			Date creationTime = application.getCreationTime();
			assertNotNull(creationTime);
			applicationName2 = ApplicationUtils.createRandomApplicationName();
			IApplication application2 = user.createApplication(applicationName2, ICartridge.JBOSSAS_7);
			Date creationTime2 = application2.getCreationTime();
			assertNotNull(creationTime2);
			assertTrue(creationTime.compareTo(creationTime2) == -1);
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, user, service);
			ApplicationUtils.silentlyDestroyAS7Application(applicationName2, user, service);
		}
	}
}

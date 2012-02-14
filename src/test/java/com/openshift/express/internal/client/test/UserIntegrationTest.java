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

import static com.openshift.express.internal.client.test.utils.ApplicationAsserts.assertApplication;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;

import com.openshift.express.client.IApplication;
import com.openshift.express.client.ICartridge;
import com.openshift.express.client.IDomain;
import com.openshift.express.client.IOpenShiftService;
import com.openshift.express.client.ISSHPublicKey;
import com.openshift.express.client.NotFoundOpenShiftException;
import com.openshift.express.client.OpenShiftEndpointException;
import com.openshift.express.client.OpenShiftException;
import com.openshift.express.client.OpenShiftService;
import com.openshift.express.client.User;
import com.openshift.express.client.configuration.DefaultConfiguration;
import com.openshift.express.client.configuration.SystemConfiguration;
import com.openshift.express.client.configuration.UserConfiguration;
import com.openshift.express.internal.client.test.fakes.TestUser;
import com.openshift.express.internal.client.test.utils.ApplicationUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * @author André Dietisheim
 */
public class UserIntegrationTest {

	private User user;
	private TestUser invalidUser;
	private TestUser badUrlUser;
	private TestUser userWithoutDomain;
	private IOpenShiftService service;

	@Before
	public void setUp() throws OpenShiftException,
			DatatypeConfigurationException, IOException {
		UserConfiguration userConfiguration = new UserConfiguration(new SystemConfiguration(new DefaultConfiguration()));
		service = new OpenShiftService(TestUser.ID, userConfiguration.getLibraServer());
		service.setEnableSSLCertChecks(Boolean.parseBoolean(System.getProperty("enableSSLCertChecks")));
		this.user = new TestUser(service);
		this.invalidUser = new TestUser("bogusPassword", service);
		this.badUrlUser = new TestUser(System.getProperty("RHLOGIN"), System.getProperty("PASSWORD"),
				"http://www.redhat.com", service);
		this.userWithoutDomain = new TestUser(
				TestUser.RHLOGIN_USER_WITHOUT_DOMAIN,
				TestUser.PASSWORD_USER_WITHOUT_DOMAIN, service);
	}

	@Test
	public void canCheckIfUserIsValid() throws OpenShiftException {
		assertTrue(user.isValid());
	}

	// @Test
	public void throwsExceptionIfInvalidCredentials() throws OpenShiftException {
		assertFalse(invalidUser.isValid());
	}

	@Test(expected = NotFoundOpenShiftException.class)
	public void throwsExceptionIfBadUrl() throws OpenShiftException {
		badUrlUser.isValid();
	}

	@Test
	public void canGetUserUUID() throws OpenShiftException {
		String uuid = user.getUUID();
		assertNotNull(uuid);
		assertTrue(uuid.length() > 0);
	}

	@Test
	public void canGetPublicKey() throws OpenShiftException {
		ISSHPublicKey key = user.getSshKey();
		assertNotNull(key);
		assertNotNull(key.getPublicKey());
		assertTrue(key.getPublicKey().length() > 0);
	}

	@Test
	public void canGetDomain() throws OpenShiftException {
		IDomain domain = user.getDomain();
		assertNotNull(domain);
		assertNotNull(domain.getRhcDomain());
		assertTrue(domain.getRhcDomain().length() > 0);
		assertNotNull(domain.getNamespace());
		assertTrue(domain.getNamespace().length() > 0);
	}

	@Test(expected = OpenShiftEndpointException.class)
	public void cannotCreateDomainIfAlreadyExists() throws OpenShiftException {
		IDomain domain = user.getDomain();
		assertNotNull(domain);
		ISSHPublicKey key = user.getSshKey();
		assertNotNull(key);
		user.createDomain("newDomain", key);
	}

	@Test
	public void getFalseIfNoDomainPresent() throws OpenShiftException {
		Boolean hasDomain = userWithoutDomain.hasDomain();
		assertFalse(hasDomain);
	}
	
	@Test
	public void getNullIfNoDomainPresent() throws OpenShiftException {
		IDomain domain = userWithoutDomain.getDomain();
		assertNull(domain);
	}

	@Test
	public void canGetCartridges() throws OpenShiftException {
		Collection<ICartridge> cartridges = user.getCartridges();
		assertNotNull(cartridges);
		assertTrue(cartridges.size() >= 5);
	}

	@Test
	public void canGetApplications() throws OpenShiftException {
		Collection<IApplication> applications = user.getApplications();
		assertNotNull(applications);
	}

	@Test
	public void canCreateApplication() throws OpenShiftException, IOException {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			Collection<IApplication> applications = user.getApplications();
			assertNotNull(applications);
			int numOfApplications = applications.size();
			IApplication application = user.createApplication(applicationName,
					ICartridge.JBOSSAS_7);
			assertEquals(numOfApplications + 1, applications.size());
			assertApplication(applicationName, ICartridge.JBOSSAS_7, application);
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName,
					user,
					service);
		}
	}

	@Test
	public void canGetApplicationByName()
			throws OpenShiftException, DatatypeConfigurationException, IOException {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			IApplication application = user.createApplication(applicationName,
					ICartridge.JBOSSAS_7);
			IApplication applicationFound = user
					.getApplicationByName(applicationName);
			assertNotNull(applicationFound);
			assertEquals(application, applicationFound);
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName,
					user,
					service);
		}
	}

	@Test
	public void canGetApplicationByCartridge()
			throws OpenShiftException, DatatypeConfigurationException, IOException {
		int currentAs7Apps = user.getApplicationsByCartridge(ICartridge.JBOSSAS_7).size();
		List<IApplication> toRemove = new ArrayList<IApplication>();
		try {
			IApplication application1 = user.createApplication(
					ApplicationUtils.createRandomApplicationName()
					, ICartridge.JBOSSAS_7);
			toRemove.add(application1);
			IApplication application2 = user.createApplication(
					ApplicationUtils.createRandomApplicationName()
					, ICartridge.JENKINS_14);
			toRemove.add(application2);

			List<IApplication> applicationsFound =
					user.getApplicationsByCartridge(ICartridge.JBOSSAS_7);
			assertNotNull(applicationsFound);
			assertEquals(currentAs7Apps + 1, applicationsFound.size());
		} finally {
			for (IApplication application : toRemove) {
				ApplicationUtils.silentlyDestroyApplication(
						application.getName(),
						application.getCartridge(),
						user, service);
			}
		}
	}

}

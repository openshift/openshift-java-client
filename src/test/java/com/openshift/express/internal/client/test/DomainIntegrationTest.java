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

import java.io.IOException;

import com.openshift.express.client.IDomain;
import com.openshift.express.client.OpenShiftException;
import com.openshift.express.client.OpenShiftService;
import com.openshift.express.client.SSHKeyPair;
import com.openshift.express.client.configuration.DefaultConfiguration;
import com.openshift.express.client.configuration.SystemConfiguration;
import com.openshift.express.client.configuration.UserConfiguration;
import com.openshift.express.internal.client.test.fakes.TestSSHKey;
import com.openshift.express.internal.client.test.fakes.TestUser;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class DomainIntegrationTest {

	private OpenShiftService openShiftService;
	private TestUser user;

	@Before
	public void setUp() throws OpenShiftException, IOException {
		UserConfiguration userConfiguration = new UserConfiguration(new SystemConfiguration(new DefaultConfiguration()));
		this.openShiftService = new OpenShiftService(TestUser.ID, userConfiguration.getLibraServer());
		openShiftService.setIgnoreCertCheck(Boolean.parseBoolean(System.getProperty("ignoreCertCheck")));
		this.user = new TestUser(openShiftService);
	}

	@Ignore
	@Test
	public void canCreateDomain() throws Exception {
		String domainName = createRandomString();
		SSHKeyPair sshKey = TestSSHKey.create();
		IDomain domain = openShiftService.createDomain(domainName, sshKey, user);

		assertNotNull(domain);
		assertEquals(domainName, domain.getNamespace());
	}

	@Test
	public void canChangeDomain() throws Exception {
		String domainName = createRandomString();
		SSHKeyPair sshKey = TestSSHKey.create();
		IDomain domain = openShiftService.changeDomain(domainName, sshKey, user);

		assertNotNull(domain);
		assertEquals(domainName, domain.getNamespace());
	}

	@Test
	public void canSetNamespaceOnDomain() throws Exception {
		IDomain domain = user.getDomain();
		assertNotNull(domain);
		String newDomainName = createRandomString();
		domain.setNamespace(newDomainName);
		assertEquals(newDomainName, domain.getNamespace());
	}

	private String createRandomString() {
		return String.valueOf(System.currentTimeMillis());
	}
}

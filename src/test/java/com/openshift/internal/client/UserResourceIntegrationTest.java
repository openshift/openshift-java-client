/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package com.openshift.internal.client;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;

import org.junit.Before;
import org.junit.Test;

import com.openshift.client.IDomain;
import com.openshift.client.IUser;
import com.openshift.client.InvalidCredentialsOpenShiftException;
import com.openshift.client.OpenShiftException;
import com.openshift.client.utils.DomainTestUtils;
import com.openshift.client.utils.OpenShiftTestConfiguration;
import com.openshift.client.utils.TestConnectionFactory;

/**
 * @author André Dietisheim
 */
public class UserResourceIntegrationTest {

	private IUser user;

	@Before
	public void setUp() throws OpenShiftException, DatatypeConfigurationException, IOException {
		this.user = new TestConnectionFactory().getConnection().getUser();
	}

	@Test(expected = InvalidCredentialsOpenShiftException.class)
	public void shouldThrowIfInvalidCredentials() throws Exception {
		new TestConnectionFactory().getConnection(
				new OpenShiftTestConfiguration().getClientId(), "bogus-password").getUser();	
	}

	@Test
	public void shouldReturnDomains() throws OpenShiftException {
		// precondition
		DomainTestUtils.ensureHasDomain(user);
		// operation
		List<IDomain> domains = user.getDomains();
		// verification
		assertThat(domains).isNotEmpty();
	}

	@Test
	public void shouldReturnNoDomains() throws OpenShiftException {
		// precondition
		DomainTestUtils.silentlyDestroyAllDomains(user);
		// operation
		List<IDomain> domains = user.getDomains();
		// verification
		assertThat(domains).isEmpty();
	}

	@Test
	public void shouldCreateDomain() throws OpenShiftException {
		// pre-condition
		// cannot create domain if there's already one
		DomainTestUtils.silentlyDestroyAllDomains(user);
		
		// operation
		String id = DomainTestUtils.createRandomName();
		IDomain domain = user.createDomain(id);

		// verification
		assertThat(domain.getId()).isEqualTo(id);
	}

	@Test
	public void shouldReturnDomainByName() throws OpenShiftException {
		// pre-condition
		IDomain domain = DomainTestUtils.ensureHasDomain(user);

		// operation
		IDomain domainByNamespace = user.getDomain(domain.getId());

		// verification
		assertThat(domainByNamespace.getId()).isEqualTo(domain.getId());
	}

	@Test
	public void shouldGetDefaultDomain() throws OpenShiftException {
		// precondition
		DomainTestUtils.ensureHasDomain(user);
		// operation
		IDomain domain = user.getDefaultDomain();
		// verification
		assertNotNull(domain);
		assertNotNull(domain.getId());
		assertTrue(domain.getId().length() > 0);
		assertNotNull(domain.getSuffix());
		assertTrue(domain.getSuffix().length() > 0);
	}

	@Test
	public void shouldReturnThatHasDomain() throws OpenShiftException {
		// precondition
		DomainTestUtils.ensureHasDomain(user);
		// operation
		Boolean hasDomain = user.hasDomain();
		// verification
		assertTrue(hasDomain);
	}

	@Test
	public void shouldReturnThatHasNoDomain() throws OpenShiftException {
		// precondition
		DomainTestUtils.silentlyDestroyAllDomains(user);
		// operation
		Boolean hasDomain = user.hasDomain();
		// verification
		assertFalse(hasDomain);
	}

	@Test
	public void shouldReturnThatHasNamedDomain() throws OpenShiftException {
		// precondition
		IDomain domain = DomainTestUtils.getFirstDomainOrCreate(user);
		// operation
		Boolean hasDomain = user.hasDomain(domain.getId());
		// verification
		assertTrue(hasDomain);
	}

	@Test
	public void shouldReturnThatHasntNamedDomain() throws OpenShiftException {
		// precondition
		// operation
		Boolean hasDomain = user.hasDomain(DomainTestUtils.createRandomName());
		// verification
		assertFalse(hasDomain);
	}


	@Test
	public void shouldReturnEmptyDomains() throws OpenShiftException {
		// precondition
		DomainTestUtils.silentlyDestroyAllDomains(user);
		// operation
		List<IDomain> domains = user.getDomains();
		// verification
		assertThat(domains).isEmpty();
	}

	@Test
	public void shouldNoDefaultDomainAfterRefresh() throws OpenShiftException, FileNotFoundException, IOException {
		// precondition
		IDomain domain = DomainTestUtils.getFirstDomainOrCreate(user);
		assertNotNull(user.getDefaultDomain());
		assertNotNull(domain);

		IUser otherUser = new TestConnectionFactory().getConnection().getUser();
		DomainTestUtils.silentlyDestroyAllDomains(otherUser);
		assertNull(otherUser.getDefaultDomain());
		
		// operation
		user.refresh();

		// verification
		assertNull(user.getDefaultDomain());
	}

}

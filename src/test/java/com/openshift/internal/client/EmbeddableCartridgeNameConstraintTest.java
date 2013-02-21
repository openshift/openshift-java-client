/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.openshift.client.ICartridgeConstraint;
import com.openshift.client.IEmbeddableCartridge;
import com.openshift.client.IEmbeddedCartridge;

/**
 * @author Andre Dietisheim
 */
public class EmbeddableCartridgeNameConstraintTest {

	@Test
	public void shouldEqualsOtherCartridgeConstraint() {
		// pre-coniditions
		// operation
		// verification
		assertEquals(new LatestVersionOfName<IEmbeddableCartridge>("redhat"),
				new LatestVersionOfName<IEmbeddableCartridge>("redhat"));
		assertFalse(new LatestVersionOfName<IEmbeddableCartridge>("redhat").equals(
				new LatestVersionOfName<IEmbeddableCartridge>("jboss")));
	}

	@Test
	public void shouldMatchMysql() {
		// pre-coniditions
		String mysqlCartridgeName = "mysql-5.1";
		List<IEmbeddedCartridge> embeddedCartridges = Arrays.asList(createEmbeddedCartridgeMock(mysqlCartridgeName));
		
		LatestVersionOfName<IEmbeddedCartridge> cartridgeConstraint = new LatestVersionOfName<IEmbeddedCartridge>("mysql");

		// operation
		List<IEmbeddedCartridge> matchingCartridges = cartridgeConstraint.getMatching(embeddedCartridges);

		// verification
		assertThat(matchingCartridges.size()).isEqualTo(1);
		assertThat(matchingCartridges.get(0).getName()).isEqualTo(mysqlCartridgeName);
	}

	@Test
	public void shouldMatchLatestMysql() {
		// pre-coniditions
		String mysql51Name = "mysql-5.1";
		List<IEmbeddedCartridge> embeddedCartridges = Arrays.asList(
				createEmbeddedCartridgeMock(mysql51Name),
				createEmbeddedCartridgeMock("mysql-5.0")
		);

		LatestVersionOfName<IEmbeddedCartridge> cartridgeFilter = new LatestVersionOfName<IEmbeddedCartridge>("mysql");

		// operation
		List<IEmbeddedCartridge> matchingCartridges = cartridgeFilter.getMatching(embeddedCartridges);

		// verification
		assertThat(matchingCartridges.size()).isEqualTo(1);
		assertThat(matchingCartridges.get(0).getName()).isEqualTo(mysql51Name);
	}

	@Test
	public void shouldMatchMajorVersionedCartridge() {
		// pre-coniditions
		String cartridgeName = "jbossas-7";
		List<IEmbeddedCartridge> embeddedCartridges = Arrays.asList(
				createEmbeddedCartridgeMock(cartridgeName),
				createEmbeddedCartridgeMock("mysql")
		);
		ICartridgeConstraint<IEmbeddedCartridge> cartridgeConstraint = new LatestVersionOfName<IEmbeddedCartridge>("jboss");

		// operation
		Collection<IEmbeddedCartridge> matchingCartridges = cartridgeConstraint.getMatching(embeddedCartridges);

		// verification
		assertThat(matchingCartridges.size()).isEqualTo(1);
		assertThat(matchingCartridges.iterator().next().getName()).isEqualTo(cartridgeName);
	}

	@Test
	public void shouldMatchAlphanumericVersionedCartridge() {
		// pre-coniditions
		String cartridgeName = "somecartridge-7b";
		List<IEmbeddedCartridge> embeddedCartridges = Arrays.asList(
				createEmbeddedCartridgeMock(cartridgeName),
				createEmbeddedCartridgeMock("mysql-5.0")
		);
		ICartridgeConstraint<IEmbeddedCartridge> cartridgeConstraint = new LatestVersionOfName<IEmbeddedCartridge>("some");

		// operation
		Collection<IEmbeddedCartridge> matchingCartridges = cartridgeConstraint.getMatching(embeddedCartridges);

		// verification
		assertThat(matchingCartridges.size()).isEqualTo(1);
		assertThat(matchingCartridges.iterator().next().getName()).isEqualTo(cartridgeName);
	}

	private IEmbeddedCartridge createEmbeddedCartridgeMock(String name) {
		IEmbeddedCartridge mock = mock(IEmbeddedCartridge.class);
		when(mock.getName()).thenReturn(name);
		return mock;
	}
}

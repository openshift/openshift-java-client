/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 *
 * Contributors: 
 * Sean Kavanagh - initial API and implementation 
 ******************************************************************************/
package com.openshift.internal.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.openshift.client.IAuthorization;
import com.openshift.client.IOpenShiftConnection;
import com.openshift.client.IUser;
import com.openshift.client.OpenShiftException;
import com.openshift.client.utils.TestConnectionFactory;
import com.openshift.internal.client.httpclient.HttpClientException;

/**
 * @author Andre Dietisheim
 */
public class AuthorizationIntegrationTest extends TestTimer {

	// TODO: add tests for expired tokens
	private IUser user;

	@Before
	public void setUp() throws HttpClientException, OpenShiftException, IOException {
		final IOpenShiftConnection connection =
				new TestConnectionFactory().getConnection();
		this.user = connection.getUser();
	}
	
	@Test
	public void shouldCreateGenericAuthorization() throws Exception {
		// pre-conditions
		IAuthorization authorization = user.getAuthorization();
		assertNotNull(authorization.getToken());
		assertEquals(authorization.getScopes(), IAuthorization.SCOPE_SESSION);

		// operations
		IOpenShiftConnection connection =
				new TestConnectionFactory().getAuthTokenConnection(authorization.getToken());
		authorization = connection.getUser().getAuthorization();
		
		// verifications
		assertEquals(authorization.getScopes(), IAuthorization.SCOPE_SESSION);

		authorization.destroy();
	}

	@Test
	public void shouldCreateAuthorization() throws Exception {
		// pre-conditions
		IAuthorization authorization = user.createAuthorization("my note", IAuthorization.SCOPE_SESSION_READ);
		assertNotNull(authorization.getToken());
		assertEquals(authorization.getScopes(), IAuthorization.SCOPE_SESSION_READ);

		// operations
		IOpenShiftConnection connection =
				new TestConnectionFactory().getAuthTokenConnection(authorization.getToken());
		authorization = connection.getUser().getAuthorization();

		// verifications
		assertEquals(authorization.getScopes(), IAuthorization.SCOPE_SESSION_READ);
		assertEquals(authorization.getNote(), "my note");

		authorization.destroy();
	}

	@Test
	public void shouldCreateAuthorizationWithExpiration() throws Exception {
		// pre-conditions
		IAuthorization authorization = user.createAuthorization("my note", IAuthorization.SCOPE_SESSION_READ, 600);
		assertNotNull(authorization.getToken());
		assertEquals(authorization.getScopes(), IAuthorization.SCOPE_SESSION_READ);

		// operations
		IOpenShiftConnection connection =
				new TestConnectionFactory().getAuthTokenConnection(authorization.getToken());

		authorization = connection.getUser().getAuthorization();

		// verifications
		assertEquals(authorization.getScopes(), IAuthorization.SCOPE_SESSION_READ);
		assertEquals(authorization.getNote(), "my note");
		assertEquals(authorization.getExpiresIn(), 600);
		
		authorization.destroy();
	}

	@Test
	public void shouldReplaceExistingAuthorization() throws Exception {
		// pre-conditions
		IAuthorization authorization = user.createAuthorization("my note", IAuthorization.SCOPE_SESSION_READ, 600);
		assertNotNull(authorization.getToken());
		assertEquals(authorization.getScopes(), IAuthorization.SCOPE_SESSION_READ);

		// operations
		user.createAuthorization("new note", IAuthorization.SCOPE_SESSION);
		IAuthorization newAuthorization = user.getAuthorization();
		
		// verifications
		assertFalse(authorization.equals(newAuthorization));
		assertEquals(newAuthorization.getScopes(), IAuthorization.SCOPE_SESSION);
		assertFalse(authorization.getToken().equals(newAuthorization.getToken()));
		assertEquals(newAuthorization.getNote(), "new note");
		assertTrue(newAuthorization.getExpiresIn() != 600);
		
		// cleanup
		authorization.destroy();
		newAuthorization.destroy();
	}
    
    @Test
    public void shouldGetAuthorizationById() throws Exception {
        // pre-conditions
        IAuthorization authorization = user.createAuthorization("my note", IAuthorization.SCOPE_SESSION_READ, 600);


        // operations
        user.getAuthorization(authorization.getId());
        IAuthorization verifyAuthorization = user.getAuthorization();

        // verifications
        assertEquals(verifyAuthorization.getScopes(), authorization.getScopes());
        assertEquals(verifyAuthorization.getNote(), authorization.getNote());
        assertTrue((verifyAuthorization.getExpiresIn() <= authorization.getExpiresIn()));

        // cleanup
        authorization.destroy();
        verifyAuthorization.destroy();
    }

    @Test
    public void shouldGetAuthorizationByToken() throws Exception {
        // pre-conditions
        IAuthorization authorization = user.createAuthorization("my note", IAuthorization.SCOPE_SESSION_READ, 600);


        // operations
        user.getAuthorization(authorization.getToken());
        IAuthorization verifyAuthorization = user.getAuthorization();

        // verifications
        assertEquals(verifyAuthorization.getScopes(), authorization.getScopes());
        assertEquals(verifyAuthorization.getNote(), authorization.getNote());
        assertTrue((verifyAuthorization.getExpiresIn()<= authorization.getExpiresIn()));

        // cleanup
        authorization.destroy();
        verifyAuthorization.destroy();
    }

    @Test
    public void shouldGetAuthorizationList() throws Exception {
        // pre-conditions
        IAuthorization authorization = user.createAuthorization("my note", IAuthorization.SCOPE_SESSION_READ, 600);


        // operations
        Collection<IAuthorization> authList = user.getAuthorizations();

        // verifications
        assertTrue(authList!=null);
        assertTrue(authList.size() > 0);

        // cleanup
        authorization.destroy();
    }

    @Test
    public void shouldRemoveAuthorizationById() throws Exception {
        // pre-conditions
        IAuthorization authorization = user.createAuthorization("my note", IAuthorization.SCOPE_SESSION_READ, 600);
        String id=authorization.getId();

        // operations
        boolean success = user.removeAuthorization(id);

        // verifications
        assertTrue(success);
        assertNull(user.getAuthorization(id));


    }

    @Test
    public void shouldRemoveAuthorizationByToken() throws Exception {
        // pre-conditions
        IAuthorization authorization = user.createAuthorization("my note", IAuthorization.SCOPE_SESSION_READ, 600);
        String token=authorization.getToken();

        // operations
        boolean success = user.removeAuthorization(token);

        // verifications
        assertTrue(success);
        assertNull(user.getAuthorization(token));


    }
}

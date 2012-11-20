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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.openshift.client.IHttpClient;
import com.openshift.client.OpenShiftException;
import com.openshift.client.fakes.HttpClientFake;
import com.openshift.client.fakes.HttpServerFake;
import com.openshift.client.utils.Base64Coder;
import com.openshift.internal.client.httpclient.HttpClientException;
import com.openshift.internal.client.httpclient.UrlConnectionHttpClientBuilder;

/**
 * @author Andre Dietisheim
 */
public class HttpClientTest {

	private static final String ACCEPT_APPLICATION_JSON = "Accept: application/json";

	private static final Pattern AUTHORIZATION_PATTERN = Pattern.compile("Authorization: Basic ([^\n]*)");

	private HttpServerFake serverFake;
	private IHttpClient httpClient;

	@Before
	public void setUp() throws IOException {
		int port = new Random().nextInt(9 * 1024) + 1024;
		this.serverFake = new HttpServerFake(port);
		serverFake.start();
		this.httpClient = new UrlConnectionHttpClientBuilder()
				.setUserAgent("com.openshift.client.test")
				.client();
	}

	@After
	public void tearDown() {
		serverFake.stop();
	}

	@Test
	public void canGet() throws SocketTimeoutException, HttpClientException, MalformedURLException {
		String response = httpClient.get(new URL(serverFake.getUrl()));
		assertNotNull(response);
		assertTrue(response.startsWith("GET"));
	}

	@Test
	public void canPost() throws SocketTimeoutException, HttpClientException, MalformedURLException,
			UnsupportedEncodingException {
		String response = httpClient.post(new HashMap<String, Object>(), new URL(serverFake.getUrl()));
		assertNotNull(response);
		assertTrue(response.startsWith("POST"));
	}

	@Test
	public void canPut() throws SocketTimeoutException, HttpClientException, MalformedURLException,
			UnsupportedEncodingException {
		String response = httpClient.put(new HashMap<String, Object>(), new URL(serverFake.getUrl()));
		assertNotNull(response);
		assertTrue(response.startsWith("PUT"));
	}

	@Test
	public void canDelete() throws SocketTimeoutException, HttpClientException, MalformedURLException,
			UnsupportedEncodingException {
		String response = httpClient.delete(new HashMap<String, Object>(), new URL(serverFake.getUrl()));
		assertNotNull(response);
		assertTrue(response.startsWith("DELETE"));
	}

	@Test
	public void canAddAuthorization() throws SocketTimeoutException, HttpClientException, MalformedURLException {
		String username = "andre.dietisheim@redhat.com";
		String password = "dummyPassword";
		IHttpClient httpClient = new UrlConnectionHttpClientBuilder()
				.setUserAgent("com.openshift.client.test")
				.setCredentials(username, password)
				.client();

		String response = httpClient.get(new URL(serverFake.getUrl()));
		assertNotNull(response);
		Matcher matcher = AUTHORIZATION_PATTERN.matcher(response);
		assertTrue(matcher.find());
		assertEquals(1, matcher.groupCount());
		String credentials = matcher.group(1);
		String cleartextCredentials = new String(Base64Coder.decode(credentials));
		assertEquals(username + ":" + password, cleartextCredentials);
	}

	@Test
	public void canAcceptJson() throws SocketTimeoutException, HttpClientException, MalformedURLException {
		IHttpClient httpClient = new UrlConnectionHttpClientBuilder()
				.setUserAgent("com.openshift.client.test")
				.client();

		String response = httpClient.get(new URL(serverFake.getUrl()));
		assertNotNull(response);
		assertTrue(response.indexOf(ACCEPT_APPLICATION_JSON) > 0);
	}
	
	@Test
	public void hasProperAgentWhenUsingKeys() {
		httpClient = new UrlConnectionHttpClientBuilder()
		.setUserAgent("com.needskey").setCredentials("blah", "bluh", "authkey", "authiv")
		.client();
		
		assertEquals("OpenShift-com.needskey", httpClient.getUserAgent());
	}

	@Test
	public void hasProperAgentWhenUsingKeysAndNoAgent() {
		httpClient = new UrlConnectionHttpClientBuilder()
		 .setCredentials("blah", "bluh", "authkey", "authiv")
		.client();
		
		assertEquals("OpenShift", httpClient.getUserAgent());
	}

	@Test
	public void shouldEncodeParametersCorrectly() throws HttpClientException, FileNotFoundException, IOException,
			OpenShiftException {
		// pre-conditions
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		String key1 = "adietish";
		String value1 = "redhat";
		parameters.put(key1, value1);
		String key2 = "xcoulon";
		String value2 = "redhat";
		parameters.put(key2, value2);

		IHttpClient httpClient = new HttpClientFake("1.0");
		// operation
		String response = httpClient.post(parameters, new URL(serverFake.getUrl()));

		// verification
		String[] entries = response.split(String.valueOf(IHttpClient.AMPERSAND));
		assertEquals(2, entries.length);
		String[] keyValuePair = entries[0].split(String.valueOf(IHttpClient.EQUALS));
		assertEquals(2, keyValuePair.length);
		assertEquals(key1, keyValuePair[0]);
		assertEquals(value1, keyValuePair[1]);
		keyValuePair = entries[1].split(String.valueOf(IHttpClient.EQUALS));
		assertEquals(2, keyValuePair.length);
		assertEquals(key2, keyValuePair[0]);
		assertEquals(value2, keyValuePair[1]);
	}

//	@Ignore("Not supported on PROD/STG yet")
	@Test
	public void shouldAddServiceVersionToAcceptHeader() throws FileNotFoundException, IOException, OpenShiftException,
			HttpClientException {
		// pre-conditions
		final String version = "1.0";
		IHttpClient httpClient = new HttpClientFake(version) {

			@Override
			protected String write(String data, String requestMethod, URL url)
					throws SocketTimeoutException, HttpClientException {
				try {
					HttpURLConnection connection = createConnection("dummyUser", "dummyPassword", "dummyUserAgent", url);
					// verification
					String accept = connection.getRequestProperty(IHttpClient.PROPERTY_ACCEPT);
					assertThat(accept).contains("; version=" + version);
					return data;
				} catch (IOException e) {
					fail("could not create HttpURLConnection");
					return null;
				}
			}
		};

		// operation
		httpClient.get(new URL(serverFake.getUrl()));

	}

}

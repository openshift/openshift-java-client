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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.openshift.express.client.OpenShiftException;
import com.openshift.express.client.configuration.DefaultConfiguration;
import com.openshift.express.client.configuration.IOpenShiftConfiguration;
import com.openshift.express.client.configuration.SystemConfiguration;
import com.openshift.express.client.configuration.SystemProperties;
import com.openshift.express.client.configuration.UserConfiguration;
import com.openshift.express.internal.client.test.fakes.SystemConfigurationFake;
import com.openshift.express.internal.client.test.fakes.UserConfigurationFake;
import com.openshift.express.internal.client.utils.StreamUtils;
import org.junit.Test;

/**
 * @author André Dietisheim
 */
public class ConfigurationTest {

	private static final String USERNAME_REGEX = "[^=]+=(.+)";
	private static final String USERNAME = "dummyUser";
	private static final String USERNAME2 = "André";
	private static final String USERNAME3 = "Dietisheim";
	private static final String ANOTHER_USERNAME = "anotherUser";
	protected static final String LIBRA_SERVER = "openshift.redhat.com";

	@Test
	public void canReadUsername() throws OpenShiftException, IOException {
		UserConfigurationFake userConfiguration = new UserConfigurationFake() {

			@Override
			protected void initFile(Writer writer) throws IOException {
				writer.append(KEY_RHLOGIN).append('=').append(USERNAME).append('\n');
			}

		};
		assertEquals(USERNAME, userConfiguration.getRhlogin());
	}

	@Test
	public void canStoreUsername() throws OpenShiftException, IOException {
		UserConfigurationFake userConfiguration = new UserConfigurationFake() {

			@Override
			protected void initFile(Writer writer) throws IOException {
				writer.append(KEY_RHLOGIN).append('=').append(USERNAME).append('\n');
			}

		};
		userConfiguration.setRhlogin(ANOTHER_USERNAME);
		userConfiguration.save();
		final File userConfigurationFile = userConfiguration.getFile();
		assertNotNull(userConfigurationFile);
		String fileContent = StreamUtils.readToString(new FileReader(userConfigurationFile));
		Pattern pattern = Pattern.compile(USERNAME_REGEX);
		Matcher matcher = pattern.matcher(fileContent);
		assertTrue(matcher.matches());
		assertEquals(1, matcher.groupCount());
		assertEquals(ANOTHER_USERNAME, matcher.group(1));
	}

	@Test
	public void canStoreAndReadUsername() throws OpenShiftException, IOException {
		UserConfigurationFake userConfiguration = new UserConfigurationFake() {

			@Override
			protected void initFile(Writer writer) throws IOException {
				writer.append(KEY_RHLOGIN).append('=').append(USERNAME).append('\n');
			}

		};
		userConfiguration.setRhlogin(ANOTHER_USERNAME);
		userConfiguration.save();
		final File userConfigurationFile = userConfiguration.getFile();
		assertNotNull(userConfigurationFile);
		UserConfigurationFake userConfiguration2 = new UserConfigurationFake() {

			@Override
			protected File doGetFile() {
				return userConfigurationFile;
			}
		};
		assertEquals(ANOTHER_USERNAME, userConfiguration2.getRhlogin());
	}
	
	@Test
	public void canReadUsernameIfItsInSystemConfigurationOnly() throws OpenShiftException, IOException {
		SystemConfiguration systemConfiguration = new SystemConfigurationFake(new DefaultConfiguration()) {

			@Override
			protected void init(Properties properties) {
				properties.put(KEY_RHLOGIN, USERNAME);
			}
			
		};
		UserConfigurationFake userConfiguration = new UserConfigurationFake(systemConfiguration);
		assertEquals(USERNAME, userConfiguration.getRhlogin());
	}

	@Test
	public void usernameInUserconfigOverridesUsernameInSystemconfig() throws OpenShiftException, IOException {
		SystemConfiguration systemConfiguration = new SystemConfigurationFake(new DefaultConfiguration()) {

			@Override
			protected void init(Properties properties) {
				properties.put(KEY_RHLOGIN, USERNAME);
			}
			
		};
		UserConfigurationFake userConfiguration = new UserConfigurationFake(systemConfiguration) {

			@Override
			protected void initFile(Writer writer) throws IOException {
				writer.append(KEY_RHLOGIN).append('=').append(USERNAME2).append('\n');
			}
			
		};
		assertEquals(USERNAME2, userConfiguration.getRhlogin());
	}

	@Test
	public void quotedLibraServerIsReturnedWithoutQuotes() throws OpenShiftException, IOException {
		SystemConfiguration systemConfiguration = new SystemConfigurationFake(new DefaultConfiguration()) {

			@Override
			protected void init(Properties properties) {
				properties.put(KEY_RHLOGIN, USERNAME);
			}
			
		};
		UserConfigurationFake userConfiguration = new UserConfigurationFake(systemConfiguration);		
		assertEquals(UserConfiguration.SCHEME_HTTPS + LIBRA_SERVER, userConfiguration.getLibraServer());
	}

	@Test
	public void nonQuotedLibraServerIsReturnedAsIs() throws OpenShiftException, IOException {
		SystemConfiguration systemConfiguration = new SystemConfigurationFake(new DefaultConfiguration()) {

			@Override
			protected void init(Properties properties) {
				properties.put(KEY_RHLOGIN, USERNAME);
			}
			
		};
		UserConfigurationFake userConfiguration = new UserConfigurationFake(systemConfiguration);		
		assertEquals(UserConfiguration.SCHEME_HTTPS + LIBRA_SERVER, userConfiguration.getLibraServer());
	}

	@Test
	public void nullLibraServerIsReturnedAsNull() throws OpenShiftException, IOException {
		SystemConfiguration systemConfiguration = new SystemConfigurationFake();
		assertNull(systemConfiguration.getLibraServer());
	}
	
	@Test
	public void libraserverInSystemPropsOverridesUsernameInSystemconfig() throws OpenShiftException, IOException {
		SystemConfiguration systemConfiguration = new SystemConfigurationFake(new DefaultConfiguration()) {

			@Override
			protected void init(Properties properties) {
				properties.put(KEY_RHLOGIN, USERNAME);
			}
			
		};
		UserConfigurationFake userConfiguration = new UserConfigurationFake(systemConfiguration) {

			@Override
			protected void initFile(Writer writer) throws IOException {
				writer.append(KEY_RHLOGIN).append('=').append(USERNAME2).append('\n');
			}
			
		};
		
		IOpenShiftConfiguration configuration = new SystemProperties(userConfiguration);
		configuration.setRhlogin(USERNAME3);
		assertEquals(USERNAME3, configuration.getRhlogin());
	}

	@Test
	public void systemPropsDefaultToUserconfig() throws OpenShiftException, IOException {
		SystemConfiguration systemConfiguration = new SystemConfigurationFake(new DefaultConfiguration()) {

			@Override
			protected void init(Properties properties) {
				properties.put(KEY_RHLOGIN, USERNAME);
			}
			
		};
		UserConfigurationFake userConfiguration = new UserConfigurationFake(systemConfiguration) {

			@Override
			protected void initFile(Writer writer) throws IOException {
				writer.append(KEY_RHLOGIN).append('=').append(USERNAME2).append('\n');
			}
			
		};
		
		IOpenShiftConfiguration configuration = new SystemProperties(userConfiguration);
		assertEquals(USERNAME2, configuration.getRhlogin());
	}

	@Test
	public void fallsBackToDefaultUrl() throws OpenShiftException, IOException {
		IOpenShiftConfiguration configuration = new SystemProperties(
				new UserConfigurationFake(new SystemConfigurationFake(new DefaultConfiguration())));
		assertNotNull(configuration.getLibraServer());
		assertTrue(configuration.getLibraServer().contains(DefaultConfiguration.LIBRA_SERVER));
		assertNull(configuration.getRhlogin());
	}
}

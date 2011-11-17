/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.openshift.express.client.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.openshift.express.internal.client.utils.Assert;
import com.openshift.express.internal.client.utils.StreamUtils;

/**
 * @author André Dietisheim
 */
public abstract class AbstractOpenshiftConfiguration implements IOpenShiftConfiguration {

	public static final String SCHEME_HTTPS = "https://";

	protected static final String KEY_RHLOGIN = "default_rhlogin";
	protected static final String KEY_LIBRA_SERVER = "libra_server";
	
	private static final Pattern SINGLEQUOTED_REGEX = Pattern.compile("'*([^']+)'*");
	
	private Properties properties;
	private File file;
	
	protected void initProperties(File file) throws FileNotFoundException, IOException {
		initProperties(file, null);
	}
	
	protected void initProperties(Properties defaultProperties) throws FileNotFoundException, IOException {
		initProperties(null, defaultProperties);
	}

	protected void initProperties(File file, Properties defaultProperties) throws FileNotFoundException, IOException {
		this.file = file;
		this.properties = getProperties(file, defaultProperties);
	}

	protected Properties getProperties(File file, Properties defaultProperties) throws FileNotFoundException, IOException {
		Properties properties = null;
		
		if (file == null
				|| !file.canRead()) {
			properties = new Properties(defaultProperties);
			
			if (System.getProperty("libra_server") != null)
				properties.put(KEY_LIBRA_SERVER, System.getProperty("libra_server"));
			return properties;
		}
		
		FileReader reader = null;
		try {
			properties = new Properties(defaultProperties);
			reader = new FileReader(file);
			properties.load(reader);
			
			if (System.getProperty("libra_server") != null)
				properties.put(KEY_LIBRA_SERVER, System.getProperty("libra_server"));
			
			return properties;
		} finally {
			StreamUtils.close(reader);
		}
		
		
	}
		
	public File getFile() {
		return file;
	}
	
	public void save() throws IOException {
		Assert.notNull(file);
		Writer writer = null;
		try {
			writer = new FileWriter(file);
			properties.store(writer, "");
		} finally {
			StreamUtils.close(writer);
		}
	}

	@Override
	public String getRhlogin() {
		return properties.getProperty(KEY_RHLOGIN);
	}
	
	@Override
	public void setRhlogin(String rhlogin) {
		properties.put(KEY_RHLOGIN, rhlogin);
	}
	
	@Override
	public String getLibraServer() {
		return appendScheme(removeSingleQuotes(properties.getProperty(KEY_LIBRA_SERVER)));
	}

	protected String appendScheme(String host) {
		if (host == null) {
			return host;
		}
		return SCHEME_HTTPS + host;

	}
	
	protected String removeSingleQuotes(String value) {
		if (value == null) {
			return null;
		}
		Matcher matcher = SINGLEQUOTED_REGEX.matcher(value);
		if (matcher.find()
				&& matcher.groupCount() == 1) {
			return matcher.group(1);
		} else {
			return value;
		}
	}

	@Override
	public void setLibraServer(String libraServer) {
		properties.put(KEY_LIBRA_SERVER, libraServer);
	}
	
	@Override
	public Properties getProperties() {
		return properties;
	}
}

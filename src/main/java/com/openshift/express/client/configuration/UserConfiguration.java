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
import java.io.IOException;
import java.util.Properties;

import com.openshift.express.client.OpenShiftException;

/**
 * @author André Dietisheim
 */
public class UserConfiguration extends AbstractOpenshiftConfiguration {

	private static final String CONFIGURATION_FOLDER = ".openshift";
	private static final String CONFIGURATION_FILE = "express.conf";
	private static final String PROPERTY_USERHOME = "user.home";
	
	public UserConfiguration(SystemConfiguration systemConfiguration) throws OpenShiftException, IOException {
		initProperties(systemConfiguration);
	}

	protected void initProperties(SystemConfiguration systemConfiguration) throws FileNotFoundException, IOException {
		File configurationFile = doGetFile();
		Properties systemProperties = null;
		if (systemConfiguration == null) {
			systemProperties = new Properties();
		} else {
			systemProperties = systemConfiguration.getProperties();
		}
		super.initProperties(configurationFile, systemProperties);
	}

	protected File doGetFile() {
		String userHome = System.getProperty(PROPERTY_USERHOME);
		
		File configurationFile = new File(userHome + File.separatorChar + CONFIGURATION_FOLDER, CONFIGURATION_FILE);
	
		return configurationFile;
	}
}

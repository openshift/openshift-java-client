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

import java.io.FileNotFoundException;
import java.io.IOException;

import com.openshift.express.client.OpenShiftException;

/**
 * @author André Dietisheim
 */
public class SystemProperties extends AbstractOpenshiftConfiguration {

	private IOpenShiftConfiguration parentConfiguration;

	public SystemProperties(IOpenShiftConfiguration parentConfiguration) throws OpenShiftException, IOException {
		initProperties(parentConfiguration);
	}

	protected void initProperties(IOpenShiftConfiguration parentConfiguration) throws FileNotFoundException, IOException {
		initProperties(System.getProperties());
		this.parentConfiguration = parentConfiguration;
	}

	@Override
	public String getLibraServer() {
		return appendScheme(removeSingleQuotes(getSystemPropertyOrParent(KEY_LIBRA_SERVER)));
	}

	@Override
	public String getRhlogin() {
		return getSystemPropertyOrParent(KEY_RHLOGIN);
	}

	private String getSystemPropertyOrParent(String key) {
		if (getProperties().containsKey(key)) {
			return getProperties().getProperty(key);
		} else {
			return parentConfiguration.getProperties().getProperty(key);
		}
	}
}

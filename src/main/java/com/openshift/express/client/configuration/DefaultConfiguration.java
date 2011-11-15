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
import java.util.Properties;

import com.openshift.express.client.OpenShiftException;

/**
 * @author André Dietisheim
 */
public class DefaultConfiguration extends AbstractOpenshiftConfiguration {

	public static final String LIBRA_SERVER = "openshift.redhat.com";

	public DefaultConfiguration() throws OpenShiftException, IOException {
		initProperties();
	}

	protected void initProperties() throws FileNotFoundException, IOException {
		Properties properties = new Properties();
		properties.put(KEY_LIBRA_SERVER, LIBRA_SERVER);
		initProperties(properties);
	}
}

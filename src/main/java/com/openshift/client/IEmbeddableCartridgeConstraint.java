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
package com.openshift.client;

import java.util.List;

import com.openshift.internal.client.EmbeddableCartridgeNameConstraint;

/**
 * @author Andre Dietisheim
 * 
 */
public interface IEmbeddableCartridgeConstraint {

	public static final IEmbeddableCartridgeConstraint MYSQL = new EmbeddableCartridgeNameConstraint("mysql");

	public static final IEmbeddableCartridgeConstraint PHPMYADMIN = new EmbeddableCartridgeNameConstraint("phpmyadmin");

	public static final IEmbeddableCartridgeConstraint POSTGRESQL = new EmbeddableCartridgeNameConstraint("postgresql");

	public static final IEmbeddableCartridgeConstraint MONGODB = new EmbeddableCartridgeNameConstraint("mongodb");

	public static final IEmbeddableCartridgeConstraint ROCKMONGO = new EmbeddableCartridgeNameConstraint("rockmongo");

	public static final IEmbeddableCartridgeConstraint _10GEN_MMS_AGENT = new EmbeddableCartridgeNameConstraint("10gen-mms-agent");

	public static final IEmbeddableCartridgeConstraint JENKINS_CLIENT = new EmbeddableCartridgeNameConstraint("jenkins-client");

	public static final IEmbeddableCartridgeConstraint METRICS = new EmbeddableCartridgeNameConstraint("metrics");

	/**
	 * Returns the cartridge that matches this constraint.
	 * 
	 * @param connection
	 *            the connection to use when retrieving the available embeddable
	 *            cartridges
	 * @return the embeddable cartridge that matches this constraint
	 */
	public List<IEmbeddableCartridge> getEmbeddableCartridges(IOpenShiftConnection connection);
}

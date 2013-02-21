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

import com.openshift.internal.client.LatestVersionOfName;

/**
 * @author Andre Dietisheim
 * 
 */
public class LatestVersionOf {

	public static <C extends IEmbeddableCartridge> ICartridgeConstraint<C> mySQL() {
		return new LatestVersionOfName<C>("mysql");
	}

	public static <C extends IEmbeddableCartridge> ICartridgeConstraint<C> phpMyAdmin() {
		return new LatestVersionOfName<C>("phpmyadmin");
	}

	public static <C extends IEmbeddableCartridge> ICartridgeConstraint<C> postgreSQL() {
		return new LatestVersionOfName<C>("postgresql");
	}
	
	public static <C extends IEmbeddableCartridge> ICartridgeConstraint<C> mongoDB() {
		return new LatestVersionOfName<C>("mongodb");
	}

	public static <C extends IEmbeddableCartridge> ICartridgeConstraint<C> rockMongo() {
		return new LatestVersionOfName<C>("rockmongo");
	}

	public static <C extends IEmbeddableCartridge> ICartridgeConstraint<C> mmsAgent() {
		return new LatestVersionOfName<C>("10gen-mms-agent");
	}

	public static <C extends IEmbeddableCartridge> ICartridgeConstraint<C> jenkinsClient() {
		return new LatestVersionOfName<C>("jenkins-client");
	}

	public static <C extends IEmbeddableCartridge> ICartridgeConstraint<C> metrics() {
		return new LatestVersionOfName<C>("metrics");
	}
}

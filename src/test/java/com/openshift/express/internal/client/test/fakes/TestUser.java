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
package com.openshift.express.internal.client.test.fakes;

import java.io.IOException;

import com.openshift.express.client.Cartridge;
import com.openshift.express.client.IApplication;
import com.openshift.express.client.IOpenShiftService;
import com.openshift.express.client.OpenShiftException;
import com.openshift.express.client.OpenShiftService;
import com.openshift.express.client.User;
import com.openshift.express.internal.client.test.utils.ApplicationUtils;

/**
 * @author André Dietisheim
 */
public class TestUser extends User {

	public static final String ID = "com.openshift.express.client.test " + OpenShiftService.VERSION;

	// public static final String RHLOGIN_USER_WITHOUT_DOMAIN = "toolsjboss+unittests_nodomain@gmail.com";
	// public static final String PASSWORD_USER_WITHOUT_DOMAIN = "1q2w3e";

	public static final String RHLOGIN_USER_WITHOUT_DOMAIN = "adietish+unittests_nodomain@redhat.com";
	public static final String PASSWORD_USER_WITHOUT_DOMAIN = "1q2w3e";

	public TestUser(IOpenShiftService service) throws OpenShiftException, IOException {
		super(System.getProperty("RHLOGIN"), System.getProperty("PASSWORD"), ID, service);
	}

	public TestUser(String password, IOpenShiftService service) throws OpenShiftException, IOException {
		super(System.getProperty("RHLOGIN"), password, ID, service);
	}

	public TestUser(String rhlogin, String password, IOpenShiftService service) throws OpenShiftException, IOException {
		super(rhlogin, password, ID,
				service);
	}

	public TestUser(String rhlogin, String password, String url, IOpenShiftService service) {
		super(rhlogin, password, ID, url, service);
	}

	public IApplication createTestApplication() throws OpenShiftException {
		return createApplication(ApplicationUtils.createRandomApplicationName(), Cartridge.JBOSSAS_7);
	}

	public void silentlyDestroyApplication(IApplication application) {
		try {
			getService().destroyApplication(application.getName(), application.getCartridge(), this);
		} catch (OpenShiftException e) {
			e.printStackTrace();
		}
	}
}

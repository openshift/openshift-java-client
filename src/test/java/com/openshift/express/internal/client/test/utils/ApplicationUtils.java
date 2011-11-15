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
package com.openshift.express.internal.client.test.utils;

import com.openshift.express.client.Cartridge;
import com.openshift.express.client.IApplication;
import com.openshift.express.client.ICartridge;
import com.openshift.express.client.IOpenShiftService;
import com.openshift.express.client.OpenShiftException;
import com.openshift.express.client.User;

/**
 * @author André Dietisheim
 */
public class ApplicationUtils {

	public static String createRandomApplicationName() {
		return String.valueOf(System.currentTimeMillis());
	}

	public static IApplication createApplication(User user, IOpenShiftService service) throws OpenShiftException {
		return service.createApplication(createRandomApplicationName(), Cartridge.JBOSSAS_7, user);
	}
	
	public static void silentlyDestroyAS7Application(String name, User user, IOpenShiftService service) {
		try {
			if (name == null) {
				return;
			}
			service.destroyApplication(name, ICartridge.JBOSSAS_7, user);
		} catch (OpenShiftException e) {
			e.printStackTrace();
		}
	}
}

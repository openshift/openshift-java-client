/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package com.openshift.express.internal.client;

import com.openshift.express.client.ICartridge;
import com.openshift.express.client.IJBossASApplication;
import com.openshift.express.client.IOpenShiftService;
import com.openshift.express.client.OpenShiftException;

/**
 * @author William DeCoste
 * @author Andre Dietisheim
 */
public class JBossASApplication extends Application implements IJBossASApplication {

	public JBossASApplication(String name, String uuid, String creationLog, String healthCheckPath, ICartridge cartridge,
			InternalUser user,
			IOpenShiftService service) {
		super(name, uuid, creationLog, healthCheckPath, cartridge, user, service);
	}

	public JBossASApplication(String name, String uuid, ICartridge cartridge, ApplicationInfo applicationInfo, InternalUser user,
			IOpenShiftService service) {
		super(name, uuid, cartridge, applicationInfo, user, service);
	}

	public String threadDump() throws OpenShiftException {
		service.threadDumpApplication(name, cartridge, getInternalUser());
		
		return "stdout.log";
	}

}

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

import java.util.ArrayList;
import java.util.List;

import com.openshift.express.client.ICartridge;
import com.openshift.express.client.IEmbeddableCartridge;
import com.openshift.express.client.IRackApplication;
import com.openshift.express.client.IOpenShiftService;
import com.openshift.express.client.OpenShiftException;

/**
 * @author William DeCoste
 */
public class RackApplication extends Application implements IRackApplication {

	public RackApplication(String name, ICartridge cartridge, InternalUser user, IOpenShiftService service) {
		super(name, cartridge, new ArrayList<IEmbeddableCartridge>(), null, user, service);
	}

	public RackApplication(String name, String creationLog, ICartridge cartridge, InternalUser user, IOpenShiftService service) {
		super(name, creationLog, cartridge, new ArrayList<IEmbeddableCartridge>(), null, user, service);
	}

	public RackApplication(String name, ICartridge cartridge, ApplicationInfo applicationInfo, InternalUser user,
			IOpenShiftService service) {
		super(name, cartridge, null, applicationInfo, user, service);
	}

	public RackApplication(String name, ICartridge cartridge, List<IEmbeddableCartridge> embeddedCartridges,
			ApplicationInfo applicationInfo, InternalUser user, IOpenShiftService service) {
		super(name, null, cartridge, embeddedCartridges, applicationInfo, user, service);
	}

	public RackApplication(String name, String creationLog, ICartridge cartridge,
			List<IEmbeddableCartridge> embeddedCartridges, ApplicationInfo applicationInfo, InternalUser user,
			IOpenShiftService service) {
		super(name, creationLog, cartridge, embeddedCartridges, applicationInfo, user, service);
	}

	public void threadDump() throws OpenShiftException {
		service.threadDumpApplication(name, cartridge, getUser());
	}


}

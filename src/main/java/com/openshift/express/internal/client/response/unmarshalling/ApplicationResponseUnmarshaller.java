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
package com.openshift.express.internal.client.response.unmarshalling;

import org.jboss.dmr.ModelNode;
import com.openshift.express.client.IApplication;
import com.openshift.express.client.ICartridge;
import com.openshift.express.client.OpenShiftService;
import com.openshift.express.internal.client.Application;
import com.openshift.express.internal.client.InternalUser;

/**
 * @author André Dietisheim
 */
public class ApplicationResponseUnmarshaller extends AbstractOpenShiftJsonResponseUnmarshaller<IApplication> {

	protected InternalUser user;
	protected String applicationName;
	protected ICartridge cartridge;
	protected OpenShiftService service;

	public ApplicationResponseUnmarshaller(String applicationName, ICartridge cartridge, InternalUser user, OpenShiftService service) {
		this.applicationName = applicationName;
		this.cartridge = cartridge;
		this.user = user;
		this.service = service;
	}

	@Override
	protected IApplication createOpenShiftObject(ModelNode node) {
		return new Application(applicationName, cartridge, user, service);
	}
}

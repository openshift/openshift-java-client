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
import com.openshift.express.client.IEmbeddableCartridge;

/**
 * @author André Dietisheim
 */
public class EmbedResponseUnmarshaller extends AbstractOpenShiftJsonResponseUnmarshaller<IApplication> {

	private IEmbeddableCartridge embeddedCartridge;
	private IApplication application;
	
	public EmbedResponseUnmarshaller(IEmbeddableCartridge embeddedCartridge, IApplication application) {
		this.embeddedCartridge = embeddedCartridge;
		this.application = application;
	}

	@Override
	protected IApplication createOpenShiftObject(ModelNode node) {
		application.setEmbbedCartridge(embeddedCartridge);
		return application;
	}
}

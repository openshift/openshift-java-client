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

import com.openshift.express.client.OpenShiftException;

/**
 * @author André Dietisheim
 */
public class NakedResponseUnmarshaller extends AbstractOpenShiftJsonResponseUnmarshaller<Object> {

	protected Object createOpenShiftObject(ModelNode node) throws OpenShiftException {
		return null;
	}
}

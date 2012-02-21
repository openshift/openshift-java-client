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
package com.openshift.express.client;

/**
 * @author Andre Dietisheim
 */
public class InvalidNameOpenShiftException  extends OpenShiftException {

	private static final long serialVersionUID = 1L;

	public InvalidNameOpenShiftException(String message, Object... arguments) {
		super(message, arguments);
	}
}

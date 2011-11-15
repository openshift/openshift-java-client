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
package com.openshift.express.internal.client.request;

/**
 * @author André Dietisheim
 */
public enum EmbedAction {

	ADD("configure"),
	REMOVE("deconfigure"),
	START("start"),
	STOP("stop"),
	RESTART("restart"),
	STATUS("status"),
	RELOAD("reload"),
	ADD_ALIAS("add-alias"),
	REMOVE_ALIAS("remove-alias");

	private String command;

	EmbedAction(String command) {
		this.command = command;
	}

	public String getCommand() {
		return command;
	}

}

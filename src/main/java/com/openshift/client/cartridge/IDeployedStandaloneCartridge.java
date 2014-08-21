/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package com.openshift.client.cartridge;

/**
 * Represents a standalone cartridge that has been deployed as opposed to IStandaloneCartridge
 * which really represents the metadata about a standalone cartridge
 */
public interface IDeployedStandaloneCartridge extends IStandaloneCartridge{

	/**
	 * set the additional gear storage for the cartridge to the given
	 * size.
	 * 
	 * @param size  The total additional gear storage for the cartridge
	 *              in gigabytes
	 */
	public void setAdditionalGearStorage(int size);
}

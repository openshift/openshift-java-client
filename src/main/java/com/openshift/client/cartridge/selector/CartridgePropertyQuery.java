/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package com.openshift.client.cartridge.selector;

import com.openshift.client.cartridge.IEmbeddedCartridge;
import com.openshift.internal.client.response.CartridgeResourceProperty;

/**
 * @author André Dietisheim
 */
public abstract class CartridgePropertyQuery {

	public abstract CartridgeResourceProperty getMatchingProperty(IEmbeddedCartridge embeddedCartridge);

}
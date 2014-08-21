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
package com.openshift.client.cartridge;

import java.net.URL;

import com.openshift.internal.client.APIResource;
import com.openshift.internal.client.ApplicationResource;
import com.openshift.client.IApplication;
import com.openshift.internal.client.CartridgeType;
import com.openshift.internal.client.cartridge.BaseCartridge;


/**
 * A cartridge that is available on the openshift server. This class is no enum
 * since we dont know all available types and they may change at any time.
 * 
 * @author André Dietisheim
 */
public class StandaloneCartridge extends BaseCartridge implements IStandaloneCartridge {

	public StandaloneCartridge(String name) {
		super(name);
	}

	public StandaloneCartridge(URL url) {
		super(url);
	}

	public StandaloneCartridge(String name, URL url) {
		super(name, url);
	}

	/**
	 * Constructor used when available cartridges are loaded from OpenShift
	 * 
	 * @see APIResource#getEmbeddableCartridges()
	 */
	public StandaloneCartridge(String name, String displayName, String description, boolean obsolete) {
		super(name, null, displayName, description, obsolete);
	}

	/**
	 * Constructor used when cartridges are reported within application
	 * 
	 * @see ApplicationResource#updateCartridges
	 */
	public StandaloneCartridge(String name, URL url, String displayName, String description, boolean obsolete) {
		super(name, url, displayName, description);
	}

	@Override
	public CartridgeType getType() {
		return CartridgeType.STANDALONE;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof IStandaloneCartridge)) {
			return false;
		}
		IStandaloneCartridge other = (IStandaloneCartridge) obj;
		// shortcut: downloadable cartridges get their name only when
		// they're deployed thus should equal on url only
		if (isDownloadable()) {
			if (other.isDownloadable()) {
				if (getUrl() == null) {
					return other.getUrl() == null;
				}
				return getUrl().equals(other.getUrl());
			}
		}
		if (getName() == null) {
			if (other.getName() != null) {
				return false;
			}
		} else if (!getName().equals(other.getName())) {
			return false;
		}
		return true;
	}
}

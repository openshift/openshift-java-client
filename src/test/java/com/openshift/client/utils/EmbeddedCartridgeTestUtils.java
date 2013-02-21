/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package com.openshift.client.utils;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.List;

import com.openshift.client.IApplication;
import com.openshift.client.ICartridgeConstraint;
import com.openshift.client.IEmbeddableCartridge;
import com.openshift.client.IEmbeddedCartridge;
import com.openshift.client.IUser;
import com.openshift.client.OpenShiftException;

/**
 * @author André Dietisheim
 */
public class EmbeddedCartridgeTestUtils {

	public static String createRandomApplicationName() {
		return String.valueOf(System.currentTimeMillis());
	}

	public static void silentlyDestroy(ICartridgeConstraint<IEmbeddableCartridge> cartridgeConstraint,
			IApplication application) {
		try {
			if (cartridgeConstraint == null
					|| application == null) {
				return;
			}
			application.removeEmbeddedCartridges(cartridgeConstraint);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void silentlyDestroy(IEmbeddableCartridge cartridge, IApplication application) {
		try {
			if (cartridge == null
					|| application == null) {
				return;
			}
			application.removeEmbeddedCartridge(cartridge);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void silentlyDestroyAllEmbeddedCartridges(IApplication application) {
		if (application == null) {
			return;
		}

		try {
			for (IEmbeddedCartridge cartridge : application.getEmbeddedCartridges()) {
				silentlyDestroy(cartridge, application);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void ensureHasEmbeddedCartridges(IEmbeddedCartridge cartridge, IApplication application)
			throws OpenShiftException {
		if (cartridge == null
				|| application == null) {
			return;
		}

		if (application.hasEmbeddedCartridge(cartridge)) {
			return;
		}

		application.addEmbeddableCartridge(cartridge);
	}

	public static Collection<IEmbeddableCartridge> getEmbeddableCartridges(ICartridgeConstraint<IEmbeddableCartridge> constraint, IUser user) {
		List<IEmbeddableCartridge> allCartridges =
				user.getConnection().getEmbeddableCartridges();
		return constraint.getMatching(allCartridges);
	}

	public static IEmbeddableCartridge getEmbeddableCartridge(ICartridgeConstraint<IEmbeddableCartridge> constraint, IUser user) {
		Collection<IEmbeddableCartridge> embeddableCartridges = getEmbeddableCartridges(constraint, user);
		assertEquals(1, embeddableCartridges.size());
		return embeddableCartridges.iterator().next();
	}
}

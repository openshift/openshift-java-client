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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.openshift.express.client.ApplicationLogReader;
import com.openshift.express.client.IApplication;
import com.openshift.express.client.ICartridge;
import com.openshift.express.client.IDomain;
import com.openshift.express.client.IEmbeddableCartridge;
import com.openshift.express.client.IOpenShiftService;
import com.openshift.express.client.OpenShiftException;
import com.openshift.express.internal.client.utils.Assert;

/**
 * @author André Dietisheim
 */
public class Application extends UserInfoAware implements IApplication {

	private static final String GIT_URI_PATTERN = "ssh://{0}@{1}-{2}.{3}/~/git/{1}.git/";
	private static final String APPLICATION_URL_PATTERN = "https://{0}-{1}.{2}/";

	private String name;
	private ICartridge cartridge;
	private List<IEmbeddableCartridge> embeddedCartridges;
	private IOpenShiftService service;
	private ApplicationLogReader logReader;
	private ApplicationInfo applicationInfo;
	private String creationLog;

	public Application(String name, ICartridge cartridge, InternalUser user, IOpenShiftService service) {
		this(name, cartridge, new ArrayList<IEmbeddableCartridge>(), null, user, service);
	}

	public Application(String name, String creationLog, ICartridge cartridge, InternalUser user, IOpenShiftService service) {
		this(name, creationLog, cartridge, new ArrayList<IEmbeddableCartridge>(), null, user, service);
	}

	public Application(String name, ICartridge cartridge, ApplicationInfo applicationInfo, InternalUser user,
			IOpenShiftService service) {
		this(name, cartridge, null, applicationInfo, user, service);
	}

	public Application(String name, ICartridge cartridge, List<IEmbeddableCartridge> embeddedCartridges,
			ApplicationInfo applicationInfo, InternalUser user, IOpenShiftService service) {
		this(name, null, cartridge, embeddedCartridges, applicationInfo, user, service);
	}

	public Application(String name, String creationLog, ICartridge cartridge,
			List<IEmbeddableCartridge> embeddedCartridges, ApplicationInfo applicationInfo, InternalUser user,
			IOpenShiftService service) {
		super(user);
		this.name = name;
		this.creationLog = creationLog;
		this.cartridge = cartridge;
		this.embeddedCartridges = embeddedCartridges;
		this.applicationInfo = applicationInfo;
		this.service = service;
	}

	public String getName() {
		return name;
	}

	public String getUUID() throws OpenShiftException {
		return getApplicationInfo().getUuid();
	}

	public ICartridge getCartridge() {
		return cartridge;
	}

	public Date getCreationTime() throws OpenShiftException {
		return getApplicationInfo().getCreationTime();
	}

	public String getCreationLog() {
		return creationLog;
	}

	public void destroy() throws OpenShiftException {
		service.destroyApplication(name, cartridge, getUser());
		getUser().remove(this);
	}

	public void start() throws OpenShiftException {
		service.startApplication(name, cartridge, getUser());
	}

	public void restart() throws OpenShiftException {
		service.restartApplication(name, cartridge, getUser());
	}

	public void stop() throws OpenShiftException {
		service.stopApplication(name, cartridge, getUser());
	}

	public ApplicationLogReader getLogReader() throws OpenShiftException {
		if (logReader == null) {
			this.logReader = new ApplicationLogReader(this, getUser(), service);
		}
		return logReader;
	}

	public String getGitUri() throws OpenShiftException {
		IDomain domain = getUser().getDomain();
		if (domain == null) {
			return null;
		}
		return MessageFormat
				.format(GIT_URI_PATTERN, getUUID(), getName(), domain.getNamespace(), domain.getRhcDomain());
	}

	public String getApplicationUrl() throws OpenShiftException {
		IDomain domain = getUser().getDomain();
		if (domain == null) {
			return null;
		}
		return MessageFormat.format(APPLICATION_URL_PATTERN, name, domain.getNamespace(), domain.getRhcDomain());
	}

	public void addEmbbedCartridge(IEmbeddableCartridge embeddedCartridge) throws OpenShiftException {
		service.addEmbeddedCartridge(getName(), embeddedCartridge, getUser());
		Assert.isTrue(embeddedCartridge instanceof EmbeddableCartridge);
		((EmbeddableCartridge) embeddedCartridge).setApplication(this);
		this.embeddedCartridges.add(embeddedCartridge);
	}

	public void addEmbbedCartridges(List<IEmbeddableCartridge> embeddedCartridges) throws OpenShiftException {
		for (IEmbeddableCartridge cartridge : embeddedCartridges) {
			// TODO: catch exceptions when removing cartridges, contine removing and report the exceptions that occurred<
			addEmbbedCartridge(cartridge);
		}
	}

	public void removeEmbbedCartridge(IEmbeddableCartridge embeddedCartridge) throws OpenShiftException {
		if (!hasEmbeddedCartridge(embeddedCartridge.getName())) {
			throw new OpenShiftException("There's no cartridge \"{0}\" embedded to the application \"{1}\"",
					cartridge.getName(), getName());
		}
		service.removeEmbeddedCartridge(getName(), embeddedCartridge, getUser());
		embeddedCartridges.remove(embeddedCartridge);
	}

	public void removeEmbbedCartridges(List<IEmbeddableCartridge> embeddedCartridges) throws OpenShiftException {
		for (IEmbeddableCartridge cartridge : embeddedCartridges) {
			// TODO: catch exceptions when removing cartridges, contine removing and report the exceptions that occurred<
			removeEmbbedCartridge(cartridge);
		}
	}

	public List<IEmbeddableCartridge> getEmbeddedCartridges() throws OpenShiftException {
		if (embeddedCartridges == null) {
			this.embeddedCartridges = new ArrayList<IEmbeddableCartridge>();
			for (EmbeddableCartridgeInfo cartridgeInfo : getApplicationInfo().getEmbeddedCartridges()) {
				embeddedCartridges.add(new EmbeddableCartridge(cartridgeInfo.getName(), this));
			}
		}
		return embeddedCartridges;
	}

	public boolean hasEmbeddedCartridge(String cartridgeName) throws OpenShiftException {
		return getEmbeddedCartridge(cartridgeName) != null;
	}

	public IEmbeddableCartridge getEmbeddedCartridge(String cartridgeName) throws OpenShiftException {
		IEmbeddableCartridge embeddedCartridge = null;
		for (IEmbeddableCartridge cartridge : getEmbeddedCartridges()) {
			if (cartridgeName.equals(cartridge.getName())) {
				embeddedCartridge = cartridge;
				break;
			}
		}
		return embeddedCartridge;
	}

	protected IOpenShiftService getService() {
		return service;
	}

	protected ApplicationInfo getApplicationInfo() throws OpenShiftException {
		if (applicationInfo == null) {
			this.applicationInfo = getUserInfo().getApplicationInfoByName(getName());
			if (applicationInfo == null) {
				throw new OpenShiftException("Could not find info for application {0}", getName());
			}
		}
		return applicationInfo;
	}
	
	public boolean waitForAccessible(long timeout) throws OpenShiftException {
		return service.waitForApplication(this, timeout);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (object == null)
			return false;
		if (getClass() != object.getClass())
			return false;
		Application other = (Application) object;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return name;
	}
}

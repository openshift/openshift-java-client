/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 *   Red Hat, Inc.
 ******************************************************************************/
package com.openshift.internal.client;

import java.net.URL;

import com.openshift.client.OpenShiftException;
import com.openshift.client.cartridge.IDeployedStandaloneCartridge;
import com.openshift.client.cartridge.IStandaloneCartridge;
import com.openshift.internal.client.httpclient.request.Parameter;
import com.openshift.internal.client.response.CartridgeResourceDTO;
import com.openshift.internal.client.utils.Assert;
import com.openshift.internal.client.utils.IOpenShiftJsonConstants;

public class StandaloneCartridgeResource extends AbstractOpenShiftResource implements IStandaloneCartridge, IDeployedStandaloneCartridge {

	private static final String LINK_UPDATE_CARTRIDGE = "UPDATE";
	

	private ApplicationResource application;
	private String name;
	private String displayName;
	private String description;
	private URL url;
	private CartridgeType cartridgeType;
	private boolean obsolete;

	protected StandaloneCartridgeResource(final CartridgeResourceDTO dto, final ApplicationResource application){
		super(application.getService(), dto.getLinks(), dto.getMessages());
		Assert.isTrue(dto.getType().equals(CartridgeType.STANDALONE));
		this.application = application;
		this.name = dto.getName();
		this.displayName = dto.getDisplayName();
		this.description = dto.getDescription();
		this.url = dto.getUrl();
		this.cartridgeType = dto.getType();
		this.obsolete = dto.getObsolete();
	}

	/**
	 * set the additional gear storage for this cartridge to the given value
	 * 
	 * @param   size   The total size of storage in gigabytes for the gear
	 */
	@Override
	public void setAdditionalGearStorage(int size) {
		new UpdateCartridgeRequest().execute(new Parameter(IOpenShiftJsonConstants.PROPERTY_ADDITIONAL_GEAR_STORAGE, String.valueOf(size)));
	}

	/**
	 * Refreshes the content of this embedded cartridge. Causes all embedded
	 * cartridges of the same application to get updated.
	 * 
	 * @see #update(CartridgeResourceDTO)
	 * @see ApplicationResource#refreshEmbeddedCartridges()
	 */
	@Override
	public void refresh() throws OpenShiftException {
		application.refreshEmbeddedCartridges(); 
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getDisplayName() {
		return this.displayName;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	@Override
	public boolean isDownloadable() {
		return this.url != null;
	}

	@Override
	public URL getUrl() {
		return this.url;
	}

	@Override
	public CartridgeType getType() {
		return this.cartridgeType;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if(!IStandaloneCartridge.class.isAssignableFrom(obj.getClass()))
			return false;
		IStandaloneCartridge other = (IStandaloneCartridge) obj;
		if (name == null) {
			if (other.getName() != null)
				return false;
		} else if (!name.equals(other.getName()))
			return false;
		return true;
	}



	private class UpdateCartridgeRequest extends ServiceRequest {
		private UpdateCartridgeRequest() {
			super(LINK_UPDATE_CARTRIDGE);
		}
	}



	@Override
	public boolean isObsolete() {
		return obsolete;
	}
}

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
package com.openshift.internal.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.openshift.client.ApplicationScale;
import com.openshift.client.IApplication;
import com.openshift.client.IDomain;
import com.openshift.client.IGearProfile;
import com.openshift.client.IHttpClient;
import com.openshift.client.IUser;
import com.openshift.client.Messages;
import com.openshift.client.OpenShiftException;
import com.openshift.client.OpenShiftRequestException;
import com.openshift.client.cartridge.IEmbeddableCartridge;
import com.openshift.client.cartridge.IStandaloneCartridge;
import com.openshift.internal.client.httpclient.request.StringParameter;
import com.openshift.internal.client.response.ApplicationResourceDTO;
import com.openshift.internal.client.response.DomainResourceDTO;
import com.openshift.internal.client.response.Link;
import com.openshift.internal.client.response.LinkParameter;
import com.openshift.internal.client.utils.Assert;
import com.openshift.internal.client.utils.CollectionUtils;
import com.openshift.internal.client.utils.IOpenShiftJsonConstants;
import com.openshift.internal.client.utils.IOpenShiftParameterConstants;

/**
 * @author André Dietisheim
 * @author Nicolas Spano
 */
public class DomainResource extends AbstractOpenShiftResource implements IDomain {

	private static final String LINK_GET = "GET";
	private static final String LINK_LIST_APPLICATIONS = "LIST_APPLICATIONS";
	private static final String LINK_ADD_APPLICATION = "ADD_APPLICATION";
	private static final String LINK_UPDATE = "UPDATE";
	private static final String LINK_DELETE = "DELETE";

	private String id;
	private String suffix;

	private final APIResource connectionResource;
	/** Applications for the domain. */
	// TODO: replace by a map indexed by application names ?
	private List<IApplication> applications = null;

	protected DomainResource(final String namespace, final String suffix, final Map<String, Link> links,
			final Messages messages, final APIResource api) {
		super(api.getService(), links, messages);
		this.id = namespace;
		this.suffix = suffix;
		this.connectionResource = api;
	}

	protected DomainResource(DomainResourceDTO domainDTO, final APIResource api) {
		this(domainDTO.getId(), domainDTO.getSuffix(), domainDTO.getLinks(), domainDTO.getMessages(), api);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getSuffix() {
		return suffix;
	}

	@Override
	public void rename(String id) throws OpenShiftException {
		Assert.notNull(id);

		DomainResourceDTO domainDTO = new UpdateDomainRequest().execute(id);
		this.id = domainDTO.getId();
		this.suffix = domainDTO.getSuffix();
		this.getLinks().clear();
		this.getLinks().putAll(domainDTO.getLinks());
	}

	@Override
	public IUser getUser() throws OpenShiftException {
		return connectionResource.getUser();
	}

	@Override
	public IApplication createApplication(final String name, final IStandaloneCartridge cartridge)
			throws OpenShiftException {
		return createApplication(name, cartridge, (String) null);
	}

	@Override
	public IApplication createApplication(final String name, final IStandaloneCartridge cartridge,
			final ApplicationScale scale) throws OpenShiftException {
		return createApplication(name, cartridge, scale, null, null);
	}

	@Override
	public IApplication createApplication(final String name, final IStandaloneCartridge cartridge, String initialGitUrl)
			throws OpenShiftException {
		return createApplication(name, cartridge, null, null, initialGitUrl);
	}

	public IApplication createApplication(final String name, final IStandaloneCartridge cartridge,
			final ApplicationScale scale, String initialGitUrl) throws OpenShiftException {
		return createApplication(name, cartridge, scale, null, initialGitUrl);
	}

	@Override
	public IApplication createApplication(final String name, final IStandaloneCartridge cartridge,
			final IGearProfile gearProfile) throws OpenShiftException {
		return createApplication(name, cartridge, null, gearProfile);
	}

	public IApplication createApplication(final String name, final IStandaloneCartridge cartridge,
			final IGearProfile gearProfile, String initialGitUrl) throws OpenShiftException {
		return createApplication(name, cartridge, null, gearProfile, initialGitUrl);
	}

	@Override
	public IApplication createApplication(final String name, final IStandaloneCartridge cartridge,
			final ApplicationScale scale, final IGearProfile gearProfile) throws OpenShiftException {
		return createApplication(name, cartridge, scale, gearProfile, null);
	}

	@Override
	public IApplication createApplication(final String name, final IStandaloneCartridge cartridge,
			final ApplicationScale scale, final IGearProfile gearProfile, String initialGitUrl)
			throws OpenShiftException {
		return createApplication(name, cartridge, scale, gearProfile, initialGitUrl, IHttpClient.NO_TIMEOUT);
	}

	@Override
	public IApplication createApplication(final String name, final IStandaloneCartridge cartridge,
			final ApplicationScale scale, final IGearProfile gearProfile, String initialGitUrl, int timeout,
			IEmbeddableCartridge... cartridges)
			throws OpenShiftException {
		if (name == null) {
			throw new OpenShiftException("Application name is mandatory but none was given.");
		}
		// this would trigger lazy loading list of available applications.
		// this is needed anyhow since we're adding the new app to the list of
		// available apps
		if (hasApplicationByName(name)) {
			throw new OpenShiftException("Application with name \"{0}\" already exists.", name);
		}

		ApplicationResourceDTO applicationDTO =
				new CreateApplicationRequest().execute(name, cartridge, scale, gearProfile, initialGitUrl, timeout,
						null, cartridges);
		IApplication application = new ApplicationResource(applicationDTO, this);

		getOrLoadApplications().add(application);
		return application;
	}

	@Override
	public IApplication createApplication(final String name, final IStandaloneCartridge cartridge,
			final ApplicationScale scale, final IGearProfile gearProfile, String initialGitUrl, int timeout,
			Map<String, String> environmentVariables, IEmbeddableCartridge... cartridges)
			throws OpenShiftException {
		if (name == null) {
			throw new OpenShiftException("Application name is mandatory but none was given.");
		}
		// this would trigger lazy loading list of available applications.
		// this is needed anyhow since we're adding the new app to the list of
		// available apps
		if (hasApplicationByName(name)) {
			throw new OpenShiftException("Application with name \"{0}\" already exists.", name);
		}

		ApplicationResourceDTO applicationDTO =
				new CreateApplicationRequest().execute(
						name, cartridge, scale, gearProfile, initialGitUrl, timeout, environmentVariables, cartridges);
		IApplication application = new ApplicationResource(applicationDTO, this);

		getOrLoadApplications().add(application);
		return application;
	}

	@Override
	public boolean hasApplicationByName(String name) throws OpenShiftException {
		return getApplicationByName(name) != null;
	}

	@Override
	public IApplication getApplicationByName(String name) throws OpenShiftException {
		Assert.notNull(name);
		return getApplicationByName(name, getApplications());
	}

	private IApplication getApplicationByName(String name, Collection<IApplication> applications)
			throws OpenShiftException {
		Assert.notNull(name);

		if (applications == null) {
			return null;
		}

		IApplication matchingApplication = null;
		for (IApplication application : applications) {
			if (application.getName().equalsIgnoreCase(name)) {
				matchingApplication = application;
				break;
			}
		}
		return matchingApplication;
	}

	@Override
	public List<IApplication> getApplicationsByCartridge(IStandaloneCartridge cartridge) throws OpenShiftException {
		List<IApplication> matchingApplications = new ArrayList<IApplication>();
		for (IApplication application : getApplications()) {
			if (cartridge.equals(application.getCartridge())) {
				matchingApplications.add(application);
			}
		}
		return matchingApplications;
	}

	@Override
	public boolean hasApplicationByCartridge(IStandaloneCartridge cartridge) throws OpenShiftException {
		return getApplicationsByCartridge(cartridge).size() > 0;
	}

	@Override
	public boolean canCreateApplicationWithEnvironmentVariables() {
		try {
			Link link = getLink(LINK_ADD_APPLICATION);
			return link.hasParameter(IOpenShiftJsonConstants.PROPERTY_ENVIRONMENT_VARIABLES);
		} catch (OpenShiftRequestException e) {
			return false;
		}
	}

	@Override
	public void destroy() throws OpenShiftException {
		destroy(false);
	}

	@Override
	public void destroy(boolean force) throws OpenShiftException {
		new DeleteDomainRequest().execute(force);
		connectionResource.removeDomain(this);
	}

	@Override
	public List<IApplication> getApplications() throws OpenShiftException {
		return CollectionUtils.toUnmodifiableCopy(getOrLoadApplications());
	}

	protected List<IApplication> getOrLoadApplications() throws OpenShiftException {
		if (applications == null) {
			this.applications = loadApplications();
		}
		return applications;
	}

	/**
	 * Requests the list of application from the backend.
	 * 
	 * @return all applications that are known to the backend for this domain
	 * @throws OpenShiftException
	 */
	private List<IApplication> loadApplications() throws OpenShiftException {
		List<IApplication> applications = new ArrayList<IApplication>();
		List<ApplicationResourceDTO> applicationDTOs = new ListApplicationsRequest().execute();
		for (ApplicationResourceDTO dto : applicationDTOs) {
			applications.add(new ApplicationResource(dto, this));
		}
		return applications;
	}

	/**
	 * Updates the list of applications in this domain. It adds new
	 * applications, updates the existing ones and removes the ones that were
	 * removed in the backend.
	 * 
	 * @param applications
	 * @return
	 * @return
	 * @throws OpenShiftException
	 */
	private List<IApplication> updateApplications() throws OpenShiftException {
		List<ApplicationResourceDTO> applicationDTOs = new ListApplicationsRequest().execute();
		addOrUpdateApplications(applicationDTOs, applications);
		removeApplications(applicationDTOs, applications);
		return applications;
	}

	private List<IApplication> addOrUpdateApplications(List<ApplicationResourceDTO> dtos,
			List<IApplication> applications) throws OpenShiftException {
		for (ApplicationResourceDTO dto : dtos) {
			addOrUpdateApplication(dto, applications);
		}
		return applications;
	}

	private void addOrUpdateApplication(ApplicationResourceDTO applicationDTO, List<IApplication> applications) {
		ApplicationResource application = (ApplicationResource) getApplicationByName(applicationDTO.getName(),
				applications);
		if (application == null) {
			final IApplication newApplication = new ApplicationResource(applicationDTO, this);
			applications.add(newApplication);
		} else {
			application.update(applicationDTO);
		}
	}

	private List<IApplication> removeApplications(List<ApplicationResourceDTO> dtos, List<IApplication> applications) {
		for (ListIterator<IApplication> it = applications.listIterator(); it.hasNext();) {
			IApplication application = it.next();
			if (!hasApplicationDTOByName(application.getName(), dtos)) {
				it.remove();
			}
		}
		return applications;
	}

	private boolean hasApplicationDTOByName(String name, List<ApplicationResourceDTO> dtos) {
		for (ApplicationResourceDTO dto : dtos) {
			if (name.equals(dto.getName())) {
				return true;
			}
		}
		return false;
	}

	protected void removeApplication(IApplication application) {
		// TODO: can this collection be a null ?
		this.applications.remove(application);
	}

	@Override
	public List<String> getAvailableCartridgeNames() throws OpenShiftException {
		final List<String> cartridges = new ArrayList<String>();
		for (LinkParameter param : getLink(LINK_ADD_APPLICATION).getRequiredParams()) {
			if (param.getName().equals(IOpenShiftJsonConstants.PROPERTY_CARTRIDGE)) {
				for (String option : param.getValidOptions()) {
					cartridges.add(option);
				}
			}
		}
		return cartridges;
	}

	@Override
	public List<IGearProfile> getAvailableGearProfiles() throws OpenShiftException {
		final List<IGearProfile> gearSizes = new ArrayList<IGearProfile>();
		for (LinkParameter param : getLink(LINK_ADD_APPLICATION).getOptionalParams()) {
			if (param.getName().equals(IOpenShiftJsonConstants.PROPERTY_GEAR_PROFILE)) {
				for (String option : param.getValidOptions()) {
					gearSizes.add(new GearProfile(option));
				}
			}
		}
		return gearSizes;
	}

	@Override
	public void refresh() throws OpenShiftException {
		final DomainResourceDTO domainResourceDTO = new GetDomainRequest().execute();
		this.id = domainResourceDTO.getId();
		this.suffix = domainResourceDTO.getSuffix();
		if (applications == null) {
			// not loaded yet
			loadApplications();
		} else {
			updateApplications();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((suffix == null) ? 0 : suffix.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DomainResource other = (DomainResource) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (suffix == null) {
			if (other.suffix != null)
				return false;
		} else if (!suffix.equals(other.suffix))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Domain ["
				+ "id=" + id + ", "
				+ "suffix=" + suffix
				+ "]";
	}

	private class GetDomainRequest extends ServiceRequest {

		private GetDomainRequest() throws OpenShiftException {
			super(LINK_GET);
		}

		protected DomainResourceDTO execute() throws OpenShiftException {
			return (DomainResourceDTO) super.execute();
		}
	}

	private class ListApplicationsRequest extends ServiceRequest {

		private ListApplicationsRequest() throws OpenShiftException {
			super(LINK_LIST_APPLICATIONS);
		}

		protected <DTO> DTO execute() throws OpenShiftException {
			// ?include=cartridges
			Parameters urlParameters = new Parameters()
					.include(IOpenShiftParameterConstants.PARAMETER_CARTRIDGES);

			return super.execute(urlParameters.toList());
		}
	}

	private class CreateApplicationRequest extends ServiceRequest {

		private CreateApplicationRequest() throws OpenShiftException {
			super(LINK_ADD_APPLICATION);
		}

		protected ApplicationResourceDTO execute(final String name, IStandaloneCartridge cartridge,
				final ApplicationScale scale, final IGearProfile gearProfile, final String initialGitUrl,
				final int timeout, Map<String, String> environmentVariables,
				final IEmbeddableCartridge... embeddableCartridges)
				throws OpenShiftException {
			if (cartridge == null) {
				throw new OpenShiftException("Application cartridge is mandatory but was not given.");
			}

			Parameters parameters = new Parameters()
					.add(IOpenShiftJsonConstants.PROPERTY_NAME, name)
					.addCartridges(cartridge, embeddableCartridges)
					.scale(scale)
					.gearProfile(gearProfile)
					.add(IOpenShiftJsonConstants.PROPERTY_INITIAL_GIT_URL, initialGitUrl)
					.addEnvironmentVariables(environmentVariables);

			// ?include=cartridges
			Parameters urlParameters = new Parameters()
					.include(IOpenShiftParameterConstants.PARAMETER_CARTRIDGES);

			return execute(timeout, urlParameters.toList(), parameters.toArray());
		}
	}

	private class UpdateDomainRequest extends ServiceRequest {

		private UpdateDomainRequest() throws OpenShiftException {
			super(LINK_UPDATE);
		}

		protected DomainResourceDTO execute(String namespace) throws OpenShiftException {
			return super.execute(new StringParameter(IOpenShiftJsonConstants.PROPERTY_ID, namespace));
		}
	}

	private class DeleteDomainRequest extends ServiceRequest {

		private DeleteDomainRequest() throws OpenShiftException {
			super(LINK_DELETE);
		}

		protected void execute(boolean force) throws OpenShiftException {
			super.execute(new StringParameter(IOpenShiftJsonConstants.PROPERTY_FORCE, String.valueOf(force)));
		}
	}
}

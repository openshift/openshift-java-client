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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.openshift.client.ApplicationScale;
import com.openshift.client.HttpMethod;
import com.openshift.client.IApplication;
import com.openshift.client.IApplicationGear;
import com.openshift.client.IApplicationGearComponent;
import com.openshift.client.IApplicationPortForwarding;
import com.openshift.client.ICartridge;
import com.openshift.client.ICartridgeConstraint;
import com.openshift.client.IDomain;
import com.openshift.client.IEmbeddableCartridge;
import com.openshift.client.IEmbeddedCartridge;
import com.openshift.client.IGearProfile;
import com.openshift.client.IOpenShiftConnection;
import com.openshift.client.OpenShiftEndpointException;
import com.openshift.client.OpenShiftException;
import com.openshift.client.OpenShiftSSHOperationException;
import com.openshift.client.utils.HostUtils;
import com.openshift.client.utils.RFC822DateUtils;
import com.openshift.internal.client.response.ApplicationResourceDTO;
import com.openshift.internal.client.response.CartridgeResourceDTO;
import com.openshift.internal.client.response.GearComponentDTO;
import com.openshift.internal.client.response.GearResourceDTO;
import com.openshift.internal.client.response.Link;
import com.openshift.internal.client.response.Message;
import com.openshift.internal.client.ssh.ApplicationPortForwarding;
import com.openshift.internal.client.utils.Assert;
import com.openshift.internal.client.utils.CollectionUtils;
import com.openshift.internal.client.utils.IOpenShiftJsonConstants;

/**
 * The Class Application.
 * 
 * @author André Dietisheim
 */
public class ApplicationResource extends AbstractOpenShiftResource implements IApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationResource.class);

	private static final long APPLICATION_WAIT_RETRY_DELAY = 2 * 1024;

	private static final String LINK_GET_APPLICATION = "GET";
	private static final String LINK_DELETE_APPLICATION = "DELETE";
	private static final String LINK_START_APPLICATION = "START";
	private static final String LINK_STOP_APPLICATION = "STOP";
	private static final String LINK_STATUS_APPLICATION = "STATUS";
	private static final String LINK_FORCE_STOP_APPLICATION = "FORCE_STOP";
	private static final String LINK_RESTART_APPLICATION = "RESTART";
	private static final String LINK_SCALE_UP = "SCALE_UP";
	private static final String LINK_SCALE_DOWN = "SCALE_DOWN";
	private static final String LINK_ADD_ALIAS = "ADD_ALIAS";
	private static final String LINK_REMOVE_ALIAS = "REMOVE_ALIAS";
	private static final String LINK_ADD_CARTRIDGE = "ADD_CARTRIDGE";
	private static final String LINK_LIST_CARTRIDGES = "LIST_CARTRIDGES";
	private static final String LINK_LIST_GEARS = "GET_GEARS";

	/** The (unique) uuid of this application. */
	private final String uuid;

	/** The name of this application. */
	private final String name;

	/** The time at which this application was created. */
	private final Date creationTime;

	/** The cartridge (application type/framework) of this application. */
	private final ICartridge cartridge;

	/** The scalability enablement. */
	private final ApplicationScale scale;

	/** The application gear profile. */
	private final IGearProfile gearProfile;

	/** The domain this application belongs to. */
	private final DomainResource domain;

	/** The url of this application. */
	private final String applicationUrl;

	/** The pathat which the health of this application may be queried. */
	private final String healthCheckUrl;

	/** The url at which the git repo of this application may be reached. */
	private final String gitUrl;

	/** The aliases of this application. */
	private final List<String> aliases;

	/**
	 * List of configured embedded cartridges. <code>null</code> means list if
	 * not loaded yet.
	 */
	// TODO: replace by a map indexed by cartridge names ?
	private List<IEmbeddedCartridge> embeddedCartridges = null;

	/**
	 * List of configured gears. <code>null</code> means list if not loaded yet.
	 */
	// TODO: replace by a map indexed by cartridge names ?
	private List<IApplicationGear> gears = null;

	/**
	 * SSH Fowardable ports for the current application.
	 */
	private List<IApplicationPortForwarding> ports = null;

	/**
	 * SSH Session used to perform port-forwarding and other ssh-based
	 * operations.
	 */
	private Session session;

	private Map<String, String> embeddedCartridgesInfos;

	/**
	 * Constructor...
	 * 
	 * @param dto
	 * @param cartridge
	 * @param domain
	 */
	protected ApplicationResource(ApplicationResourceDTO dto, ICartridge cartridge, DomainResource domain) {
		this(dto.getName(), dto.getUuid(), dto.getCreationTime(), dto.getCreationLog(), dto.getApplicationUrl(), dto
				.getGitUrl(), dto.getHealthCheckPath(), dto.getGearProfile(), dto.getApplicationScale(), cartridge, dto
				.getAliases(), dto.getEmbeddedCartridgeInfos(), dto.getLinks(), domain);
	}

	/**
	 * Instantiates a new application.
	 * 
	 * @param name
	 *            the name
	 * @param uuid
	 *            the uuid
	 * @param creationTime
	 *            the creation time
	 * @param creationLog
	 *            the creation log
	 * @param applicationUrl
	 *            the application url
	 * @param gitUrl
	 *            the git url
	 * @param cartridge
	 *            the cartridge (type/framework)
	 * @param aliases
	 *            the aliases
	 * @param links
	 *            the links
	 * @param domain
	 *            the domain this application belongs to
	 * @throws DatatypeConfigurationException
	 */
	protected ApplicationResource(final String name, final String uuid, final String creationTime,
			final List<Message> creationLog, final String applicationUrl, final String gitUrl,
			final String healthCheckPath, final IGearProfile gearProfile, final ApplicationScale scale,
			final ICartridge cartridge, final List<String> aliases, final Map<String, String> embeddedCartridgesInfos,
			final Map<String, Link> links, final DomainResource domain) {
		super(domain.getService(), links, creationLog);
		this.name = name;
		this.uuid = uuid;
		this.creationTime = RFC822DateUtils.safeGetDate(creationTime);
		this.scale = scale;
		this.gearProfile = gearProfile;
		this.cartridge = cartridge;
		this.applicationUrl = applicationUrl;
		this.gitUrl = gitUrl;
		this.healthCheckUrl = applicationUrl + healthCheckPath;
		this.domain = domain;
		this.aliases = aliases;
		// TODO: fix this workaround once
		// https://bugzilla.redhat.com/show_bug.cgi?id=812046 is fixed
		this.embeddedCartridgesInfos = embeddedCartridgesInfos;
	}

	public String getName() {
		return name;
	}

	@Override
	public ApplicationScale getApplicationScale() {
		return scale;
	}

	@Override
	public IGearProfile getGearProfile() {
		return gearProfile;
	}

	public String getUUID() {
		return uuid;
	}

	public ICartridge getCartridge() {
		return cartridge;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public IDomain getDomain() {
		return this.domain;
	}

	public void destroy() throws OpenShiftException {
		new DeleteApplicationRequest().execute();
		domain.removeApplication(this);
	}

	public void start() throws OpenShiftException {
		new StartApplicationRequest().execute();
	}

	public void restart() throws OpenShiftException {
		new RestartApplicationRequest().execute();
	}

	public void stop() throws OpenShiftException {
		stop(false);
	}

	public void stop(boolean force) throws OpenShiftException {
		if (force) {
			new ForceStopApplicationRequest().execute();
		} else {
			new StopApplicationRequest().execute();

		}
	}

	public void getDescriptor() {
		throw new UnsupportedOperationException();
	}

	public void scaleDown() throws OpenShiftException {
		new ScaleDownRequest().execute();
	}

	public void scaleUp() throws OpenShiftException {
		new ScaleUpRequest().execute();
	}

	public void addAlias(String alias) throws OpenShiftException {
		Assert.notNull(alias);

		ApplicationResourceDTO applicationDTO = new AddAliasRequest().execute(alias);
		updateAliases(applicationDTO);

	}

	private void updateAliases(ApplicationResourceDTO applicationDTO) {
		Assert.notNull(applicationDTO);

		this.aliases.clear();
		this.aliases.addAll(applicationDTO.getAliases());
	}

	public List<String> getAliases() {
		return Collections.unmodifiableList(this.aliases);
	}

	public boolean hasAlias(String name) {
		Assert.notNull(name);

		return aliases.contains(name);
	}

	public void removeAlias(String alias) throws OpenShiftException {
		Assert.notNull(alias);

		ApplicationResourceDTO applicationDTO = new RemoveAliasRequest().execute(alias);
		updateAliases(applicationDTO);
	}

	public String getGitUrl() {
		return this.gitUrl;
	}

	public String getApplicationUrl() {
		return applicationUrl;
	}

	public String getHealthCheckUrl() {
		return healthCheckUrl;
	}

	protected String getHealthCheckSuccessResponse() throws OpenShiftException {
		return "1";
	}

	public List<IEmbeddedCartridge> addEmbeddableCartridge(ICartridgeConstraint cartridgeConstraint) throws OpenShiftException {
		Collection<IEmbeddableCartridge> cartridges = (Collection<IEmbeddableCartridge>) cartridgeConstraint.getMatching(getConnection().getEmbeddableCartridges());
		return addEmbeddableCartridges(cartridges);
	}

	/**
	 * Adds the given embedded cartridge to this application.
	 * 
	 * @param cartridge
	 *            the embeddable cartridge that shall be added to this
	 *            application
	 */
	public IEmbeddedCartridge addEmbeddableCartridge(IEmbeddableCartridge cartridge) throws OpenShiftException {
		Assert.notNull(cartridge);

		if (this.embeddedCartridges == null) {
			loadEmbeddedCartridges();
		}
		final CartridgeResourceDTO embeddedCartridgeDTO =
				new AddEmbeddedCartridgeRequest().execute(cartridge.getName());
		final EmbeddedCartridgeResource embeddedCartridge =
				new EmbeddedCartridgeResource(
						embeddedCartridgesInfos.get(embeddedCartridgeDTO.getName()),
						embeddedCartridgeDTO, this);
		this.embeddedCartridges.add(embeddedCartridge);
		return embeddedCartridge;
	}

	public List<IEmbeddedCartridge> addEmbeddableCartridges(Collection<IEmbeddableCartridge> cartridges)
			throws OpenShiftException {
		Assert.notNull(cartridges);

		final List<IEmbeddedCartridge> addedCartridge = new ArrayList<IEmbeddedCartridge>();
		for (IEmbeddableCartridge cartridge : cartridges) {
			// TODO: catch exceptions when removing cartridges, contine removing
			// and report the exceptions that occurred
			addedCartridge.add(addEmbeddableCartridge(cartridge));
		}
		return addedCartridge;
	}

	/**
	 * "callback" from the embeddedCartridge once it has been destroyed.
	 * 
	 * @param embeddedCartridge
	 * @throws OpenShiftException
	 */
	protected void removeEmbeddedCartridge(IEmbeddedCartridge embeddedCartridge) throws OpenShiftException {
		Assert.notNull(embeddedCartridge);

		this.embeddedCartridges.remove(embeddedCartridge);
	}

	private List<IEmbeddedCartridge> loadEmbeddedCartridges() throws OpenShiftException {
		// load collection if necessary
		this.embeddedCartridges = new ArrayList<IEmbeddedCartridge>();
		List<CartridgeResourceDTO> embeddableCartridgeDTOs = new ListEmbeddableCartridgesRequest().execute();
		for (CartridgeResourceDTO embeddableCartridgeDTO : embeddableCartridgeDTOs) {
			IEmbeddedCartridge embeddableCartridge =
					new EmbeddedCartridgeResource(
							embeddedCartridgesInfos.get(embeddableCartridgeDTO.getName()),
							embeddableCartridgeDTO, this);
			embeddedCartridges.add(embeddableCartridge);
		}
		return embeddedCartridges;
	}

	public List<IEmbeddedCartridge> getEmbeddedCartridges() throws OpenShiftException {
		if (this.embeddedCartridges == null) {
			this.embeddedCartridges = loadEmbeddedCartridges();
		}
		return CollectionUtils.toUnmodifiableCopy(this.embeddedCartridges);
	}

	public boolean hasEmbeddedCartridge(String cartridgeName) throws OpenShiftException {
		Assert.notNull(cartridgeName);

		return getEmbeddedCartridge(cartridgeName) != null;
	}

	public boolean hasEmbeddedCartridge(IEmbeddableCartridge cartridge) throws OpenShiftException {
		return getEmbeddedCartridge(cartridge) != null;
	}

	public IEmbeddedCartridge getEmbeddedCartridge(IEmbeddableCartridge cartridge) throws OpenShiftException {
		Assert.notNull(cartridge);

		return getEmbeddedCartridge(cartridge.getName());
	}

	public Collection<IEmbeddedCartridge> getEmbeddedCartridges(ICartridgeConstraint constraint) throws OpenShiftException {
		Assert.notNull(constraint);

		return constraint.getMatching(getEmbeddedCartridges());
	}
	
	public IEmbeddedCartridge getEmbeddedCartridge(String cartridgeName) throws OpenShiftException {
		Assert.notNull(cartridgeName);

		for (IEmbeddedCartridge embeddedCartridge : getEmbeddedCartridges()) {
			if (cartridgeName.equals(embeddedCartridge.getName())) {
				return embeddedCartridge;
			}
		}
		return null;
	}
	
	public void removeEmbeddedCartridges(ICartridgeConstraint cartridgeConstraint) throws OpenShiftException {
		Collection<IEmbeddableCartridge> embeddableCartridges = cartridgeConstraint.getMatching(getConnection().getEmbeddableCartridges());
		removeEmbeddedCartridges(embeddableCartridges);
	}
	
	public void removeEmbeddedCartridge(IEmbeddableCartridge cartridge) throws OpenShiftException {
		Assert.notNull(cartridge);

		IEmbeddedCartridge embeddedCartridge = getEmbeddedCartridge(cartridge);
		if (embeddedCartridge != null) {
			embeddedCartridge.destroy();
		}
	}

	public void removeEmbeddedCartridges(Collection<IEmbeddableCartridge> cartridges) throws OpenShiftException {
		Assert.notNull(cartridges);

		for(IEmbeddableCartridge cartridge : cartridges) {
			// TODO: catch exceptions when removing cartridges, contine removing
			// and report the exceptions that occurred
			removeEmbeddedCartridge(cartridge);
		}
	}

	/**
	 * Returns the gears that this application is running on.
	 * 
	 * @return
	 * 
	 * @return the gears
	 * @throws OpenShiftException
	 */
	public List<IApplicationGear> getGears() throws OpenShiftException {
		// load collection if necessary
		if (gears == null) {
			this.gears = loadGears();
		}
		return Collections.unmodifiableList(gears);
	}

	/**
	 * @return
	 * @throws OpenShiftException
	 */
	private List<IApplicationGear> loadGears() throws OpenShiftException {
		this.gears = new ArrayList<IApplicationGear>();
		List<GearResourceDTO> gearDTOs = new ListGearsRequest().execute();
		for (GearResourceDTO gearDTO : gearDTOs) {
			final List<IApplicationGearComponent> components = new ArrayList<IApplicationGearComponent>();
			for (GearComponentDTO gearComponentDTO : gearDTO.getComponents()) {
				components.add(new ApplicationGearComponentResource(gearComponentDTO));
			}
			IApplicationGear gear = new ApplicationGearResource(gearDTO, components, this);
			gears.add(gear);
		}
		return gears;
	}

	public boolean waitForAccessible(long timeout) throws OpenShiftException {
		try {
			long startTime = System.currentTimeMillis();

			if (!waitForResolved(timeout, startTime)) {
				return false;
			}

			return waitForPositiveHealthResponse(timeout, startTime);
		} catch (InterruptedException e) {
			return false;
		}
	}

	public Future<Boolean> waitForAccessibleAsync(final long timeout) throws OpenShiftException {
		IOpenShiftConnection connection = getConnection();
		return connection.getExecutorService().submit(new Callable<Boolean>() {

			public Boolean call() throws Exception {
				return waitForAccessible(timeout);
			}
		});
	}
	
	public String getCartridge(String cartridgeName) throws OpenShiftException {
		Assert.notNull(cartridgeName);

		return new GetCartridgeRequest().execute(cartridgeName);

	}

	protected IOpenShiftConnection getConnection() {
		return getDomain().getUser().getConnection();
	}

	private boolean waitForPositiveHealthResponse(long timeout, long startTime) throws OpenShiftException,
			InterruptedException, OpenShiftEndpointException {
		String response = null;
		while (!isPositiveHealthResponse(response)
				&& !isTimeouted(timeout, startTime)) {
			try {
				Thread.sleep(APPLICATION_WAIT_RETRY_DELAY);
				response = getService().request(healthCheckUrl, HttpMethod.GET, null);
			} catch (OpenShiftEndpointException e) {
				throw e;
			} catch (OpenShiftException e) {
			}
		}
		return isPositiveHealthResponse(response);
	}

	private boolean isPositiveHealthResponse(String response) throws OpenShiftException {
		return response != null
				&& response.startsWith(getHealthCheckSuccessResponse());
	}

	private boolean waitForResolved(long timeout, long startTime) throws OpenShiftException, InterruptedException {
		try {
			while (!HostUtils.canResolv(healthCheckUrl)
					&& !isTimeouted(timeout, startTime)) {
				Thread.sleep(APPLICATION_WAIT_RETRY_DELAY);
			}
			return HostUtils.canResolv(healthCheckUrl);
		} catch (MalformedURLException e) {
			throw new OpenShiftException(e,
					"Could not wait for application {0} to become accessible, it has an invalid URL \"{1}\": {2}",
					name, healthCheckUrl, e.getMessage());
		}
	}

	private boolean isTimeouted(long timeout, long startTime) {
		return !(System.currentTimeMillis() < (startTime + timeout));
	}

	public void refresh() throws OpenShiftException {
		if (this.embeddedCartridges != null) {
			this.embeddedCartridges = loadEmbeddedCartridges();
		}
		if (this.gears != null) {
			this.gears = loadGears();
		}
		if (this.ports != null) {
			this.ports = loadPorts();
		}
	}

	public void setSSHSession(final Session session) {
		this.session = session;
	}

	public Session getSSHSession() {
		return this.session;
	}

	public boolean hasSSHSession() {
		return this.session != null && this.session.isConnected();
	}

	public boolean isPortFowardingStarted() throws OpenShiftSSHOperationException {
		try {
			return this.session != null && this.session.isConnected() && this.session.getPortForwardingL().length > 0;
		} catch (JSchException e) {
			throw new OpenShiftSSHOperationException(e,
					"Unable to verify if port-forwarding has been started for application \"{0}\"", this.getName());
		}
	}

	public List<IApplicationPortForwarding> refreshForwardablePorts() throws OpenShiftSSHOperationException {
		this.ports = loadPorts();
		return this.ports;
	}

	public List<IApplicationPortForwarding> getForwardablePorts() throws OpenShiftSSHOperationException {
		if (ports == null) {
			this.ports = loadPorts();
		}
		return ports;
	}

	@Override
	public List<String> getEnvironmentProperties() throws OpenShiftSSHOperationException {
		List<String> openshiftProps = new ArrayList<String>();
		List<String> allEnvProps = sshExecCmd("set", EnumSshStream.INPUT);
		for (String line : allEnvProps) {
			if (line.startsWith("OPENSHIFT_") || line.startsWith("JENKINS_")) {
				openshiftProps.add(line);
			}
		}
		return openshiftProps;
	}

	/**
	 * List all forwardable ports for a given application.
	 * 
	 * @param application
	 * @return the forwardable ports in an unmodifiable collection
	 * @throws JSchException
	 * @throws OpenShiftSSHOperationException
	 */
	private List<IApplicationPortForwarding> loadPorts() throws OpenShiftSSHOperationException {
		this.ports = new ArrayList<IApplicationPortForwarding>();
		List<String> lines = sshExecCmd("rhc-list-ports", EnumSshStream.EXT_INPUT);
		for (String line : lines) {
			ApplicationPortForwarding port = extractForwardablePortFrom(this, line);
			if (port != null) {
				ports.add(port);
			}
		}
		return ports;
	}

	private enum EnumSshStream {
		EXT_INPUT, INPUT;
	}

	/**
	 * 
	 * @param command
	 * @return
	 * @throws OpenShiftSSHOperationException
	 */
	private List<String> sshExecCmd(final String command, final EnumSshStream streamToUse)
			throws OpenShiftSSHOperationException {
		final Session session = getSSHSession();
		if (session == null) {
			throw new OpenShiftSSHOperationException("No SSH session available for application ''{0}''", this.getName());
		}
		Channel channel = null;
		BufferedReader reader = null;
		try {
			session.openChannel("exec");
			channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);
			channel.connect();
			List<String> lines = new ArrayList<String>();
			String line;
			// Read File Line By Line, but from only one of the 2 inputstreams
			// (the other one blocks..)
			switch (streamToUse) {
			case EXT_INPUT:
				reader = new BufferedReader(new InputStreamReader(channel.getExtInputStream()));
				break;
			case INPUT:
				reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));
				break;
			}
			// Read File Line By Line
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}

			return lines;
		} catch (JSchException e) {
			throw new OpenShiftSSHOperationException(e, "Failed to list forwardable ports for application \"{0}\"",
					this.getName());
		} catch (IOException e) {
			throw new OpenShiftSSHOperationException(e, "Failed to list forwardable ports for application \"{0}\"",
					this.getName());
		} finally {

			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					LOGGER.error("Failed to close SSH error stream reader", e);
				}
			}

			if (channel != null && channel.isConnected()) {
				channel.disconnect();
			}
		}
	}

	/**
	 * Extract the named forwardable port from the 'rhc-list-ports' command
	 * result line, with the following format:
	 * <code>java -> 127.10.187.1:4447</code>.
	 * 
	 * @param portValue
	 * @return the forwardable port.
	 */
	private static ApplicationPortForwarding extractForwardablePortFrom(final IApplication application,
			final String portValue) {
		if (portValue.contains("->")) {
			try {
				final StringTokenizer nameTokenizer = new StringTokenizer(portValue, "->");
				final String name = nameTokenizer.nextToken().trim();
				final StringTokenizer ipPortTokenizer = new StringTokenizer(nameTokenizer.nextToken(), ":");
				final String remoteIp = ipPortTokenizer.nextToken().trim();
				final int remotePort = Integer.parseInt(ipPortTokenizer.nextToken().trim());
				return new ApplicationPortForwarding(application, name, remoteIp, remotePort);
			} catch (NoSuchElementException e) {
				LOGGER.error("Failed to parse remote port: " + portValue, e);
				throw e;
			}
		}
		return null;
	}

	public List<IApplicationPortForwarding> startPortForwarding() throws OpenShiftSSHOperationException {
		if (!hasSSHSession()) {
			throw new OpenShiftSSHOperationException(
					"SSH session for application \"{0}\" is closed or null. Cannot start port forwarding",
					this.getName());
		}
		for (IApplicationPortForwarding port : ports) {
			try {
				port.start(session);
			} catch (OpenShiftSSHOperationException oss) {
				// ignore for now
				// FIXME: should store this error on the forward to let user
				// know why it could not start/stop
			}
		}
		return ports;
	}

	public List<IApplicationPortForwarding> stopPortForwarding() throws OpenShiftSSHOperationException {
		for (IApplicationPortForwarding port : ports) {
			try {
				port.stop(session);
			} catch (OpenShiftSSHOperationException oss) {
				// ignore for now
				// should store this error on the forward to let user know why
				// it could not start/stop
			}
		}
		// make sure port forwarding is stopped by closing session...
		session.disconnect();
		return ports;
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
		ApplicationResource other = (ApplicationResource) object;
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

	private class RefreshApplicationRequest extends ServiceRequest {

		protected RefreshApplicationRequest() {
			super(LINK_GET_APPLICATION);
		}
	}

	private class DeleteApplicationRequest extends ServiceRequest {

		protected DeleteApplicationRequest() {
			super(LINK_DELETE_APPLICATION);
		}
	}

	private class StartApplicationRequest extends ServiceRequest {

		protected StartApplicationRequest() {
			super(LINK_START_APPLICATION);
		}

		public <DTO> DTO execute() throws OpenShiftException {
			return super.execute(new ServiceParameter(IOpenShiftJsonConstants.PROPERTY_EVENT,
					IOpenShiftJsonConstants.VALUE_START));
		}
	}

	private class StopApplicationRequest extends ServiceRequest {

		protected StopApplicationRequest() {
			super(LINK_STOP_APPLICATION);
		}

		public <DTO> DTO execute() throws OpenShiftException {
			return super.execute(new ServiceParameter(IOpenShiftJsonConstants.PROPERTY_EVENT,
					IOpenShiftJsonConstants.VALUE_STOP));
		}
	}

	private class ForceStopApplicationRequest extends ServiceRequest {

		protected ForceStopApplicationRequest() {
			super(LINK_FORCE_STOP_APPLICATION);
		}

		public <DTO> DTO execute() throws OpenShiftException {
			return super.execute(new ServiceParameter(IOpenShiftJsonConstants.PROPERTY_EVENT,
					IOpenShiftJsonConstants.VALUE_FORCESTOP));
		}
	}

	private class RestartApplicationRequest extends ServiceRequest {

		protected RestartApplicationRequest() {
			super(LINK_RESTART_APPLICATION);
		}

		public <DTO> DTO execute() throws OpenShiftException {
			return super.execute(new ServiceParameter(IOpenShiftJsonConstants.PROPERTY_EVENT,
					IOpenShiftJsonConstants.VALUE_RESTART));
		}
	}

	private class ScaleUpRequest extends ServiceRequest {

		protected ScaleUpRequest() {
			super(LINK_SCALE_UP);
		}

		public <DTO> DTO execute() throws OpenShiftException {
			return super.execute(new ServiceParameter(IOpenShiftJsonConstants.PROPERTY_EVENT,
					IOpenShiftJsonConstants.VALUE_SCALE_UP));
		}
	}

	private class ScaleDownRequest extends ServiceRequest {

		protected ScaleDownRequest() {
			super(LINK_SCALE_DOWN);
		}

		public <DTO> DTO execute() throws OpenShiftException {
			return super.execute(new ServiceParameter(IOpenShiftJsonConstants.PROPERTY_EVENT,
					IOpenShiftJsonConstants.VALUE_SCALE_DOWN));
		}
	}

	private class AddAliasRequest extends ServiceRequest {

		protected AddAliasRequest() {
			super(LINK_ADD_ALIAS);
		}

		public <DTO> DTO execute(String alias) throws OpenShiftException {
			return super.execute(new ServiceParameter(IOpenShiftJsonConstants.PROPERTY_EVENT,
					IOpenShiftJsonConstants.VALUE_ADD_ALIAS), new ServiceParameter(
					IOpenShiftJsonConstants.PROPERTY_ALIAS, alias));
		}
	}

	private class RemoveAliasRequest extends ServiceRequest {

		protected RemoveAliasRequest() {
			super(LINK_REMOVE_ALIAS);
		}

		public <DTO> DTO execute(String alias) throws OpenShiftException {
			return super.execute(new ServiceParameter(IOpenShiftJsonConstants.PROPERTY_EVENT,
					IOpenShiftJsonConstants.VALUE_REMOVE_ALIAS), new ServiceParameter(
					IOpenShiftJsonConstants.PROPERTY_ALIAS, alias));
		}
	}

	private class AddEmbeddedCartridgeRequest extends ServiceRequest {

		protected AddEmbeddedCartridgeRequest() {
			super(LINK_ADD_CARTRIDGE);
		}

		public <DTO> DTO execute(String embeddedCartridgeName) throws OpenShiftException {
			return super
					.execute(new ServiceParameter(IOpenShiftJsonConstants.PROPERTY_CARTRIDGE, embeddedCartridgeName));
		}
	}

	private class ListEmbeddableCartridgesRequest extends ServiceRequest {

		protected ListEmbeddableCartridgesRequest() {
			super(LINK_LIST_CARTRIDGES);
		}
	}
	
	private class GetCartridgeRequest extends ServiceRequest {

		protected GetCartridgeRequest() {
			super(LINK_LIST_CARTRIDGES);
		}
		
		public String execute(String cartridgeName) throws OpenShiftException {
			return super
					.execute(cartridgeName);
		}
	}

	private class ListGearsRequest extends ServiceRequest {

		protected ListGearsRequest() {
			super(LINK_LIST_GEARS);
		}
	}
	
	private class CartridgeSAXParser extends DefaultHandler {

		Properties props = new Properties();
		boolean inProperty = false;
		boolean inName = false;
		boolean inDescription = false;
		String tmpName = null;
		String tmpDescription = null;
		
		public CartridgeSAXParser(){
			props = new Properties();
		}
		
		public Properties getProperties() {
			return props;
		}

		public void parseDocument(String xml) {
			
			//get a factory
			SAXParserFactory spf = SAXParserFactory.newInstance();
			InputStream is = null;
			try {
			
				//get a new instance of parser
				SAXParser sp = spf.newSAXParser();
				
				is = new ByteArrayInputStream(xml.getBytes());
				sp.parse(is, this);
				
			} catch(SAXException se) {
				se.printStackTrace();
			} catch(ParserConfigurationException pce) {
				pce.printStackTrace();
			} catch (IOException ie) {
				ie.printStackTrace();
			} finally {
				if (is != null)
					try { is.close(); } catch (IOException e){}
			}
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (qName.equalsIgnoreCase("property")) {
				inProperty = true;
			} else if (inProperty) {
				if (qName.equalsIgnoreCase("name")) {
					inName = true;
				} else if (qName.equalsIgnoreCase("description")) {
					inDescription = true;
				}
			}
		}
		

		public void characters(char[] ch, int start, int length) throws SAXException {
			if (inName)
				tmpName = new String(ch,start,length);
			else if (inDescription)
				tmpDescription = new String(ch,start,length);
		}
		
		public void endElement(String uri, String localName, String qName) throws SAXException {

			if (inProperty){
				if (qName.equalsIgnoreCase("property")) {
					inProperty = false;
				} else if (qName.equalsIgnoreCase("name")) {
					inName = false;
				} else if (qName.equalsIgnoreCase("description")) {
					inDescription = false;
					props.put(tmpName, tmpDescription);
				}
			}
		}
		
	}
}

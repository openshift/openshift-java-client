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

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;

import com.openshift.express.internal.client.InternalUser;
import com.openshift.express.internal.client.UserInfo;
import com.openshift.express.internal.client.httpclient.BadRequestException;
import com.openshift.express.internal.client.httpclient.HttpClientException;
import com.openshift.express.internal.client.httpclient.InternalServerErrorException;
import com.openshift.express.internal.client.httpclient.NotFoundException;
import com.openshift.express.internal.client.httpclient.UnauthorizedException;
import com.openshift.express.internal.client.httpclient.UrlConnectionHttpClient;
import com.openshift.express.internal.client.request.AbstractDomainRequest;
import com.openshift.express.internal.client.request.ApplicationAction;
import com.openshift.express.internal.client.request.ApplicationRequest;
import com.openshift.express.internal.client.request.ChangeDomainRequest;
import com.openshift.express.internal.client.request.CreateDomainRequest;
import com.openshift.express.internal.client.request.EmbedAction;
import com.openshift.express.internal.client.request.EmbedRequest;
import com.openshift.express.internal.client.request.ListCartridgesRequest;
import com.openshift.express.internal.client.request.OpenShiftEnvelopeFactory;
import com.openshift.express.internal.client.request.UserInfoRequest;
import com.openshift.express.internal.client.request.marshalling.ApplicationRequestJsonMarshaller;
import com.openshift.express.internal.client.request.marshalling.DomainRequestJsonMarshaller;
import com.openshift.express.internal.client.request.marshalling.EmbedRequestJsonMarshaller;
import com.openshift.express.internal.client.request.marshalling.ListCartridgesRequestJsonMarshaller;
import com.openshift.express.internal.client.request.marshalling.UserInfoRequestJsonMarshaller;
import com.openshift.express.internal.client.response.OpenShiftResponse;
import com.openshift.express.internal.client.response.unmarshalling.ApplicationResponseUnmarshaller;
import com.openshift.express.internal.client.response.unmarshalling.ApplicationStatusResponseUnmarshaller;
import com.openshift.express.internal.client.response.unmarshalling.DomainResponseUnmarshaller;
import com.openshift.express.internal.client.response.unmarshalling.EmbedResponseUnmarshaller;
import com.openshift.express.internal.client.response.unmarshalling.JsonSanitizer;
import com.openshift.express.internal.client.response.unmarshalling.ListCartridgesResponseUnmarshaller;
import com.openshift.express.internal.client.response.unmarshalling.ListEmbeddableCartridgesResponseUnmarshaller;
import com.openshift.express.internal.client.response.unmarshalling.UserInfoResponseUnmarshaller;

/**
 * @author André Dietisheim
 */
public class OpenShiftService implements IOpenShiftService {
	
	// TODO extract to properties file
	private static final String USERAGENT_FORMAT = "Java OpenShift/{0} ({1})";
	
	private static final String MALFORMED_URL_EXCEPTION_MSG = "Application URL {0} is invalid";

	private static final long APPLICATION_WAIT_DELAY = 2;

	private String baseUrl;
	private String id;
	private boolean ignoreCertCheck;

	public OpenShiftService(String id, String baseUrl) {
		this.id = id;
		this.baseUrl = baseUrl;
	}
	
	public void setIgnoreCertCheck(boolean ignoreCertCheck) {
		this.ignoreCertCheck = ignoreCertCheck;
	}
	
	public void setProxySet(boolean proxySet) {
		if (proxySet)
			System.setProperty("proxySet", "true");
		else
			System.setProperty("proxySet", "false");
	}
	
	public void setProxyHost(String proxyHost) {
		System.setProperty("proxyHost", proxyHost);
	}
	
	public void setProxyPort(String proxyPort) {
		System.setProperty("proxyPort", proxyPort);
	}

	public String getServiceUrl() {
		return baseUrl + SERVICE_PATH;
	}

	public String getPlatformUrl() {
		return baseUrl;
	}

	public boolean isValid(final IUser user) throws OpenShiftException {
		return getUserInfo(user) != null;
	}

	public UserInfo getUserInfo(final IUser user) throws OpenShiftException {
		UserInfoRequest userInfoRequest = new UserInfoRequest(user.getRhlogin(), true);
		String url = userInfoRequest.getUrlString(getServiceUrl());
			
		String request = new UserInfoRequestJsonMarshaller().marshall(userInfoRequest);
		String response = sendRequest(request, url, user.getPassword(), user.getAuthKey(), user.getAuthIV(),
				"Could not get user info for user \"{0}\" at \"{1}\"");
		OpenShiftResponse<UserInfo> userInfoResponse =
				new UserInfoResponseUnmarshaller().unmarshall(response);
		return userInfoResponse.getOpenShiftObject();
	}
	
	public List<IEmbeddableCartridge> getEmbeddableCartridges(final IUser user) throws OpenShiftException {
		ListCartridgesRequest listCartridgesRequest = new ListCartridgesRequest(
				ListCartridgesRequest.CartridgeType.EMBEDDED, user.getRhlogin(), true);
		String url = listCartridgesRequest.getUrlString(getServiceUrl());
		String request =
				new ListCartridgesRequestJsonMarshaller().marshall(listCartridgesRequest);
		String response = sendRequest(request, url, user.getPassword(), user.getAuthKey(), user.getAuthIV(),
				MessageFormat.format("Could not list available embeddable cartridges at \"{0}\"", url));
		OpenShiftResponse<List<IEmbeddableCartridge>> listCartridgesResponse =
				new ListEmbeddableCartridgesResponseUnmarshaller().unmarshall(response);
		return listCartridgesResponse.getOpenShiftObject();
	}

	public List<ICartridge> getCartridges(final IUser user) throws OpenShiftException {
		ListCartridgesRequest listCartridgesRequest = new ListCartridgesRequest(
				ListCartridgesRequest.CartridgeType.STANDALONE, user.getRhlogin(), true);
		String url = listCartridgesRequest.getUrlString(getServiceUrl());
		String request =
				new ListCartridgesRequestJsonMarshaller().marshall(listCartridgesRequest);
		String response = sendRequest(request, url, user.getPassword(), user.getAuthKey(), user.getAuthIV(),
				MessageFormat.format("Could not list embeddable cartridges at \"{0}\"", url));
		OpenShiftResponse<List<ICartridge>> cartridgesResponse =
				new ListCartridgesResponseUnmarshaller().unmarshall(response);
		return cartridgesResponse.getOpenShiftObject();
	}


	public IDomain createDomain(final String name, final ISSHPublicKey sshKey, final IUser user) throws OpenShiftException {
		return requestDomainAction(new CreateDomainRequest(name, sshKey, user.getRhlogin(), true), user);
	}

	public IDomain changeDomain(final String newName, final ISSHPublicKey sshKey, final IUser user) throws OpenShiftException {
		return requestDomainAction(new ChangeDomainRequest(newName, sshKey, user.getRhlogin(), true), user);
	}

	protected IDomain requestDomainAction(final AbstractDomainRequest domainRequest, final IUser user)
			throws OpenShiftException {
		String url = domainRequest.getUrlString(getServiceUrl());
		String request = new DomainRequestJsonMarshaller().marshall(domainRequest);
		String response = sendRequest(request, url, user.getPassword(), user.getAuthKey(), user.getAuthIV(),
				MessageFormat.format("Could not {0}", domainRequest.getOperation()));
		OpenShiftResponse<IDomain> domainResponse =
				new DomainResponseUnmarshaller(domainRequest.getName(), user, this).unmarshall(response);
		return domainResponse.getOpenShiftObject();
	}

	public IApplication createApplication(final String name, final ICartridge cartridge, final IUser user)
			throws OpenShiftException {
		IApplication application = requestApplicationAction(
				new ApplicationRequest(name, cartridge, ApplicationAction.CONFIGURE, user.getRhlogin(), true),
				user);
		return application;
	}

	public void destroyApplication(final String name, final ICartridge cartridge, final IUser user) throws OpenShiftException {
		requestApplicationAction(
				new ApplicationRequest(name, cartridge, ApplicationAction.DECONFIGURE, user.getRhlogin(), true),
				user);
	}

	public IApplication startApplication(final String name, final ICartridge cartridge, final IUser user)
			throws OpenShiftException {
		return requestApplicationAction(
				new ApplicationRequest(name, cartridge, ApplicationAction.START, user.getRhlogin(), true),
				user);
	}


	public IApplication restartApplication(final String name, final ICartridge cartridge, final IUser user)
			throws OpenShiftException {
		return requestApplicationAction(
				new ApplicationRequest(name, cartridge, ApplicationAction.RESTART, user.getRhlogin(), true),
				user);
	}

	public IApplication stopApplication(final String name, final ICartridge cartridge, final IUser user) throws OpenShiftException {
		return requestApplicationAction(
				new ApplicationRequest(name, cartridge, ApplicationAction.STOP, user.getRhlogin(), true),
				user);
	}

	public String getStatus(final String applicationName, final ICartridge cartridge, final IUser user) throws OpenShiftException {
		ApplicationRequest applicationRequest =
				new ApplicationRequest(applicationName, cartridge, ApplicationAction.STATUS, user.getRhlogin(), true);
		String url = applicationRequest.getUrlString(getServiceUrl());
		String request =
				new ApplicationRequestJsonMarshaller().marshall(applicationRequest);
		String response = sendRequest(request, url, user.getPassword(), user.getAuthKey(), user.getAuthIV(),
				MessageFormat.format("Could not {0} application \"{1}\" at \"{2}\"",
						applicationRequest.getAction().getCommand(), applicationRequest.getName(), url));
		OpenShiftResponse<String> openshiftResponse =
				new ApplicationStatusResponseUnmarshaller().unmarshall(response);
		return openshiftResponse.getOpenShiftObject();
	}

	protected IApplication requestApplicationAction(final ApplicationRequest applicationRequest, final IUser user)
			throws OpenShiftException {
		String url = applicationRequest.getUrlString(getServiceUrl());
		String request = new ApplicationRequestJsonMarshaller().marshall(applicationRequest);
		String response = sendRequest(request, url, user.getPassword(), user.getAuthKey(), user.getAuthIV(),
				MessageFormat.format("Could not {0} application \"{1}\" at \"{2}\"",
						applicationRequest.getAction().getCommand(), applicationRequest.getName(), url));
		OpenShiftResponse<IApplication> openshiftResponse =
				new ApplicationResponseUnmarshaller(applicationRequest.getName(),
						applicationRequest.getCartridge(), user, this).unmarshall(response);
		return openshiftResponse.getOpenShiftObject();
	}
	
	public boolean waitForApplication(final IApplication application, final long timeout) 
			throws OpenShiftException {
		try {
			IHttpClient client = createHttpClient(id, application.getApplicationUrl(), false);
			String response = null;
			long startTime = System.currentTimeMillis();
			while (response == null
					&& System.currentTimeMillis() < startTime + timeout) {
				try {
					Thread.sleep(APPLICATION_WAIT_DELAY);
					response = client.get();
				} catch (InternalServerErrorException e) {
					return true;
				} catch (BadRequestException e) {
					return true;
				} catch (NotFoundException e) {
					return true;
				} catch (HttpClientException e) {
					// not available yet
				}
			}
		} catch (InterruptedException e) {
			return false;
		} catch (MalformedURLException e) {
			throw new OpenShiftException(e, MALFORMED_URL_EXCEPTION_MSG, application.getApplicationUrl());
		}
		return true;
	}

	public IEmbeddableCartridge addEmbeddedCartridge(final String applicationName, final IEmbeddableCartridge cartridge,
			IUser user) throws OpenShiftException {
		return requestEmbedAction(
				new EmbedRequest(applicationName, cartridge, EmbedAction.ADD, user.getRhlogin(), true)
				, user);
	}
	
	public void removeEmbeddedCartridge(final String applicationName, final IEmbeddableCartridge cartridge,
			final IUser user) throws OpenShiftException {
		requestEmbedAction(
				new EmbedRequest(applicationName, cartridge, EmbedAction.REMOVE, user.getRhlogin(), true)
				, user);
	}

	protected IEmbeddableCartridge requestEmbedAction(final EmbedRequest embedRequest, final IUser user)
			throws OpenShiftException {
		String url = embedRequest.getUrlString(getServiceUrl());
		String request = new EmbedRequestJsonMarshaller().marshall(embedRequest);
		String response = sendRequest(request, url, user.getPassword(), user.getAuthKey(), user.getAuthIV(),
				MessageFormat.format("Could not {0} application \"{1}\" at \"{2}\"",
						embedRequest.getAction().getCommand(), embedRequest.getName(), url));
		OpenShiftResponse<IEmbeddableCartridge> openshiftResponse =
				new EmbedResponseUnmarshaller(embedRequest.getEmbeddableCartridge())
						.unmarshall(response);
		return openshiftResponse.getOpenShiftObject();
	}
	
	private String sendRequest(final String request, final String url, final String password, final String authKey, final String authIV, final String errorMessage) throws OpenShiftException {
		try {
			String requestMessage = new OpenShiftEnvelopeFactory(password, authKey, authIV, request).createString();
			String response = createHttpClient(id, url).post(requestMessage);
			return JsonSanitizer.sanitize(response);
		} catch (MalformedURLException e) {
			throw new OpenShiftException(e, errorMessage);
		} catch (UnauthorizedException e) {
			throw new InvalidCredentialsOpenShiftException(url, e);
		} catch (NotFoundException e) {
			throw new NotFoundOpenShiftException(url, e);
		} catch (HttpClientException e) {
			throw new OpenShiftEndpointException(url, e, errorMessage);
		}
	}
	
	protected IHttpClient createHttpClient(final String id, final String url) throws MalformedURLException {
		return createHttpClient(id, url, true);

	}

	private IHttpClient createHttpClient(String id, String url, final boolean verifyHostnames) throws MalformedURLException {
		String userAgent = MessageFormat.format(USERAGENT_FORMAT, VERSION, id);
		if (verifyHostnames) {
			return new UrlConnectionHttpClient(userAgent, new URL(url), ignoreCertCheck);
		} else {
			return new UrlConnectionHttpClient(userAgent, new URL(url), ignoreCertCheck, UrlConnectionHttpClient.NOOP_HOSTNAMEVERIFIER);
		}
	}

}
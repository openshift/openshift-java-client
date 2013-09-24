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
package com.openshift.internal.client;

import static com.openshift.client.utils.Cartridges.JBOSSAS_7_NAME;
import static com.openshift.client.utils.Cartridges.JENKINS_14_NAME;
import static com.openshift.client.utils.Cartridges.MONGODB_22_NAME;
import static com.openshift.client.utils.Samples.DELETE_DOMAINS_FOOBARZ;
import static com.openshift.client.utils.Samples.GET_DOMAINS;
import static com.openshift.client.utils.Samples.GET_DOMAINS_EMPTY;
import static com.openshift.client.utils.Samples.GET_DOMAINS_FOOBARS;
import static com.openshift.client.utils.Samples.GET_DOMAINS_FOOBARZ;
import static com.openshift.client.utils.Samples.GET_DOMAINS_FOOBARZ_APPLICATIONS_1EMBEDDED;
import static com.openshift.client.utils.Samples.GET_DOMAINS_FOOBARZ_APPLICATIONS_DOWNLOADABLECART;
import static com.openshift.client.utils.Samples.GET_DOMAINS_FOOBARZ_APPLICATIONS_NOAPPS;
import static com.openshift.client.utils.Samples.POST_JEKYLL_DOMAINS_FOOBARZ_APPLICATIONS;
import static com.openshift.client.utils.Samples.POST_SCALABLE_DOMAINS_FOOBARZ_APPLICATIONS;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.reset;

import java.net.SocketTimeoutException;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;

import com.openshift.client.ApplicationScale;
import com.openshift.client.IApplication;
import com.openshift.client.IDomain;
import com.openshift.client.IField;
import com.openshift.client.IGearProfile;
import com.openshift.client.IHttpClient;
import com.openshift.client.ISeverity;
import com.openshift.client.IUser;
import com.openshift.client.InvalidCredentialsOpenShiftException;
import com.openshift.client.Message;
import com.openshift.client.Messages;
import com.openshift.client.OpenShiftEndpointException;
import com.openshift.client.OpenShiftException;
import com.openshift.client.cartridge.IStandaloneCartridge;
import com.openshift.client.utils.ApplicationAssert;
import com.openshift.client.utils.CartridgeAssert;
import com.openshift.client.utils.Cartridges;
import com.openshift.client.utils.DomainAssert;
import com.openshift.client.utils.MessageAssert;
import com.openshift.client.utils.Samples;
import com.openshift.client.utils.TestConnectionFactory;
import com.openshift.internal.client.httpclient.BadRequestException;
import com.openshift.internal.client.httpclient.HttpClientException;
import com.openshift.internal.client.httpclient.UnauthorizedException;
import com.openshift.internal.client.httpclient.request.Parameter;
import com.openshift.internal.client.httpclient.request.ParameterValueArray;
import com.openshift.internal.client.httpclient.request.ParameterValueMap;
import com.openshift.internal.client.httpclient.request.StringParameter;
import com.openshift.internal.client.utils.IOpenShiftJsonConstants;

/**
 * @author Xavier Coulon
 * @author Andre Dietisheim
 */
public class DomainResourceTest {

	private IUser user;
	private IDomain domain;
	private IHttpClient clientMock;
	private HttpClientMockDirector mockDirector;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Rule
	public ErrorCollector errorCollector = new ErrorCollector();

	@Before
	public void setup() throws Throwable {
		this.mockDirector = new HttpClientMockDirector();
		this.clientMock = mockDirector.mockGetDomains(GET_DOMAINS).client();
		this.user = new TestConnectionFactory().getConnection(clientMock).getUser();
		this.domain = user.getDomain("foobarz");
	}

	@Test
	public void shouldLoadEmptyListOfDomains() throws Throwable {
		// pre-conditions
		HttpClientMockDirector mockBuilder = new HttpClientMockDirector();
		IHttpClient clientMock =  mockBuilder.mockGetDomains(GET_DOMAINS_EMPTY).client();
		IUser user = new TestConnectionFactory().getConnection(clientMock).getUser();
		// operation
		final List<IDomain> domains = user.getDomains();
		// verifications
		assertThat(domains).hasSize(0);
		// 3 calls: /API + /API/user + /API/domains
		mockBuilder
			.verifyGetAny(3)
			.verifyGetDomains();
	}

	@Test
	public void shouldLoadSingleUserDomain() throws Throwable {
		// pre-conditions
		// operation
		final List<IDomain> domains = user.getDomains();
		// verifications
		assertThat(domains).hasSize(1);
		// 3 calls: /API + /API/user + /API/domains
		mockDirector.verifyGetAny(3);
	}

	@Test
	public void shouldCreateNewDomain() throws Throwable {
		// pre-conditions
		mockDirector.mockCreateDomain(GET_DOMAINS_FOOBARS);
		int numOfDomains = user.getDomains().size();
		// operation
		final IDomain domain = user.createDomain("foobars");
		// verifications
		assertThat(user.getDomains().size()).isSameAs(numOfDomains + 1);
		assertThat(domain.getId()).isEqualTo("foobars");
		assertThat(domain.getSuffix()).isEqualTo("rhcloud.com");
	}

	@Test(expected = OpenShiftException.class)
	public void shouldNotRecreateExistingDomain() throws Throwable {
		// pre-conditions
		// operation
		user.createDomain("foobarz");
		// verifications
		// expect an exception
	}

	@Test
	public void shouldDestroyDomain() throws Throwable {
		// pre-conditions
		mockDirector.mockDeleteDomain("foobarz", DELETE_DOMAINS_FOOBARZ);
		// operation
		final IDomain domain = user.getDomain("foobarz");
		domain.destroy();
		// verifications
		assertThat(user.getDomain("foobarz")).isNull();
		assertThat(user.getDomains()).isEmpty();
	}

	@Test
	public void shouldNotDestroyDomainWithApp() throws Throwable {
		// pre-conditions
		mockDirector.mockDeleteDomain("foobarz", 
				new BadRequestException("Domain contains applications. Delete applications first or set force to true.", null));
		// operation
		final IDomain domain = user.getDomain("foobarz");
		try {
			domain.destroy();
			fail("Expected an exception here..");
		} catch (OpenShiftEndpointException e) {
			assertThat(e.getCause()).isInstanceOf(BadRequestException.class);
		}
		// verifications
		assertThat(domain).isNotNull();
		assertThat(user.getDomains()).isNotEmpty().contains(domain);
	}

	@Test
	public void shouldUpdateDomainId() throws Throwable {
		// pre-conditions
		mockDirector.mockRenameDomain("foobarz", GET_DOMAINS_FOOBARS);
		final IDomain domain = user.getDomain("foobarz");
		// operation
		domain.rename("foobars");
		// verifications
		assertThat(domain.getId()).isEqualTo("foobars");
		final IDomain updatedDomain = user.getDomain("foobars");
		assertThat(updatedDomain).isNotNull();
		assertThat(updatedDomain.getId()).isEqualTo("foobars");
		mockDirector.verifyRenameDomain("foobarz");
	}

	@Test
	public void shouldListAvailableGearSizes() throws Throwable {
		// pre-conditions
		final IDomain domain = user.getDomain("foobarz");
		// operation
		List<IGearProfile> availableGearSizes = domain.getAvailableGearProfiles();
		// verifications
		assertThat(availableGearSizes).onProperty("name")
				.contains("small", "micro", "medium", "large", "exlarge", "jumbo");
	}

	@Test
	public void shouldRefreshDomainAndReloadApplications() throws Throwable {
		// pre-conditions
		mockDirector
			.mockGetDomain("foobarz", GET_DOMAINS_FOOBARZ)
			.mockGetApplications("foobarz", GET_DOMAINS_FOOBARZ_APPLICATIONS_1EMBEDDED);
		
		final IDomain domain = user.getDomain("foobarz");
		assertThat(domain).isNotNull();
		domain.getApplications();
		// operation
		domain.refresh();
		// verifications
		mockDirector
			.verifyGetDomain("foobarz") // explicit refresh 
			.verifyGetApplications("foobarz", 2); // two calls, before and while refresh
	}

	@Test
	public void shouldRefreshDomainAndNotReloadApplications() throws Throwable {
		// pre-conditions
		mockDirector
			.mockGetDomain("foobarz", GET_DOMAINS_FOOBARZ)
			.mockGetApplications("foobarz", GET_DOMAINS_FOOBARZ_APPLICATIONS_1EMBEDDED);
		final IDomain domain = user.getDomain("foobarz");
		assertThat(domain).isNotNull();
		// operation
		domain.refresh();
		// verifications
		mockDirector
			.verifyGetDomains() // explicit refresh 
			.verifyGetApplications("foobarz", 0); // // no call, neither before and while refresh
	}

	@Test
	public void shouldLoadListOfApplicationsWithNoElement() throws Throwable {
		// pre-conditions
		mockDirector.mockGetApplications("foobarz", GET_DOMAINS_FOOBARZ_APPLICATIONS_NOAPPS);
		// operation
		final List<IApplication> apps = domain.getApplications();
		// verifications
		assertThat(apps).isEmpty();
		mockDirector
				.verifyGetAPI()
				.verifyGetUser()
				.verifyGetDomains()
				.verifyGetApplications("foobarz", 1)
				.verifyGetAny(4);
	}

	@Test
	public void shouldLoadListOfApplicationsWith2Elements() throws Throwable {
		// pre-conditions
		mockDirector.mockGetApplications("foobarz", GET_DOMAINS_FOOBARZ_APPLICATIONS_1EMBEDDED);
		// operation
		final List<IApplication> apps = domain.getApplications();
		// verifications
		assertThat(apps).hasSize(2);
		mockDirector
				.verifyGetAPI()
				.verifyGetUser()
				.verifyGetDomains()
				.verifyGetApplications("foobarz", 1)
				.verifyGetAny(4);
	}

	@Test
	public void shouldNotLoadApplicationTwice() throws Throwable {
		// pre-conditions
		mockDirector.mockGetApplications("foobarz", GET_DOMAINS_FOOBARZ_APPLICATIONS_1EMBEDDED);
		// operation
		List<IApplication> apps = domain.getApplications();
		assertThat(apps).hasSize(2);

		// verifications
		reset(clientMock);
		apps = domain.getApplications(); // dont do new client request
		mockDirector.verifyGetAny(0);
	}
	
	@Test(expected = InvalidCredentialsOpenShiftException.class)
	public void shouldNotLoadListOfApplicationsWithInvalidCredentials() 
			throws OpenShiftException, HttpClientException, SocketTimeoutException {
		// pre-conditions
		mockDirector.mockGetApplications("foobarz", new UnauthorizedException("invalid credentials (mock)", null));
		// operation
		domain.getApplications();
		// verifications
		mockDirector.verifyGetAPI()
			.verifyGetUser()
			.verifyGetAny(2);
	}

	@Test
	public void shouldCreateApplicationWithDownloadableCartridge() throws Throwable {
		// pre-conditions
		mockDirector
				.mockGetApplications("foobarz", GET_DOMAINS_FOOBARZ_APPLICATIONS_NOAPPS)
				.mockCreateApplication("foobarz", Samples.GET_DOMAINS_FOOBARZ_APPLICATIONS_DOWNLOADABLECART);

		// operation
		final IApplication app = domain.createApplication(
				"downloadablecart", Cartridges.go11(), null, null, null, IHttpClient.NO_TIMEOUT,
				Cartridges.foreman063(),
				Cartridges.mysql51());

		// verifications
		new ApplicationAssert(app)
				.hasName("downloadablecart")
				.hasGearProfile(IGearProfile.SMALL)
				.hasCreationTime()
				.hasUUID()
				.hasDomain(domain)
				.hasEmbeddableCartridges(2)
				.hasEmbeddedCartridge(Cartridges.mysql51())
				.hasEmbeddedCartridge(Cartridges.foreman063());
		new DomainAssert(domain)
				.hasApplications(app)
				.hasApplications(1);
	}

	@Test
	public void shouldUpdateDownloadableStandaloneCartridgeAfterDeploy() throws Throwable {
		// pre-conditions
		mockDirector
				.mockGetApplications("foobarz", GET_DOMAINS_FOOBARZ_APPLICATIONS_NOAPPS)
				.mockCreateApplication("foobarz", GET_DOMAINS_FOOBARZ_APPLICATIONS_DOWNLOADABLECART);
		new CartridgeAssert<IStandaloneCartridge>(Cartridges.go11())
				.hasName(null)
				.hasDescription(null)
				.hasDisplayName(null)
				.hasUrl(Cartridges.GO_DOWNLOAD_URL);

		// operation
		final IApplication app = domain.createApplication(
				"springeap6", Cartridges.eap6(), null, null, null, IHttpClient.NO_TIMEOUT,
				Cartridges.foreman063(),
				Cartridges.mysql51());

		// verifications
		// cartridge was updated with name, description, display name
		new CartridgeAssert<IStandaloneCartridge>(app.getCartridge())
			.hasName("smarterclayton-go-1.1")
			.hasDescription("OpenShift Go cartridge")
			.hasDisplayName("Go 1.1")
			.hasUrl(Cartridges.GO_DOWNLOAD_URL);
	}

	@Test
    public void shouldRequestCreateApplicationWithDownloadableCartridge() throws Throwable {    	
        // pre-conditions
        mockDirector
                .mockGetApplications("foobarz", GET_DOMAINS_FOOBARZ_APPLICATIONS_NOAPPS)
                .mockCreateApplication("foobarz", Samples.GET_DOMAINS_FOOBARZ_APPLICATIONS_DOWNLOADABLECART);

        // operation
		domain.createApplication(
				"downloadablecart", Cartridges.go11(), null, null, null, IHttpClient.NO_TIMEOUT, 
				Cartridges.foreman063(),
				Cartridges.mysql51());

        // verifications
        mockDirector.verifyCreateApplication("foobarz", IHttpClient.NO_TIMEOUT,
				new StringParameter(IOpenShiftJsonConstants.PROPERTY_NAME, "downloadablecart"),
				new Parameter(IOpenShiftJsonConstants.PROPERTY_CARTRIDGES,
						new ParameterValueArray()
								.add(new ParameterValueMap().add(IOpenShiftJsonConstants.PROPERTY_URL, Cartridges.GO_DOWNLOAD_URL))
								.add(new ParameterValueMap().add(IOpenShiftJsonConstants.PROPERTY_URL, Cartridges.FOREMAN_DOWNLOAD_URL))
								.add(new ParameterValueMap().add(IOpenShiftJsonConstants.PROPERTY_NAME, Cartridges.mysql51().getName()))));
    }

    @Test
	public void shouldHaveMessagesWhenCreating() throws Throwable {
		// pre-conditions
		mockDirector
			.mockGetApplications("foobarz", GET_DOMAINS_FOOBARZ_APPLICATIONS_NOAPPS)
			.mockCreateApplication("foobarz", POST_JEKYLL_DOMAINS_FOOBARZ_APPLICATIONS);
		// operation
		final IApplication app = domain.createApplication("jekyll", Cartridges.jenkins14());
		// verifications
		Messages messages = app.getMessages();
		assertThat(messages).isNotNull();
		assertThat(messages.getAll()).hasSize(3);
		List<Message> defaultMessages = messages.getBy(IField.DEFAULT);
		assertThat(defaultMessages).hasSize(3);
		List<Message> infoSeverityMessages = messages.getBy(IField.DEFAULT, ISeverity.INFO);
		assertThat(infoSeverityMessages).hasSize(1);
		new MessageAssert(infoSeverityMessages.get(0))
			.hasExitCode(0)
			.hasText("Application jekyll was created.");
		List<Message> debugSeverityMessages = app.getMessages().getBy(IField.DEFAULT, ISeverity.DEBUG);
		assertThat(debugSeverityMessages).hasSize(1);
		new MessageAssert(debugSeverityMessages.get(0))
			.hasExitCode(0)
			.hasText("The cartridge jenkins deployed a template application");
		List<Message> resultSeverityMessages = messages.getBy(IField.DEFAULT, ISeverity.RESULT);
		assertThat(resultSeverityMessages).hasSize(1);
		new MessageAssert(resultSeverityMessages.get(0))
				.hasExitCode(0)
				.hasText("Jenkins created successfully.  "
						+ "Please make note of these credentials:\n   User: admin\n   Password: wLwSzJPh6dqN\n"
						+ "Note:  You can change your password at: https://jekyll-foobarz.rhcloud.com/me/configure\n");
	}

	@Test
	public void shouldRequestCreateApplicationWithNameAndCartridgeOnly() throws Throwable {
		// pre-conditions
		mockDirector
			.mockGetApplications("foobarz", GET_DOMAINS_FOOBARZ_APPLICATIONS_NOAPPS)
			.mockCreateApplication("foobarz", POST_SCALABLE_DOMAINS_FOOBARZ_APPLICATIONS);
		// operation
		domain.createApplication("foo", Cartridges.as7());
		
		// verification
		mockDirector.verifyCreateApplication(
				"foobarz",
				IHttpClient.NO_TIMEOUT,
				new StringParameter(IOpenShiftJsonConstants.PROPERTY_NAME, "foo"),
				new Parameter(IOpenShiftJsonConstants.PROPERTY_CARTRIDGES,
						new ParameterValueArray()
								.add(new ParameterValueMap().add(
										IOpenShiftJsonConstants.PROPERTY_NAME, Cartridges.JBOSSAS_7_NAME))));
	}

	@Test
	public void shouldRequestCreateApplicationWithNameCartridgeAndScaleOnly() throws Throwable {
		// pre-conditions
		mockDirector
				.mockGetApplications("foobarz", GET_DOMAINS_FOOBARZ_APPLICATIONS_NOAPPS)
				.mockCreateApplication("foobarz", POST_SCALABLE_DOMAINS_FOOBARZ_APPLICATIONS);
		// operation
		domain.createApplication("foo", Cartridges.as7(), ApplicationScale.SCALE);
		
		// verification
		mockDirector.verifyCreateApplication("foobarz", IHttpClient.NO_TIMEOUT,  
				new StringParameter(IOpenShiftJsonConstants.PROPERTY_NAME, "foo"),
				new Parameter(IOpenShiftJsonConstants.PROPERTY_CARTRIDGES,
						new ParameterValueArray()
								.add(new ParameterValueMap().add(IOpenShiftJsonConstants.PROPERTY_NAME, JBOSSAS_7_NAME))),
				new StringParameter(IOpenShiftJsonConstants.PROPERTY_SCALE, ApplicationScale.SCALE.getValue()));
	}

	@Test
	public void shouldRequestCreateApplicationWithNameCartridgeScaleGearProfileOnly() throws Throwable {
		// pre-conditions
		mockDirector
				.mockGetApplications("foobarz", GET_DOMAINS_FOOBARZ_APPLICATIONS_NOAPPS)
				.mockCreateApplication("foobarz", POST_SCALABLE_DOMAINS_FOOBARZ_APPLICATIONS);
		// operation
		domain.createApplication("foo", Cartridges.as7(), ApplicationScale.SCALE, GearProfile.JUMBO);
		
		// verification
		mockDirector.verifyCreateApplication("foobarz", IHttpClient.NO_TIMEOUT,  
				new StringParameter(IOpenShiftJsonConstants.PROPERTY_NAME, "foo"),
				new Parameter(IOpenShiftJsonConstants.PROPERTY_CARTRIDGES,
						new ParameterValueArray()
								.add(new ParameterValueMap().add(IOpenShiftJsonConstants.PROPERTY_NAME, JBOSSAS_7_NAME))),
				new StringParameter(IOpenShiftJsonConstants.PROPERTY_SCALE, ApplicationScale.SCALE.getValue()),
				new StringParameter(IOpenShiftJsonConstants.PROPERTY_GEAR_PROFILE, GearProfile.JUMBO.getName())
		);
	}

	@Test
	public void shouldRequestCreateApplicationWithNameCartridgeScaleGearProfileAndGitUrl() throws Throwable {
		// pre-conditions
		mockDirector
				.mockGetApplications("foobarz", GET_DOMAINS_FOOBARZ_APPLICATIONS_NOAPPS)
				.mockCreateApplication("foobarz", POST_SCALABLE_DOMAINS_FOOBARZ_APPLICATIONS);
		// operation
		domain.createApplication(
				"foo", Cartridges.as7(), 
				ApplicationScale.SCALE, 
				GearProfile.JUMBO, 
				"git://github.com/adietish/openshift-java-client.git");
		
		// verification
		mockDirector.verifyCreateApplication("foobarz", IHttpClient.NO_TIMEOUT,  
				new StringParameter(IOpenShiftJsonConstants.PROPERTY_NAME, "foo"),
				new Parameter(IOpenShiftJsonConstants.PROPERTY_CARTRIDGES,
						new ParameterValueArray()
								.add(new ParameterValueMap().add(IOpenShiftJsonConstants.PROPERTY_NAME, JBOSSAS_7_NAME))),
				new StringParameter(IOpenShiftJsonConstants.PROPERTY_SCALE, ApplicationScale.SCALE.getValue()),
				new StringParameter(IOpenShiftJsonConstants.PROPERTY_GEAR_PROFILE, GearProfile.JUMBO.getName()),
				new StringParameter(IOpenShiftJsonConstants.PROPERTY_INITIAL_GIT_URL, "git://github.com/adietish/openshift-java-client.git")
		);
	}

	@Test
	public void shouldRequestCreateApplicationWithEmbeddableCartridges() throws Throwable {
		// pre-conditions
		mockDirector
				.mockGetApplications("foobarz", GET_DOMAINS_FOOBARZ_APPLICATIONS_NOAPPS)
				.mockCreateApplication("foobarz", POST_SCALABLE_DOMAINS_FOOBARZ_APPLICATIONS);
				
		// operation
		domain.createApplication(
				"jekyll", 
				Cartridges.jenkins14(), 
				ApplicationScale.SCALE, 
				GearProfile.LARGE, 
				"git://github.com/adietish/openshift-java-client.git", 
				42001, 
				Cartridges.mongodb22(), 
				Cartridges.mysql51());
		
		// verification
		mockDirector.verifyCreateApplication(
				"foobarz",
				42001,
				new StringParameter(IOpenShiftJsonConstants.PROPERTY_NAME, "jekyll"),
				new StringParameter(IOpenShiftJsonConstants.PROPERTY_SCALE, ApplicationScale.SCALE.getValue()),
				new StringParameter(IOpenShiftJsonConstants.PROPERTY_GEAR_PROFILE, GearProfile.LARGE.getName()),
				new StringParameter(IOpenShiftJsonConstants.PROPERTY_INITIAL_GIT_URL, "git://github.com/adietish/openshift-java-client.git"),
				new Parameter(IOpenShiftJsonConstants.PROPERTY_CARTRIDGES,
						new ParameterValueArray()
								.add(new ParameterValueMap().add(IOpenShiftJsonConstants.PROPERTY_NAME,
										JENKINS_14_NAME))
								.add(new ParameterValueMap().add(IOpenShiftJsonConstants.PROPERTY_NAME,
										MONGODB_22_NAME))
								.add(new ParameterValueMap().add(IOpenShiftJsonConstants.PROPERTY_NAME,
										Cartridges.MYSQL_51_NAME))));
	}

	@Test(expected = OpenShiftException.class)
	public void shouldNotCreateApplicationWithMissingName() throws Throwable {
		// pre-conditions
		mockDirector.mockGetApplications("foobarz", GET_DOMAINS_FOOBARZ_APPLICATIONS_1EMBEDDED);
		// operation
		domain.createApplication(null, Cartridges.as7(), null, null);
		// verifications
		// expected exception
	}

	@Test(expected = OpenShiftException.class)
	public void shouldNotCreateApplicationWithMissingCartridge() throws Throwable {
		// pre-conditions
		mockDirector.mockGetApplications("foobarz", GET_DOMAINS_FOOBARZ_APPLICATIONS_NOAPPS);
		// operation
		domain.createApplication("foo", null, null, null);
		// verifications
		// expected exception
	}

	@Test
	public void shouldNotRecreateExistingApplication() throws Throwable {
		// pre-conditions
		mockDirector.mockGetApplications("foobarz", GET_DOMAINS_FOOBARZ_APPLICATIONS_1EMBEDDED);
		// operation
		try {
			domain.createApplication("springeap6", Cartridges.as7(), null, null);
			// expect an exception
			fail("Expected exception here...");
		} catch (OpenShiftException e) {
			// OK
		}
		// verifications
		assertThat(domain.getApplications()).hasSize(2);
	}

	@Test
	public void shouldGetApplicationByNameCaseInsensitive() throws Throwable {
		// pre-conditions
		mockDirector.mockGetApplications("foobarz", GET_DOMAINS_FOOBARZ_APPLICATIONS_1EMBEDDED);
		// operation
		IApplication lowerCaseQueryResult = domain.getApplicationByName("springeap6");
		IApplication upperCaseQueryResult = domain.getApplicationByName("SPRINGEAP6");

		// verifications
		assertThat(lowerCaseQueryResult).isNotNull();
		assertThat(lowerCaseQueryResult.getName()).isEqualTo("springeap6");
		assertThat(upperCaseQueryResult).isNotNull();
		assertThat(upperCaseQueryResult.getName()).isEqualTo("springeap6");
	}
	
	@Test
	@Ignore
	public void shouldRefreshDomain() throws Throwable {
		fail("not implemented yet");
	}

	@Test
	@Ignore
	public void shouldNotReloadDomainTwice() throws Throwable {
		fail("not implemented yet");
	}

	@Test
	@Ignore
	public void shouldNotifyAfterDomainCreated() throws Throwable {
		fail("not implemented yet");
	}

	@Test
	@Ignore
	public void shouldNotifyAfterDomainUpdated() throws Throwable {
		fail("not implemented yet");
	}

	@Test
	@Ignore
	public void shouldNotifyAfterDomainDestroyed() throws Throwable {
		fail("not implemented yet");
	}

	@Test
	public void shouldPassTimeoutToClient() throws Throwable {
		// pre-conditions
		int timeout = 42 * 1000;
		mockDirector
			.mockGetApplications("foobarz", GET_DOMAINS_FOOBARZ_APPLICATIONS_NOAPPS)
			.mockCreateApplication("foobarz", POST_SCALABLE_DOMAINS_FOOBARZ_APPLICATIONS);

		// operation
		domain.createApplication("scalable", Cartridges.as7(), ApplicationScale.NO_SCALE, GearProfile.SMALL, null, timeout);

		// verifications
		mockDirector.verifyCreateApplication("foobarz", timeout, 
				new StringParameter(IOpenShiftJsonConstants.PROPERTY_SCALE, String.valueOf(Boolean.FALSE)),
				new StringParameter(IOpenShiftJsonConstants.PROPERTY_GEAR_PROFILE, GearProfile.SMALL.getName()),
				new Parameter(IOpenShiftJsonConstants.PROPERTY_CARTRIDGES,
						new ParameterValueArray().add(
								new ParameterValueMap().add(IOpenShiftJsonConstants.PROPERTY_NAME, JBOSSAS_7_NAME))),
				new StringParameter(IOpenShiftJsonConstants.PROPERTY_NAME, "scalable"));
	}
}

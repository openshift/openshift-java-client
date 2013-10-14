package com.openshift.internal.client;
import static org.fest.assertions.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.openshift.client.IApplication;
import com.openshift.client.IDomain;
import com.openshift.client.IEnvironmentVariable;
import com.openshift.client.IOpenShiftConnection;
import com.openshift.client.IUser;
import com.openshift.client.utils.ApplicationTestUtils;
import com.openshift.client.utils.DomainTestUtils;
import com.openshift.client.utils.TestConnectionFactory;

public class EnvironmentVariableResourceIntegrationTest {

	private static final long WAIT_TIMEOUT = 3 * 60 * 1000;
    private IUser user;
	private IDomain domain;
	private IApplication application;
	
	@Before
	public void setUp() throws Exception {
		IOpenShiftConnection connection = new TestConnectionFactory().getConnection();
		this.user = connection.getUser();
		this.domain = DomainTestUtils.ensureHasDomain(user);
		ApplicationTestUtils.silentlyDestroyAllApplications(domain);
		this.application = ApplicationTestUtils.getOrCreateApplication(domain);
		
	}
	
    @Test
    public void shouldGetEnvironmentVariableNameAndValue() throws Throwable{
    	//operation
    	IEnvironmentVariable environmentVariable = application.addEnvironmentVariable("X_NAME","X_VALUE");
    	//verification
    	assertThat(environmentVariable).isNotNull();
    	assertThat(environmentVariable.getName()).isEqualTo("X_NAME");
    	assertThat(environmentVariable.getValue()).isEqualTo("X_VALUE");
    }
    
   @Test
    public void shouldUpdateEnvironmentVariableValue() throws Throwable{
      //precondition
	  IEnvironmentVariable environmentVariable = application.addEnvironmentVariable("Y_NAME","Y_VALUE"); 
      //operation
	 assertThat(environmentVariable).isNotNull();
     assertThat(environmentVariable.getName()).isEqualTo("Y_NAME");
     assertThat(environmentVariable.getValue()).isEqualTo("Y_VALUE");
     environmentVariable.update("UPDATED_Y_VALUE");
     assertThat(environmentVariable.getValue()).isEqualTo("UPDATED_Y_VALUE");
    }
    
    @Test
    public void shouldDeleteEnvironmentVariableValue() throws Throwable{
      //precondition
      application.addEnvironmentVariable("Z_NAME","Z_VALUE"); 	
      //operation
      IEnvironmentVariable zEnvironmentVariable = application.getEnvironmentVariableByName("Z_NAME");
      zEnvironmentVariable.delete();
      zEnvironmentVariable = application.getEnvironmentVariableByName("Z_NAME");
      assertThat(zEnvironmentVariable).isNull();
    }


	

}

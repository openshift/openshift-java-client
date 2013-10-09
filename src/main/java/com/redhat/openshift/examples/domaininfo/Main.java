/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.openshift.examples.domaininfo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.openshift.client.IApplication;
import com.openshift.client.IDomain;
import com.openshift.client.IEnvironmentVariable;
import com.openshift.client.IOpenShiftConnection;
import com.openshift.client.IUser;
import com.openshift.client.OpenShiftConnectionFactory;
import com.openshift.client.OpenShiftException;
import com.openshift.client.cartridge.IEmbeddedCartridge;
import com.openshift.client.configuration.OpenShiftConfiguration;
import com.openshift.internal.client.ApplicationResource;
import com.openshift.internal.client.response.Link;

/**
 * @author Andre Dietisheim
 */
public class Main {
	public static void main(String[] argv) throws OpenShiftException, FileNotFoundException, IOException {
		if (argv.length < 2) {
			System.out.println("You have to provide username and password.");
			return;
		}
		
		String username = argv[0];
		String password = argv[1];
		IOpenShiftConnection connection =
				new OpenShiftConnectionFactory()
						.getConnection("rhc-domain-show", username, password, new OpenShiftConfiguration().getLibraServer());
		IUser user = connection.getUser();
		printUserInfo(user);
		printApplicationInfo(user);
	}
	
	private static void printUserInfo(IUser user) {
		System.out.println("User Info");
		System.out.println("================");
		IDomain domain = user.getDefaultDomain();
		if (domain == null) {
			System.out.println("No namespaces found. You can use 'rhc domain create -n <namespace>' to create a namespace for your applications.");
		} else {
			System.out.println("Namespace:\t" + domain.getId());
			System.out.println("RHLogin:\t" + user.getRhlogin());
		}
		System.out.println("");
		
	}

	private static void printApplicationInfo(IUser user) {
		System.out.println("Application Info");
		System.out.println("================");
		IDomain domain = user.getDefaultDomain();
		if (domain == null) {
			System.out.println("No applications found.  You can use 'rhc app create' to create new applications.");
		} else 
		{
			   String environmentName = "SixKey";
			   String environmentValue = "SIX_VALUE";
			   String updatedEnvironmentValue="UPDATED_SIX_VALUE";
			   boolean stop = true;
			   boolean deleteAllVariables = false;
			   boolean createAllVariables=false;
			   IApplication application = domain.getApplicationByName("test");
			  addEnvironmentVariableMapToApplications(application);
			   printAllEnvironmentVariable(application);
			   if(deleteAllVariables){
			   deleteAllVariables(application);
			   printAllEnvironmentVariable(application);
			   }
			   if(createAllVariables){
			   createEnvironmentVariables(application);
			   printAllEnvironmentVariable(application);
			   }
			   
			   
			   if(stop){
				   return;
			   } 
			   /*****************Adding New Variable*********************/
			   System.out.println("********\tAdding New Variable\t************");
			   if(!application.hasEnvironmentVariableByName(environmentName))
			   application.addEnvironmentVariable(environmentName,environmentValue);
			   printAllEnvironmentVariable(application);
			   /*****************Get Variable By Name*********************/
			   System.out.println("********\tGetting Variable By Name\t************");
			   IEnvironmentVariable environmentVariable = application.getEnvironmentVariableByName(environmentName);
			   printEnvironmentVariable(environmentVariable);
			   /*****************Updating Variable*********************/
			   System.out.println("********\tUpdating Variable\t************");
			   environmentVariable.update(updatedEnvironmentValue);
			   printEnvironmentVariable(environmentVariable);
			   printEnvironmentVariable(application.getEnvironmentVariableByName(environmentName));
			   /*****************Print All Variables Again*********************/
			   printAllEnvironmentVariable(application);
			   /*****Delete All Variables*****/
			   System.out.println("********\tDelete Variable\t************");
			   environmentVariable.delete();
			   System.out.println("********\tAfter Deleting Variable\t************");
			   printAllEnvironmentVariable(application);
		}
		
	}
	private static void  printAllEnvironmentVariable(IApplication application){
		List<IEnvironmentVariable> environmentVariables = application.getEnvironmentVariables();
		if(environmentVariables.isEmpty()){
			System.out.println("No environment variables present in "+application.getName());
			return;
		}
		   System.out.println("Getting all environment variables for application "+application.getName());
		   for(IEnvironmentVariable environmentVariable : environmentVariables){
			   printEnvironmentVariable(environmentVariable);
		   }
	}
	private static void  printEnvironmentVariable(IEnvironmentVariable environmentVariable){
		System.out.println("Name = \t"+environmentVariable.getName()+"       \t"+"Value = \t"+environmentVariable.getValue());
	}
	
	private static void createEnvironmentVariables(IApplication application){
		application.addEnvironmentVariable("OneKey","ONE_VALUE");
		application.addEnvironmentVariable("TwoKey","TWO_VALUE");
		application.addEnvironmentVariable("ThreeKey","THREE_VALUE");
		application.addEnvironmentVariable("FourKey","FOUR_VALUE");
		application.addEnvironmentVariable("FiveKey","FIVE_VALUE");
	}
	
	private static void deleteAllVariables(IApplication application){
		List<IEnvironmentVariable> tempList = new ArrayList<IEnvironmentVariable>();
		tempList.addAll(application.getEnvironmentVariables());
		for(IEnvironmentVariable environmentVariable :tempList){
			environmentVariable.delete();
		}
	}
	
	private static Map<String,String> createEnvironmentVariablesMap(){
		Map<String,String> envMap = new HashMap<String,String>();
		envMap.put("M_OneKey","M_ONE_VALUE");
		envMap.put("M_TwoKey","M_TWO_VALUE");
		envMap.put("M_ThreeKey","M_THREE_VALUE");
		envMap.put("M_FourKey","M_FOUR_VALUE");
		envMap.put("M_FiveKey","M_FIVE_VALUE");
		envMap.put("M_SixKey","M_SIX_VALUE");
		return envMap;
		
	}
	
	private static void addEnvironmentVariableMapToApplications(IApplication applications){
		applications.addEnvironmentVariables(createEnvironmentVariablesMap());
	}

}

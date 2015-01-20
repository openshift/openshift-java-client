package com.openshift3.client.builders;

import static org.junit.Assert.*;

import java.util.List;

import org.jboss.dmr.ModelNode;
import org.junit.Test;

import com.openshift.client.utils.Samples;
import com.openshift3.client.ResourceKind;
import com.openshift3.client.images.ImageUri;
import com.openshift3.internal.client.builders.ImageDeploymentBuilder;
import com.openshift3.internal.client.model.DeploymentConfig;
import com.openshift3.internal.client.model.KubernetesResource;

//TODO WIP determine if this is still needed
public class ImageDeploymentBuilderTest {
	
//	@Test
	public void test() {
		ImageUri tag = new ImageUri("172.30.17.59:5001/jcantrill/javaparks:latest");
		ImageDeploymentBuilder builder = new ImageDeploymentBuilder("hello-openshift-project", tag, 8080);
		List<KubernetesResource> resources = builder.build();
		assertEquals("Exp. only one resource", 1, resources.size());
		assertEquals("Exp. a deployment config", ResourceKind.DeploymentConfig, resources.get(0).getKind());
		DeploymentConfig config = (DeploymentConfig) resources.get(0);
		
		assertEquals(ModelNode.fromJSONString(Samples.DEPLOYMENT_CONFIG_MINIMAL.getContentAsString()), ModelNode.fromJSONString(config.toPrettyString()));
	}

}

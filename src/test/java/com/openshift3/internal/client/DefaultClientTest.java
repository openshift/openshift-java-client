package com.openshift3.internal.client;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.dmr.ModelNode;
import org.junit.Before;
import org.junit.Test;

import com.openshift.client.IHttpClient;
import com.openshift3.client.ResourceKind;
import com.openshift3.client.model.IPod;
import com.openshift3.internal.client.model.Pod;

public class DefaultClientTest {

	private DefaultClient client;
	private IHttpClient httpClient;
	private ModelNode response;
	private Pod podFrontEnd;
	private Pod podBackEnd;
	
	private void givenAPodList(){
		
		podFrontEnd = new Pod();
		podFrontEnd.setName("frontend");
		podFrontEnd.setNamespace("aNamespace");
		podFrontEnd.addLabel("name", "frontend");
		podFrontEnd.addLabel("env", "production");
		
		podBackEnd = new Pod();
		podBackEnd.setName("backend");
		podBackEnd.setNamespace("aNamespace");
		podBackEnd.addLabel("name", "backend");
		podBackEnd.addLabel("env", "production");
		
		Pod otherPod = new Pod();
		otherPod.setName("other");
		otherPod.setNamespace("aNamespace");
		otherPod.addLabel("env", "production");
		
		response = new ModelNode();
		response.get("kind").set("PodList");
		ModelNode items = response.get("items");
		items.add(podFrontEnd.getNode());
		items.add(otherPod.getNode());
		items.add(podBackEnd.getNode());
	}
	
	private void givenAClient() throws MalformedURLException{
		httpClient = mock(IHttpClient.class);
		client = new DefaultClient(new URL("http://myopenshift"), httpClient);
	}
	
	@Before
	public void setUp() throws Exception{
		givenAClient();
		givenAPodList();
		when(httpClient.get(any(URL.class), anyInt()))
			.thenReturn(response.toJSONString(false));
		
	}
	@SuppressWarnings("serial")
	@Test
	public void testListResourceFilteringWithExactMatch() throws Exception {
		Map<String, String> labels = new HashMap<String, String>(){{
			put("name","backend");
			put("env","production");
		}};
		List<IPod> pods = client.list(ResourceKind.Pod, "aNamespace", labels);
		assertEquals("Expected 1 pod to be returned", 1, pods.size());
		assertEquals("Expected the frontend pod", podBackEnd, pods.get(0));
	}

	@Test
	public void testListResourceFilteringNoMatch() throws Exception {
		Map<String, String> labels = new HashMap<String, String>();
		labels.put("foo", "bar");
		List<IPod> pods = client.list(ResourceKind.Pod, "aNamespace", labels);
		assertEquals("Expected no pod to be returned", 0, pods.size());
	}

	@SuppressWarnings("serial")
	@Test
	public void testListResourceFilteringWithPartialMatch() throws Exception {
		Map<String, String> labels = new HashMap<String, String>(){{
			put("name","frontend");
		}};
		List<IPod> pods = client.list(ResourceKind.Pod, "aNamespace", labels);
		assertEquals("Expected 1 pod to be returned", 1, pods.size());
		assertEquals("Expected the backend pod", podFrontEnd, pods.get(0));
	}
	
	@SuppressWarnings("serial")
	@Test
	public void testListResourceFilteringSingleLabel() throws Exception {
		Map<String, String> labels = new HashMap<String, String>(){{
			put("env","production");
		}};
		List<IPod> pods = client.list(ResourceKind.Pod, "aNamespace", labels);
		assertEquals("Expected all pods to be returned", 3, pods.size());
	}

}

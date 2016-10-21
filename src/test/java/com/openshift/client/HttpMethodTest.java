package com.openshift.client;

import static org.junit.Assert.*;

import org.junit.Test;

public class HttpMethodTest {

	@Test
	public void hasValueForKnownValueShouldReturnTrue() {
		assertTrue(HttpMethod.hasValue("POST"));
		assertTrue(HttpMethod.hasValue("pOst"));
	}
	@Test
	public void hasValueForUnKnownValueShouldReturnFalse() {
		assertFalse(HttpMethod.hasValue("afdadsfads"));
	}

    @Test
    public void hasValueForNullValueShouldReturnFalse() {
        assertFalse(HttpMethod.hasValue(null));
    }

}

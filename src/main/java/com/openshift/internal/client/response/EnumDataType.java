/******************************************************************************* 
 * Copyright (c) 2012-2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package com.openshift.internal.client.response;

import java.util.regex.Pattern;

/**
 * The Enum EnumDataType.
 * 
 * @author Xavier Coulon
 * @author Andre Dieitsheim
 * @author Sean Kavanagh
 */
public enum EnumDataType {
		
	/** Links / the root node that allows for navigation amongst resources.*/
	LINKS,
	USER,
	/** the user's keys. */
	KEYS,
	/** one user's key.*/
	KEY,
	DOMAINS,
	DOMAIN,
	APPLICATIONS,
	APPLICATION,
    AUTHORIZATION,
    AUTHORIZATIONS,
	/** The embedded cartridge type. */
	EMBEDDED,
	GEAR_GROUPS,
	/** The standalone cartridges type. */
	CARTRIDGES,
	/** The standalone cartridge type. */
	CARTRIDGE,
	/** The environment-variables type*/
	ENVIRONMENT_VARIABLES,
	/** The environment-variable type*/
	ENVIRONMENT_VARIABLE
	;
	
	private static final Pattern pattern = Pattern.compile("-");

	/**
	 * Returns the enum value matching the given value (as string), or 'undefined' if null/unknown value.
	 * 
	 * @param value
	 *            as String
	 * @return value as enum
	 */
	static EnumDataType safeValueOf(String value) {
		if (value != null) {
			try {
				 return valueOf(pattern.matcher(value).replaceAll("_"));
			} catch (IllegalArgumentException e) {
				// do nothing, will just return 'undefined'
			}
		}
		return null;
	}
}
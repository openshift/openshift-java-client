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
package com.openshift.client;


public interface IAuthorization extends IOpenShiftResource {


    /**
     * authorization id
     *
     * @return
     */
    public String getId();

    /**
     * authorization note
     *
     * @return
     */
    public String getNote();


    /**
     * authorization scopes
     *
     * @return
     */
    public String getScopes();

    /**
     * authorization token
     *
     * @return
     */
    public String getToken();


    /**
     * Destroys this authorization
     *
     * @throws OpenShiftException
     */
    public void destroy() throws OpenShiftException;

    /**
     * Refresh the authorization but reloading its content from OpenShift.
     *
     * @throws OpenShiftException
     */
    public void refresh() throws OpenShiftException;
}

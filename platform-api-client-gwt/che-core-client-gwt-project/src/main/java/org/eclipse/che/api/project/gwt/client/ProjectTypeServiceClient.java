/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.project.gwt.client;

import org.eclipse.che.api.project.shared.dto.ProjectTypeDto;
import org.eclipse.che.api.promises.client.Promise;

import java.util.List;

/**
 * Client for Project Type API.
 *
 * @author Artem Zatsarynnyi
 */
public interface ProjectTypeServiceClient {

    /**
     * Get information about all registered project types.
     *
     * @param workspaceId
     *         id of current workspace
     */
    Promise<List<ProjectTypeDto>> getProjectTypes(String workspaceId);

    /**
     * Get information about project type with the specified ID.
     *
     * @param workspaceId
     *         id of current workspace
     * @param id
     *         id of the project type to get
     */
    Promise<ProjectTypeDto> getProjectType(String workspaceId, String id);
}

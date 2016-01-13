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
package org.eclipse.che.ide.api.app;

import com.google.inject.Inject;

import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;
import org.eclipse.che.ide.api.project.ProjectImpl;
import org.eclipse.che.ide.api.project.WorkspaceProjects;
import org.eclipse.che.ide.util.StartUpAction;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes current state of application.
 * E.g. current project, current workspace and etc.
 *
 * @author Vitaly Parfonov
 */
@Singleton
public class AppContext {

    private UsersWorkspaceDto workspace;
    private CurrentProject    currentProject;
    private CurrentUser       currentUser;
    private Factory           factory;
    private String            devMachineId;
    private String            projectsRoot;

    private final List<String> projectsInImport = new ArrayList<>();

    /**
     * Returns list of projects paths which are in importing state.
     *
     * @return list of project paths
     */
    public List<String> getImportingProjects() {
        return projectsInImport;
    }

    /**
     * Adds project path to list of projects which are in importing state.
     *
     * @param pathToProject
     *         project path
     */
    public void addProjectToImporting(String pathToProject) {
        projectsInImport.add(pathToProject);
    }

    /**
     * Removes project path to list of projects which are in importing state.
     *
     * @param pathToProject
     *         project path
     */
    public void removeProjectFromImporting(String pathToProject) {
        projectsInImport.remove(pathToProject);
    }

    /**
     * List of action with params that comes from startup URL.
     * Can be proccesed after IDE itialization as usual after starting Extension Server
     * and Project API initialization
     */
    private List<StartUpAction> startAppActions;


    /**
     * List of action with params that comes from startup URL.
     * Can be proccesed after IDE itialization as usual after starting Extension Server
     * and Project API initialization
     */
    public void setStartUpActions(List<StartUpAction> startUpActions) {
        this.startAppActions = startUpActions;
    }

    /**
     * List of startUp actions with parameters that comes form URL during IDE itialization
     * @return
     */
    public List<StartUpAction> getStartAppActions() {
        return startAppActions;
    }

    private ProjectImpl       activeProject;
    private WorkspaceProjects projectFactory;

    @Inject
    public AppContext(WorkspaceProjects projectFactory) {
        this.projectFactory = projectFactory;
    }

    public UsersWorkspaceDto getWorkspace() {
        return workspace;
    }

    public void setWorkspace(UsersWorkspaceDto workspace) {
        this.workspace = workspace;
        projectFactory.init(workspace.getProjects());
    }

    /** Returns id of current workspace of throws IllegalArgumentException if workspace is null. */
    public String getWorkspaceId() {
        if (workspace == null) {
            throw new IllegalArgumentException(getClass() + " Workspace can not be null.");
        }

        return workspace.getId();
    }

    /**
     * Returns {@link CurrentProject} instance that describes the project
     * that is currently opened or <code>null</code> if none opened.
     * <p/>
     * Note that current project may also represent a project's module.
     *
     * @return opened project or <code>null</code> if none opened
     */
    public CurrentProject getCurrentProject() {
        return currentProject;
    }

    /**
     * Set the current project instance.
     * <p/>
     * Should not be called directly as the current
     * project is managed by the core.
     */
    public void setCurrentProject(CurrentProject currentProject) {
        this.currentProject = currentProject;
    }

    /**
     * Returns current user.
     *
     * @return current user
     */
    public CurrentUser getCurrentUser() {
        return currentUser;
    }


    /**
     * Returns {@link Factory} instance which id was set on startup,
     * or {@code null} if no factory was specified.
     *
     * @return loaded factory or {@code null}
     */
    public Factory getFactory() {
        return factory;
    }

    public void setCurrentUser(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    /** Returns ID of the developer machine (where workspace is bound). */
    public String getDevMachineId() {
        return devMachineId;
    }

    public void setDevMachineId(String id) {
        this.devMachineId = id;
    }

    public void setFactory(Factory factory) {
        this.factory = factory;
    }

    public String getProjectsRoot() {
        return projectsRoot;
    }

    public void setProjectsRoot(String projectsRoot) {
        this.projectsRoot = projectsRoot;
    }


    /**
     * TODO experimental
     * @return
     */
    public ProjectImpl getActiveProject() {
        return activeProject;

    }

    /**
     * TODO experimental
     * @param configDto
     */
    public void setActiveProject(String projectName) {
        //this.activeProject = projectFactory.createProject(configDto);
        this.activeProject = projectFactory.getProject(projectName);
    }

}

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
package org.eclipse.che.ide.part.editor.recent;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.project.node.FileReferenceNode;

import javax.validation.constraints.NotNull;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.part.editor.recent.RecentFileStore.getShortPath;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action for the recent file. When user click on this action, recent file opens again in editor.
 *
 * @author Vlad Zhukovskiy
 */
public class RecentFileAction extends AbstractPerspectiveAction {

    private final FileReferenceNode file;

    @Inject
    public RecentFileAction(@Assisted FileReferenceNode file) {
        super(singletonList(PROJECT_PERSPECTIVE_ID), getShortPath(file.getStorablePath()), null, null, null);
        this.file = file;
    }

    /** {@inheritDoc} */
    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        event.getPresentation().setEnabledAndVisible(true);
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        file.actionPerformed();
    }

    /**
     * Return an id for the registration in action manager.
     *
     * @return action id
     */
    public String getId() {
        return "close-file-" + file.getName();
    }
}

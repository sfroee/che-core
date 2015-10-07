/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.workspace.server.model.impl;

import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.Recipe;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.EnvironmentState;
import org.eclipse.che.api.machine.server.model.impl.MachineStateImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

/**
 * @author Alexander Garagatyi
 */
public class EnvironmentStateImpl /*extends EnvironmentImpl*/ implements EnvironmentState {
    private String                 name;
    private Recipe                 recipe;
    private List<MachineStateImpl> machineStates;

    public EnvironmentStateImpl(String name, Recipe recipe, List<? extends MachineConfig> machineStates) {
        this.name = name;
        this.recipe = recipe;
        if (this.machineStates != null) {
            this.machineStates = machineStates.stream()
                                              .map(MachineStateImpl::new)
                                              .collect(toList());
        }
    }

    public EnvironmentStateImpl(Environment environment) {
        this(environment.getName(), environment.getRecipe(), environment.getMachineConfigs());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Recipe getRecipe() {
        return recipe;
    }

    @Override
    public List<MachineStateImpl> getMachineConfigs() {
        if (machineStates == null) {
            machineStates = new ArrayList<>();
        }
        return machineStates;
    }

    public void setMachineConfigs(List<MachineStateImpl> machineStates) {
        this.machineStates = machineStates;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof EnvironmentStateImpl)) return false;
        final EnvironmentStateImpl other = (EnvironmentStateImpl)obj;
        return Objects.equals(name, other.name) &&
               Objects.equals(recipe, other.recipe) &&
               getMachineConfigs().equals(other.getMachineConfigs());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + Objects.hashCode(name);
        hash = hash * 31 + Objects.hashCode(recipe);
        hash = hash * 31 + getMachineConfigs().hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "EnvironmentImpl{" +
               "name='" + name + '\'' +
               ", recipe=" + recipe +
               ", machineStates=" + machineStates +
               '}';
    }
}

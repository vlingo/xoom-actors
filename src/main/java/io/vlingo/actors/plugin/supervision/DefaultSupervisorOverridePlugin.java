// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.supervision;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Configuration;
import io.vlingo.actors.Registrar;
import io.vlingo.actors.plugin.AbstractPlugin;
import io.vlingo.actors.plugin.Plugin;
import io.vlingo.actors.plugin.PluginConfiguration;
import io.vlingo.actors.plugin.PluginProperties;

public class DefaultSupervisorOverridePlugin extends AbstractPlugin implements Plugin {
  private final DefaultSupervisorOverridePluginConfiguration configuration;

  public DefaultSupervisorOverridePlugin() {
    this.configuration = new DefaultSupervisorOverridePluginConfiguration();
  }

  @Override
  public void close() {
  }

  @Override
  public PluginConfiguration configuration() {
    return configuration;
  }

  @Override
  public String name() {
    return "override_supervisor";
  }

  @Override
  public int pass() {
    return 2;
  }

  @Override
  public void start(final Registrar registrar) {
    for (final ConfiguredSupervisor supervisor : configuration.supervisors) {
      registrar.registerDefaultSupervisor(supervisor.stageName, supervisor.supervisorName, supervisor.supervisorClass);
    }
  }

  @Override
  public Plugin with(final PluginConfiguration overrideConfiguration) {
    if (overrideConfiguration == null) {
      return this;
    }
    return new DefaultSupervisorOverridePlugin(overrideConfiguration);
  }

  @Override
  public void __internal_Only_Init(final String name, final Configuration configuration, final Properties properties) {
    // no-op
  }

  private DefaultSupervisorOverridePlugin(final PluginConfiguration configuration) {
    this.configuration = (DefaultSupervisorOverridePluginConfiguration) configuration;
  }

  public static class DefaultSupervisorOverridePluginConfiguration implements PluginConfiguration {
    private final List<ConfiguredSupervisor> supervisors;

    public static DefaultSupervisorOverridePluginConfiguration define() {
      return new DefaultSupervisorOverridePluginConfiguration();
    }

    public DefaultSupervisorOverridePluginConfiguration supervisor(final String stageName, final String supervisorName, final Class<? extends Actor> supervisorClass) {
      supervisors.add(new ConfiguredSupervisor(stageName, supervisorName, supervisorClass));
      return this;
    }

    public int count() {
      return supervisors.size();
    }

    public String name(final int index) {
      return supervisors.get(index).supervisorName;
    }

    public String stageName(final int index) {
      return supervisors.get(index).stageName;
    }

    public Class<? extends Actor> supervisorClass(final int index) {
      return supervisors.get(index).supervisorClass;
    }

    @Override
    public void build(final Configuration configuration) {
      configuration.with(supervisor("default", "overrideSupervisor", ConfiguredSupervisor.supervisorFrom("io.vlingo.actors.plugin.supervision.DefaultSupervisorOverride")));
    }

    @Override
    public void buildWith(final Configuration configuration, final PluginProperties properties) {
      for (final DefinitionValues values : DefinitionValues.allDefinitionValues(properties)) {
        final ConfiguredSupervisor supervisor =
                new ConfiguredSupervisor(
                        values.stageName,
                        values.name,
                        values.supervisor);

        supervisors.add(supervisor);
      }
      configuration.with(this);
    }

    @Override
    public String name() {
      return name(0);
    }

    private DefaultSupervisorOverridePluginConfiguration() {
      this.supervisors = new ArrayList<>();
    }
  }
}

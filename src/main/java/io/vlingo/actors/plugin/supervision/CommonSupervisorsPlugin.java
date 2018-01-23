// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.supervision;

import io.vlingo.actors.Registrar;
import io.vlingo.actors.plugin.Plugin;
import io.vlingo.actors.plugin.PluginProperties;

public class CommonSupervisorsPlugin implements Plugin {
  private String name;
  
  @Override
  public void close() {
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public int pass() {
    return 2;
  }

  @Override
  public void start(final Registrar registrar, final String name, final PluginProperties properties) {
    this.name = name;
    
    for (final DefinitionValues values : DefinitionValues.allDefinitionValues(properties)) {
      registrar.registerCommonSupervisor(values.stageName, values.name, values.protocol, values.supervisor);
    }
  }
}

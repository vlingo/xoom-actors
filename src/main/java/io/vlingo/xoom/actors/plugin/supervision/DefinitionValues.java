// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin.supervision;

import java.util.ArrayList;
import java.util.List;

import io.vlingo.xoom.actors.plugin.PluginProperties;

class DefinitionValues {
  private static final String STAGE = "stage=";
  private static final String NAME = "name=";
  private static final String PROTOCOL = "protocol=";
  private static final String SUPERVISOR = "supervisor=";
  
  final String name;
  final String protocol;
  final String stageName;
  final String supervisor;
  
  static List<DefinitionValues> allDefinitionValues(final PluginProperties properties) {
    final List<DefinitionValues> settings = new ArrayList<>();
    
    final String types = properties.getString("types", "");
    
    int nextDefinition = 0;
    boolean hasNext = true;
    while (hasNext) {
      final int open = types.indexOf("[", nextDefinition);
      final int close = types.indexOf("]", open+1);
      
      if (open >= 0 && close >= 0) {
        final String definition = types.substring(open+1, close);
        settings.add(new DefinitionValues(definition));
        nextDefinition = close+1;
      } else {
        hasNext = false;
      }
    }
    
    return settings;
  }

  DefinitionValues(final String definition) {
    this.stageName = stageFrom(definition);
    this.name = nameFrom(definition);
    this.protocol = protocolFrom(definition);
    this.supervisor = supervisorFrom(definition);
  }

  private String nameFrom(final String definition) {
    return partFor(definition, NAME);
  }

  private String protocolFrom(final String definition) {
    return partFor(definition, PROTOCOL);
  }

  private String stageFrom(final String definition) {
    return partFor(definition, STAGE);
  }

  private String supervisorFrom(final String definition) {
    return partFor(definition, SUPERVISOR);
  }
  
  private String partFor(final String definition, final String partName) {
    final int start = definition.indexOf(partName);
    
    if (start == -1) {
      return "";
    }
    
    final int startName = start + partName.length();
    final int end = definition.indexOf(" ", startName);
    final int actualEnd = end >= 0 ? end : definition.length();
    final String part = definition.substring(startName, actualEnd);
    return part;
  }
}

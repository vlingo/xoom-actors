// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import io.vlingo.actors.Configuration;

public class PluginLoader {
  private static final String pluginNamePrefix = "plugin.name.";

  private final Map<String,Plugin> plugins;

  public PluginLoader() {
    this.plugins = new HashMap<>();
  }

  public Collection<Plugin> loadEnabledPlugins(final Configuration configuration, final Properties properties) {
    if (!properties.isEmpty()) {
      for (String enabledPlugin : findEnabledPlugins(properties)) {
        loadPlugin(configuration, properties, enabledPlugin);
      }
    }
    return plugins.values();
  }

  private Set<String> findEnabledPlugins(final Properties properties) {
    final Set<String> enabledPlugins = new HashSet<String>();

    for (Enumeration<?> e = properties.keys(); e.hasMoreElements(); ) {
      final String key = (String) e.nextElement();
      if (key.startsWith(pluginNamePrefix)) {
        if (Boolean.parseBoolean(properties.getProperty(key)))
          enabledPlugins.add(key);
      }
    }

    return enabledPlugins;
  }

  private void loadPlugin(final Configuration configuration, final Properties properties, final String enabledPlugin) {
    final String pluginName = enabledPlugin.substring(pluginNamePrefix.length());
    final String classnameKey = "plugin." + pluginName + ".classname";
    final String classname = properties.getProperty(classnameKey);

    try {
      final Plugin maybePlugin = plugins.get(classname);
      if (maybePlugin == null) {
        final Class<?> pluginClass = Class.forName(classname);
        final Plugin plugin = (Plugin) pluginClass.newInstance();
        plugins.put(classname, plugin);
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new IllegalStateException("Cannot load plugin " + classname);
    }
  }
}

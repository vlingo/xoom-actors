// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import io.vlingo.actors.Registrar;

public class PluginLoader {

  private static final String propertiesFile = "/vlingo-actors.properties";
  private static final String pluginNamePrefix = "plugin.name.";

  private final Map<String,Plugin> plugins;

  public PluginLoader() {
    this.plugins = new HashMap<>();
  }

  public void loadEnabledPlugins(final Registrar registrar, final int pass, final boolean forceDefaults) {
    final Properties properties = forceDefaults ? loadDefaultProperties() : loadProperties();

    for (String enabledPlugin : findEnabledPlugins(properties)) {
      registerPlugin(registrar, properties, enabledPlugin, pass);
    }
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

  private Properties loadDefaultProperties() {
    final Properties properties = new Properties();
    setUpDefaulted(properties);
    return properties;
  }

  private Properties loadProperties() {
    final Properties properties = new Properties();

    try {
      properties.load(PluginLoader.class.getResourceAsStream(propertiesFile));
    } catch (IOException e) {
      setUpDefaulted(properties);
    }

    return properties;
  }

  private void registerPlugin(final Registrar registrar, final Properties properties, final String enabledPlugin, final int pass) {
    final String pluginName = enabledPlugin.substring(pluginNamePrefix.length());
    final String classnameKey = "plugin." + pluginName + ".classname";
    final String classname = properties.getProperty(classnameKey);

    try {
      final Plugin plugin = pluginOf(classname);
      if (plugin.pass() == pass) {
        plugin.start(registrar, pluginName, new PluginProperties(pluginName, properties));
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new IllegalStateException("Cannot load plugin " + classname);
    }
  }
  
  private Plugin pluginOf(final String classname) throws Exception {
    final Plugin maybePlugin = plugins.get(classname);
    if (maybePlugin == null) {
      final Class<?> pluginClass = Class.forName(classname);
      final Plugin plugin = (Plugin) pluginClass.newInstance();
      plugins.put(classname, plugin);
      return plugin;
    }
    return maybePlugin;
  }

  private void setUpDefaulted(final Properties properties) {
    properties.setProperty("plugin.name.pooledCompletes", "true");
    properties.setProperty("plugin.pooledCompletes.classname", "io.vlingo.actors.plugin.completes.PooledCompletesPlugin");
    properties.setProperty("plugin.pooledCompletes.pool", "10");

    properties.setProperty("plugin.name.queueMailbox", "true");
    properties.setProperty("plugin.queueMailbox.classname", "io.vlingo.actors.plugin.mailbox.concurrentqueue.ConcurrentQueueMailboxPlugin");
    properties.setProperty("plugin.queueMailbox.defaultMailbox", "true");
    properties.setProperty("plugin.queueMailbox.numberOfDispatchersFactor", "1.5");
    properties.setProperty("plugin.queueMailbox.dispatcherThrottlingCount", "10");

    properties.setProperty("plugin.name.jdkLogger", "true");
    properties.setProperty("plugin.jdkLogger.classname", "io.vlingo.actors.plugin.logging.jdk.JDKLoggerPlugin");
    properties.setProperty("plugin.jdkLogger.name", "vlingo/actors");
    properties.setProperty("plugin.jdkLogger.defaultLogger", "true");
    properties.setProperty("plugin.jdkLogger.handler.classname", "io.vlingo.actors.plugin.logging.jdk.DefaultHandler");
    properties.setProperty("plugin.jdkLogger.handler.name", "vlingo");
    properties.setProperty("plugin.jdkLogger.handler.level", "ALL");
  }
}

// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import io.vlingo.actors.plugin.Plugin;
import io.vlingo.actors.plugin.PluginLoader;
import io.vlingo.actors.plugin.PluginProperties;
import io.vlingo.actors.plugin.completes.PooledCompletesPlugin.PooledCompletesPluginConfiguration;
import io.vlingo.actors.plugin.logging.jdk.JDKLoggerPlugin.JDKLoggerPluginConfiguration;
import io.vlingo.actors.plugin.mailbox.agronampscarrayqueue.ManyToOneConcurrentArrayQueuePlugin.ManyToOneConcurrentArrayQueuePluginConfiguration;
import io.vlingo.actors.plugin.mailbox.concurrentqueue.ConcurrentQueueMailboxPlugin.ConcurrentQueueMailboxPluginConfiguration;
import io.vlingo.actors.plugin.mailbox.sharedringbuffer.SharedRingBufferMailboxPlugin.SharedRingBufferMailboxPluginConfiguration;
import io.vlingo.actors.plugin.supervision.CommonSupervisorsPlugin.CommonSupervisorsPluginConfiguration;
import io.vlingo.actors.plugin.supervision.DefaultSupervisorOverridePlugin.DefaultSupervisorOverridePluginConfiguration;

public class Configuration {
  private ConcurrentQueueMailboxPluginConfiguration concurrentQueueMailboxPluginConfiguration;
  private CommonSupervisorsPluginConfiguration commonSupervisorsPluginConfiguration;
  private DefaultSupervisorOverridePluginConfiguration defaultSupervisorOverridePluginConfiguration;
  private JDKLoggerPluginConfiguration jdkLoggerPluginConfiguration;
  private PooledCompletesPluginConfiguration pooledCompletesPluginConfiguration;
  private ManyToOneConcurrentArrayQueuePluginConfiguration manyToOneConcurrentArrayQueuePluginConfiguration;
  private SharedRingBufferMailboxPluginConfiguration sharedRingBufferMailboxPluginConfiguration;

  private String mainProxyGeneratedClassesPath;
  private String mainProxyGeneratedSourcesPath;
  private String testProxyGeneratedClassesPath;
  private String testProxyGeneratedSourcesPath;

  private final List<Plugin> plugins;

  public static Configuration define() {
    return new Configuration();
  }

  public static Configuration defineAlongWith(final Properties properties) {
    return new Configuration(properties, true);
  }

  public static Configuration defineWith(final Properties properties) {
    return new Configuration(properties, false);
  }

  public Collection<Plugin> allPlugins() {
    return Collections.unmodifiableCollection(plugins);
  }

  public Configuration with(final CommonSupervisorsPluginConfiguration configuration) {
    this.commonSupervisorsPluginConfiguration = configuration;
    return this;
  }

  public CommonSupervisorsPluginConfiguration commonSupervisorsPluginConfiguration() {
    return commonSupervisorsPluginConfiguration;
  }

  public Configuration with(final ConcurrentQueueMailboxPluginConfiguration configuration) {
    concurrentQueueMailboxPluginConfiguration = configuration;
    return this;
  }

  public ConcurrentQueueMailboxPluginConfiguration concurrentQueueMailboxPluginConfiguration() {
    return concurrentQueueMailboxPluginConfiguration;
  }

  public Configuration with(final DefaultSupervisorOverridePluginConfiguration configuration) {
    this.defaultSupervisorOverridePluginConfiguration = configuration;
    return this;
  }

  public DefaultSupervisorOverridePluginConfiguration defaultSupervisorOverridePluginConfiguration() {
    return defaultSupervisorOverridePluginConfiguration;
  }

  public Configuration with(final JDKLoggerPluginConfiguration configuration) {
    this.jdkLoggerPluginConfiguration = configuration;
    return this;
  }

  public JDKLoggerPluginConfiguration jdkLoggerPluginConfiguration() {
    return jdkLoggerPluginConfiguration;
  }

  public Configuration with(final ManyToOneConcurrentArrayQueuePluginConfiguration configuration) {
    this.manyToOneConcurrentArrayQueuePluginConfiguration = configuration;
    return this;
  }

  public ManyToOneConcurrentArrayQueuePluginConfiguration manyToOneConcurrentArrayQueuePluginConfiguration() {
    return manyToOneConcurrentArrayQueuePluginConfiguration;
  }

  public Configuration with(final PooledCompletesPluginConfiguration configuration) {
    pooledCompletesPluginConfiguration = configuration;
    return this;
  }

  public PooledCompletesPluginConfiguration pooledCompletesPluginConfiguration() {
    return pooledCompletesPluginConfiguration;
  }

  public Configuration with(final SharedRingBufferMailboxPluginConfiguration configuration) {
    this.sharedRingBufferMailboxPluginConfiguration = configuration;
    return this;
  }

  public SharedRingBufferMailboxPluginConfiguration sharedRingBufferMailboxPluginConfiguration() {
    return sharedRingBufferMailboxPluginConfiguration;
  }

  public Configuration usingMainProxyGeneratedClassesPath(final String path) {
    mainProxyGeneratedClassesPath = path;
    return this;
  }

  public String mainProxyGeneratedClassesPath() {
    return mainProxyGeneratedClassesPath;
  }

  public Configuration usingMainProxyGeneratedSourcesPath(final String path) {
    mainProxyGeneratedSourcesPath = path;
    return this;
  }

  public String mainProxyGeneratedSourcesPath() {
    return mainProxyGeneratedSourcesPath;
  }

  public Configuration usingTestProxyGeneratedClassesPath(final String path) {
    testProxyGeneratedClassesPath = path;
    return this;
  }

  public String testProxyGeneratedClassesPath() {
    return testProxyGeneratedClassesPath;
  }

  public Configuration usingTestProxyGeneratedSourcesPath(final String path) {
    testProxyGeneratedSourcesPath = path;
    return this;
  }

  public String testProxyGeneratedSourcesPath() {
    return testProxyGeneratedSourcesPath;
  }

  public void startPlugins(final World world, final int pass) {
    for (final Plugin plugin : plugins) {
      if (plugin.pass() == pass) {
        plugin.start(world);
      }
    }
  }

  private Configuration() {
    this.plugins = loadPlugins(true);

    this
      .usingMainProxyGeneratedClassesPath("target/classes/")
      .usingMainProxyGeneratedSourcesPath("target/generated-sources/")
      .usingTestProxyGeneratedClassesPath("target/test-classes/")
      .usingTestProxyGeneratedSourcesPath("target/generated-test-sources/");
  }

  private Configuration(final Properties properties, final boolean includeBaseLoad) {
    if (includeBaseLoad) {
      final List<Plugin> plugins = loadPlugins(false);
      this.plugins = loadPropertiesPlugins(properties, plugins);
    } else {
      this.plugins = loadPropertiesPlugins(properties, new ArrayList<>());
    }
  }

  private List<Plugin> loadPropertiesPlugins(final Properties properties, final List<Plugin> plugins) {
    final Set<Plugin> unique = new HashSet<>(plugins);
    unique.addAll(new PluginLoader().loadEnabledPlugins(this, properties));
    for (final Plugin plugin : unique) {
      plugin.configuration().buildWith(this, new PluginProperties(plugin.name(), properties));
    }
    return new ArrayList<>(unique);
  }

  private List<Plugin> loadPlugins(final boolean build) {
    final List<Class<?>> pluginClasses = Arrays.asList(
            io.vlingo.actors.plugin.completes.PooledCompletesPlugin.class,
            io.vlingo.actors.plugin.logging.jdk.JDKLoggerPlugin.class,
            io.vlingo.actors.plugin.mailbox.agronampscarrayqueue.ManyToOneConcurrentArrayQueuePlugin.class,
            io.vlingo.actors.plugin.mailbox.concurrentqueue.ConcurrentQueueMailboxPlugin.class,
            io.vlingo.actors.plugin.mailbox.sharedringbuffer.SharedRingBufferMailboxPlugin.class,
            io.vlingo.actors.plugin.supervision.CommonSupervisorsPlugin.class,
            io.vlingo.actors.plugin.supervision.DefaultSupervisorOverridePlugin.class);

    final List<Plugin> plugins = new ArrayList<>();
    for (final Class<?> pluginClass : pluginClasses) {
      final String classname = pluginClass.getName();
      try {
        final Plugin plugin = (Plugin) pluginClass.newInstance();
        if (build) { plugin.configuration().build(this); }
        plugins.add(plugin);
      } catch (Exception e) {
        throw new IllegalStateException("Cannot load plugin class: " + classname);
      }
    }
    return plugins;
  }
}

// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vlingo.xoom.actors.plugin.Plugin;
import io.vlingo.xoom.actors.plugin.PluginClassLoader;
import io.vlingo.xoom.actors.plugin.PluginConfiguration;
import io.vlingo.xoom.actors.plugin.PluginFactory;
import io.vlingo.xoom.actors.plugin.PluginLoader;
import io.vlingo.xoom.actors.plugin.PluginProperties;
import io.vlingo.xoom.actors.plugin.completes.PooledCompletesPlugin.PooledCompletesPluginConfiguration;
import io.vlingo.xoom.actors.plugin.logging.slf4j.Slf4jLoggerPlugin;
import io.vlingo.xoom.actors.plugin.mailbox.agronampscarrayqueue.ManyToOneConcurrentArrayQueuePlugin.ManyToOneConcurrentArrayQueuePluginConfiguration;
import io.vlingo.xoom.actors.plugin.mailbox.concurrentqueue.ConcurrentQueueMailboxPlugin.ConcurrentQueueMailboxPluginConfiguration;
import io.vlingo.xoom.actors.plugin.mailbox.sharedringbuffer.SharedRingBufferMailboxPlugin.SharedRingBufferMailboxPluginConfiguration;
import io.vlingo.xoom.actors.plugin.supervision.CommonSupervisorsPlugin.CommonSupervisorsPluginConfiguration;
import io.vlingo.xoom.actors.plugin.supervision.DefaultSupervisorOverridePlugin.DefaultSupervisorOverridePluginConfiguration;

public class Configuration {
  private ConcurrentQueueMailboxPluginConfiguration concurrentQueueMailboxPluginConfiguration;
  private CommonSupervisorsPluginConfiguration commonSupervisorsPluginConfiguration;
  private DefaultSupervisorOverridePluginConfiguration defaultSupervisorOverridePluginConfiguration;
  private Slf4jLoggerPlugin.Slf4jLoggerPluginConfiguration slf4jPluginConfiguration;
  private PooledCompletesPluginConfiguration pooledCompletesPluginConfiguration;
  private ManyToOneConcurrentArrayQueuePluginConfiguration manyToOneConcurrentArrayQueuePluginConfiguration;
  private SharedRingBufferMailboxPluginConfiguration sharedRingBufferMailboxPluginConfiguration;
  private DirectoryEvictionConfiguration directoryEvictionConfiguration;

  private String mainProxyGeneratedClassesPath;
  private String mainProxyGeneratedSourcesPath;
  private String testProxyGeneratedClassesPath;
  private String testProxyGeneratedSourcesPath;

  private final Map<String,PluginConfiguration> configurationOverrides;
  private final boolean mergeProperties;
  private final List<Plugin> plugins;
  private final List<Plugin> dynamicPlugins;
  private final Properties properties;

  private AddressFactory addressFactory;

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
    return Stream.concat(plugins.stream(), dynamicPlugins.stream())
        .collect(Collectors.toCollection(() -> new ArrayList<>(plugins.size() + dynamicPlugins.size())));
  }

  public Configuration with(final AddressFactory addressFactory) {
    this.addressFactory = addressFactory;
    return this;
  }

  public AddressFactory addressFactoryOr(final Supplier<AddressFactory> addressFactorySupplier) {
    return addressFactory == null ? addressFactorySupplier.get() : addressFactory;
  }

  public String getProperty(final String key) {
    return properties == null
        ? null
        : properties.getProperty(key);
  }

  public String getProperty(final String key, final String defaultValue) {
    return properties == null
        ? defaultValue
        : properties.getProperty(key, defaultValue);
  }

  public Configuration with(final CommonSupervisorsPluginConfiguration configuration) {
    if (this.commonSupervisorsPluginConfiguration == null) {
      this.commonSupervisorsPluginConfiguration = configuration;
    }
    this.configurationOverrides.put(configuration.getClass().getSimpleName(), configuration);
    return this;
  }

  public CommonSupervisorsPluginConfiguration commonSupervisorsPluginConfiguration() {
    return commonSupervisorsPluginConfiguration;
  }

  public Configuration with(final ConcurrentQueueMailboxPluginConfiguration configuration) {
    if (this.concurrentQueueMailboxPluginConfiguration == null) {
      this.concurrentQueueMailboxPluginConfiguration = configuration;
    }
    this.configurationOverrides.put(configuration.getClass().getSimpleName(), configuration);
    return this;
  }

  public ConcurrentQueueMailboxPluginConfiguration concurrentQueueMailboxPluginConfiguration() {
    return concurrentQueueMailboxPluginConfiguration;
  }

  public Configuration with(final DefaultSupervisorOverridePluginConfiguration configuration) {
    if (this.defaultSupervisorOverridePluginConfiguration == null) {
      this.defaultSupervisorOverridePluginConfiguration = configuration;
    }
    this.configurationOverrides.put(configuration.getClass().getSimpleName(), configuration);
    return this;
  }

  public DefaultSupervisorOverridePluginConfiguration defaultSupervisorOverridePluginConfiguration() {
    return defaultSupervisorOverridePluginConfiguration;
  }

  public Configuration with(final Slf4jLoggerPlugin.Slf4jLoggerPluginConfiguration configuration) {
    // NOTE: There may be only one registered Slf4jLoggerPluginConfiguration
    this.slf4jPluginConfiguration = configuration;
    this.configurationOverrides.put(configuration.getClass().getSimpleName(), configuration);
    return this;
  }

  public Slf4jLoggerPlugin.Slf4jLoggerPluginConfiguration slf4jPluginConfiguration() {
    return slf4jPluginConfiguration;
  }

  public Configuration with(final ManyToOneConcurrentArrayQueuePluginConfiguration configuration) {
    if (this.manyToOneConcurrentArrayQueuePluginConfiguration == null) {
      this.manyToOneConcurrentArrayQueuePluginConfiguration = configuration;
    }
    this.configurationOverrides.put(configuration.getClass().getSimpleName(), configuration);
    return this;
  }

  public ManyToOneConcurrentArrayQueuePluginConfiguration manyToOneConcurrentArrayQueuePluginConfiguration() {
    return manyToOneConcurrentArrayQueuePluginConfiguration;
  }

  public Configuration with(final PooledCompletesPluginConfiguration configuration) {
    if (this.pooledCompletesPluginConfiguration == null) {
      this.pooledCompletesPluginConfiguration = configuration;
    }
    this.configurationOverrides.put(configuration.getClass().getSimpleName(), configuration);
    return this;
  }

  public PooledCompletesPluginConfiguration pooledCompletesPluginConfiguration() {
    return pooledCompletesPluginConfiguration;
  }

  public Configuration with(final SharedRingBufferMailboxPluginConfiguration configuration) {
    if (this.sharedRingBufferMailboxPluginConfiguration == null) {
      this.sharedRingBufferMailboxPluginConfiguration = configuration;
    }
    this.configurationOverrides.put(configuration.getClass().getSimpleName(), configuration);
    return this;
  }

  public SharedRingBufferMailboxPluginConfiguration sharedRingBufferMailboxPluginConfiguration() {
    return sharedRingBufferMailboxPluginConfiguration;
  }

  public Configuration with(final DirectoryEvictionConfiguration configuration) {
    if (this.directoryEvictionConfiguration == null) {
      this.directoryEvictionConfiguration = configuration;
    }
    this.configurationOverrides.put(configuration.getClass().getSimpleName(), configuration);
    return this;
  }

  public DirectoryEvictionConfiguration directoryEvictionConfiguration() {
    return directoryEvictionConfiguration;
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

  public void startPlugins(final Registrar registrar, final int pass) {
    load(pass);

    for (final Plugin plugin : plugins) {
//      if (plugin.toString().contains("JDKLoggerPlugin")) {
//        System.out.println("PASS: " + pass + " LOOKS LIKE: " + plugin.toString());
//      }
      if (plugin.pass() == pass) {
        plugin.start(registrar);
      }
    }
  }

  public void load(final int pass) {
    if (pass == 0) {
      if (properties != null) {
        if (mergeProperties) {
          final List<Plugin> plugins = loadPlugins(false);
          plugins.addAll(loadPropertiesPlugins(properties, plugins));
        } else {
          plugins.addAll(loadPropertiesPlugins(properties, new ArrayList<>()));
        }
      } else {
//        System.out.println("################################### LOADING FOR PASS " + pass);
        plugins.addAll(loadPlugins(true));
      }
    }
  }

  public void loadAndStartDynamicPlugins(final Registrar registrar, final PluginClassLoader pluginClassLoader, final Properties pluginsProperties) {
    Collection<Plugin> dynamicPlugins = (new PluginLoader()).loadEnabledPlugins(this, pluginsProperties, pluginClassLoader);
    dynamicPlugins.forEach(plugin -> plugin.configuration().buildWith(this, new PluginProperties(plugin.name(), pluginsProperties)));

    dynamicPlugins.forEach(plugin -> plugin.start(registrar));
    this.dynamicPlugins.addAll(dynamicPlugins);
  }

  private PluginConfiguration overrideConfiguration(final Plugin plugin) {
    return configurationOverrides.get(plugin.configuration().getClass().getSimpleName());
  }

  private Configuration() {
    this(null, false);
  }

  private Configuration(final Properties properties, final boolean includeBaseLoad) {
    this.configurationOverrides = new HashMap<>();
    this.plugins = new ArrayList<>();
    this.dynamicPlugins = new ArrayList<>();
    this.properties = properties;
    this.mergeProperties = includeBaseLoad;

    this
      .usingMainProxyGeneratedClassesPath("target/classes/")
      .usingMainProxyGeneratedSourcesPath("target/generated-sources/")
      .usingTestProxyGeneratedClassesPath("target/test-classes/")
      .usingTestProxyGeneratedSourcesPath("target/generated-test-sources/");
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
    final List<PluginFactory> pluginFactories = Arrays.asList(
            io.vlingo.xoom.actors.plugin.completes.PooledCompletesPlugin::new,
            io.vlingo.xoom.actors.plugin.logging.slf4j.Slf4jLoggerPlugin::new,
            io.vlingo.xoom.actors.plugin.mailbox.agronampscarrayqueue.ManyToOneConcurrentArrayQueuePlugin::new,
            io.vlingo.xoom.actors.plugin.mailbox.concurrentqueue.ConcurrentQueueMailboxPlugin::new,
            io.vlingo.xoom.actors.plugin.mailbox.sharedringbuffer.SharedRingBufferMailboxPlugin::new,
            io.vlingo.xoom.actors.plugin.supervision.CommonSupervisorsPlugin::new,
            io.vlingo.xoom.actors.plugin.supervision.DefaultSupervisorOverridePlugin::new,
            io.vlingo.xoom.actors.plugin.eviction.DirectoryEvictionPlugin::new
        );

    final List<Plugin> plugins = new ArrayList<>();
    for (final PluginFactory pluginFactory : pluginFactories) {
      try {
        final Plugin plugin = pluginFactory.build();
        final PluginConfiguration pc = overrideConfiguration(plugin);
        final boolean reallyBuild = pc == null ? build : false;
        final Plugin configuredPlugin = plugin.with(pc);
        if (reallyBuild) {
          configuredPlugin.configuration().build(this);
//          if (configuredPlugin.toString().contains("JDKLoggerPlugin")) {
//            System.out.println("BUILDING THE JDKLOGGERPLUGIN FOR: " + configuredPlugin);
//          }
//          if (configuredPlugin.toString().contains("DefaultHandler")) {
//            System.out.println("WRONG PLUGIN: " + configuredPlugin + " STACK TRACE: ");
//            (new Exception()).printStackTrace();
//          }
        }
//        if (configuredPlugin.toString().contains("JDKLoggerPlugin")) {
//          System.out.println("JDKLOGGERPLUGIN OVERRIDE IS: " + pc);
//        }
        plugins.add(configuredPlugin);
      } catch (Exception e) {
        throw new IllegalStateException("Cannot load plugin class: " + e.getMessage(), e);
      }
    }
    return plugins;
  }
}

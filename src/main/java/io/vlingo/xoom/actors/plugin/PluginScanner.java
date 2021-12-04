// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin;

import io.vlingo.xoom.actors.Configuration;
import io.vlingo.xoom.actors.Registrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * This class scans and loads dynamically {@link Plugin}s at runtime.
 */
public class PluginScanner {
  private static final Logger logger = LoggerFactory.getLogger(PluginScanner.class);

  private static final String PROPERTIES_EXTENSION = ".properties";
  private static final String JAR_EXTENSION = ".jar";
  private static final String PLUGIN_FOLDER = System.getProperty("java.io.tmpdir") + "xoom"
      + System.getProperty("file.separator") + "plugins";

  private final Configuration configuration;
  private final Registrar registrar;

  private ScheduledExecutorService executorService = null;
  private final Set<String> jarPathNames = new HashSet<>();

  public PluginScanner(Configuration configuration, Registrar registrar) {
    this.configuration = configuration;
    this.registrar = registrar;
  }

  public void startScan() {
    if (executorService != null) {
      throw new IllegalStateException("Scanning of plugins has already been started!");
    }

    executorService = Executors.newScheduledThreadPool(1);
    executorService.scheduleAtFixedRate(this::scan, 5, 20, TimeUnit.SECONDS);
  }

  public void stopScan() {
    if (executorService == null) {
      throw new IllegalStateException("Scanning of plugins has already been stopped!");
    }

    executorService.shutdown();
    try {
      executorService.awaitTermination(3, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      logger.warn("Stopping plugin scanner failed because of " + e.getMessage(), e);
    } finally {
      executorService = null;
    }
  }

  private void scan() {
    Path pluginPath = Paths.get(PLUGIN_FOLDER);
    if (Files.exists(pluginPath) && Files.isDirectory(pluginPath)) {
      try (Stream<Path> fileStream = Files.walk(pluginPath)) {
        fileStream.filter(path -> Files.isRegularFile(path) && Files.isReadable(path) && path.toFile().getName().endsWith(JAR_EXTENSION))
            .filter(path -> !jarPathNames.contains(path.toFile().getPath()))
            .forEach(this::loadAndStartPlugins);
      } catch (Throwable t) {
        logger.warn("Plugin scanner failed because of " + t.getMessage(), t);
      }
    }
  }

  private void loadAndStartPlugins(Path jarPath) {
    String jarPathName = jarPath.toFile().getPath();
    String jarFileName = jarPath.toFile().getName();

    try {
      PluginClassLoader pluginClassLoader = PluginClassLoader.dynamicClassLoader(new URL[]{jarPath.toUri().toURL()});
      String propertiesFileName = jarFileName.substring(0, jarFileName.length() - JAR_EXTENSION.length()) + PROPERTIES_EXTENSION;
      Properties pluginProperties = new Properties();
      InputStream propertiesInStream = pluginClassLoader.loadResource(propertiesFileName);

      if (propertiesInStream == null) {
        // properties file must have the same name as the jar name
        logger.warn("Configuration properties file is missing from plugin jar " + jarPathName);
        return;
      }

      pluginProperties.load(propertiesInStream);
      configuration.loadAndStartDynamicPlugins(registrar, pluginClassLoader, pluginProperties);
      jarPathNames.add(jarPathName);
    } catch (Exception e) {
      logger.warn("Failed to load and start plugins from " + jarPathName + " because of " + e.getMessage(), e);
    }
  }
}

// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

public interface PluginClassLoader {
  Class<?> loadClass(String className) throws ClassNotFoundException;
  InputStream loadResource(String resourceName);

  static PluginClassLoader staticClassLoader() {
    return new PluginClassLoader() {
      @Override
      public Class<?> loadClass(String className) throws ClassNotFoundException {
        return Class.forName(className);
      }

      @Override
      public InputStream loadResource(String resourceName) {
        return getClass().getResourceAsStream(resourceName);
      }
    };
  }

  @SuppressWarnings("resource")
  static PluginClassLoader dynamicClassLoader(URL[] jarUrls) {
    final URLClassLoader classLoader = new URLClassLoader(jarUrls, PluginClassLoader.class.getClassLoader());

    return new PluginClassLoader() {
      @Override
      public Class<?> loadClass(String className) throws ClassNotFoundException {
        return classLoader.loadClass(className);
      }

      @Override
      public InputStream loadResource(String resourceName) {
        return classLoader.getResourceAsStream(resourceName);
      }
    };
  }
}

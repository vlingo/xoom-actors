// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.HashMap;
import java.util.Map;

final class LoggerProviderKeeper {
  private final Map<String,LoggerProviderInfo> loggerProviderInfos;
  
  LoggerProviderKeeper() {
    this.loggerProviderInfos = new HashMap<>();
  }
  
  void close() {
    for (final LoggerProviderInfo info : loggerProviderInfos.values()) {
      info.loggerProvider.close();
    }
  }
  
  LoggerProvider findDefault() {
    for (final LoggerProviderInfo info : loggerProviderInfos.values()) {
      if (info.isDefault) {
        return info.loggerProvider;
      }
    }
    return null;
  }

  LoggerProvider findNamed(final String name) {
    final LoggerProviderInfo info = loggerProviderInfos.get(name);
    
    if (info != null) {
      return info.loggerProvider;
    }
    
    throw new IllegalStateException("No registered LoggerProvider named: " + name);
  }

  void keep(final String name, boolean isDefault, final LoggerProvider loggerProvider) {
    if (loggerProviderInfos.isEmpty() || findDefault() == null) {
      isDefault = true;
    }
    
    if (isDefault) {
      undefaultCurrentDefault();
    }

    loggerProviderInfos.put(name, new LoggerProviderInfo(name, loggerProvider, isDefault));
  }

  private void undefaultCurrentDefault() {
    for (final String key : loggerProviderInfos.keySet()) {
      final LoggerProviderInfo info = loggerProviderInfos.get(key);
      if (info != null && info.isDefault) {
        loggerProviderInfos.put(key, new LoggerProviderInfo(info.name, info.loggerProvider, false));
      }
    }
  }

  final class LoggerProviderInfo {
    final boolean isDefault;
    final LoggerProvider loggerProvider;
    final String name;

    LoggerProviderInfo(final String name, final LoggerProvider loggerProvider, final boolean isDefault) {
      this.name = name;
      this.loggerProvider = loggerProvider;
      this.isDefault = isDefault;
    }
  }
}

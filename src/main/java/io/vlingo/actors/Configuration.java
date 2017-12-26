// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.function.Supplier;

public class Configuration {
  private Supplier<Logger> logger = SystemOutLogger::new;
  private Supplier<ProxyFactory> proxyFactory = JdkProxyFactory::new;

  public Configuration() { }

  public Supplier<Logger> logger() {
    return logger;
  }

  public void setLogger(final Supplier<Logger> logger) {
    this.logger = logger;
  }

  public Supplier<ProxyFactory> proxyFactory() {
    return proxyFactory;
  }

  public void setProxyFactory(final Supplier<ProxyFactory> proxyFactory) {
    this.proxyFactory = proxyFactory;
  }
}

// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.function.Supplier;

public class Configuration {
  private Supplier<ProxyFactory> proxyFactory = JdkProxyFactory::new;

  public Configuration() { }

  public Supplier<ProxyFactory> proxyFactory() {
    return proxyFactory;
  }

  public void setProxyFactory(final Supplier<ProxyFactory> proxyFactory) {
    this.proxyFactory = proxyFactory;
  }
}

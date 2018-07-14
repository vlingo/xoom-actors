// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.completes;

import io.vlingo.actors.CompletesEventuallyProvider;
import io.vlingo.actors.Registrar;
import io.vlingo.actors.plugin.Plugin;
import io.vlingo.actors.plugin.PluginProperties;

public class PooledCompletesPlugin implements Plugin {
  private CompletesEventuallyProvider completesEventuallyProvider;
  private String name;
  
  @Override
  public void close() {
    completesEventuallyProvider.close();
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public int pass() {
    return 2;
  }

  @Override
  public void start(final Registrar registrar, final String name, final PluginProperties properties) {
    final int poolSize = properties.getInteger("pool", 10);
    final String mailboxName = properties.getString("mailbox", null);
    
    this.name = name;
    
    this.completesEventuallyProvider = new CompletesEventuallyPool(poolSize, mailboxName);
    
    registrar.register(name, completesEventuallyProvider);
  }
}

// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.completes;

import java.util.concurrent.atomic.AtomicLong;

import io.vlingo.actors.Address;
import io.vlingo.actors.CompletesEventually;
import io.vlingo.actors.CompletesEventuallyActor;
import io.vlingo.actors.CompletesEventuallyProvider;
import io.vlingo.actors.Definition;
import io.vlingo.actors.PooledCompletes;
import io.vlingo.actors.Returns;
import io.vlingo.actors.Stage;

public class CompletesEventuallyPool implements CompletesEventuallyProvider {
  private final AtomicLong completesEventuallyId;
  private final String mailboxName;
  private final CompletesEventually[] pool;
  private final AtomicLong poolIndex;
  private final long poolSize;

  CompletesEventuallyPool(final int poolSize, final String mailboxName) {
    this.completesEventuallyId = new AtomicLong(0);
    this.poolSize = poolSize;
    this.mailboxName = mailboxName;
    this.poolIndex = new AtomicLong(0);
    this.pool = new CompletesEventually[poolSize];
  }

  @Override
  public void close() {
    for (final CompletesEventually completes : pool) {
      completes.stop();
    }
  }

  @Override
  public CompletesEventually completesEventually() {
    final int index = (int)(poolIndex.incrementAndGet() % poolSize);
    return pool[index];
  }

  @Override
  public void initializeUsing(final Stage stage) {
    for (int idx = 0; idx < poolSize; ++idx) {
      pool[idx] =
              stage.actorFor(
                      CompletesEventually.class,
                      Definition.has(
                              CompletesEventuallyActor.class,
                              CompletesEventually.Instantiator,
                              mailboxName,
                              "completes-eventually-" + (idx + 1)));
    }
  }

  @Override
  public CompletesEventually provideCompletesFor(final Returns<?> clientReturns) {
    return new PooledCompletes(
            completesEventuallyId.getAndIncrement(),
            clientReturns,
            completesEventually());
  }

  @Override
  public CompletesEventually provideCompletesFor(final Address address, final Returns<?> clientReturns) {
    return new PooledCompletes(
            completesEventuallyId.getAndIncrement(),
            clientReturns,
            completesEventuallyOf(address));
  }

  private CompletesEventually completesEventuallyOf(final Address address) {
    for (final CompletesEventually completesEventually : pool) {
      if (completesEventually.address().equals(address)) {
        return completesEventually;
      }
    }
    return completesEventually();
  }
}

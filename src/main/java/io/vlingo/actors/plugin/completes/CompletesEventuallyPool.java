// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.completes;

import java.util.concurrent.atomic.AtomicLong;

import io.vlingo.actors.Completes;
import io.vlingo.actors.CompletesEventually;
import io.vlingo.actors.CompletesEventuallyActor;
import io.vlingo.actors.CompletesEventuallyProvider;
import io.vlingo.actors.CompletesHolder;
import io.vlingo.actors.Definition;
import io.vlingo.actors.Stage;

public class CompletesEventuallyPool implements CompletesEventuallyProvider {
  private final AtomicLong completesEventuallyId;
  private final CompletesEventually[] pool;
  private final AtomicLong poolIndex;
  private final long poolSize;

  CompletesEventuallyPool(final int poolSize) {
    this.completesEventuallyId = new AtomicLong(0);
    this.poolSize = poolSize;
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
      pool[idx] = stage.actorFor(Definition.has(CompletesEventuallyActor.class, Definition.NoParameters), CompletesEventually.class);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Completes<T> provideCompletesFor(final Completes<T> clientCompletes) {
    return (Completes<T>) new CompletesHolder(
            completesEventuallyId.getAndIncrement(),
            (Completes<Object>) clientCompletes,
            completesEventually());
  }
}

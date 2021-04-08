// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin.mailbox.concurrentqueue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.vlingo.xoom.actors.Dispatcher;
import io.vlingo.xoom.actors.Mailbox;

public class ExecutorDispatcher implements Dispatcher {
  private final AtomicBoolean closed = new AtomicBoolean(false);
  private final ExecutorService executor;
  private final int numberOfThreads;

  protected ExecutorDispatcher(final int availableThreads, final int numberOfDispatchers, final float numberOfDispatchersFactor) {
    this.numberOfThreads =
            numberOfDispatchers > 0 ?
                    numberOfDispatchers :
                    (int) (availableThreads * numberOfDispatchersFactor);

    this.executor = new ThreadPoolExecutor(numberOfThreads, numberOfThreads,
        0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<>(),
        new RejectionHandler());
  }

  @Override
  public int concurrencyCapacity() {
    return numberOfThreads;
  }

  @Override
  public void close() {
    closed.set(true);
    executor.shutdown();
  }

  @Override
  public boolean isClosed() {
    return closed.get();
  }

  @Override
  public void execute(final Mailbox mailbox) {
    if (!closed.get()) {
      executor.execute(mailbox);
    }
  }

  @Override
  public boolean requiresExecutionNotification() {
    return false;
  }

  private class RejectionHandler implements RejectedExecutionHandler {

    @Override
    public void rejectedExecution(final Runnable runnable, final ThreadPoolExecutor executor) {
      if (!executor.isShutdown() && !executor.isTerminated())
        throw new IllegalStateException("Message cannot be sent due to current system resource limitations.");
    }
  }
}

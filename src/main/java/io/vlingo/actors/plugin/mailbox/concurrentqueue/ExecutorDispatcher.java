// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.concurrentqueue;

import io.vlingo.actors.Dispatcher;
import io.vlingo.actors.Mailbox;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExecutorDispatcher implements Dispatcher {
  private final AtomicBoolean closed = new AtomicBoolean(false);
  private final ExecutorService executor;

  protected ExecutorDispatcher(final int availableThreads, final float numberOfDispatchersFactor) {
    final int numberOfThreads = (int) ((float) availableThreads * numberOfDispatchersFactor);
    this.executor = new ThreadPoolExecutor(numberOfThreads, numberOfThreads,
        0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<>(),
        new RejectionHandler());
  }

  public void close() {
    closed.set(true);
    executor.shutdown();
  }

  public void execute(final Mailbox mailbox) {
    if (!closed.get()) {
      if (mailbox.delivering(true)) {
        executor.execute(mailbox);
      }
    }
  }

  public boolean requiresExecutionNotification() {
    return false;
  }

  private class RejectionHandler implements RejectedExecutionHandler {

    public void rejectedExecution(final Runnable runnable, final ThreadPoolExecutor executor) {
      if (!executor.isShutdown() && !executor.isTerminated())
        runnable.run();
    }
  }
}

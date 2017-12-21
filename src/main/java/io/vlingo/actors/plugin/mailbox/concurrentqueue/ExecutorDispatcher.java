// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.concurrentqueue;

import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import io.vlingo.actors.Dispatcher;
import io.vlingo.actors.Mailbox;

public class ExecutorDispatcher implements Dispatcher {
  private volatile boolean closed;
  private final ThreadPoolExecutor executor;

  public void close() {
    closed = true;
    executor.shutdown();
  }

  public void execute(final Mailbox mailbox) {
    if (!closed) {
      if (mailbox.delivering(true)) {
        executor.execute(mailbox);
      }
    }
  }

  public boolean requiresExecutionNotification() {
    return false;
  }

  protected ExecutorDispatcher(final int availableThreads, final float numberOfDispatchersFactor) {
    int numberOfThreads = (int) ((float) availableThreads * numberOfDispatchersFactor);
    this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numberOfThreads);
    this.executor.setRejectedExecutionHandler(new RejectionHandler());
  }

  private class RejectionHandler implements RejectedExecutionHandler {
    protected RejectionHandler() { }

    public void rejectedExecution(final Runnable runnable, final ThreadPoolExecutor executor) {
      if (!executor.isShutdown() && !executor.isTerminated())
        runnable.run();
    }
  }
}

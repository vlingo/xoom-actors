// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.agronampscarrayqueue;

public class ManyToOneConcurrentArrayQueueDispatcherPool {
  private final ManyToOneConcurrentArrayQueueDispatcher[] dispatchers;

  protected ManyToOneConcurrentArrayQueueDispatcherPool(
          final int availableThreads,
          final float numberOfDispatchersFactor,
          final int ringBufferSize,
          final long fixedBackoff,
          final int throttlingCount,
          final int totalSendRetries) {

    final int numberOfDispatchers = (int) ((float) availableThreads * numberOfDispatchersFactor);
    dispatchers = new ManyToOneConcurrentArrayQueueDispatcher[numberOfDispatchers];

    for (int idx = 0; idx < dispatchers.length; ++idx) {
      final ManyToOneConcurrentArrayQueueDispatcher dispatcher =
              new ManyToOneConcurrentArrayQueueDispatcher(
                      ringBufferSize,
                      fixedBackoff,
                      throttlingCount,
                      totalSendRetries);
      
      dispatcher.start();
      dispatchers[idx] = dispatcher;
    }
  }

  protected ManyToOneConcurrentArrayQueueDispatcher assignFor(final int hashCode) {
    final int index = Math.abs(hashCode) % dispatchers.length;
    return dispatchers[index];
  }
  
  protected void close() {
    for (int idx = 0; idx < dispatchers.length; ++idx) {
      dispatchers[idx].close();
    }
  }
}

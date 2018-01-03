// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.sharedringbuffer;

public class RingBufferDispatcherPool {
  private final RingBufferDispatcher[] dispatchers;

  protected RingBufferDispatcherPool(
      final int availableThreads,
      final float numberOfDispatchersFactor,
      final int ringBufferSize,
      final long fixedBackoff,
      final int throttlingCount) {

    final int numberOfDispatchers = (int) ((float) availableThreads * numberOfDispatchersFactor);
    dispatchers = new RingBufferDispatcher[numberOfDispatchers];

    for (int idx = 0; idx < dispatchers.length; ++idx) {
      final RingBufferDispatcher dispatcher = new RingBufferDispatcher(ringBufferSize, fixedBackoff, throttlingCount);
      dispatcher.start();
      dispatchers[idx] = dispatcher;
    }
  }

  protected RingBufferDispatcher assignFor(final int hashCode) {
    final int index = Math.abs(hashCode) % dispatchers.length;
    return dispatchers[index];
  }
  
  protected void close() {
    for (int idx = 0; idx < dispatchers.length; ++idx) {
      dispatchers[idx].close();
    }
  }
}

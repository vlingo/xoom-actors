// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

public interface Dispatcher {
  /**
   * Close me.
   */
  void close();

  /**
   * Answer whether or not I am closed.
   * @return boolean
   */
  boolean isClosed();

  /**
   * Answer the total capacity for concurrent operations.
   * @return int
   */
  int concurrencyCapacity();

  /**
   * Execute message dispatching for the {@code mailbox}'s next message(s).
   * @param mailbox the Mailbox to execute message dispatching
   */
  void execute(final Mailbox mailbox);

  /**
   * Answer whether or not I require notification of execution.
   * @return boolean
   */
  boolean requiresExecutionNotification();
}

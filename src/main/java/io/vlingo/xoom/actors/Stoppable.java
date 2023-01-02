// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

/**
 * Arranges for stopping the receiver.
 * <p>
 * Note that the contract of stop() is to suspend the receivers mailbox
 * deliveries, which will prevent further processing. To arrange for
 * eventually stopping the receiver send conclude(), which will then
 * cause stop(). In essence the conclude() marks the mailbox for ending
 * operations, but allows messages already queued to first be delivered.
 */
@SafeProxyGenerable
public interface Stoppable {
  /**
   * Concludes the receiver, eventually causing
   * it to receive a stop() message.
   */
  void conclude();

  /**
   * Answer whether or not the receiver is stopped.
   * @return boolean
   */
  boolean isStopped();

  /**
   * Causes the receiver to stop reacting to messages and to eventually
   * be garbage collected.
   * <p>
   * Note that the contract of stop() is to suspend the receivers mailbox
   * deliveries, which will prevent further processing. To arrange for
   * eventually stopping the receiver send conclude(), which will then
   * cause stop(). In essence the conclude() marks the mailbox for ending
   * operations, but allows messages already queued to first be delivered.
   */
  void stop();
}

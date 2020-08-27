// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ReusableQueueMailboxTest {
  private static final String QueueMailbox = "queueMailbox";
  private static final String ReuseQueueMailbox = "reuseQueueMailbox";

  private World world;

  @Test
  public void testThatBothMailboxTypesExist() {
    final String queueMailboxName = world.mailboxNameFrom(QueueMailbox);
    Assert.assertEquals(QueueMailbox, queueMailboxName);
    final Mailbox queueMailbox = world.assignMailbox(queueMailboxName, 1234567);
    Assert.assertNotNull(queueMailbox);

    final String reuseQueueMailboxName = world.mailboxNameFrom(ReuseQueueMailbox);
    Assert.assertEquals(ReuseQueueMailbox, reuseQueueMailboxName);
    final Mailbox reuseQueueMailbox = world.assignMailbox(reuseQueueMailboxName, 123456789);
    Assert.assertNotNull(reuseQueueMailbox);
  }

  @Before
  public void setUp() {
    world = World.start("test-reuse-concurrentqueue");
  }
}

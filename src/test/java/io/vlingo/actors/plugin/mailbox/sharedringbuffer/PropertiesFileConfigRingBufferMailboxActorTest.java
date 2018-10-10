// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.sharedringbuffer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Definition;
import io.vlingo.actors.World;
import io.vlingo.actors.testkit.TestUntil;

public class PropertiesFileConfigRingBufferMailboxActorTest {
  @Test
  public void testThatRingBufferIsUsed() {
    final World world = World.start("ring-mailbox-test");

    final TestResults results = new TestResults();

    final OneBehavior one =
            world.actorFor(
                    Definition.has(OneBehaviorActor.class, Definition.parameters(results), "ringMailbox", "one-behavior"),
                    OneBehavior.class);

    one.doSomething();

    results.until.completes();

    assertEquals(1, results.times);
  }

  public static interface OneBehavior {
    void doSomething();
  }

  public static class OneBehaviorActor extends Actor implements OneBehavior {
    private final TestResults results;

    public OneBehaviorActor(final TestResults results) {
      this.results = results;
    }

    @Override
    public void doSomething() {
      results.times++;
      results.until.happened();
    }
  }

  private static class TestResults {
    public volatile int times = 0;
    public TestUntil until = TestUntil.happenings(1);
  }
}

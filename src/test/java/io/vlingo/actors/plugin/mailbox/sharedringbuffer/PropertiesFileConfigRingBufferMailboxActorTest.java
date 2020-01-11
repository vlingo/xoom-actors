// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.sharedringbuffer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.vlingo.actors.Actor;
import io.vlingo.actors.ActorsTest;
import io.vlingo.actors.Definition;
import io.vlingo.actors.testkit.AccessSafely;

import java.util.concurrent.atomic.AtomicInteger;

public class PropertiesFileConfigRingBufferMailboxActorTest extends ActorsTest {
  @Test
  public void testThatRingBufferIsUsed() {
    final TestResults results = new TestResults(1);

    final OneBehavior one =
            world.actorFor(
                    OneBehavior.class,
                    Definition.has(OneBehaviorActor.class, Definition.parameters(results), "ringMailbox", "one-behavior"));

    one.doSomething();

    assertEquals(1, ((int) results.accessSafely.readFrom("times")));
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
      results.accessSafely.writeUsing("times", 1);
    }
  }

  private static class TestResults {
    private final AccessSafely accessSafely;

    private TestResults(int happenings) {
      final AtomicInteger times = new AtomicInteger(0);
      this.accessSafely = AccessSafely
              .afterCompleting(happenings)
              .writingWith("times", (Integer ignored) -> times.incrementAndGet())
              .readingWith("times", times::get);
    }
  }
}

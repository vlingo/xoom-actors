// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.vlingo.actors.Protocols.Two;
import io.vlingo.actors.testkit.TestUntil;

public class ActorStowDisperseTest extends ActorsTest {

  @Test
  public void testThatStowedMessagesDisperseOnOverride() {
    final Results results = new Results(1, 10);

    final Two<StowThese,OverrideStowage> protocols =
            Protocols.two(
                    world.actorFor(
                            new Class<?>[] { StowThese.class, OverrideStowage.class },
                            Definition.has(StowTestActor.class, Definition.parameters(results), "stow-override")));

    for (int idx = 0; idx < 10; ++idx) {
      protocols._1.stow();
    }
    protocols._2.override();

    results.overrideReceived.completes();
    results.stowReceived.completes();

    assertEquals(1, results.overrideReceivedCount);
    assertEquals(10, results.stowReceivedCount);
  }

  @Test
  public void testThatStowedMessagesDisperseOnCrash() {
    final Results results = new Results(1, 10);

    final Two<StowThese,OverrideStowage> protocols =
            Protocols.two(
                    world.actorFor(
                            new Class<?>[] { StowThese.class, OverrideStowage.class },
                            Definition.has(StowTestActor.class, Definition.parameters(results), "stow-override")));

    for (int idx = 0; idx < 10; ++idx) {
      protocols._1.stow();
    }
    protocols._2.crash();

    results.overrideReceived.completes();
    results.stowReceived.completes();

    assertEquals(1, results.overrideReceivedCount);
    assertEquals(10, results.stowReceivedCount);
  }

  public static class Results {
    public final TestUntil overrideReceived;
    public int overrideReceivedCount;
    public final TestUntil stowReceived;
    public int stowReceivedCount;

    Results(final int overrideReceived, final int stowReceived) {
      this.overrideReceived = TestUntil.happenings(overrideReceived);
      this.stowReceived = TestUntil.happenings(stowReceived);
    }
  }

  public static interface OverrideStowage {
    void crash();
    void override();
  }

  public static interface StowThese {
    void stow();
  }

  public static class StowTestActor extends Actor implements StowThese, OverrideStowage {
    private final Results results;

    public StowTestActor(final Results results) {
      this.results = results;
      stowMessages(OverrideStowage.class);
    }

    @Override
    public void crash() {
      ++results.overrideReceivedCount;
      results.overrideReceived.happened();
      throw new IllegalStateException("Intended failure");
    }

    @Override
    public void override() {
      ++results.overrideReceivedCount;
      disperseStowedMessages();
      results.overrideReceived.happened();
    }

    @Override
    public void stow() {
      ++results.stowReceivedCount;
      results.stowReceived.happened();
    }
  }
}

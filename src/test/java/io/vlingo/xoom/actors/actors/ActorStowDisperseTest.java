// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import io.vlingo.xoom.actors.Protocols.Two;
import io.vlingo.xoom.actors.testkit.AccessSafely;

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

    assertEquals(1, (int) results.overrideAccess.readFrom("overrideReceivedCount"));
    assertEquals(10, (int) results.stowedAccess.readFrom("stowReceivedCount"));
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

    assertEquals(1, (int) results.overrideAccess.readFrom("overrideReceivedCount"));
    assertEquals(10, (int) results.stowedAccess.readFrom("stowReceivedCount"));
  }

  public static class Results {
    public final AccessSafely overrideAccess;
    public final AccessSafely stowedAccess;
    public AtomicInteger overrideReceivedCount;
    public AtomicInteger stowReceivedCount;

    Results(final int overrideReceived, final int stowReceived) {
      this.overrideReceivedCount = new AtomicInteger(0);
      this.stowReceivedCount = new AtomicInteger(0);

      this.stowedAccess = AccessSafely.afterCompleting(stowReceived);
      this.stowedAccess
        .writingWith("stowReceivedCount", (Integer increment) -> stowReceivedCount.incrementAndGet())
        .readingWith("stowReceivedCount", () -> stowReceivedCount.get());

      this.overrideAccess = AccessSafely.afterCompleting(overrideReceived);
      this.overrideAccess
        .writingWith("overrideReceivedCount", (Integer increment) -> overrideReceivedCount.incrementAndGet())
        .readingWith("overrideReceivedCount", () -> overrideReceivedCount.get());
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
    public void start() {
      super.start();
    }

    @Override
    protected void beforeResume(final Throwable reason) {
      disperseStowedMessages();
      super.beforeResume(reason);
    }

    @Override
    protected void afterRestart(final Throwable reason) {
      disperseStowedMessages();
      super.afterRestart(reason);
    }

    @Override
    public void crash() {
      results.overrideAccess.writeUsing("overrideReceivedCount", 1);
      throw new IllegalStateException("Intended failure");
    }

    @Override
    public void override() {
      results.overrideAccess.writeUsing("overrideReceivedCount", 1);
      disperseStowedMessages();
    }

    @Override
    public void stow() {
      results.stowedAccess.writeUsing("stowReceivedCount", 1);
    }
  }
}

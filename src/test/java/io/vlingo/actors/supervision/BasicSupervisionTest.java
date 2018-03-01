// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.supervision;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.Actor;
import io.vlingo.actors.ActorsTest;
import io.vlingo.actors.Definition;
import io.vlingo.actors.Supervised;
import io.vlingo.actors.SupervisionStrategy;
import io.vlingo.actors.Supervisor;
import io.vlingo.actors.testkit.TestActor;

public class BasicSupervisionTest extends ActorsTest {

  @Test
  public void testPublicRootDefaultParentSupervisor() {
    // this test cannot use the TestActor because the timing
    // and message enqueue is important to testing some
    // aspects of supervision. for example, in a restarting
    // situation, as is the case here with the default
    // supervisor, the suspending and subsequent stowed
    // will never be delivered unless time is given to
    // empty to stowed messages
    
    final FailureControl failure =
            world.actorFor(
                    Definition.has(FailureControlActor.class, Definition.NoParameters, world.defaultParent(), "failure-for-default"),
                    FailureControl.class);
    
    FailureControlActor.untilFailNow = until(1);
    assertEquals(0, FailureControlActor.failNowCount);
    failure.failNow();
    FailureControlActor.untilFailNow.completes();
    assertEquals(1, FailureControlActor.failNowCount);
    
    // actor may or may not be resumed by now
    
    FailureControlActor.untilAfterFail = until(1);
    assertEquals(0, FailureControlActor.afterFailureCount);
    failure.afterFailure();
    FailureControlActor.untilAfterFail.completes();
    assertEquals(1, FailureControlActor.afterFailureCount);
  }
  
  @Test
  public void testStoppingSupervisor() {
    final TestActor<Supervisor> supervisor =
            testWorld.actorFor(
                    Definition.has(StoppingSupervisorActor.class, Definition.NoParameters, "stopping-supervisor"),
                    Supervisor.class);
    
    final TestActor<FailureControl> failure =
            testWorld.actorFor(
                    Definition.has(FailureControlActor.class, Definition.NoParameters, supervisor.actorInside(), "failure-for-stop"),
                    FailureControl.class);
    
    assertEquals(0, FailureControlActor.failNowCount);
    failure.actor().failNow();
    assertEquals(1, FailureControlActor.failNowCount);
    
    assertEquals(0, FailureControlActor.afterFailureCount);
    failure.actor().afterFailure();
    assertEquals(0, FailureControlActor.afterFailureCount);
    
    assertEquals(1, FailureControlActor.stoppedCount);
  }
  
  @Test
  public void testRestartSupervisor() {
    final TestActor<Supervisor> supervisor =
            testWorld.actorFor(
                    Definition.has(RestartSupervisorActor.class, Definition.NoParameters, "restart-supervisor"),
                    Supervisor.class);
    
    final TestActor<FailureControl> failure =
            testWorld.actorFor(
                    Definition.has(FailureControlActor.class, Definition.NoParameters, supervisor.actorInside(), "failure-for-restart"),
                    FailureControl.class);
    
    assertEquals(0, FailureControlActor.failNowCount);
    assertEquals(0, RestartSupervisorActor.informedCount);
    assertEquals(0, FailureControlActor.afterRestartCount);
    assertEquals(0, FailureControlActor.afterStopCount);
    assertEquals(0, FailureControlActor.beforeRestartCount);
    assertEquals(1, FailureControlActor.beforeStartCount);
    failure.actor().failNow();
    assertEquals(1, FailureControlActor.failNowCount);
    assertEquals(1, RestartSupervisorActor.informedCount);
    assertEquals(1, FailureControlActor.afterRestartCount);
    assertEquals(1, FailureControlActor.afterStopCount);
    assertEquals(1, FailureControlActor.beforeRestartCount);
    assertEquals(2, FailureControlActor.beforeStartCount);

    assertEquals(0, FailureControlActor.afterFailureCount);
    failure.actor().afterFailure();
    assertEquals(1, FailureControlActor.afterFailureCount);
    
    assertEquals(0, FailureControlActor.stoppedCount);
    
  }
  
  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    
    FailureControlActor.afterFailureCount = 0;
    FailureControlActor.afterRestartCount = 0;
    FailureControlActor.afterStopCount = 0;
    FailureControlActor.beforeRestartCount = 0;
    FailureControlActor.beforeStartCount = 0;
    FailureControlActor.failNowCount = 0;
    FailureControlActor.stoppedCount = 0;
    
    RestartSupervisorActor.informedCount = 0;
    
    StoppingSupervisorActor.informedCount = 0;
  }
  
  public static class StoppingSupervisorActor extends Actor implements Supervisor {
    public static int informedCount;
    
    @Override
    public void inform(final Throwable throwable, final Supervised supervised) {
      logger().log("StoppingSupervisorActor informed of failure in: " + supervised.address().name() + " because: " + throwable.getMessage(), throwable);
      supervised.stop(supervisionStrategy().scope());
    }

    @Override
    public SupervisionStrategy supervisionStrategy() {
      return
        new SupervisionStrategy() {
          @Override
          public int intensity() {
            return SupervisionStrategy.DefaultIntensity;
          }

          @Override
          public long period() {
            return SupervisionStrategy.DefaultPeriod;
          }

          @Override
          public Scope scope() {
            return Scope.One;
          }
        };
    }
  }
  
  public static class RestartSupervisorActor extends Actor implements Supervisor {
    public static int informedCount;
    
    private final SupervisionStrategy strategy =
            new SupervisionStrategy() {
              @Override
              public int intensity() {
                return 2;
              }
        
              @Override
              public long period() {
                return SupervisionStrategy.DefaultPeriod;
              }
        
              @Override
              public Scope scope() {
                return Scope.One;
              }
            };
    
    @Override
    public void inform(final Throwable throwable, final Supervised supervised) {
      logger().log("RestartSupervisorActor informed of failure in: " + supervised.address().name() + " because: " + throwable.getMessage(), throwable);
      supervised.restartWithin(strategy.period(), strategy.intensity(), strategy.scope());
      ++informedCount;
    }

    @Override
    public SupervisionStrategy supervisionStrategy() {
      return strategy;
    }
  }
}

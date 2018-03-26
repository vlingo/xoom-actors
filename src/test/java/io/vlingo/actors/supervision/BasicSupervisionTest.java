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
    
    FailureControlActor.instance.untilFailNow = until(1);
    assertEquals(0, FailureControlActor.instance.failNowCount.get());
    failure.failNow();
    FailureControlActor.instance.untilFailNow.completes();
    assertEquals(1, FailureControlActor.instance.failNowCount.get());
    
    // actor may or may not be resumed by now
    
    FailureControlActor.instance.untilAfterFail = until(1);
    assertEquals(0, FailureControlActor.instance.afterFailureCount.get());
    failure.afterFailure();
    FailureControlActor.instance.untilAfterFail.completes();
    assertEquals(1, FailureControlActor.instance.afterFailureCount.get());
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
    
    assertEquals(0, FailureControlActor.instance.failNowCount.get());
    failure.actor().failNow();
    assertEquals(1, FailureControlActor.instance.failNowCount.get());
    
    assertEquals(0, FailureControlActor.instance.afterFailureCount.get());
    failure.actor().afterFailure();
    assertEquals(0, FailureControlActor.instance.afterFailureCount.get());
    
    assertEquals(1, FailureControlActor.instance.stoppedCount.get());
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
    
    assertEquals(0, FailureControlActor.instance.failNowCount.get());
    assertEquals(0, RestartSupervisorActor.instance.informedCount);
    assertEquals(0, FailureControlActor.instance.afterRestartCount.get());
    assertEquals(0, FailureControlActor.instance.afterStopCount.get());
    assertEquals(0, FailureControlActor.instance.beforeRestartCount.get());
    assertEquals(1, FailureControlActor.instance.beforeStartCount.get());
    failure.actor().failNow();
    assertEquals(1, FailureControlActor.instance.failNowCount.get());
    assertEquals(1, RestartSupervisorActor.instance.informedCount);
    assertEquals(1, FailureControlActor.instance.afterRestartCount.get());
    assertEquals(1, FailureControlActor.instance.afterStopCount.get());
    assertEquals(1, FailureControlActor.instance.beforeRestartCount.get());
    assertEquals(2, FailureControlActor.instance.beforeStartCount.get());

    assertEquals(0, FailureControlActor.instance.afterFailureCount.get());
    failure.actor().afterFailure();
    assertEquals(1, FailureControlActor.instance.afterFailureCount.get());
    
    assertEquals(0, FailureControlActor.instance.stoppedCount.get());
    
  }
  
  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
  }
  
  public static class StoppingSupervisorActor extends Actor implements Supervisor {
    public static StoppingSupervisorActor instance;
    
    public int informedCount;
    
    public StoppingSupervisorActor() {
      instance = this;
    }

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
    public static RestartSupervisorActor instance;
    
    public int informedCount;
    
    public RestartSupervisorActor() {
      instance = this;
    }
    
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

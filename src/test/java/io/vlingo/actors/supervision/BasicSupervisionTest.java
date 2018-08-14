// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.supervision;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.Actor;
import io.vlingo.actors.ActorsTest;
import io.vlingo.actors.Definition;
import io.vlingo.actors.Supervised;
import io.vlingo.actors.SupervisionStrategy;
import io.vlingo.actors.Supervisor;
import io.vlingo.actors.supervision.FailureControlActor.FailureControlTestResults;
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
    
    final FailureControlTestResults failureControlTestResults = new FailureControlTestResults();
    
    final FailureControl failure =
            world.actorFor(
                    Definition.has(FailureControlActor.class, Definition.parameters(failureControlTestResults), world.defaultParent(), "failure-for-default"),
                    FailureControl.class);

    failureControlTestResults.untilFailNow = until(1);
    assertEquals(0, failureControlTestResults.failNowCount.get());
    failure.failNow();
    failureControlTestResults.untilFailNow.completes();
    assertEquals(1, failureControlTestResults.failNowCount.get());

    // actor may or may not be resumed by now
    
    failureControlTestResults.untilAfterFail = until(1);
    assertEquals(0, failureControlTestResults.afterFailureCount.get());
    failure.afterFailure();
    failureControlTestResults.untilAfterFail.completes();
    assertEquals(1, failureControlTestResults.afterFailureCount.get());
  }
  
  @Test
  public void testStoppingSupervisor() {
    final TestActor<Supervisor> supervisor =
            testWorld.actorFor(
                    Definition.has(StoppingSupervisorActor.class, Definition.NoParameters, "stopping-supervisor"),
                    Supervisor.class);
    
    final FailureControlTestResults failureControlTestResults = new FailureControlTestResults();
    
    final TestActor<FailureControl> failure =
            testWorld.actorFor(
                    Definition.has(FailureControlActor.class, Definition.parameters(failureControlTestResults), supervisor.actorInside(), "failure-for-stop"),
                    FailureControl.class);
    
    assertEquals(0, failureControlTestResults.failNowCount.get());
    failure.actor().failNow();
    assertEquals(1, failureControlTestResults.failNowCount.get());
    
    assertEquals(0, failureControlTestResults.afterFailureCount.get());
    failure.actor().afterFailure();
    assertEquals(0, failureControlTestResults.afterFailureCount.get());
    
    assertEquals(1, failureControlTestResults.stoppedCount.get());
  }
  
  @Test
  public void testRestartSupervisor() {
    final RestartSupervisorTestResults restartSupervisorTestResults = new RestartSupervisorTestResults();
    
    final TestActor<Supervisor> supervisor =
            testWorld.actorFor(
                    Definition.has(RestartSupervisorActor.class, Definition.parameters(restartSupervisorTestResults), "restart-supervisor"),
                    Supervisor.class);
    
    final FailureControlTestResults failureControlTestResults = new FailureControlTestResults();
    
    final TestActor<FailureControl> failure =
            testWorld.actorFor(
                    Definition.has(FailureControlActor.class, Definition.parameters(failureControlTestResults), supervisor.actorInside(), "failure-for-restart"),
                    FailureControl.class);
    
    assertEquals(0, failureControlTestResults.failNowCount.get());
    assertEquals(0, restartSupervisorTestResults.informedCount.get());
    assertEquals(0, failureControlTestResults.afterRestartCount.get());
    assertEquals(0, failureControlTestResults.afterStopCount.get());
    assertEquals(0, failureControlTestResults.beforeRestartCount.get());
    assertEquals(1, failureControlTestResults.beforeStartCount.get());
    failure.actor().failNow();
    assertEquals(1, failureControlTestResults.failNowCount.get());
    assertEquals(1, restartSupervisorTestResults.informedCount.get());
    assertEquals(1, failureControlTestResults.afterRestartCount.get());
    assertEquals(1, failureControlTestResults.afterStopCount.get());
    assertEquals(1, failureControlTestResults.beforeRestartCount.get());
    assertEquals(2, failureControlTestResults.beforeStartCount.get());

    assertEquals(0, failureControlTestResults.afterFailureCount.get());
    failure.actor().afterFailure();
    assertEquals(1, failureControlTestResults.afterFailureCount.get());
    
    assertEquals(0, failureControlTestResults.stoppedCount.get());
  }
  
  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
  }
  
  public static class StoppingSupervisorActor extends Actor implements Supervisor {
    public StoppingSupervisorActor() { }

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
    private final RestartSupervisorTestResults testResults;
    
    public RestartSupervisorActor(final RestartSupervisorTestResults testResults) {
      this.testResults = testResults;
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
      testResults.informedCount.incrementAndGet();
    }

    @Override
    public SupervisionStrategy supervisionStrategy() {
      return strategy;
    }
  }
  
  private static class RestartSupervisorTestResults {
    public AtomicInteger informedCount = new AtomicInteger(0);
  }
}

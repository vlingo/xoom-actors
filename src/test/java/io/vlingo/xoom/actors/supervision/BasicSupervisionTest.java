// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.supervision;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.ActorsTest;
import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.Supervised;
import io.vlingo.xoom.actors.SupervisionStrategy;
import io.vlingo.xoom.actors.Supervisor;
import io.vlingo.xoom.actors.supervision.FailureControlActor.FailureControlTestResults;
import io.vlingo.xoom.actors.testkit.AccessSafely;
import io.vlingo.xoom.actors.testkit.TestActor;

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
                    FailureControl.class,
                    Definition.has(FailureControlActor.class, Definition.parameters(failureControlTestResults), world.defaultParent(), "failure-for-default"));

    AccessSafely access = failureControlTestResults.afterCompleting(3);

    failure.failNow();
    assertEquals(1, (int) access.readFrom("failNowCount"));

    // actor may or may not be resumed by now
    assertEquals(1, (int) access.readFromExpecting("afterRestartCount", 1, 1_000));

    access = failureControlTestResults.afterCompleting(1);

    failure.afterFailure();
    assertEquals(1, (int) access.readFrom("afterFailureCount"));
  }

  @Test
  public void testStoppingSupervisor() {
    final TestActor<Supervisor> supervisor =
            testWorld.actorFor(
                    Supervisor.class,
                    Definition.has(StoppingSupervisorActor.class, Definition.NoParameters, "stopping-supervisor"));

    final FailureControlTestResults failureControlTestResults = new FailureControlTestResults();

    final TestActor<FailureControl> failure =
            testWorld.actorFor(
                    FailureControl.class,
                    Definition.has(FailureControlActor.class, Definition.parameters(failureControlTestResults), supervisor.actorInside(), "failure-for-stop"));

    AccessSafely access = failureControlTestResults.afterCompleting(2);

    failure.actor().failNow();
    assertEquals(1, (int) access.readFrom("failNowCount"));

    failure.actor().afterFailure();
    assertEquals(1, (int) access.readFrom("stoppedCount"));
    assertEquals(0, (int) access.readFrom("afterFailureCount"));
  }

  @Test
  public void testRestartSupervisor() {
    final RestartSupervisorTestResults restartSupervisorTestResults = new RestartSupervisorTestResults();

    final TestActor<Supervisor> supervisor =
            testWorld.actorFor(
                    Supervisor.class,
                    Definition.has(RestartSupervisorActor.class, Definition.parameters(restartSupervisorTestResults), "restart-supervisor"));

    final FailureControlTestResults failureControlTestResults = new FailureControlTestResults();

    final TestActor<FailureControl> failure =
            testWorld.actorFor(
                    FailureControl.class,
                    Definition.has(FailureControlActor.class, Definition.parameters(failureControlTestResults), supervisor.actorInside(), "failure-for-restart"));

    AccessSafely failureAccess = failureControlTestResults.afterCompleting(6);
    AccessSafely restartAccess = restartSupervisorTestResults.afterCompleting(1);

    failure.actor().failNow();
    assertEquals(1, (int) restartAccess.readFrom("informedCount"));
    assertEquals(2, (int) failureAccess.readFrom("beforeStartCount"));
    assertEquals(1, (int) failureAccess.readFrom("failNowCount"));
    assertEquals(1, (int) failureAccess.readFrom("afterRestartCount"));
    assertEquals(1, (int) failureAccess.readFrom("afterStopCount"));
    assertEquals(1, (int) failureAccess.readFrom("beforeRestartCount"));

    AccessSafely afterFailureAccess = failureControlTestResults.afterCompleting(1);

    failure.actor().afterFailure();
    assertEquals(1, (int) afterFailureAccess.readFrom("afterFailureCount"));

    assertEquals(0, (int) afterFailureAccess.readFrom("stoppedCount"));
  }

  public static class StoppingSupervisorActor extends Actor implements Supervisor {
    public StoppingSupervisorActor() { }

    @Override
    public void inform(final Throwable throwable, final Supervised supervised) {
      //logger().log("StoppingSupervisorActor informed of failure in: " + supervised.address().name() + " because: " + throwable.getMessage(), throwable);
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
      //logger().log("RestartSupervisorActor informed of failure in: " + supervised.address().name() + " because: " + throwable.getMessage(), throwable);
      supervised.restartWithin(strategy.period(), strategy.intensity(), strategy.scope());
      System.out.println("SUPERVISOR RESTART");
      testResults.access.writeUsing("informedCount", 1);
    }

    @Override
    public SupervisionStrategy supervisionStrategy() {
      return strategy;
    }
  }

  private static class RestartSupervisorTestResults {
    public AccessSafely access = afterCompleting(0);

    public AtomicInteger informedCount = new AtomicInteger(0);

    public AccessSafely afterCompleting(final int times) {
      access =
        AccessSafely.afterCompleting(times)
        .writingWith("informedCount", (Integer increment) -> informedCount.incrementAndGet())
        .readingWith("informedCount", () -> informedCount.get());

      return access;
    }
  }
}

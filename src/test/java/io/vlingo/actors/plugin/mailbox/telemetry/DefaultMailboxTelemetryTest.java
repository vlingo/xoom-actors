package io.vlingo.actors.plugin.mailbox.telemetry;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.vlingo.actors.*;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class DefaultMailboxTelemetryTest extends ActorsTest {
  private MeterRegistry registry;
  private Message message;
  private String addressOfActor;
  private DefaultMailboxTelemetry telemetry;

  @Before
  public void setUp() throws Exception {
    super.setUp();

    addressOfActor = UUID.randomUUID().toString();
    final Actor receiver = testWorld.actorFor(
        Definition.has(RandomActor.class, Definition.NoParameters, addressOfActor),
        NoProtocol.class
    ).actorInside();

    message = mock(Message.class);
    doReturn(receiver).when(message).actor();

    registry = new SimpleMeterRegistry(SimpleConfig.DEFAULT, Clock.SYSTEM);
    telemetry = new DefaultMailboxTelemetry(registry);
  }

  @Test
  public void testThatSendAndPullRegistersACounterOnTheActorsMailbox() {
    telemetry.onSendMessage(message);
    assertLagsAre(1, 1);

    telemetry.onPulledMessage(message);
    assertLagsAre(0, 0);
  }

  @Test
  public void testThatPullingAnEmptyMailboxCountsAsIdle() {
    telemetry.onPullEmptyMailbox();
    assertIdlesAre(1);
  }

  @Test
  public void testThatFailedSentsAreCounted() {
    telemetry.onSendMessageFailed(message, expectedException());
    assertFailuresAre(1, 1, DefaultMailboxTelemetry.FAILED_SEND);
    assertIllegalStateExceptionCount(1);
  }

  @Test
  public void testThatPullingFailuresAreCounted() {
    telemetry.onPullMessageFailed(expectedException());
    assertIllegalStateExceptionCount(1);
  }

  @Test
  public void testThatDeliveringFailuresAreCountedFromMessage() {
    telemetry.onDeliverMessageFailed(message, expectedException());

    assertFailuresAre(1, 1, DefaultMailboxTelemetry.FAILED_DELIVER);
    assertIllegalStateExceptionCount(1);
  }

  private void assertLagsAre(final int expectedActor, final int expectedClass) {
    AtomicInteger lag = telemetry.gaugeFor(message, DefaultMailboxTelemetry.SCOPE_INSTANCE, DefaultMailboxTelemetry.PENDING);
    assertEquals(expectedActor, lag.get());

    AtomicInteger globalLagOfActorClass = telemetry.gaugeFor(message, DefaultMailboxTelemetry.SCOPE_CLASS, DefaultMailboxTelemetry.PENDING);
    assertEquals(expectedClass, globalLagOfActorClass.get());
  }

  private void assertFailuresAre(final int expectedActor, final int expectedClass, final String typeOfOp) {
    double failures = telemetry.counterFor(message, DefaultMailboxTelemetry.SCOPE_INSTANCE, typeOfOp + ".IllegalStateException").count();
    assertEquals(expectedActor, failures, 0.0);

    double globalFailuresOfActorClass = telemetry.counterFor(message, DefaultMailboxTelemetry.SCOPE_CLASS, typeOfOp + ".IllegalStateException").count();
    assertEquals(expectedClass, globalFailuresOfActorClass, 0.0);
  }

  private void assertIllegalStateExceptionCount(final int expected) {
    double count = telemetry.counterForException(IllegalStateException.class).count();
    assertEquals(expected, count, 0.0);
  }

  private void assertIdlesAre(final int expectedIdles) {
    double idle = telemetry.idleCounter().count();
    assertEquals(expectedIdles, idle, 0.0);
  }

  private IllegalStateException expectedException() {
    return new IllegalStateException("Expected exception");
  }

  public static class RandomActor extends Actor {
  }
}

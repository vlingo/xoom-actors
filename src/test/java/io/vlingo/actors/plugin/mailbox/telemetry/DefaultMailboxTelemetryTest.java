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


  private void assertLagsAre(final int expectedActor, final int expectedClass) {
    AtomicInteger lag = telemetry.gaugeFor(message, DefaultMailboxTelemetry.SCOPE_INSTANCE, DefaultMailboxTelemetry.LAG);
    assertEquals(expectedActor, lag.get());

    AtomicInteger globalLagOfActorClass = telemetry.gaugeFor(message, DefaultMailboxTelemetry.SCOPE_CLASS, DefaultMailboxTelemetry.LAG);
    assertEquals(expectedClass, globalLagOfActorClass.get());
  }

  private void assertIdlesAre(final int expectedIdles) {
    double idle = telemetry.idleCounter().count();
    assertEquals(expectedIdles, idle, 0.0);
  }

  public static class RandomActor extends Actor {
  }
}

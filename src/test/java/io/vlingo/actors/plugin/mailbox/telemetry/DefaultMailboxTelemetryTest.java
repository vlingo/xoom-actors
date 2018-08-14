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
    assertGaugesForMessageAre(1, 1);

    telemetry.onPulledMessage(message);
    assertGaugesForMessageAre(0, 0);
  }

  private void assertGaugesForMessageAre(final int expectedPerActor, final int expectedGlobal) {
    AtomicInteger lag = telemetry.gauges().get("RandomActor."+addressOfActor);
    assertEquals(expectedPerActor, lag.get());

    AtomicInteger globalLagOfActorClass = telemetry.gauges().get("RandomActor::Class");
    assertEquals(expectedGlobal, globalLagOfActorClass.get());
  }

  public static class RandomActor extends Actor {
  }
}

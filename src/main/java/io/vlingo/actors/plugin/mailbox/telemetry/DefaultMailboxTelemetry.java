package io.vlingo.actors.plugin.mailbox.telemetry;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.vlingo.actors.Actor;
import io.vlingo.actors.Message;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultMailboxTelemetry implements MailboxTelemetry {
  static final String SCOPE_INSTANCE = "Instance";
  static final String SCOPE_CLASS = "Class";

  static final String LAG = "lag";
  static final String IDLE = "idle";

  private final MeterRegistry registry;

  private final Map<String, AtomicInteger> gauges;
  private final Counter idleCounter;

  public DefaultMailboxTelemetry(final MeterRegistry registry) {
    this.registry = registry;
    this.gauges = new HashMap<>();
    this.idleCounter = registry.counter(IDLE);
  }

  @Override
  public void onSendMessage(final Message message) {
    gaugeFor(message, SCOPE_INSTANCE, LAG, forMessage(message)).incrementAndGet();
    gaugeFor(message, SCOPE_CLASS, LAG).incrementAndGet();
  }

  @Override
  public void onSendMessageFailed(final Message message, final Throwable exception) {

  }

  @Override
  public void onPullEmptyMailbox() {
    idleCounter.increment();
  }

  @Override
  public void onPulledMessage(final Message message) {
    gaugeFor(message, SCOPE_INSTANCE, LAG, forMessage(message)).decrementAndGet();
    gaugeFor(message, SCOPE_CLASS, LAG).decrementAndGet();
  }

  @Override
  public void onPullMessageFailed(final Throwable exception) {

  }

  final AtomicInteger gaugeFor(final Message message, final String scope, final String concept, Tag... tags) {
    Actor actor = message.actor();
    String actorClassName = actor.getClass().getSimpleName();
    String metricId = (actor.address().name() != null ? actor.address().name() : "" + actor.address().id());

    String key = actorClassName + "::" + scope + "." + metricId + "." + concept;
    if (scope.equals(SCOPE_CLASS)) {
      key = actorClassName + "::" + SCOPE_CLASS + "." + concept;
    }

    AtomicInteger gauge = gauges.getOrDefault(
        key,
        registry.gauge(
            actorClassName + "." + concept,
            Arrays.asList(tags),
            new AtomicInteger(0)));

    gauges.put(key, gauge);
    return gauge;
  }

  final Counter idleCounter() {
    return idleCounter;
  }

  private Tag forMessage(final Message message) {
    return new ImmutableTag("Address", message.actor().address().name());
  }
}

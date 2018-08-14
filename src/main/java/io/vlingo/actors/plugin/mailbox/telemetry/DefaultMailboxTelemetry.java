package io.vlingo.actors.plugin.mailbox.telemetry;

import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.MeterRegistry;
import io.vlingo.actors.Actor;
import io.vlingo.actors.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.singleton;

public class DefaultMailboxTelemetry implements MailboxTelemetry {
  private static final String LAG = "lag";

  private final MeterRegistry registry;
  private final Map<String, AtomicInteger> gauges;

  public DefaultMailboxTelemetry(final MeterRegistry registry) {
    this.registry = registry;
    this.gauges = new HashMap<>();
  }

  @Override
  public void onSendMessage(final Message message) {
    actorGaugeFor(message, LAG).incrementAndGet();
    actorClassGaugeFor(message, LAG).incrementAndGet();
  }

  @Override
  public void onSendMessageFailed(final Message message, final Throwable exception) {

  }

  @Override
  public void onPullEmptyMailbox() {

  }

  @Override
  public void onPulledMessage(final Message message) {
    actorGaugeFor(message, LAG).decrementAndGet();
    actorClassGaugeFor(message, LAG).decrementAndGet();
  }

  @Override
  public void onPullMessageFailed(final Throwable exception) {

  }

  public Map<String, AtomicInteger> gauges() {
    return gauges;
  }

  private AtomicInteger actorGaugeFor(final Message message, final String concept) {
    Actor actor = message.actor();
    String actorClassName = actor.getClass().getSimpleName();
    String metricId = (actor.address().name() != null ? actor.address().name() : "" + actor.address().id());

    String key = actorClassName + "." + metricId + "." + concept;

    AtomicInteger gauge = gauges.getOrDefault(
        key,
        registry.gauge(
            actorClassName + "." + concept,
            singleton(new ImmutableTag("Address", metricId)),
            new AtomicInteger(0)));

    gauges.put(key, gauge);
    return gauge;
  }

  private AtomicInteger actorClassGaugeFor(final Message message, final String concept) {
    Actor actor = message.actor();
    String actorClassName = actor.getClass().getSimpleName();
    String key = actorClassName + "::Class." + concept;

    AtomicInteger gauge = gauges.getOrDefault(
        key,
        registry.gauge(actorClassName + "." + concept, new AtomicInteger(0))
    );

    gauges.put(key, gauge);
    return gauge;
  }

}

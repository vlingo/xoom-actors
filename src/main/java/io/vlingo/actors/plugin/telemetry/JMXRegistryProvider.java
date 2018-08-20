package io.vlingo.actors.plugin.telemetry;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.jmx.JmxConfig;
import io.micrometer.jmx.JmxMeterRegistry;
import io.vlingo.actors.World;

public class JMXRegistryProvider implements RegistryProvider {
  @Override
  public MeterRegistry provide(final World world) {
    return new JmxMeterRegistry(JmxConfig.DEFAULT, Clock.SYSTEM);
  }
}

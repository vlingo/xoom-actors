package io.vlingo.actors.plugin.telemetry;

import io.micrometer.core.instrument.MeterRegistry;
import io.vlingo.actors.World;

import java.lang.reflect.InvocationTargetException;

@FunctionalInterface
public interface RegistryProvider {
  MeterRegistry provide(final World world);

  class InvalidRegistryProviderException extends Exception {
    InvalidRegistryProviderException(String className, Throwable ex) {
      super(className + " is not a valid RegistryProvider.", ex);
    }
  }

  static RegistryProvider fromClass(final String className) throws InvalidRegistryProviderException {

    try {
      Class<? extends RegistryProvider> providerClass = (Class<? extends RegistryProvider>) Class.forName(className);
      return providerClass.getDeclaredConstructor().newInstance();
    } catch (final Exception ex) {
      throw new InvalidRegistryProviderException(className, ex);
    }
  }
}

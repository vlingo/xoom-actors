package io.vlingo.actors.plugin.logging;

public class LoggingEvent {
  private Class<?> clazz;
  private long timestamp;
  private String threadName;
  private Object[] args;
  private String message;
}

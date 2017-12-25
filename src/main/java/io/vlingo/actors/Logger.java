package io.vlingo.actors;

public interface Logger {
    boolean isEnabled();
    void log(String message);
}

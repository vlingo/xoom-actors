package io.vlingo.xoom.actors.plugin;

@FunctionalInterface
public interface PluginFactory {
    Plugin build();
}

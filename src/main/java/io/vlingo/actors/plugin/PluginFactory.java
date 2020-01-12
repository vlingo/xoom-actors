package io.vlingo.actors.plugin;

@FunctionalInterface
public interface PluginFactory {
    Plugin build();
}

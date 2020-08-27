package io.vlingo.actors.plugin.eviction;

import java.util.Properties;

import io.vlingo.actors.Configuration;
import io.vlingo.actors.DirectoryEvictionConfiguration;
import io.vlingo.actors.Registrar;
import io.vlingo.actors.plugin.AbstractPlugin;
import io.vlingo.actors.plugin.Plugin;
import io.vlingo.actors.plugin.PluginConfiguration;

public class DirectoryEvictionPlugin extends AbstractPlugin {

  private final DirectoryEvictionConfiguration configuration;

  public DirectoryEvictionPlugin() {
    this(DirectoryEvictionConfiguration.define());
  }

  private DirectoryEvictionPlugin(DirectoryEvictionConfiguration configuration) {
    this.configuration = configuration;
  }


  @Override
  public void close() { }

  @Override
  public PluginConfiguration configuration() {
    return configuration;
  }

  @Override
  public String name() {
    return configuration.name();
  }

  @Override
  public int pass() {
    return 2;
  }

  @Override
  public void start(Registrar registrar) { }

  @Override
  public Plugin with(PluginConfiguration overrideConfiguration) {
    if (overrideConfiguration == null) {
      return this;
    }
    return new DirectoryEvictionPlugin(
        (DirectoryEvictionConfiguration)overrideConfiguration);
  }

  @Override
  public void __internal_Only_Init(final String name, final Configuration configuration, final Properties properties) {
    // no-op
  }
}

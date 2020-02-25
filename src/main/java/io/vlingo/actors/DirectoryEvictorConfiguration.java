package io.vlingo.actors;

import io.vlingo.actors.plugin.PluginConfiguration;
import io.vlingo.actors.plugin.PluginProperties;

public class DirectoryEvictorConfiguration implements PluginConfiguration {

  public static final long DEFAULT_LRU_MILLIS = 10 * 60 * 1_000L;
  public static final float DEFAULT_FILL_RATIO_HIGH = 0.8F;


  public static DirectoryEvictorConfiguration define() {
    return new DirectoryEvictorConfiguration();
  }


  private String name = "directoryEvictor";
  private long lruThresholdMillis;
  private float fillRatioHigh;


  public DirectoryEvictorConfiguration() {
    this(DEFAULT_LRU_MILLIS, DEFAULT_FILL_RATIO_HIGH);
  }

  public DirectoryEvictorConfiguration(long lruThresholdMillis, float fillRatioHigh) {
    this.lruThresholdMillis = lruThresholdMillis;
    this.fillRatioHigh = fillRatioHigh;
  }


  public DirectoryEvictorConfiguration lruThresholdMillis(final long millis) {
    this.lruThresholdMillis = millis;
    return this;
  }

  public long lruThresholdMillis() {
    return lruThresholdMillis;
  }

  public DirectoryEvictorConfiguration fillRatioHigh(final float ratio) {
    this.fillRatioHigh = ratio;
    return this;
  }

  public float fillRatioHigh() {
    return fillRatioHigh;
  }

  @Override
  public void build(Configuration configuration) {
    configuration.with(lruThresholdMillis(DEFAULT_LRU_MILLIS)
        .fillRatioHigh(DEFAULT_FILL_RATIO_HIGH));
  }

  @Override
  public void buildWith(Configuration configuration, PluginProperties properties) {
    this.name = properties.name;
    this.lruThresholdMillis = properties.getLong("lruThresholdMillis", DEFAULT_LRU_MILLIS);
    this.fillRatioHigh = properties.getFloat("fillRatioHigh", DEFAULT_FILL_RATIO_HIGH);
    configuration.with(this);
  }

  @Override
  public String name() {
    return this.name;
  }


  @Override
  public String toString() {
    return String.format(
        "EvictorActor#Config(lruThresholdMillis='%s', fillRatioHigh='%.2f')",
        lruThresholdMillis, fillRatioHigh);
  }
}

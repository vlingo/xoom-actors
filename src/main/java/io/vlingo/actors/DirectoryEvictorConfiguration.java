package io.vlingo.actors;

public class DirectoryEvictorConfiguration {

  public static final long DEFAULT_LRU_MILLIS = 10 * 60 * 1_000L;
  public static final float DEFAULT_FILL_RATIO_HIGH = 0.8F;

  public final long lruThresholdMillis;
  public final float fillRatioHigh;

  public DirectoryEvictorConfiguration() {
    this(DEFAULT_LRU_MILLIS, DEFAULT_FILL_RATIO_HIGH);
  }

  public DirectoryEvictorConfiguration(long lruThresholdMillis, float fillRatioHigh) {
    this.lruThresholdMillis = lruThresholdMillis;
    this.fillRatioHigh = fillRatioHigh;
  }

  @Override
  public String toString() {
    return String.format(
        "EvictorActor#Config(lruThresholdMillis='%s', fillRatioHigh='%.2f')",
        lruThresholdMillis, fillRatioHigh);
  }
}

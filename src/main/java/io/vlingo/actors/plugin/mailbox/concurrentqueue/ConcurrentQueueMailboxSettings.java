package io.vlingo.actors.plugin.mailbox.concurrentqueue;

class ConcurrentQueueMailboxSettings {

  private static ConcurrentQueueMailboxSettings settings;
  
  protected final int throttlingCount;
  
  protected static ConcurrentQueueMailboxSettings instance() {
    return settings;
  }
  
  protected static void with(final int throttlingCount) {
    settings = new ConcurrentQueueMailboxSettings(throttlingCount);
  }
  
  private ConcurrentQueueMailboxSettings(final int throttlingCount) {
    this.throttlingCount = throttlingCount;
  }
}

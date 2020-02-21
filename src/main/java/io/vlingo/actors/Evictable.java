package io.vlingo.actors;

final class Evictable {

  final Actor actor;

  private long activeOn;


  Evictable(Actor actor) {
    this.actor = actor;
    this.activeOn = System.currentTimeMillis();
  }

  void receivedMessage() {
    activeOn(System.currentTimeMillis());
  }

  void activeOn(long activeOn) {
    this.activeOn = activeOn;
  }

  boolean stop(long thresholdMillis) {
    return stop(System.currentTimeMillis(), thresholdMillis);
  }

  boolean stop(long referenceMillis, long thresholdMillis) {
    final int pendingMessageCount = actor.lifeCycle.environment.mailbox.pendingMessages();
    if (isStale(referenceMillis, thresholdMillis)) {
      if (pendingMessageCount == 0) {
        actor.selfAs(Stoppable.class).stop();
        return true;
      }
      else {
        actor.logger().warn(
            "Inactive Actor at {} failed to evict because it has {} undelivered messages in its mailbox",
            actor.address(), pendingMessageCount);
      }
    }
    return false;
  }

  boolean isStale(long thresholdMillis) {
    return isStale(System.currentTimeMillis(), thresholdMillis);
  }

  boolean isStale(long referenceMillis, long thresholdMillis) {
    return activeOn < referenceMillis - thresholdMillis;
  }
}

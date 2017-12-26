package io.vlingo.actors;

public interface ProxyFactory {
  <T> T createFor(final Class<T> protocol, final Actor actor, final Mailbox mailbox);
  Object createFor(final Class<?>[] protocol, final Actor actor, final Mailbox mailbox);
}

package io.vlingo.actors;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class JdkProxyFactory implements ProxyFactory {

  @Override
  @SuppressWarnings("unchecked")
  public <T> T createFor(final Class<T> protocol, final Actor actor, final Mailbox mailbox) {
    final InvocationHandler handler = new ActorProxyInvocationHandler(actor, mailbox);
    return (T) Proxy.newProxyInstance(protocol.getClassLoader(), new Class[] { protocol }, handler);
  }

  @Override
  public Object createFor(final Class<?>[] protocol, final Actor actor, final Mailbox mailbox) {
    final InvocationHandler handler = new ActorProxyInvocationHandler(actor, mailbox);
    return Proxy.newProxyInstance(ProxyFactory.class.getClassLoader(), protocol, handler);
  }
}

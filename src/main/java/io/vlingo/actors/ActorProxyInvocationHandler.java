package io.vlingo.actors;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ActorProxyInvocationHandler implements InvocationHandler {
  private final Actor actor;
  private final Mailbox mailbox;

  ActorProxyInvocationHandler(final Actor actor, final Mailbox mailbox) {
    this.actor = actor;
    this.mailbox = mailbox;
  }

  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
    if (!actor.isStopped()) {
      mailbox.send(new Message(actor, method, args));
    } else if (!actor.__internalOnlyStage().world().isTerminated()) {
      actor.__internalOnlyStage().world().deadLetters().failedDelivery(new DeadLetter(actor, method, args));
    }

    return null;
  }
}

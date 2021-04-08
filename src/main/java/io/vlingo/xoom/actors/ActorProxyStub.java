package io.vlingo.xoom.actors;

class ActorProxyStub<T> extends ActorProxyBase<T> {

  private static final long serialVersionUID = -7711746801630270471L;

  public ActorProxyStub(Actor actor) {
    super(null,
        Definition.SerializationProxy.from(actor.definition()),
        actor.address());
  }

  public ActorProxyStub() { }
}

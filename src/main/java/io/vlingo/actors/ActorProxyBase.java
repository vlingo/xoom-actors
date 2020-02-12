package io.vlingo.actors;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public abstract class ActorProxyBase<T> implements Externalizable {

  public static <T> T thunk(ActorProxyBase<?> proxy, Actor actor, T arg) {
    if (proxy.isDistributable() && arg instanceof ActorProxyBase) {
      final Stage stage = actor.lifeCycle.environment.stage;
      final ActorProxyBase<T> base = (ActorProxyBase<T>)arg;
      final Actor argActor = stage.directory.actorOf(base.address);
      if (argActor == null) {
        System.out.println(base.type);
        return stage.actorFor(base.protocol, base.type);
      }
      else {
        return stage.actorProxyFor(base.protocol, argActor, argActor.lifeCycle.environment.mailbox);
      }
    }
    return arg;
  }

  public Class<T> protocol;
  public Class<? extends Actor> type;
  public Address address;

  public ActorProxyBase(Class<T> protocol, Class<? extends Actor> type, Address address) {
    this.protocol = protocol;
    this.type = type;
    this.address = address;
  }

  public ActorProxyBase() { }

  public final boolean isDistributable() {
    return address.isDistributable();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(protocol);
    out.writeObject(type);
    out.writeObject(address);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException { ;
    this.protocol = (Class<T>) in.readObject();
    this.type = (Class<? extends Actor>) in.readObject();
    this.address = (Address) in.readObject();
  }
}

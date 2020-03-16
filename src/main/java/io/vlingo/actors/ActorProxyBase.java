package io.vlingo.actors;

import com.sun.deploy.security.BadCertificateDialog;
import io.vlingo.actors.Definition.SerializationProxy;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public abstract class ActorProxyBase<T> implements Externalizable {

  private static final long serialVersionUID = -2047182900594333760L;

  public static <T> T thunk(ActorProxyBase<?> proxy, Actor actor, T arg) {
    return proxy.isDistributable()
        ? thunk(actor.lifeCycle.environment.stage, arg)
        : arg;
  }

  public static <T> T thunk(Stage stage, T arg) {
    if (arg instanceof ActorProxyBase) {
      @SuppressWarnings("unchecked")
      final ActorProxyBase<T> base = (ActorProxyBase<T>) arg;
      return stage.lookupOrStart(base.protocol,
          Definition.from(stage, base.definition, stage.world().defaultLogger()),
          base.address);
    }
    return arg;
  }


  public Class<T> protocol;
  public SerializationProxy definition;
  public Address address;


  public ActorProxyBase(Class<T> protocol, SerializationProxy definition, Address address) {
    this.protocol = protocol;
    this.definition = definition;
    this.address = address;
  }

  public ActorProxyBase() { }

  public final boolean isDistributable() {
    return address.isDistributable();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(protocol);
    out.writeObject(definition);
    out.writeObject(address);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException { ;
    this.protocol = (Class<T>) in.readObject();
    this.definition = (SerializationProxy) in.readObject();
    this.address = (Address) in.readObject();
  }
}

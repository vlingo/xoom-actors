package io.vlingo.actors;

import org.junit.Test;

import java.io.*;

public class ActorProxyBaseTest {

  interface Proto {
    void hello(String name);
  }

  static class ActorProxyBaseImpl extends ActorProxyBase<Proto> implements Proto {

    public ActorProxyBaseImpl(Class<Proto> protocol, Class<? extends Actor> type, Address address) {
      super(protocol, type, address);
    }

    public ActorProxyBaseImpl() {
      super();
    }

    @Override
    public void hello(String name) { }
  }

  static class TestAddress implements Address, Serializable {

    public long id;

    public TestAddress(long id) {
      this.id = id;
    }

    @Override
    public long id() {
      return id;
    }

    @Override
    public long idSequence() {
      return id;
    }

    @Override
    public String idSequenceString() {
      return idString();
    }

    @Override
    public String idString() {
      return ""+id;
    }

    @Override
    public <T> T idTyped() {
      return null;
    }

    @Override
    public String name() {
      return null;
    }

    @Override
    public boolean isDistributable() {
      return false;
    }

    @Override
    public int compareTo(Address o) {
      return 0;
    }
  }

  @Test
  public void testWriteRead() throws IOException, ClassNotFoundException {
    ActorProxyBase<Proto> proxy = new ActorProxyBaseImpl(Proto.class, Actor.class, new TestAddress(1));
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
      out.writeObject(proxy);
      out.flush();
      byte[] bytes = bos.toByteArray();
      ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
      try (ObjectInputStream in = new ObjectInputStream(bis)) {
        ActorProxyBase<Proto> proxy1 = (ActorProxyBase<Proto>) in.readObject();
        assert proxy1.protocol == Proto.class;
        assert proxy1.type == Actor.class;
        assert proxy1.address.id() == 1;
      }
    }
  }
}

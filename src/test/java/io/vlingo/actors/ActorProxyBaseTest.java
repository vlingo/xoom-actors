package io.vlingo.actors;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.junit.Test;

public class ActorProxyBaseTest {

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
        @SuppressWarnings("unchecked")
        ActorProxyBase<Proto> proxy1 = (ActorProxyBase<Proto>) in.readObject();
        assert proxy1.protocol == Proto.class;
        assert proxy1.type == Actor.class;
        assert proxy1.address.id() == 1;
      }
    }
  }


  interface Proto {
  }

  static class ActorProxyBaseImpl extends ActorProxyBase<Proto> implements Proto {

    public ActorProxyBaseImpl(Class<Proto> protocol, Class<? extends Actor> type, Address address) {
      super(protocol, type, address);
    }

    public ActorProxyBaseImpl() {
      super();
    }
  }

  static class TestAddress implements Address, Serializable {
    private static final long serialVersionUID = -211461114490694305L;

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
      return "" + id;
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
}

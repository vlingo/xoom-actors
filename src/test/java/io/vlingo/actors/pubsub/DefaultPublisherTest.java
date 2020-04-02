// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.pubsub;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.vlingo.actors.Actor;
import io.vlingo.actors.ActorsTest;
import io.vlingo.actors.Definition;

public class DefaultPublisherTest extends ActorsTest {

  @Test
  public void testSubscribingAndUnsubscribing() {
    DefaultPublisher pub = new DefaultPublisher();
    final SimpleActorProtocol client = world.actorFor(
            SimpleActorProtocol.class,
            Definition.has(SimpleActor.class, Definition.NoParameters));

    InvoiceParentTopic invoiceParentTopic = new InvoiceParentTopic("Invoice");
    assertTrue(pub.subscribe(invoiceParentTopic, client));

    InvoiceChildTopic recurringInvoiceTopic = new InvoiceChildTopic("Recurring Invoice");
    assertTrue(pub.subscribe(recurringInvoiceTopic, client));
    InvoiceChildTopic fixedBillingTopic = new InvoiceChildTopic("Fixed-Bid Billing");
    assertTrue(pub.subscribe(fixedBillingTopic, client));

    assertTrue(recurringInvoiceTopic.isSubTopic(invoiceParentTopic));

    assertTrue(pub.unsubscribe(recurringInvoiceTopic, client));
    assertFalse(pub.unsubscribe(recurringInvoiceTopic, client));

    pub.unsubscribeAllTopics(client);
    assertFalse(pub.unsubscribe(recurringInvoiceTopic, client));
    assertFalse(pub.unsubscribe(fixedBillingTopic, client));
  }

  public interface SimpleActorProtocol extends Subscriber<Message> {
    @Override
    void receive(final Message message);
  }

  public static class SimpleActor extends Actor implements SimpleActorProtocol {
    @Override
    public void receive(Message message) {
      assertTrue(true);
    }
  }

  public static class InvoiceParentTopic extends Topic {
    InvoiceParentTopic(String name) {
      super(name);
    }

    @Override
    public boolean isSubTopic(Topic anotherTopic) {
      return false;
    }
  }

  public static class InvoiceChildTopic extends InvoiceParentTopic {
    InvoiceChildTopic(String name) {
      super(name);
    }

    @Override
    public boolean isSubTopic(Topic anotherTopic) {
      return anotherTopic instanceof InvoiceParentTopic;
    }
  }
}
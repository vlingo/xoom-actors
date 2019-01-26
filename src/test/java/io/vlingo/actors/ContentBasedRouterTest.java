// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.actors;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import io.vlingo.actors.testkit.TestActor;
import io.vlingo.actors.testkit.TestUntil;
/**
 * ContentBasedRouterTest tests {@link ContentBasedRouter}.
 */
public class ContentBasedRouterTest extends ActorsTest {
  
  @Test
  public void testThatItRoutes() throws Exception {
    final int messagesToSend = 63;
    final TestUntil until = TestUntil.happenings(messagesToSend);
    
    final ERPSystemCode[] erpsToTest = {ERPSystemCode.Alpha, ERPSystemCode.Beta, ERPSystemCode.Charlie};
    
    final Protocols routerTestActorProtocols = world.actorFor(
            new Class[] {InvoiceSubmitter.class, InvoiceSubmitterSubscription.class},
            Definition.has(InvoiceSubmissionRouter.class, Definition.parameters(until)));
    final Protocols.Two<InvoiceSubmitter, InvoiceSubmitterSubscription> routerProtocols = Protocols.two(routerTestActorProtocols);
    InvoiceSubmitter routerAsInvoiceSubmitter = routerProtocols._1;
    InvoiceSubmitterSubscription routerAsInvoiceSubmitterSubscription = routerProtocols._2;
    
    final TestActor<InvoiceSubmitter> alphaSubmitterTestActor = testWorld.actorFor(
            InvoiceSubmitter.class,
            Definition.has(ERPSpecificInvoiceSubmitter.class, Definition.parameters(ERPSystemCode.Alpha, until)));
    routerAsInvoiceSubmitterSubscription.subscribe(alphaSubmitterTestActor.actorAs());
    
    final TestActor<InvoiceSubmitter> betaSubmitterTestActor = testWorld.actorFor(
            InvoiceSubmitter.class,
            Definition.has(ERPSpecificInvoiceSubmitter.class, Definition.parameters(ERPSystemCode.Beta, until)));
    routerAsInvoiceSubmitterSubscription.subscribe(betaSubmitterTestActor.actorAs());
    
    final TestActor<InvoiceSubmitter> charlieSubmitterTestActor = testWorld.actorFor(
            InvoiceSubmitter.class,
            Definition.has(ERPSpecificInvoiceSubmitter.class, Definition.parameters(ERPSystemCode.Charlie, until)));
    routerAsInvoiceSubmitterSubscription.subscribe(charlieSubmitterTestActor.actorAs());
    
    final Random random = new Random(System.currentTimeMillis());
    int[] countByERP = new int[erpsToTest.length];
    Arrays.fill(countByERP, 0);
    for (int i = 0; i < messagesToSend; i++) {
      int erpIndex = random.nextInt(3);
      ERPSystemCode erp = erpsToTest[erpIndex];
      Invoice invoice = Invoice.with(erp, i, randomMoney(random));
      routerAsInvoiceSubmitter.submitInvoice(invoice);
      countByERP[erpIndex] += 1;
    }
    
    until.completes();
    
    ERPSpecificInvoiceSubmitter alphaSubmitter = (ERPSpecificInvoiceSubmitter) alphaSubmitterTestActor.actorInside();
    assertEquals("alpha invoice count", countByERP[Arrays.binarySearch(erpsToTest, ERPSystemCode.Alpha)], alphaSubmitter.submitted.size());
    for (Invoice invoice : alphaSubmitter.submitted) {
      assertEquals("invoice expected to be " + ERPSystemCode.Alpha, ERPSystemCode.Alpha, invoice.erp);
    }
    
    ERPSpecificInvoiceSubmitter betaSubmitter = (ERPSpecificInvoiceSubmitter) betaSubmitterTestActor.actorInside();
    assertEquals("beta invoice count", countByERP[Arrays.binarySearch(erpsToTest, ERPSystemCode.Beta)], betaSubmitter.submitted.size());
    for (Invoice invoice : betaSubmitter.submitted) {
      assertEquals("invoice expected to be " + ERPSystemCode.Beta, ERPSystemCode.Beta, invoice.erp);
    }
    
    ERPSpecificInvoiceSubmitter charlieSubmitter = (ERPSpecificInvoiceSubmitter) charlieSubmitterTestActor.actorInside();
    assertEquals("charlie invoice count", countByERP[Arrays.binarySearch(erpsToTest, ERPSystemCode.Charlie)], charlieSubmitter.submitted.size());
    for (Invoice invoice : charlieSubmitter.submitted) {
      assertEquals("invoice expected to be " + ERPSystemCode.Charlie, ERPSystemCode.Charlie, invoice.erp);
    }
  }
  
  public static interface InvoiceSubmitter {
    void submitInvoice(final Invoice invoice);
  }
  
  public static interface InvoiceSubmitterSubscription {
    void subscribe(InvoiceSubmitter submitter);
    void unsubscribe(InvoiceSubmitter submitter);
  }
  
  public static class InvoiceSubmissionRouter extends ContentBasedRouter<InvoiceSubmitter> implements InvoiceSubmitter, InvoiceSubmitterSubscription {
    
    public InvoiceSubmissionRouter(final TestUntil testUntil) {
      super(
        new RouterSpecification<InvoiceSubmitter>(
          0,
          Definition.has(ERPSpecificInvoiceSubmitter.class, Definition.parameters(ERPSystemCode.None, testUntil)),
          InvoiceSubmitter.class)
        );
    }
    
    /* @see io.vlingo.actors.Router#routingFor(java.lang.Object) */
    @Override
    protected <T1> Routing<InvoiceSubmitter> routingFor(final T1 routable1) {
      return Routing.with(routees); //routees filter on erp
    }
    
    /* @see io.vlingo.actors.ContentBasedRoutingStrategyTest.InvoiceSubmitterSubscription#subscribe(io.vlingo.actors.ContentBasedRoutingStrategyTest.InvoiceSubmitter) */
    @Override
    public void subscribe(InvoiceSubmitter submitter) {
      subscribe(Routee.of(submitter));
    }

    /* @see io.vlingo.actors.ContentBasedRoutingStrategyTest.InvoiceSubmitterSubscription#unsubscribe(io.vlingo.actors.ContentBasedRoutingStrategyTest.InvoiceSubmitter) */
    @Override
    public void unsubscribe(InvoiceSubmitter submitter) {
      unsubscribe(Routee.of(submitter));
    }

    /* @see io.vlingo.actors.ContentBasedRoutingStrategyTest.InvoiceSubmitter#submitInvoice(io.vlingo.actors.ContentBasedRoutingStrategyTest.Invoice) */
    @Override
    public void submitInvoice(final Invoice invoice) {
      dispatchCommand(InvoiceSubmitter::submitInvoice, invoice);
    }
  }
  
  public static class ERPSpecificInvoiceSubmitter extends Actor implements InvoiceSubmitter {
    
    private final ERPSystemCode erp;
    private final TestUntil testUntil;
    private final List<Invoice> submitted;
    
    public ERPSpecificInvoiceSubmitter(final ERPSystemCode erp, final TestUntil testUntil) {
      super();
      this.erp = erp;
      this.testUntil = testUntil;
      this.submitted = new ArrayList<>();
    }

    /* @see io.vlingo.actors.ContentBasedRoutingStrategyTest.InvoiceSubmitter#submitInvoice(io.vlingo.actors.ContentBasedRoutingStrategyTest.Invoice) */
    @Override
    public void submitInvoice(final Invoice invoice) {
      if (erp.equals(invoice.erp)) {
        submitted.add(invoice);
        testUntil.happened();
      }
    }
  }
  
  public static enum ERPSystemCode {
    Alpha, Beta, Charlie, None
  }
  
  public static class Invoice {
    private final ERPSystemCode erp;
    private final Integer invoiceId;
    private final BigDecimal amount;
    
    public static Invoice with(final ERPSystemCode erp, final Integer invoiceId, final BigDecimal amount) {
      return new Invoice(erp, invoiceId, amount);
    }
    
    Invoice(final ERPSystemCode erp, final Integer invoiceId, final BigDecimal amount) {
      this.erp = erp;
      this.invoiceId = invoiceId;
      this.amount = amount;
    }

    /* @see java.lang.Object#hashCode() */
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((invoiceId == null) ? 0 : invoiceId.hashCode());
      return result;
    }

    /* @see java.lang.Object#equals(java.lang.Object) */
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Invoice other = (Invoice) obj;
      if (invoiceId == null) {
        if (other.invoiceId != null)
          return false;
      } else if (!invoiceId.equals(other.invoiceId))
        return false;
      return true;
    }

    /* @see java.lang.Object#toString() */
    @Override
    public String toString() {
      return new StringBuilder()
        .append("Invoice(")
        .append("erp=").append(erp)
        .append(", invoiceId=").append(invoiceId)
        .append(", amount=").append(amount)
        .append(")")
        .toString();
    }
  }
  
  private static BigDecimal randomMoney(final Random random) {
    int dollars = random.nextInt(10000);
    int cents = random.nextInt(100);
    String amount = String.valueOf(dollars) + "." + ((cents < 10) ? ("0" + cents) : String.valueOf(cents));
    return new BigDecimal(amount);
  }

}

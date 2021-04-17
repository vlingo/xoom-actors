// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.xoom.actors;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import io.vlingo.xoom.actors.testkit.AccessSafely;
import org.junit.Test;

import io.vlingo.xoom.actors.testkit.TestActor;

/**
 * ContentBasedRouterTest tests {@link ContentBasedRouter}.
 */
public class ContentBasedRouterTest extends ActorsTest {

  @Test
  public void testThatItRoutes() {
    final int messagesToSend = 63;

    final TestResults testResults = new TestResults(messagesToSend);

    final ERPSystemCode[] erpsToTest = {ERPSystemCode.Alpha, ERPSystemCode.Beta, ERPSystemCode.Charlie};

    final Protocols routerTestActorProtocols = world.actorFor(
            new Class[] {InvoiceSubmitter.class, InvoiceSubmitterSubscription.class},
            Definition.has(InvoiceSubmissionRouter.class, Definition.parameters(testResults)));

    final Protocols.Two<InvoiceSubmitter, InvoiceSubmitterSubscription> routerProtocols = Protocols.two(routerTestActorProtocols);
    InvoiceSubmitter routerAsInvoiceSubmitter = routerProtocols._1;
    InvoiceSubmitterSubscription routerAsInvoiceSubmitterSubscription = routerProtocols._2;

    final TestActor<InvoiceSubmitter> alphaSubmitterTestActor = testWorld.actorFor(
            InvoiceSubmitter.class,
            Definition.has(ERPSpecificInvoiceSubmitter.class, Definition.parameters(ERPSystemCode.Alpha, testResults)));
    routerAsInvoiceSubmitterSubscription.subscribe(alphaSubmitterTestActor.actorAs());

    final TestActor<InvoiceSubmitter> betaSubmitterTestActor = testWorld.actorFor(
            InvoiceSubmitter.class,
            Definition.has(ERPSpecificInvoiceSubmitter.class, Definition.parameters(ERPSystemCode.Beta, testResults)));
    routerAsInvoiceSubmitterSubscription.subscribe(betaSubmitterTestActor.actorAs());

    final TestActor<InvoiceSubmitter> charlieSubmitterTestActor = testWorld.actorFor(
            InvoiceSubmitter.class,
            Definition.has(ERPSpecificInvoiceSubmitter.class, Definition.parameters(ERPSystemCode.Charlie, testResults)));
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

    assertSubmittedInvoices(testResults, erpsToTest, countByERP, ERPSystemCode.Alpha);
    assertSubmittedInvoices(testResults, erpsToTest, countByERP, ERPSystemCode.Beta);
    assertSubmittedInvoices(testResults, erpsToTest, countByERP, ERPSystemCode.Charlie);
}


  private void assertSubmittedInvoices(TestResults testResults, ERPSystemCode[] erpsToTest, int[] countByERP, ERPSystemCode systemCode) {
    final List<Invoice> alphaSubmittedInvoices = testResults.getSubmittedInvoices(systemCode);
    assertEquals(systemCode + " invoice count", countByERP[Arrays.binarySearch(erpsToTest, systemCode)], alphaSubmittedInvoices.size());
    for (Invoice invoice : alphaSubmittedInvoices) {
      assertEquals("invoice expected to be " + systemCode, systemCode, invoice.erp);
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

    public InvoiceSubmissionRouter(final TestResults testResults) {
      super(
        new RouterSpecification<>(
          0,
          Definition.has(ERPSpecificInvoiceSubmitter.class, Definition.parameters(ERPSystemCode.None, testResults)),
          InvoiceSubmitter.class)
        );
    }

    /* @see io.vlingo.xoom.actors.Router#routingFor(java.lang.Object) */
    @Override
    protected <T1> Routing<InvoiceSubmitter> routingFor(final T1 routable1) {
      if (routable1 instanceof Invoice){
        return Routing.with(routees); //routees filter on erp
      } else {
        return Routing.with(Collections.emptyList());
      }
    }

    /* @see io.vlingo.xoom.actors.ContentBasedRoutingStrategyTest.InvoiceSubmitterSubscription#subscribe(io.vlingo.xoom.actors.ContentBasedRoutingStrategyTest.InvoiceSubmitter) */
    @Override
    public void subscribe(InvoiceSubmitter submitter) {
      subscribe(Routee.of(submitter));
    }

    /* @see io.vlingo.xoom.actors.ContentBasedRoutingStrategyTest.InvoiceSubmitterSubscription#unsubscribe(io.vlingo.xoom.actors.ContentBasedRoutingStrategyTest.InvoiceSubmitter) */
    @Override
    public void unsubscribe(InvoiceSubmitter submitter) {
      unsubscribe(Routee.of(submitter));
    }

    /* @see io.vlingo.xoom.actors.ContentBasedRoutingStrategyTest.InvoiceSubmitter#submitInvoice(io.vlingo.xoom.actors.ContentBasedRoutingStrategyTest.Invoice) */
    @Override
    public void submitInvoice(final Invoice invoice) {
      dispatchCommand(InvoiceSubmitter::submitInvoice, invoice);
    }
  }

  public static class ERPSpecificInvoiceSubmitter extends Actor implements InvoiceSubmitter {

    private final ERPSystemCode erp;
    private final TestResults testResults;

    public ERPSpecificInvoiceSubmitter(final ERPSystemCode erp, final TestResults testResults) {
      super();
      this.erp = erp;
      this.testResults = testResults;
    }

    /* @see io.vlingo.xoom.actors.ContentBasedRoutingStrategyTest.InvoiceSubmitter#submitInvoice(io.vlingo.xoom.actors.ContentBasedRoutingStrategyTest.Invoice) */
    @Override
    public void submitInvoice(final Invoice invoice) {
      if (this.erp.equals(invoice.erp)) {
        testResults.invoiceSubmitted(invoice);
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

  private static class TestResults {
    private final AccessSafely submittedInvoices;

    private TestResults(int times) {
      final Map<ERPSystemCode, List<Invoice>> invoices = new ConcurrentHashMap<>(times);
      this.submittedInvoices = AccessSafely.afterCompleting(times);
      this.submittedInvoices.writingWith("submittedInvoices",
              (ERPSystemCode key, Invoice value) -> invoices.computeIfAbsent(key, (code) ->  new ArrayList<>()).add(value));
      this.submittedInvoices.readingWith("submittedInvoices",
              (Function<ERPSystemCode, List<Invoice>>) invoices::get);
    }

    private void invoiceSubmitted(Invoice invoice){
      this.submittedInvoices.writeUsing("submittedInvoices", invoice.erp, invoice);
    }

    private List<Invoice> getSubmittedInvoices(ERPSystemCode code){
      return this.submittedInvoices.readFrom("submittedInvoices", code);
    }
  }
}

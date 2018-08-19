package io.vlingo.actors.plugin.mailbox.telemetry;

import io.vlingo.actors.MailboxProvider;
import io.vlingo.actors.MailboxProviderKeeper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TelemetryMailboxProviderKeeperTest {
  private static final String NAME = UUID.randomUUID().toString();
  private static final int HASHCODE = UUID.randomUUID().toString().hashCode();
  private static final boolean IS_DEFAULT = new Random().nextBoolean();

  private MailboxProviderKeeper delegate;
  private MailboxTelemetry telemetry;
  private MailboxProvider provider;
  private MailboxProviderKeeper telemetryMailboxProviderKeeper;

  @Before
  public void setUp() throws Exception {
    delegate = mock(MailboxProviderKeeper.class);
    telemetry = mock(MailboxTelemetry.class);
    provider = mock(MailboxProvider.class);

    telemetryMailboxProviderKeeper = new TelemetryMailboxProviderKeeper(delegate, telemetry);
  }

  @Test
  public void testThatKeepingAMailboxProviderWrapsIntoTelemetryMailboxProvider() {
    telemetryMailboxProviderKeeper.keep(NAME, IS_DEFAULT, provider);
    verify(delegate).keep(eq(NAME), eq(IS_DEFAULT), any());
  }

  @Test
  public void testThatAssignMailboxIsDelegated() {
    telemetryMailboxProviderKeeper.assignMailbox(NAME, HASHCODE);
    verify(delegate).assignMailbox(NAME, HASHCODE);
  }

  @Test
  public void testThatCloseIsDelegated() {
    telemetryMailboxProviderKeeper.close();
    verify(delegate).close();
  }

  @Test
  public void testThatFindDefaultIsDelegated() {
    final String randomString = UUID.randomUUID().toString();
    doReturn(randomString).when(delegate).findDefault();

    final String result = telemetryMailboxProviderKeeper.findDefault();

    verify(delegate).findDefault();
    assertEquals(randomString, result);
  }

  @Test
  public void testThatValidMailboxNameIsDelegated() {
    final String randomString = UUID.randomUUID().toString();
    final boolean randomBool = new Random().nextBoolean();

    doReturn(randomBool).when(delegate).isValidMailboxName(randomString);

    final boolean result = telemetryMailboxProviderKeeper.isValidMailboxName(randomString);
    verify(delegate).isValidMailboxName(randomString);
    assertEquals(randomBool, result);
  }
}
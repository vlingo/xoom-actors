package io.vlingo.actors.plugin.mailbox;

import io.vlingo.actors.MailboxProvider;
import io.vlingo.actors.MailboxProviderKeeper;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DefaultMailboxProviderKeeperTest {
  private static String MAILBOX_NAME = "Mailbox-" + UUID.randomUUID().toString();
  private static int SOME_HASHCODE = UUID.randomUUID().hashCode();

  private MailboxProvider mailboxProvider;
  private MailboxProviderKeeper keeper;

  @Before
  public void setUp() {
    mailboxProvider = mock(MailboxProvider.class);
    keeper = new DefaultMailboxProviderKeeper();

    keeper.keep(MAILBOX_NAME, false, mailboxProvider);
  }

  @Test
  public void testThatAssignsAMailboxFromTheSpecifiedProvider() {
    keeper.assignMailbox(MAILBOX_NAME, SOME_HASHCODE);

    verify(mailboxProvider).provideMailboxFor(SOME_HASHCODE);
  }

  @Test
  public void testThatKeepingADefaultMailboxOverridesTheCurrentDefault() {
    MailboxProvider newDefault = mock(MailboxProvider.class);
    String name = "NewDefault-" + UUID.randomUUID().toString();

    keeper.keep(name, true, newDefault);
    assertEquals(name, keeper.findDefault());
  }

  @Test
  public void testThatClosesAllProviders() {
    keeper.close();
    verify(mailboxProvider).close();
  }

  @Test
  public void testThatIsValidMailboxNameChecksForMailboxExistance() {
    assertTrue(keeper.isValidMailboxName(MAILBOX_NAME));
    assertFalse(keeper.isValidMailboxName(MAILBOX_NAME + "_DoesNotExist"));
  }

  @Test(expected = RuntimeException.class)
  public void testThatAssigningAnUnknownMailboxFailsGracefully() {
    keeper.assignMailbox(MAILBOX_NAME + "_DoesNotExist", SOME_HASHCODE);
  }

  @Test(expected = RuntimeException.class)
  public void testThatNoDefaultProviderWillFailGracefully() {
    new DefaultMailboxProviderKeeper().findDefault();
  }
}
package io.aggregator.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import com.google.protobuf.Timestamp;

import io.aggregator.TimeTo;
import io.aggregator.api.StripedSecondApi;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class StripedSecondTest {

  @Test
  @Ignore("to be implemented")
  public void exampleTest() {
    // StripedSecondTestKit service = StripedSecondTestKit.of(StripedSecond::new);
    // // use the testkit to execute a command
    // // of events emitted, or a final updated state:
    // SomeCommand command = SomeCommand.newBuilder()...build();
    // EventSourcedResult<SomeResponse> result = service.someOperation(command);
    // // verify the emitted events
    // ExpectedEvent actualEvent = result.getNextEventOfType(ExpectedEvent.class);
    // assertEquals(expectedEvent, actualEvent);
    // // verify the final state after applying the events
    // assertEquals(expectedState, service.getState());
    // // verify the reply
    // SomeReply reply = result.getReply();
    // assertEquals(expectedReply, reply);
  }

  @Test
  public void addTransactionTest() {
    StripedSecondTestKit testKit = StripedSecondTestKit.of(StripedSecond::new);

    var timestamp = TimeTo.now();
    var stripe1 = TimeTo.stripe("transaction-1");
    var stripe2 = TimeTo.stripe("transaction-2");

    var response = testKit.addTransaction(addTransactionCommand(timestamp.getSeconds(), stripe1, "transaction-1", 1.23));

    var stripedSecondActivated = response.getNextEventOfType(StripedSecondEntity.StripedSecondActivated.class);
    var transactionAdded = response.getNextEventOfType(StripedSecondEntity.StripedSecondTransactionAdded.class);

    assertEquals("merchant-1", stripedSecondActivated.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", stripedSecondActivated.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", stripedSecondActivated.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", stripedSecondActivated.getMerchantKey().getAccountTo());
    assertEquals(timestamp.getSeconds(), stripedSecondActivated.getEpochSecond());
    assertEquals(stripe1, stripedSecondActivated.getStripe());

    assertEquals("merchant-1", transactionAdded.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", transactionAdded.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", transactionAdded.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", transactionAdded.getMerchantKey().getAccountTo());
    assertEquals(timestamp.getSeconds(), transactionAdded.getEpochSecond());
    assertEquals(stripe1, transactionAdded.getStripe());

    assertEquals("transaction-1", transactionAdded.getTransactionKey().getTransactionId());
    assertEquals("service-code-1", transactionAdded.getTransactionKey().getServiceCode());
    assertEquals("account-from-1", transactionAdded.getTransactionKey().getAccountFrom());
    assertEquals("account-to-1", transactionAdded.getTransactionKey().getAccountTo());
    assertEquals(1.23, transactionAdded.getAmount(), 0.0);
    assertTrue(transactionAdded.getTimestamp().getSeconds() > 0);

    var state = testKit.getState();

    assertEquals("merchant-1", state.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", state.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", state.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", state.getMerchantKey().getAccountTo());
    assertTrue(state.getEpochSecond() > 0);
    assertEquals(stripe1, state.getStripe());
    assertEquals(1, state.getTransactionsList().size());

    var transaction = state.getTransactionsList().get(0);
    assertEquals("merchant-1", transaction.getMerchantId());
    assertEquals(timestamp.getSeconds(), transaction.getEpochSecond());
    assertEquals(stripe1, transaction.getStripe());
    assertEquals("transaction-1", transaction.getTransactionKey().getTransactionId());
    assertEquals("service-code-1", transaction.getTransactionKey().getServiceCode());
    assertEquals("account-from-1", transaction.getTransactionKey().getAccountFrom());
    assertEquals("account-to-1", transaction.getTransactionKey().getAccountTo());
    assertEquals(1.23, transaction.getAmount(), 0.0);
    assertTrue(transaction.getTimestamp().getSeconds() > 0);

    testKit.addTransaction(addTransactionCommand(timestamp.getSeconds(), stripe2, "transaction-2", 4.56));
    response = testKit.addTransaction(addTransactionCommand(timestamp.getSeconds(), stripe2, "transaction-2", 4.56)); // try adding the same transaction again - should be idempotent

    transactionAdded = response.getNextEventOfType(StripedSecondEntity.StripedSecondTransactionAdded.class);
    assertNotNull(transactionAdded);

    state = testKit.getState();

    assertEquals(2, state.getTransactionsList().size());

    transaction = state.getTransactionsList().get(1);
    assertEquals("merchant-1", transaction.getMerchantId());
    assertEquals(timestamp.getSeconds(), transaction.getEpochSecond());
    assertEquals(stripe2, transaction.getStripe());
    assertEquals("transaction-2", transaction.getTransactionKey().getTransactionId());
    assertEquals("service-code-1", transaction.getTransactionKey().getServiceCode());
    assertEquals("account-from-1", transaction.getTransactionKey().getAccountFrom());
    assertEquals("account-to-1", transaction.getTransactionKey().getAccountTo());
    assertEquals(4.56, transaction.getAmount(), 0.0);
    assertTrue(transaction.getTimestamp().getSeconds() > 0);
  }

  @Test
  public void aggregateStripedSecondTest() {
    StripedSecondTestKit testKit = StripedSecondTestKit.of(StripedSecond::new);

    var timestamp = TimeTo.now();
    var stripe = 3;

    testKit.addTransaction(addTransactionCommand(timestamp.getSeconds(), stripe, "transaction-1", 1.23, TimeTo.fromTimestamp(timestamp).toTimestamp()));
    testKit.addTransaction(addTransactionCommand(timestamp.getSeconds(), stripe, "transaction-2", 4.56, TimeTo.fromTimestamp(timestamp).plus().nanos(10).toTimestamp()));
    testKit.addTransaction(addTransactionCommand(timestamp.getSeconds(), stripe, "transaction-2", 4.56, TimeTo.fromTimestamp(timestamp).plus().nanos(10).toTimestamp())); // idempotent
    testKit.addTransaction(addTransactionCommand(timestamp.getSeconds(), stripe, "transaction-3", 7.89, TimeTo.fromTimestamp(timestamp).plus().nanos(20).toTimestamp()));

    var response = testKit.aggregateStripedSecond(aggregateStripedSecondCommand("payment-1", timestamp.getSeconds(), stripe, TimeTo.fromTimestamp(timestamp).plus().hours(2).toTimestamp()));

    assertEquals(4, response.getAllEvents().size());
    var stripedSecondAggregated = response.getNextEventOfType(StripedSecondEntity.StripedSecondAggregated.class);
    response.getNextEventOfType(StripedSecondEntity.TransactionPaid.class);
    response.getNextEventOfType(StripedSecondEntity.TransactionPaid.class);
    var transactionPaid = response.getNextEventOfType(StripedSecondEntity.TransactionPaid.class);

    assertEquals("transaction-3", transactionPaid.getTransactionKey().getTransactionId());
    assertEquals("service-code-1", transactionPaid.getTransactionKey().getServiceCode());
    assertEquals("account-from-1", transactionPaid.getTransactionKey().getAccountFrom());
    assertEquals("account-to-1", transactionPaid.getTransactionKey().getAccountTo());
    assertEquals("merchant-1", transactionPaid.getMerchantId());
    assertEquals("payment-1", transactionPaid.getPaymentId());
    assertEquals(timestamp.getSeconds(), transactionPaid.getEpochSecond());
    assertEquals(stripe, transactionPaid.getStripe());

    assertNotNull(stripedSecondAggregated);
    assertEquals("merchant-1", stripedSecondAggregated.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", stripedSecondAggregated.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", stripedSecondAggregated.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", stripedSecondAggregated.getMerchantKey().getAccountTo());
    assertEquals(timestamp.getSeconds(), stripedSecondAggregated.getEpochSecond());
    assertEquals(stripe, stripedSecondAggregated.getStripe());
    assertEquals(1.23 + 4.56 + 7.89, stripedSecondAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(3, stripedSecondAggregated.getTransactionCount());
    assertEquals(TimeTo.fromTimestamp(timestamp).plus().hours(2).toTimestamp(), stripedSecondAggregated.getAggregateRequestTimestamp());
    assertEquals(TimeTo.fromTimestamp(timestamp).plus().nanos(20).toTimestamp(), stripedSecondAggregated.getLastUpdateTimestamp());
    assertEquals("payment-1", stripedSecondAggregated.getPaymentId());

    // when the same aggregate command is received again, all of the processed transactions should be ignored
    response = testKit.aggregateStripedSecond(aggregateStripedSecondCommand("payment-1", timestamp.getSeconds(), stripe, TimeTo.fromTimestamp(timestamp).plus().hours(2).toTimestamp()));

    assertEquals(1, response.getAllEvents().size());
    stripedSecondAggregated = response.getNextEventOfType(StripedSecondEntity.StripedSecondAggregated.class);

    assertNotNull(stripedSecondAggregated);
    assertEquals("merchant-1", stripedSecondAggregated.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", stripedSecondAggregated.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", stripedSecondAggregated.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", stripedSecondAggregated.getMerchantKey().getAccountTo());
    assertEquals(timestamp.getSeconds(), stripedSecondAggregated.getEpochSecond());
    assertEquals(stripe, stripedSecondAggregated.getStripe());
    assertEquals(0.0, stripedSecondAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(0, stripedSecondAggregated.getTransactionCount());
    assertEquals(TimeTo.fromTimestamp(timestamp).plus().hours(2).toTimestamp(), stripedSecondAggregated.getAggregateRequestTimestamp());
    assertEquals(TimeTo.zero(), stripedSecondAggregated.getLastUpdateTimestamp());
    assertEquals("payment-1", stripedSecondAggregated.getPaymentId());

    // add more transactions after aggregation
    response = testKit.addTransaction(addTransactionCommand(timestamp.getSeconds(), stripe, "transaction-4", 6.54, TimeTo.fromTimestamp(timestamp).plus().nanos(30).toTimestamp()));

    assertEquals(2, response.getAllEvents().size());
    response.getNextEventOfType(StripedSecondEntity.StripedSecondActivated.class);
    response.getNextEventOfType(StripedSecondEntity.StripedSecondTransactionAdded.class);

    testKit.addTransaction(addTransactionCommand(timestamp.getSeconds(), stripe, "transaction-5", 3.21, TimeTo.fromTimestamp(timestamp).plus().nanos(40).toTimestamp()));

    response = testKit.aggregateStripedSecond(aggregateStripedSecondCommand("payment-2", timestamp.getSeconds(), stripe, TimeTo.fromTimestamp(timestamp).plus().hours(3).toTimestamp()));

    assertEquals(3, response.getAllEvents().size());
    stripedSecondAggregated = response.getNextEventOfType(StripedSecondEntity.StripedSecondAggregated.class);
    response.getNextEventOfType(StripedSecondEntity.TransactionPaid.class);
    response.getNextEventOfType(StripedSecondEntity.TransactionPaid.class);

    assertNotNull(stripedSecondAggregated);
    assertEquals("merchant-1", stripedSecondAggregated.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", stripedSecondAggregated.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", stripedSecondAggregated.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", stripedSecondAggregated.getMerchantKey().getAccountTo());
    assertEquals(timestamp.getSeconds(), stripedSecondAggregated.getEpochSecond());
    assertEquals(stripe, stripedSecondAggregated.getStripe());
    assertEquals(6.54 + 3.21, stripedSecondAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(2, stripedSecondAggregated.getTransactionCount());
    assertEquals(TimeTo.fromTimestamp(timestamp).plus().hours(3).toTimestamp(), stripedSecondAggregated.getAggregateRequestTimestamp());
    assertEquals(TimeTo.fromTimestamp(timestamp).plus().nanos(40).toTimestamp(), stripedSecondAggregated.getLastUpdateTimestamp());
    assertEquals("payment-2", stripedSecondAggregated.getPaymentId());
  }

  static TransactionMerchantKey.MerchantKey merchantKey(String merchantId) {
    return TransactionMerchantKey.MerchantKey
        .newBuilder()
        .setMerchantId(merchantId)
        .setServiceCode("service-code-1")
        .setAccountFrom("account-from-1")
        .setAccountTo("account-to-1")
        .build();
  }

  static TransactionMerchantKey.TransactionKey transactionKey(String transactionId) {
    return TransactionMerchantKey.TransactionKey
        .newBuilder()
        .setTransactionId(transactionId)
        .setServiceCode("service-code-1")
        .setAccountFrom("account-from-1")
        .setAccountTo("account-to-1")
        .build();
  }

  static StripedSecondApi.AddTransactionCommand addTransactionCommand(long epochSecond, int stripe, String transactionId, double amount) {
    return addTransactionCommand(epochSecond, stripe, transactionId, amount, TimeTo.now());
  }

  static StripedSecondApi.AddTransactionCommand addTransactionCommand(long epochSecond, int stripe, String transactionId, double amount, Timestamp timestamp) {
    return StripedSecondApi.AddTransactionCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setServiceCode("service-code-1")
        .setAccountFrom("account-from-1")
        .setAccountTo("account-to-1")
        .setEpochSecond(epochSecond)
        .setStripe(stripe)
        .setTransactionId(transactionId)
        .setAmount(amount)
        .setTimestamp(timestamp)
        .build();
  }

  static StripedSecondApi.AggregateStripedSecondCommand aggregateStripedSecondCommand(String paymentId, long epochSecond, int stripe, Timestamp timestamp) {
    return StripedSecondApi.AggregateStripedSecondCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setServiceCode("service-code-1")
        .setAccountFrom("account-from-1")
        .setAccountTo("account-to-1")
        .setEpochSecond(epochSecond)
        .setStripe(stripe)
        .setAggregateRequestTimestamp(timestamp)
        .setPaymentId(paymentId)
        .build();
  }
}

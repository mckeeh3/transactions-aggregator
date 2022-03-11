package io.aggregator.entity;

import static org.junit.Assert.*;

import com.google.protobuf.Timestamp;

import org.junit.Test;

import io.aggregator.TimeTo;
import io.aggregator.api.SubSecondApi;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class SubSecondTest {

  @Test
  public void exampleTest() {
    // SubSecondTestKit testKit = SubSecondTestKit.of(SubSecond::new);
    // use the testkit to execute a command
    // of events emitted, or a final updated state:
    // EventSourcedResult<SomeResponse> result = testKit.someOperation(SomeRequest);
    // verify the emitted events
    // ExpectedEvent actualEvent = result.getNextEventOfType(ExpectedEvent.class);
    // assertEquals(expectedEvent, actualEvent)
    // verify the final state after applying the events
    // assertEquals(expectedState, testKit.getState());
    // verify the response
    // SomeResponse actualResponse = result.getReply();
    // assertEquals(expectedResponse, actualResponse);
  }

  @Test
  public void addTransactionTest() {
    SubSecondTestKit testKit = SubSecondTestKit.of(SubSecond::new);

    var epochSubSecond = TimeTo.fromTimestamp(TimeTo.now()).toEpochSubSecond();

    var response = testKit.addTransaction(addTransactionCommand(epochSubSecond, "transaction-1", 1.23));

    var subSecondActivated = response.getNextEventOfType(SubSecondEntity.SubSecondActivated.class);
    var transactionAdded = response.getNextEventOfType(SubSecondEntity.SubSecondTransactionAdded.class);

    assertEquals("merchant-1", subSecondActivated.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", subSecondActivated.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", subSecondActivated.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", subSecondActivated.getMerchantKey().getAccountTo());
    assertTrue(subSecondActivated.getEpochSubSecond() > 0);

    assertEquals("merchant-1", transactionAdded.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", transactionAdded.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", transactionAdded.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", transactionAdded.getMerchantKey().getAccountTo());
    assertTrue(transactionAdded.getEpochSubSecond() > 0);

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
    assertTrue(state.getEpochSubSecond() > 0);
    assertEquals(1, state.getTransactionsList().size());

    var transaction = state.getTransactionsList().get(0);
    assertEquals("merchant-1", transaction.getMerchantId());
    assertTrue(transaction.getEpochSubSecond() > 0);
    assertEquals("transaction-1", transaction.getTransactionKey().getTransactionId());
    assertEquals("service-code-1", transaction.getTransactionKey().getServiceCode());
    assertEquals("account-from-1", transaction.getTransactionKey().getAccountFrom());
    assertEquals("account-to-1", transaction.getTransactionKey().getAccountTo());
    assertEquals(1.23, transaction.getAmount(), 0.0);
    assertTrue(transaction.getTimestamp().getSeconds() > 0);

    testKit.addTransaction(addTransactionCommand(epochSubSecond, "transaction-2", 4.56));
    response = testKit.addTransaction(addTransactionCommand(epochSubSecond, "transaction-2", 4.56)); // try adding the same transaction again - should be idempotent

    transactionAdded = response.getNextEventOfType(SubSecondEntity.SubSecondTransactionAdded.class);
    assertNotNull(transactionAdded);

    state = testKit.getState();

    assertEquals(2, state.getTransactionsList().size());

    transaction = state.getTransactionsList().get(1);
    assertEquals("merchant-1", transaction.getMerchantId());
    assertTrue(transaction.getEpochSubSecond() > 0);
    assertEquals("transaction-2", transaction.getTransactionKey().getTransactionId());
    assertEquals("service-code-1", transaction.getTransactionKey().getServiceCode());
    assertEquals("account-from-1", transaction.getTransactionKey().getAccountFrom());
    assertEquals("account-to-1", transaction.getTransactionKey().getAccountTo());
    assertEquals(4.56, transaction.getAmount(), 0.0);
    assertTrue(transaction.getTimestamp().getSeconds() > 0);
  }

  @Test
  public void aggregateSubSecondTest() {
    SubSecondTestKit testKit = SubSecondTestKit.of(SubSecond::new);

    var epochSubSecond = TimeTo.fromNow().toEpochSubSecond();

    testKit.addTransaction(addTransactionCommand(epochSubSecond, "transaction-1", 1.23, TimeTo.fromEpochSubSecond(epochSubSecond).toTimestamp()));
    testKit.addTransaction(addTransactionCommand(epochSubSecond, "transaction-2", 4.56, TimeTo.fromEpochSubSecond(epochSubSecond).plus().nanos(10).toTimestamp()));
    testKit.addTransaction(addTransactionCommand(epochSubSecond, "transaction-2", 4.56, TimeTo.fromEpochSubSecond(epochSubSecond).plus().nanos(10).toTimestamp())); // idempotent
    testKit.addTransaction(addTransactionCommand(epochSubSecond, "transaction-3", 7.89, TimeTo.fromEpochSubSecond(epochSubSecond).plus().nanos(20).toTimestamp()));

    var response = testKit.aggregateSubSecond(aggregateSubSecondCommand("payment-1", epochSubSecond, TimeTo.fromEpochSubSecond(epochSubSecond).plus().hours(2).toTimestamp()));

    assertEquals(4, response.getAllEvents().size());
    var subSecondAggregated = response.getNextEventOfType(SubSecondEntity.SubSecondAggregated.class);
    response.getNextEventOfType(SubSecondEntity.TransactionPaid.class);
    response.getNextEventOfType(SubSecondEntity.TransactionPaid.class);
    var transactionPaid = response.getNextEventOfType(SubSecondEntity.TransactionPaid.class);

    assertEquals("transaction-3", transactionPaid.getTransactionKey().getTransactionId());
    assertEquals("service-code-1", transactionPaid.getTransactionKey().getServiceCode());
    assertEquals("account-from-1", transactionPaid.getTransactionKey().getAccountFrom());
    assertEquals("account-to-1", transactionPaid.getTransactionKey().getAccountTo());
    assertEquals("merchant-1", transactionPaid.getMerchantId());
    assertEquals("payment-1", transactionPaid.getPaymentId());
    assertEquals(epochSubSecond, transactionPaid.getEpochSubSecond());

    assertNotNull(subSecondAggregated);
    assertEquals("merchant-1", subSecondAggregated.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", subSecondAggregated.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", subSecondAggregated.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", subSecondAggregated.getMerchantKey().getAccountTo());
    assertEquals(epochSubSecond, subSecondAggregated.getEpochSubSecond());
    assertEquals(1.23 + 4.56 + 7.89, subSecondAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(3, subSecondAggregated.getTransactionCount());
    assertEquals(TimeTo.fromEpochSubSecond(epochSubSecond).plus().hours(2).toTimestamp(), subSecondAggregated.getAggregateRequestTimestamp());
    assertEquals(TimeTo.fromEpochSubSecond(epochSubSecond).plus().nanos(20).toTimestamp(), subSecondAggregated.getLastUpdateTimestamp());
    assertEquals("payment-1", subSecondAggregated.getPaymentId());

    // when the same aggregate command is received again, all of the processed transactions should be ignored
    response = testKit.aggregateSubSecond(aggregateSubSecondCommand("payment-1", epochSubSecond, TimeTo.fromEpochSubSecond(epochSubSecond).plus().hours(2).toTimestamp()));

    assertEquals(1, response.getAllEvents().size());
    subSecondAggregated = response.getNextEventOfType(SubSecondEntity.SubSecondAggregated.class);

    assertNotNull(subSecondAggregated);
    assertEquals("merchant-1", subSecondAggregated.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", subSecondAggregated.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", subSecondAggregated.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", subSecondAggregated.getMerchantKey().getAccountTo());
    assertEquals(epochSubSecond, subSecondAggregated.getEpochSubSecond());
    assertEquals(0.0, subSecondAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(0, subSecondAggregated.getTransactionCount());
    assertEquals(TimeTo.fromEpochSubSecond(epochSubSecond).plus().hours(2).toTimestamp(), subSecondAggregated.getAggregateRequestTimestamp());
    assertEquals(TimeTo.zero(), subSecondAggregated.getLastUpdateTimestamp());
    assertEquals("payment-1", subSecondAggregated.getPaymentId());

    // add more transactions after aggregation
    response = testKit.addTransaction(addTransactionCommand(epochSubSecond, "transaction-4", 6.54, TimeTo.fromEpochSubSecond(epochSubSecond).plus().nanos(30).toTimestamp()));

    assertEquals(2, response.getAllEvents().size());
    response.getNextEventOfType(SubSecondEntity.SubSecondActivated.class);
    response.getNextEventOfType(SubSecondEntity.SubSecondTransactionAdded.class);

    testKit.addTransaction(addTransactionCommand(epochSubSecond, "transaction-5", 3.21, TimeTo.fromEpochSubSecond(epochSubSecond).plus().nanos(40).toTimestamp()));

    response = testKit.aggregateSubSecond(aggregateSubSecondCommand("payment-2", epochSubSecond, TimeTo.fromEpochSubSecond(epochSubSecond).plus().hours(3).toTimestamp()));

    assertEquals(3, response.getAllEvents().size());
    subSecondAggregated = response.getNextEventOfType(SubSecondEntity.SubSecondAggregated.class);
    response.getNextEventOfType(SubSecondEntity.TransactionPaid.class);
    response.getNextEventOfType(SubSecondEntity.TransactionPaid.class);

    assertNotNull(subSecondAggregated);
    assertEquals("merchant-1", subSecondAggregated.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", subSecondAggregated.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", subSecondAggregated.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", subSecondAggregated.getMerchantKey().getAccountTo());
    assertEquals(epochSubSecond, subSecondAggregated.getEpochSubSecond());
    assertEquals(6.54 + 3.21, subSecondAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(2, subSecondAggregated.getTransactionCount());
    assertEquals(TimeTo.fromEpochSubSecond(epochSubSecond).plus().hours(3).toTimestamp(), subSecondAggregated.getAggregateRequestTimestamp());
    assertEquals(TimeTo.fromEpochSubSecond(epochSubSecond).plus().nanos(40).toTimestamp(), subSecondAggregated.getLastUpdateTimestamp());
    assertEquals("payment-2", subSecondAggregated.getPaymentId());
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

  static SubSecondApi.AddTransactionCommand addTransactionCommand(long epochSubSecond, String transactionId, double amount) {
    return addTransactionCommand(epochSubSecond, transactionId, amount, TimeTo.now());
  }

  static SubSecondApi.AddTransactionCommand addTransactionCommand(long epochSubSecond, String transactionId, double amount, Timestamp timestamp) {
    return SubSecondApi.AddTransactionCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setServiceCode("service-code-1")
        .setAccountFrom("account-from-1")
        .setAccountTo("account-to-1")
        .setEpochSubSecond(epochSubSecond)
        .setTransactionId(transactionId)
        .setAmount(amount)
        .setTimestamp(timestamp)
        .build();
  }

  static SubSecondApi.AggregateSubSecondCommand aggregateSubSecondCommand(String paymentId, long epochSubSecond, Timestamp timestamp) {
    return SubSecondApi.AggregateSubSecondCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setServiceCode("service-code-1")
        .setAccountFrom("account-from-1")
        .setAccountTo("account-to-1")
        .setEpochSubSecond(epochSubSecond)
        .setAggregateRequestTimestamp(timestamp)
        .setPaymentId(paymentId)
        .build();
  }
}

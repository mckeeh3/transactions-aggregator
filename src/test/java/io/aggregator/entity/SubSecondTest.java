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

    var response = testKit.addLedgerItems(addLedgerItemsCommand(epochSubSecond, "transaction-1", 1.23));

    var subSecondActivated = response.getNextEventOfType(SubSecondEntity.SubSecondActivated.class);
    var subSecondLedgerItemsAdded = response.getNextEventOfType(SubSecondEntity.SubSecondLedgerItemsAdded.class);

    assertEquals("merchant-1", subSecondActivated.getMerchantKey().getMerchantId());
    assertTrue(subSecondActivated.getEpochSubSecond() > 0);

    assertEquals("merchant-1", subSecondLedgerItemsAdded.getMerchantKey().getMerchantId());
    assertTrue(subSecondLedgerItemsAdded.getEpochSubSecond() > 0);
    assertEquals(1, subSecondLedgerItemsAdded.getLedgerEntriesCount());
    assertEquals("transaction-1", subSecondLedgerItemsAdded.getLedgerEntries(0).getTransactionKey().getTransactionId());
    assertEquals("service-code-1", subSecondLedgerItemsAdded.getLedgerEntries(0).getTransactionKey().getServiceCode());
    assertEquals("account-from-1", subSecondLedgerItemsAdded.getLedgerEntries(0).getTransactionKey().getAccountFrom());
    assertEquals("account-to-1", subSecondLedgerItemsAdded.getLedgerEntries(0).getTransactionKey().getAccountTo());
    assertEquals(1.23, subSecondLedgerItemsAdded.getLedgerEntries(0).getAmount(), 0.0);
    assertTrue(subSecondLedgerItemsAdded.getTimestamp().getSeconds() > 0);

    var state = testKit.getState();

    assertEquals("merchant-1", state.getMerchantKey().getMerchantId());
    assertTrue(state.getEpochSubSecond() > 0);
    assertEquals(1, state.getLedgerEntriesCount());

    var ledgerEntry = state.getLedgerEntries(0);
    assertTrue(ledgerEntry.getEpochSubSecond() > 0);
    assertEquals("transaction-1", ledgerEntry.getTransactionKey().getTransactionId());
    assertEquals("service-code-1", ledgerEntry.getTransactionKey().getServiceCode());
    assertEquals("account-from-1", ledgerEntry.getTransactionKey().getAccountFrom());
    assertEquals("account-to-1", ledgerEntry.getTransactionKey().getAccountTo());
    assertEquals(1.23, ledgerEntry.getAmount(), 0.0);
    assertTrue(ledgerEntry.getTimestamp().getSeconds() > 0);

    testKit.addLedgerItems(addLedgerItemsCommand(epochSubSecond, "transaction-2", 4.56));
    response = testKit.addLedgerItems(addLedgerItemsCommand(epochSubSecond, "transaction-2", 4.56)); // try adding the same transaction again - should be idempotent

    subSecondLedgerItemsAdded = response.getNextEventOfType(SubSecondEntity.SubSecondLedgerItemsAdded.class);
    assertNotNull(subSecondLedgerItemsAdded);

    state = testKit.getState();

    assertEquals(2, state.getLedgerEntriesCount());

    ledgerEntry = state.getLedgerEntries(1);
    assertTrue(ledgerEntry.getEpochSubSecond() > 0);
    assertEquals("transaction-2", ledgerEntry.getTransactionKey().getTransactionId());
    assertEquals("service-code-1", ledgerEntry.getTransactionKey().getServiceCode());
    assertEquals("account-from-1", ledgerEntry.getTransactionKey().getAccountFrom());
    assertEquals("account-to-1", ledgerEntry.getTransactionKey().getAccountTo());
    assertEquals(4.56, ledgerEntry.getAmount(), 0.0);
    assertTrue(ledgerEntry.getTimestamp().getSeconds() > 0);
  }

  @Test
  public void aggregateSubSecondTest() {
    SubSecondTestKit testKit = SubSecondTestKit.of(SubSecond::new);

    var epochSubSecond = TimeTo.fromNow().toEpochSubSecond();

    testKit.addLedgerItems(addLedgerItemsCommand(epochSubSecond, "transaction-1", 1.23, TimeTo.fromEpochSubSecond(epochSubSecond).toTimestamp()));
    testKit.addLedgerItems(addLedgerItemsCommand(epochSubSecond, "transaction-2", 4.56, TimeTo.fromEpochSubSecond(epochSubSecond).plus().nanos(10).toTimestamp()));
    testKit.addLedgerItems(addLedgerItemsCommand(epochSubSecond, "transaction-2", 4.56, TimeTo.fromEpochSubSecond(epochSubSecond).plus().nanos(10).toTimestamp())); // idempotent
    testKit.addLedgerItems(addLedgerItemsCommand(epochSubSecond, "transaction-3", 7.89, TimeTo.fromEpochSubSecond(epochSubSecond).plus().nanos(20).toTimestamp()));

    var response = testKit.aggregateSubSecond(aggregateSubSecondCommand("payment-1", epochSubSecond, TimeTo.fromEpochSubSecond(epochSubSecond).plus().hours(2).toTimestamp()));

    assertEquals(1, response.getAllEvents().size());
    var subSecondAggregated = response.getNextEventOfType(SubSecondEntity.SubSecondAggregated.class);

    assertNotNull(subSecondAggregated);
    assertEquals("merchant-1", subSecondAggregated.getMerchantKey().getMerchantId());
    assertEquals(epochSubSecond, subSecondAggregated.getEpochSubSecond());
    assertEquals(1, subSecondAggregated.getMoneyMovementsCount());
    assertEquals(1.23 + 4.56 + 7.89, subSecondAggregated.getMoneyMovements(0).getAmount(), 0.0);
    assertEquals(TimeTo.fromEpochSubSecond(epochSubSecond).plus().hours(2).toTimestamp(), subSecondAggregated.getAggregateRequestTimestamp());
    assertEquals(TimeTo.fromEpochSubSecond(epochSubSecond).plus().nanos(20).toTimestamp(), subSecondAggregated.getLastUpdateTimestamp());
    assertEquals("payment-1", subSecondAggregated.getPaymentId());

    // when the same aggregate command is received again, all of the processed transactions should be ignored
    response = testKit.aggregateSubSecond(aggregateSubSecondCommand("payment-1", epochSubSecond, TimeTo.fromEpochSubSecond(epochSubSecond).plus().hours(2).toTimestamp()));

    assertEquals(1, response.getAllEvents().size());
    subSecondAggregated = response.getNextEventOfType(SubSecondEntity.SubSecondAggregated.class);

    assertNotNull(subSecondAggregated);
    assertEquals("merchant-1", subSecondAggregated.getMerchantKey().getMerchantId());
    assertEquals(epochSubSecond, subSecondAggregated.getEpochSubSecond());
    assertEquals(0, subSecondAggregated.getMoneyMovementsCount());
    assertEquals(TimeTo.fromEpochSubSecond(epochSubSecond).plus().hours(2).toTimestamp(), subSecondAggregated.getAggregateRequestTimestamp());
    assertEquals(TimeTo.zero(), subSecondAggregated.getLastUpdateTimestamp());
    assertEquals("payment-1", subSecondAggregated.getPaymentId());

    // add more transactions after aggregation
    response = testKit.addLedgerItems(addLedgerItemsCommand(epochSubSecond, "transaction-4", 6.54, TimeTo.fromEpochSubSecond(epochSubSecond).plus().nanos(30).toTimestamp()));

    assertEquals(2, response.getAllEvents().size());
    response.getNextEventOfType(SubSecondEntity.SubSecondActivated.class);
    response.getNextEventOfType(SubSecondEntity.SubSecondLedgerItemsAdded.class);

    testKit.addLedgerItems(addLedgerItemsCommand(epochSubSecond, "transaction-5", 3.21, TimeTo.fromEpochSubSecond(epochSubSecond).plus().nanos(40).toTimestamp()));

    response = testKit.aggregateSubSecond(aggregateSubSecondCommand("payment-2", epochSubSecond, TimeTo.fromEpochSubSecond(epochSubSecond).plus().hours(3).toTimestamp()));

    assertEquals(1, response.getAllEvents().size());
    subSecondAggregated = response.getNextEventOfType(SubSecondEntity.SubSecondAggregated.class);

    assertNotNull(subSecondAggregated);
    assertEquals("merchant-1", subSecondAggregated.getMerchantKey().getMerchantId());
    assertEquals(epochSubSecond, subSecondAggregated.getEpochSubSecond());
    assertEquals(1, subSecondAggregated.getMoneyMovementsCount());
    assertEquals(6.54 + 3.21, subSecondAggregated.getMoneyMovements(0).getAmount(), 0.0);
    assertEquals(TimeTo.fromEpochSubSecond(epochSubSecond).plus().hours(3).toTimestamp(), subSecondAggregated.getAggregateRequestTimestamp());
    assertEquals(TimeTo.fromEpochSubSecond(epochSubSecond).plus().nanos(40).toTimestamp(), subSecondAggregated.getLastUpdateTimestamp());
    assertEquals("payment-2", subSecondAggregated.getPaymentId());
  }

  static SubSecondApi.AddLedgerItemsCommand addLedgerItemsCommand(long epochSubSecond, String transactionId, double amount) {
    return addLedgerItemsCommand(epochSubSecond, transactionId, amount, TimeTo.now());
  }

  static SubSecondApi.AddLedgerItemsCommand addLedgerItemsCommand(long epochSubSecond, String transactionId, double amount, Timestamp timestamp) {
    return SubSecondApi.AddLedgerItemsCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setEpochSubSecond(epochSubSecond)
        .setTransactionId(transactionId)
        .setTimestamp(timestamp)
        .addLedgerItem(SubSecondApi.LedgerItem.newBuilder()
            .setServiceCode("service-code-1")
            .setAmount(amount)
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .build())
        .build();
  }

  static SubSecondApi.AggregateSubSecondCommand aggregateSubSecondCommand(String paymentId, long epochSubSecond, Timestamp timestamp) {
    return SubSecondApi.AggregateSubSecondCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setEpochSubSecond(epochSubSecond)
        .setAggregateRequestTimestamp(timestamp)
        .setPaymentId(paymentId)
        .build();
  }
}

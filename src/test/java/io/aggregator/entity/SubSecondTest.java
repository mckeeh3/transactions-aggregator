package io.aggregator.entity;

import static org.junit.Assert.*;

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
    var response = testKit.addTransaction(
        SubSecondApi.AddTransactionCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochSubSecond(epochSubSecond)
            .setTransactionId("transaction-1")
            .setAmount(1.23)
            .setTimestamp(TimeTo.now())
            .build());

    var subSecondCreated = response.getNextEventOfType(SubSecondEntity.SubSecondCreated.class);
    var transactionAdded = response.getNextEventOfType(SubSecondEntity.SubSecondTransactionAdded.class);

    assertNotNull(subSecondCreated);
    assertNotNull(transactionAdded);

    assertEquals("merchant-1", subSecondCreated.getMerchantId());
    assertTrue(subSecondCreated.getEpochSubSecond() > 0);

    assertEquals("merchant-1", transactionAdded.getMerchantId());
    assertTrue(transactionAdded.getEpochSubSecond() > 0);
    assertEquals("transaction-1", transactionAdded.getTransactionId());
    assertEquals(1.23, transactionAdded.getAmount(), 0.0);
    assertTrue(transactionAdded.getTimestamp().getSeconds() > 0);

    var state = (SubSecondEntity.SubSecondState) response.getUpdatedState();

    assertEquals("merchant-1", state.getMerchantId());
    assertTrue(state.getEpochSubSecond() > 0);
    assertEquals(1, state.getTransactionsList().size());

    var transaction = state.getTransactionsList().get(0);
    assertEquals("merchant-1", transaction.getMerchantId());
    assertTrue(transaction.getEpochSubSecond() > 0);
    assertEquals("transaction-1", transaction.getTransactionId());
    assertEquals(1.23, transaction.getAmount(), 0.0);
    assertTrue(transaction.getTimestamp().getSeconds() > 0);

    testKit.addTransaction(
        SubSecondApi.AddTransactionCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochSubSecond(epochSubSecond)
            .setTransactionId("transaction-2")
            .setAmount(4.56)
            .setTimestamp(TimeTo.now())
            .build());

    response = testKit.addTransaction( // try adding the same transaction again - should be idempotent
        SubSecondApi.AddTransactionCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochSubSecond(epochSubSecond)
            .setTransactionId("transaction-2")
            .setAmount(4.56)
            .setTimestamp(TimeTo.now())
            .build());

    transactionAdded = response.getNextEventOfType(SubSecondEntity.SubSecondTransactionAdded.class);
    assertNotNull(transactionAdded);

    state = (SubSecondEntity.SubSecondState) response.getUpdatedState();

    assertEquals(2, state.getTransactionsList().size());

    transaction = state.getTransactionsList().get(1);
    assertEquals("merchant-1", transaction.getMerchantId());
    assertTrue(transaction.getEpochSubSecond() > 0);
    assertEquals("transaction-2", transaction.getTransactionId());
    assertEquals(4.56, transaction.getAmount(), 0.0);
    assertTrue(transaction.getTimestamp().getSeconds() > 0);
  }

  @Test
  public void aggregateSubSecondTest() {
    SubSecondTestKit testKit = SubSecondTestKit.of(SubSecond::new);

    var epochSubSecond = TimeTo.fromNow().toEpochSubSecond();

    testKit.addTransaction(
        SubSecondApi.AddTransactionCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochSubSecond(epochSubSecond)
            .setTransactionId("transaction-1")
            .setAmount(1.23)
            .setTimestamp(TimeTo.fromEpochSubSecond(epochSubSecond).toTimestamp())
            .build());

    testKit.addTransaction(
        SubSecondApi.AddTransactionCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochSubSecond(epochSubSecond)
            .setTransactionId("transaction-2")
            .setAmount(4.56)
            .setTimestamp(TimeTo.fromEpochSubSecond(epochSubSecond).plus().nanos(10).toTimestamp())
            .build());

    testKit.addTransaction( // try adding the same transaction again - should be idempotent
        SubSecondApi.AddTransactionCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochSubSecond(epochSubSecond)
            .setTransactionId("transaction-2")
            .setAmount(4.56)
            .setTimestamp(TimeTo.fromEpochSubSecond(epochSubSecond).plus().nanos(10).toTimestamp())
            .build());

    testKit.addTransaction(
        SubSecondApi.AddTransactionCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochSubSecond(epochSubSecond)
            .setTransactionId("transaction-3")
            .setAmount(7.89)
            .setTimestamp(TimeTo.fromEpochSubSecond(epochSubSecond).plus().nanos(20).toTimestamp())
            .build());

    var response = testKit.aggregateSubSecond(
        SubSecondApi.AggregateSubSecondCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochSubSecond(epochSubSecond)
            .setAggregateRequestTimestamp(TimeTo.fromEpochSubSecond(epochSubSecond).plus().hours(2).toTimestamp())
            .build());

    var aggregated = response.getNextEventOfType(SubSecondEntity.SubSecondAggregated.class);

    assertNotNull(aggregated);
    assertEquals("merchant-1", aggregated.getMerchantId());
    assertEquals(epochSubSecond, aggregated.getEpochSubSecond());
    assertEquals(1.23 + 4.56 + 7.89, aggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(3, aggregated.getTransactionCount());
    assertEquals(TimeTo.fromEpochSubSecond(epochSubSecond).plus().hours(2).toTimestamp(), aggregated.getAggregateRequestTimestamp());
    assertEquals(TimeTo.fromEpochSubSecond(epochSubSecond).plus().nanos(20).toTimestamp(), aggregated.getLastUpdateTimestamp());
  }
}

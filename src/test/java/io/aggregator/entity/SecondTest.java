package io.aggregator.entity;

import io.aggregator.api.SecondApi;

import org.junit.Test;

import io.aggregator.TimeTo;

import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class SecondTest {

  @Test
  public void exampleTest() {
    // SecondTestKit testKit = SecondTestKit.of(Second::new);
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
    SecondTestKit testKit = SecondTestKit.of(Second::new);

    var epochSecond = TimeTo.epochSecondFor(TimeTo.now());
    var response = testKit.addTransaction(
        SecondApi.AddTransactionCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochSecond(epochSecond)
            .setTransactionId("transaction-1")
            .setAmount(1.23)
            .setTimestamp(TimeTo.now())
            .build());

    var secondCreated = response.getNextEventOfType(SecondEntity.SecondCreated.class);
    var transactionAdded = response.getNextEventOfType(SecondEntity.SecondTransactionAdded.class);

    assertNotNull(secondCreated);
    assertNotNull(transactionAdded);

    assertEquals("merchant-1", secondCreated.getMerchantId());
    assertTrue(secondCreated.getEpochSecond() > 0);

    assertEquals("merchant-1", transactionAdded.getMerchantId());
    assertTrue(transactionAdded.getEpochSecond() > 0);
    assertEquals("transaction-1", transactionAdded.getTransactionId());
    assertEquals(1.23, transactionAdded.getAmount(), 0.0);
    assertTrue(transactionAdded.getTimestamp().getSeconds() > 0);

    var state = (SecondEntity.SecondState) response.getUpdatedState();

    assertEquals("merchant-1", state.getMerchantId());
    assertTrue(state.getEpochSecond() > 0);
    assertEquals(1, state.getTransactionsList().size());

    var transaction = state.getTransactionsList().get(0);
    assertEquals("merchant-1", transaction.getMerchantId());
    assertTrue(transaction.getEpochSecond() > 0);
    assertEquals("transaction-1", transaction.getTransactionId());
    assertEquals(1.23, transaction.getAmount(), 0.0);
    assertTrue(transaction.getTimestamp().getSeconds() > 0);

    testKit.addTransaction(
        SecondApi.AddTransactionCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochSecond(epochSecond)
            .setTransactionId("transaction-2")
            .setAmount(4.56)
            .setTimestamp(TimeTo.now())
            .build());

    response = testKit.addTransaction( // try adding the same transaction again - should be idempotent
        SecondApi.AddTransactionCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochSecond(epochSecond)
            .setTransactionId("transaction-2")
            .setAmount(4.56)
            .setTimestamp(TimeTo.now())
            .build());

    transactionAdded = response.getNextEventOfType(SecondEntity.SecondTransactionAdded.class);
    assertNotNull(transactionAdded);

    state = (SecondEntity.SecondState) response.getUpdatedState();

    assertEquals(2, state.getTransactionsList().size());

    transaction = state.getTransactionsList().get(1);
    assertEquals("merchant-1", transaction.getMerchantId());
    assertTrue(transaction.getEpochSecond() > 0);
    assertEquals("transaction-2", transaction.getTransactionId());
    assertEquals(4.56, transaction.getAmount(), 0.0);
    assertTrue(transaction.getTimestamp().getSeconds() > 0);
  }

  @Test
  public void aggregateTest() {
    SecondTestKit testKit = SecondTestKit.of(Second::new);

    var epochSecond = TimeTo.epochSecondFor(TimeTo.now());

    testKit.addTransaction(
        SecondApi.AddTransactionCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochSecond(epochSecond)
            .setTransactionId("transaction-1")
            .setAmount(1.23)
            .setTimestamp(TimeTo.now())
            .build());

    testKit.addTransaction(
        SecondApi.AddTransactionCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochSecond(epochSecond)
            .setTransactionId("transaction-2")
            .setAmount(4.56)
            .setTimestamp(TimeTo.now())
            .build());

    testKit.addTransaction( // try adding the same transaction again - should be idempotent
        SecondApi.AddTransactionCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochSecond(epochSecond)
            .setTransactionId("transaction-2")
            .setAmount(6.54)
            .setTimestamp(TimeTo.now())
            .build());

    testKit.addTransaction(
        SecondApi.AddTransactionCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochSecond(epochSecond)
            .setTransactionId("transaction-3")
            .setAmount(7.89)
            .setTimestamp(TimeTo.now())
            .build());

    var response = testKit.aggregate(
        SecondApi.AggregateSecondCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochSecond(epochSecond)
            .build());

    var aggregated = response.getNextEventOfType(SecondEntity.SecondAggregated.class);

    assertNotNull(aggregated);
    assertEquals("merchant-1", aggregated.getMerchantId());
    assertEquals(epochSecond, aggregated.getEpochSecond());
    assertEquals(1.23 + 4.56 + 7.89, aggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(3, aggregated.getTransactionCount());
  }
}

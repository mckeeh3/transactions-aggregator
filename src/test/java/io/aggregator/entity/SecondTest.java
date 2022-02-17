package io.aggregator.entity;

import io.aggregator.TimeTo;
import io.aggregator.api.SecondApi;
import org.junit.Test;

import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class SecondTest {

  @Test
  public void exampleTest() {
    // SecondTestKit testKit = SecondTestKit.of(SubSecond::new);
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
  public void addSubSubSecondTest() {
    SecondTestKit testKit = SecondTestKit.of(Second::new);

    var epochSubSecond = TimeTo.fromNow().toEpochSubSecond();
    var nextEpochSubSecond = TimeTo.fromEpochSubSecond(epochSubSecond).plus().seconds(1).toEpochSubSecond();
    var epochSecond = TimeTo.fromEpochSubSecond(epochSubSecond).toEpochSecond();

    var response = testKit.addSubSecond(
        SecondApi.AddSubSecondCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochSecond(epochSecond)
            .setEpochSubSecond(epochSubSecond)
            .build());

    var secondCreated = response.getNextEventOfType(SecondEntity.SecondCreated.class);
    var secondAdded = response.getNextEventOfType(SecondEntity.SubSecondAdded.class);

    assertEquals("merchant-1", secondCreated.getMerchantId());
    assertEquals(epochSecond, secondCreated.getEpochSecond());
    assertEquals("merchant-1", secondAdded.getMerchantId());
    assertEquals(epochSubSecond, secondAdded.getEpochSubSecond());

    var state = testKit.getState();

    assertEquals("merchant-1", state.getMerchantId());
    assertEquals(epochSecond, state.getEpochSecond());
    assertEquals(1, state.getActiveSubSecondsCount());

    var activeSubSecond = state.getActiveSubSeconds(0);

    assertEquals(epochSubSecond, activeSubSecond.getEpochSubSecond());

    response = testKit.addSubSecond(
        SecondApi.AddSubSecondCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochSecond(epochSecond)
            .setEpochSubSecond(nextEpochSubSecond)
            .build());

    secondAdded = response.getNextEventOfType(SecondEntity.SubSecondAdded.class);

    assertEquals("merchant-1", secondAdded.getMerchantId());
    assertEquals(nextEpochSubSecond, secondAdded.getEpochSubSecond());

    state = testKit.getState();

    assertEquals("merchant-1", state.getMerchantId());
    assertEquals(epochSecond, state.getEpochSecond());
    assertEquals(2, state.getActiveSubSecondsCount());

    activeSubSecond = state.getActiveSubSeconds(1);

    assertEquals(nextEpochSubSecond, activeSubSecond.getEpochSubSecond());
  }

  @Test
  public void aggregateSecondTest() {
    SecondTestKit testKit = SecondTestKit.of(Second::new);

    var epochSubSecond = TimeTo.fromNow().toEpochSubSecond();
    var nextEpochSubSecond = TimeTo.fromEpochSubSecond(epochSubSecond).plus().seconds(1).toEpochSubSecond();
    var epochSecond = TimeTo.fromEpochSubSecond(epochSubSecond).toEpochSecond();
    var now = TimeTo.fromEpochSubSecond(epochSubSecond).toTimestamp();

    testKit.addSubSecond(
        SecondApi.AddSubSecondCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochSecond(epochSecond)
            .setEpochSubSecond(epochSubSecond)
            .build());

    testKit.addSubSecond(
        SecondApi.AddSubSecondCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochSecond(epochSecond)
            .setEpochSubSecond(nextEpochSubSecond)
            .build());

    var response = testKit.aggregateSecond(
        SecondApi.AggregateSecondCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochSecond(epochSecond)
            .setAggregateRequestTimestamp(now)
            .build());

    var secondAggregationRequested = response.getNextEventOfType(SecondEntity.SecondAggregationRequested.class);

    assertEquals("merchant-1", secondAggregationRequested.getMerchantId());
    assertEquals(epochSecond, secondAggregationRequested.getEpochSecond());
    assertEquals(now, secondAggregationRequested.getAggregateRequestTimestamp());
    assertEquals(2, secondAggregationRequested.getEpochSubSecondsCount());
    assertEquals(epochSubSecond, secondAggregationRequested.getEpochSubSeconds(0));
    assertEquals(nextEpochSubSecond, secondAggregationRequested.getEpochSubSeconds(1));

    var state = testKit.getState();

    assertEquals(now, state.getAggregateRequestTimestamp());
  }

  @Test
  public void subSubSecondAggregationTest() {
    SecondTestKit testKit = SecondTestKit.of(Second::new);

    var epochSubSecond = TimeTo.fromNow().toEpochSubSecond();
    var nextEpochSubSecond = TimeTo.fromEpochSubSecond(epochSubSecond).plus().seconds(1).toEpochSubSecond();
    var epochSecond = TimeTo.fromEpochSubSecond(epochSubSecond).toEpochSecond();
    var now = TimeTo.fromEpochSubSecond(epochSubSecond).toTimestamp();

    testKit.addSubSecond(
        SecondApi.AddSubSecondCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochSecond(epochSecond)
            .setEpochSubSecond(epochSubSecond)
            .build());

    testKit.addSubSecond(
        SecondApi.AddSubSecondCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochSecond(epochSecond)
            .setEpochSubSecond(nextEpochSubSecond)
            .build());

    testKit.aggregateSecond(
        SecondApi.AggregateSecondCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochSecond(epochSecond)
            .setAggregateRequestTimestamp(now)
            .build());

    var response = testKit.subSecondAggregation(
        SecondApi.SubSecondAggregationCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochSecond(epochSecond)
            .setEpochSubSecond(epochSubSecond)
            .setTransactionTotalAmount(123.45)
            .setTransactionCount(10)
            .setLastUpdateTimestamp(now)
            .setAggregateRequestTimestamp(now)
            .build());

    var activeSubSecondAggregated = response.getNextEventOfType(SecondEntity.ActiveSubSecondAggregated.class);

    assertEquals("merchant-1", activeSubSecondAggregated.getMerchantId());
    assertEquals(epochSubSecond, activeSubSecondAggregated.getEpochSubSecond());
    assertEquals(123.45, activeSubSecondAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(10, activeSubSecondAggregated.getTransactionCount());
    assertEquals(now, activeSubSecondAggregated.getLastUpdateTimestamp());
    assertEquals(now, activeSubSecondAggregated.getAggregateRequestTimestamp());

    response = testKit.subSecondAggregation(
        SecondApi.SubSecondAggregationCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochSecond(epochSecond)
            .setEpochSubSecond(nextEpochSubSecond)
            .setTransactionTotalAmount(678.90)
            .setTransactionCount(20)
            .setLastUpdateTimestamp(now)
            .setAggregateRequestTimestamp(now)
            .build());

    var secondAggregated = response.getNextEventOfType(SecondEntity.SecondAggregated.class);
    activeSubSecondAggregated = response.getNextEventOfType(SecondEntity.ActiveSubSecondAggregated.class);

    assertEquals("merchant-1", activeSubSecondAggregated.getMerchantId());
    assertEquals(nextEpochSubSecond, activeSubSecondAggregated.getEpochSubSecond());
    assertEquals(678.90, activeSubSecondAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(20, activeSubSecondAggregated.getTransactionCount());
    assertEquals(now, activeSubSecondAggregated.getLastUpdateTimestamp());
    assertEquals(now, activeSubSecondAggregated.getAggregateRequestTimestamp());

    assertEquals("merchant-1", secondAggregated.getMerchantId());
    assertEquals(epochSecond, secondAggregated.getEpochSecond());
    assertEquals(123.45 + 678.90, secondAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(10 + 20, secondAggregated.getTransactionCount());
    assertEquals(now, secondAggregated.getLastUpdateTimestamp());
    assertEquals(now, secondAggregated.getAggregateRequestTimestamp());
  }
}

package io.aggregator.entity;

import io.aggregator.TimeTo;
import io.aggregator.api.MinuteApi;

import org.junit.Test;

import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class MinuteTest {

  @Test
  public void exampleTest() {
    // MinuteTestKit testKit = MinuteTestKit.of(Minute::new);
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
  public void addSecondTest() {
    MinuteTestKit testKit = MinuteTestKit.of(Minute::new);

    var now = TimeTo.now();
    var epochSecond = TimeTo.fromTimestamp(now).toEpochSecond();
    var epochMinute = TimeTo.fromTimestamp(now).toEpochMinute();

    var response = testKit.addSecond(
        MinuteApi.AddSecondCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochMinute(epochMinute)
            .setEpochSecond(epochSecond)
            .build());

    var minuteCreated = response.getNextEventOfType(MinuteEntity.MinuteCreated.class);
    var secondAdded = response.getNextEventOfType(MinuteEntity.SecondAdded.class);

    assertEquals("merchant-1", minuteCreated.getMerchantId());
    assertEquals(epochMinute, minuteCreated.getEpochMinute());
    assertEquals("merchant-1", secondAdded.getMerchantId());
    assertEquals(epochSecond, secondAdded.getEpochSecond());

    var state = testKit.getState();

    assertEquals("merchant-1", state.getMerchantId());
    assertEquals(epochMinute, state.getEpochMinute());
    assertEquals(1, state.getActiveSecondsCount());

    var activeSecond = state.getActiveSeconds(0);

    assertEquals(epochSecond, activeSecond.getEpochSecond());

    var nextEpochSecond = TimeTo.fromEpochSecond(epochSecond + 1).toEpochMinute() == epochMinute ? epochSecond + 1 : epochSecond - 1;

    response = testKit.addSecond(
        MinuteApi.AddSecondCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochMinute(epochMinute)
            .setEpochSecond(nextEpochSecond)
            .build());

    secondAdded = response.getNextEventOfType(MinuteEntity.SecondAdded.class);

    assertEquals("merchant-1", secondAdded.getMerchantId());
    assertEquals(nextEpochSecond, secondAdded.getEpochSecond());

    state = testKit.getState();

    assertEquals("merchant-1", state.getMerchantId());
    assertEquals(epochMinute, state.getEpochMinute());
    assertEquals(2, state.getActiveSecondsCount());

    activeSecond = state.getActiveSeconds(1);

    assertEquals(nextEpochSecond, activeSecond.getEpochSecond());
  }

  @Test
  public void aggregateMinuteTest() {
    MinuteTestKit testKit = MinuteTestKit.of(Minute::new);

    var now = TimeTo.now();
    var epochSecond = TimeTo.fromTimestamp(now).toEpochSecond();
    var epochMinute = TimeTo.fromTimestamp(now).toEpochMinute();

    testKit.addSecond(
        MinuteApi.AddSecondCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochMinute(epochMinute)
            .setEpochSecond(epochSecond)
            .build());

    var nextEpochSecond = TimeTo.fromEpochSecond(epochSecond + 1).toEpochMinute() == epochMinute ? epochSecond + 1 : epochSecond - 1;

    testKit.addSecond(
        MinuteApi.AddSecondCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochMinute(epochMinute)
            .setEpochSecond(nextEpochSecond)
            .build());

    var response = testKit.aggregateMinute(
        MinuteApi.AggregateMinuteCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochMinute(epochMinute)
            .setAggregateRequestTimestamp(now)
            .build());

    var minuteAggregationRequested = response.getNextEventOfType(MinuteEntity.MinuteAggregationRequested.class);

    assertEquals("merchant-1", minuteAggregationRequested.getMerchantId());
    assertEquals(epochMinute, minuteAggregationRequested.getEpochMinute());
    assertEquals(now, minuteAggregationRequested.getAggregateRequestTimestamp());
    assertEquals(2, minuteAggregationRequested.getEpochSecondsCount());
    assertEquals(epochSecond, minuteAggregationRequested.getEpochSeconds(0));
    assertEquals(nextEpochSecond, minuteAggregationRequested.getEpochSeconds(1));

    var state = testKit.getState();

    assertEquals(now, state.getAggregateRequestTimestamp());
  }

  @Test
  public void secondAggregationTest() {
    MinuteTestKit testKit = MinuteTestKit.of(Minute::new);

    var now = TimeTo.now();
    var epochSecond = TimeTo.fromTimestamp(now).toEpochSecond();
    var epochMinute = TimeTo.fromTimestamp(now).toEpochMinute();

    testKit.addSecond(
        MinuteApi.AddSecondCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochMinute(epochMinute)
            .setEpochSecond(epochSecond)
            .build());

    var nextEpochSecond = TimeTo.fromEpochSecond(epochSecond + 1).toEpochMinute() == epochMinute ? epochSecond + 1 : epochSecond - 1;

    testKit.addSecond(
        MinuteApi.AddSecondCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochMinute(epochMinute)
            .setEpochSecond(nextEpochSecond)
            .build());

    testKit.aggregateMinute(
        MinuteApi.AggregateMinuteCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochMinute(epochMinute)
            .setAggregateRequestTimestamp(now)
            .build());

    var response = testKit.secondAggregation(
        MinuteApi.SecondAggregationCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochMinute(epochMinute)
            .setEpochSecond(epochSecond)
            .setTransactionTotalAmount(123.45)
            .setTransactionCount(10)
            .setLastUpdateTimestamp(now)
            .setAggregateRequestTimestamp(now)
            .build());

    var activeSecondAggregated = response.getNextEventOfType(MinuteEntity.ActiveSecondAggregated.class);

    assertEquals("merchant-1", activeSecondAggregated.getMerchantId());
    assertEquals(epochSecond, activeSecondAggregated.getEpochSecond());
    assertEquals(123.45, activeSecondAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(10, activeSecondAggregated.getTransactionCount());
    assertEquals(now, activeSecondAggregated.getLastUpdateTimestamp());
    assertEquals(now, activeSecondAggregated.getAggregateRequestTimestamp());

    response = testKit.secondAggregation(
        MinuteApi.SecondAggregationCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochMinute(epochMinute)
            .setEpochSecond(nextEpochSecond)
            .setTransactionTotalAmount(678.90)
            .setTransactionCount(20)
            .setLastUpdateTimestamp(now)
            .setAggregateRequestTimestamp(now)
            .build());

    var minuteAggregated = response.getNextEventOfType(MinuteEntity.MinuteAggregated.class);
    activeSecondAggregated = response.getNextEventOfType(MinuteEntity.ActiveSecondAggregated.class);

    assertEquals("merchant-1", activeSecondAggregated.getMerchantId());
    assertEquals(nextEpochSecond, activeSecondAggregated.getEpochSecond());
    assertEquals(678.90, activeSecondAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(20, activeSecondAggregated.getTransactionCount());
    assertEquals(now, activeSecondAggregated.getLastUpdateTimestamp());
    assertEquals(now, activeSecondAggregated.getAggregateRequestTimestamp());

    assertEquals("merchant-1", minuteAggregated.getMerchantId());
    assertEquals(epochMinute, minuteAggregated.getEpochMinute());
    assertEquals(123.45 + 678.90, minuteAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(10 + 20, minuteAggregated.getTransactionCount());
    assertEquals(now, minuteAggregated.getLastUpdateTimestamp());
    assertEquals(now, minuteAggregated.getAggregateRequestTimestamp());
  }
}

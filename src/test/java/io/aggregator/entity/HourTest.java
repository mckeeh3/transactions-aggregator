package io.aggregator.entity;

import io.aggregator.TimeTo;
import io.aggregator.api.HourApi;

import org.junit.Test;

import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class HourTest {

  @Test
  public void exampleTest() {
    // HourTestKit testKit = HourTestKit.of(Hour::new);
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
  public void addMinuteTest() {
    HourTestKit testKit = HourTestKit.of(Hour::new);

    var epochHour = TimeTo.fromNow().toEpochHour();
    var epochMinute = TimeTo.fromEpochHour(epochHour).toEpochMinute();
    var nextEpochMinute = TimeTo.fromEpochMinute(epochMinute).plus().minutes(1).toEpochMinute();

    var response = testKit.addMinute(
        HourApi.AddMinuteCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochHour(epochHour)
            .setEpochMinute(epochMinute)
            .build());

    var hourCreated = response.getNextEventOfType(HourEntity.HourCreated.class);
    var minuteAdded = response.getNextEventOfType(HourEntity.MinuteAdded.class);

    assertEquals("merchant-1", hourCreated.getMerchantId());
    assertEquals(epochHour, hourCreated.getEpochHour());
    assertEquals("merchant-1", minuteAdded.getMerchantId());
    assertEquals(epochMinute, minuteAdded.getEpochMinute());

    var state = testKit.getState();

    assertEquals("merchant-1", state.getMerchantId());
    assertEquals(epochHour, state.getEpochHour());
    assertEquals(1, state.getActiveMinutesCount());

    var activeMinute = state.getActiveMinutes(0);

    assertEquals(epochMinute, activeMinute.getEpochMinute());

    response = testKit.addMinute(
        HourApi.AddMinuteCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochHour(epochHour)
            .setEpochMinute(nextEpochMinute)
            .build());

    minuteAdded = response.getNextEventOfType(HourEntity.MinuteAdded.class);

    assertEquals("merchant-1", minuteAdded.getMerchantId());
    assertEquals(nextEpochMinute, minuteAdded.getEpochMinute());

    state = testKit.getState();

    assertEquals("merchant-1", state.getMerchantId());
    assertEquals(epochHour, state.getEpochHour());
    assertEquals(2, state.getActiveMinutesCount());

    activeMinute = state.getActiveMinutes(1);

    assertEquals(nextEpochMinute, activeMinute.getEpochMinute());
  }

  @Test
  public void aggregateHourTest() {
    HourTestKit testKit = HourTestKit.of(Hour::new);

    var epochHour = TimeTo.fromNow().toEpochHour();
    var epochMinute = TimeTo.fromEpochHour(epochHour).toEpochMinute();
    var nextEpochMinute = TimeTo.fromEpochMinute(epochMinute).plus().minutes(1).toEpochMinute();
    var now = TimeTo.fromEpochHour(epochHour).toTimestamp();

    testKit.addMinute(
        HourApi.AddMinuteCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochHour(epochHour)
            .setEpochMinute(epochMinute)
            .build());

    testKit.addMinute(
        HourApi.AddMinuteCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochHour(epochHour)
            .setEpochMinute(nextEpochMinute)
            .build());

    var response = testKit.aggregateHour(
        HourApi.AggregateHourCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochHour(epochHour)
            .setAggregateRequestTimestamp(now)
            .build());

    var hourAggregationRequested = response.getNextEventOfType(HourEntity.HourAggregationRequested.class);

    assertEquals("merchant-1", hourAggregationRequested.getMerchantId());
    assertEquals(epochHour, hourAggregationRequested.getEpochHour());
    assertEquals(now, hourAggregationRequested.getAggregateRequestTimestamp());
    assertEquals(2, hourAggregationRequested.getEpochMinutesCount());
    assertEquals(epochMinute, hourAggregationRequested.getEpochMinutes(0));
    assertEquals(nextEpochMinute, hourAggregationRequested.getEpochMinutes(1));

    var state = testKit.getState();

    assertEquals(now, state.getAggregateRequestTimestamp());
  }

  @Test
  public void minuteAggregationTest() {
    HourTestKit testKit = HourTestKit.of(Hour::new);

    var epochHour = TimeTo.fromNow().toEpochHour();
    var epochMinute = TimeTo.fromEpochHour(epochHour).toEpochMinute();
    var nextEpochMinute = TimeTo.fromEpochMinute(epochMinute).plus().minutes(1).toEpochMinute();
    var now = TimeTo.fromEpochHour(epochHour).toTimestamp();

    testKit.addMinute(
        HourApi.AddMinuteCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochHour(epochHour)
            .setEpochMinute(epochMinute)
            .build());

    testKit.addMinute(
        HourApi.AddMinuteCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochHour(epochHour)
            .setEpochMinute(nextEpochMinute)
            .build());

    testKit.aggregateHour(
        HourApi.AggregateHourCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochHour(epochHour)
            .setAggregateRequestTimestamp(now)
            .build());

    var response = testKit.minuteAggregation(
        HourApi.MinuteAggregationCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochHour(epochHour)
            .setEpochMinute(epochMinute)
            .setTransactionTotalAmount(123.45)
            .setTransactionCount(10)
            .setLastUpdateTimestamp(now)
            .setAggregateRequestTimestamp(now)
            .build());

    var activeMinuteAggregated = response.getNextEventOfType(HourEntity.ActiveMinuteAggregated.class);

    assertEquals("merchant-1", activeMinuteAggregated.getMerchantId());
    assertEquals(epochMinute, activeMinuteAggregated.getEpochMinute());
    assertEquals(123.45, activeMinuteAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(10, activeMinuteAggregated.getTransactionCount());
    assertEquals(now, activeMinuteAggregated.getLastUpdateTimestamp());
    assertEquals(now, activeMinuteAggregated.getAggregateRequestTimestamp());

    response = testKit.minuteAggregation(
        HourApi.MinuteAggregationCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochHour(epochHour)
            .setEpochMinute(nextEpochMinute)
            .setTransactionTotalAmount(678.90)
            .setTransactionCount(20)
            .setLastUpdateTimestamp(now)
            .setAggregateRequestTimestamp(now)
            .build());

    var hourAggregated = response.getNextEventOfType(HourEntity.HourAggregated.class);
    activeMinuteAggregated = response.getNextEventOfType(HourEntity.ActiveMinuteAggregated.class);

    assertEquals("merchant-1", activeMinuteAggregated.getMerchantId());
    assertEquals(nextEpochMinute, activeMinuteAggregated.getEpochMinute());
    assertEquals(678.90, activeMinuteAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(20, activeMinuteAggregated.getTransactionCount());
    assertEquals(now, activeMinuteAggregated.getLastUpdateTimestamp());
    assertEquals(now, activeMinuteAggregated.getAggregateRequestTimestamp());

    assertEquals("merchant-1", hourAggregated.getMerchantId());
    assertEquals(epochHour, hourAggregated.getEpochHour());
    assertEquals(123.45 + 678.90, hourAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(10 + 20, hourAggregated.getTransactionCount());
    assertEquals(now, hourAggregated.getLastUpdateTimestamp());
    assertEquals(now, hourAggregated.getAggregateRequestTimestamp());
  }
}

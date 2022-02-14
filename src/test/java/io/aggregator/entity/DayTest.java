package io.aggregator.entity;

import io.aggregator.TimeTo;
import io.aggregator.api.DayApi;

import org.junit.Test;

import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class DayTest {

  @Test
  public void exampleTest() {
    // DayTestKit testKit = DayTestKit.of(Day::new);
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
  public void addHourTest() {
    DayTestKit testKit = DayTestKit.of(Day::new);

    var now = TimeTo.now();
    var epochHour = TimeTo.epochHourFor(now);
    var epochDay = TimeTo.epochDayFor(now);

    var response = testKit.addHour(
        DayApi.AddHourCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochDay(epochDay)
            .setEpochHour(epochHour)
            .build());

    var dayCreated = response.getNextEventOfType(DayEntity.DayCreated.class);
    var hourAdded = response.getNextEventOfType(DayEntity.HourAdded.class);

    assertEquals("merchant-1", dayCreated.getMerchantId());
    assertEquals(epochDay, dayCreated.getEpochDay());
    assertEquals("merchant-1", hourAdded.getMerchantId());
    assertEquals(epochHour, hourAdded.getEpochHour());

    var state = testKit.getState();

    assertEquals("merchant-1", state.getMerchantId());
    assertEquals(epochDay, state.getEpochDay());
    assertEquals(1, state.getActiveHoursCount());

    var activeHour = state.getActiveHours(0);

    assertEquals(epochHour, activeHour.getEpochHour());

    var nextEpochHour = TimeTo.epochDayFor(epochHour + 1) == epochDay
                                                                      ? TimeTo.epochHourFor(epochHour + 1)
                                                                      : TimeTo.epochHourFor(epochHour - 1);

    response = testKit.addHour(
        DayApi.AddHourCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochDay(epochDay)
            .setEpochHour(nextEpochHour)
            .build());

    hourAdded = response.getNextEventOfType(DayEntity.HourAdded.class);

    assertEquals("merchant-1", hourAdded.getMerchantId());
    assertEquals(nextEpochHour, hourAdded.getEpochHour());

    state = testKit.getState();

    assertEquals("merchant-1", state.getMerchantId());
    assertEquals(epochDay, state.getEpochDay());
    assertEquals(2, state.getActiveHoursCount());

    activeHour = state.getActiveHours(1);

    assertEquals(nextEpochHour, activeHour.getEpochHour());
  }

  @Test
  public void aggregateDayTest() {
    DayTestKit testKit = DayTestKit.of(Day::new);

    var now = TimeTo.now();
    var epochHour = TimeTo.epochHourFor(now);
    var epochDay = TimeTo.epochDayFor(now);

    testKit.addHour(
        DayApi.AddHourCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochDay(epochDay)
            .setEpochHour(epochHour)
            .build());

    var nextEpochHour = TimeTo.epochDayFor(epochHour + 1) == epochDay
                                                                      ? TimeTo.epochHourFor(epochHour + 1)
                                                                      : TimeTo.epochHourFor(epochHour - 1);

    testKit.addHour(
        DayApi.AddHourCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochDay(epochDay)
            .setEpochHour(nextEpochHour)
            .build());

    var response = testKit.aggregateDay(
        DayApi.AggregateDayCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochDay(epochDay)
            .setAggregateRequestTimestamp(now)
            .build());

    var dayAggregationRequested = response.getNextEventOfType(DayEntity.DayAggregationRequested.class);

    assertEquals("merchant-1", dayAggregationRequested.getMerchantId());
    assertEquals(epochDay, dayAggregationRequested.getEpochDay());
    assertEquals(now, dayAggregationRequested.getAggregateRequestTimestamp());
    assertEquals(2, dayAggregationRequested.getEpochHoursCount());
    assertEquals(epochHour, dayAggregationRequested.getEpochHours(0));
    assertEquals(nextEpochHour, dayAggregationRequested.getEpochHours(1));

    var state = testKit.getState();

    assertEquals(now, state.getAggregateRequestTimestamp());
  }

  @Test
  public void hourAggregationTest() {
    DayTestKit testKit = DayTestKit.of(Day::new);

    var now = TimeTo.now();
    var epochHour = TimeTo.epochHourFor(now);
    var epochDay = TimeTo.epochDayFor(now);

    testKit.addHour(
        DayApi.AddHourCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochDay(epochDay)
            .setEpochHour(epochHour)
            .build());

    var nextEpochHour = TimeTo.epochDayFor(epochHour + 1) == epochDay
                                                                      ? TimeTo.epochHourFor(epochHour + 1)
                                                                      : TimeTo.epochHourFor(epochHour - 1);

    testKit.addHour(
        DayApi.AddHourCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochDay(epochDay)
            .setEpochHour(nextEpochHour)
            .build());

    testKit.aggregateDay(
        DayApi.AggregateDayCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochDay(epochDay)
            .setAggregateRequestTimestamp(now)
            .build());

    var response = testKit.hourAggregation(
        DayApi.HourAggregationCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochDay(epochDay)
            .setEpochHour(epochHour)
            .setTransactionTotalAmount(123.45)
            .setTransactionCount(10)
            .setLastUpdateTimestamp(now)
            .setAggregateRequestTimestamp(now)
            .build());

    var activeHourAggregated = response.getNextEventOfType(DayEntity.ActiveHourAggregated.class);

    assertEquals("merchant-1", activeHourAggregated.getMerchantId());
    assertEquals(epochHour, activeHourAggregated.getEpochHour());
    assertEquals(123.45, activeHourAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(10, activeHourAggregated.getTransactionCount());
    assertEquals(now, activeHourAggregated.getLastUpdateTimestamp());
    assertEquals(now, activeHourAggregated.getAggregateRequestTimestamp());

    response = testKit.hourAggregation(
        DayApi.HourAggregationCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setEpochDay(epochDay)
            .setEpochHour(nextEpochHour)
            .setTransactionTotalAmount(678.90)
            .setTransactionCount(20)
            .setLastUpdateTimestamp(now)
            .setAggregateRequestTimestamp(now)
            .build());

    var dayAggregated = response.getNextEventOfType(DayEntity.DayAggregated.class);
    activeHourAggregated = response.getNextEventOfType(DayEntity.ActiveHourAggregated.class);

    assertEquals("merchant-1", activeHourAggregated.getMerchantId());
    assertEquals(nextEpochHour, activeHourAggregated.getEpochHour());
    assertEquals(678.90, activeHourAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(20, activeHourAggregated.getTransactionCount());
    assertEquals(now, activeHourAggregated.getLastUpdateTimestamp());
    assertEquals(now, activeHourAggregated.getAggregateRequestTimestamp());

    assertEquals("merchant-1", dayAggregated.getMerchantId());
    assertEquals(epochDay, dayAggregated.getEpochDay());
    assertEquals(123.45 + 678.90, dayAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(10 + 20, dayAggregated.getTransactionCount());
    assertEquals(now, dayAggregated.getLastUpdateTimestamp());
    assertEquals(now, dayAggregated.getAggregateRequestTimestamp());
  }
}

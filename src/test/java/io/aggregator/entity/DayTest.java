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
  public void activateHourTest() {
    DayTestKit testKit = DayTestKit.of(Day::new);

    var epochDay = TimeTo.fromNow().toEpochDay();
    var epochHour = TimeTo.fromEpochDay(epochDay).toEpochHour();
    var nextEpochHour = TimeTo.fromEpochHour(epochHour).plus().hours(1).toEpochHour();

    var response = testKit.activateHour(
        DayApi.ActivateHourCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay)
            .setEpochHour(epochHour)
            .build());

    var dayActivated = response.getNextEventOfType(DayEntity.DayActivated.class);
    var hourActivated = response.getNextEventOfType(DayEntity.HourActivated.class);

    assertEquals("merchant-1", dayActivated.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", dayActivated.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", dayActivated.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", dayActivated.getMerchantKey().getAccountTo());
    assertEquals(epochDay, dayActivated.getEpochDay());

    assertEquals("merchant-1", hourActivated.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", hourActivated.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", hourActivated.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", hourActivated.getMerchantKey().getAccountTo());
    assertEquals(epochHour, hourActivated.getEpochHour());

    var state = testKit.getState();

    assertEquals("merchant-1", state.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", state.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", state.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", state.getMerchantKey().getAccountTo());
    assertEquals(epochDay, state.getEpochDay());
    assertEquals(1, state.getActiveHoursCount());

    var activeHour = state.getActiveHours(0);

    assertEquals(epochHour, activeHour.getEpochHour());

    response = testKit.activateHour(
        DayApi.ActivateHourCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay)
            .setEpochHour(nextEpochHour)
            .build());

    hourActivated = response.getNextEventOfType(DayEntity.HourActivated.class);

    assertEquals("merchant-1", hourActivated.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", hourActivated.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", hourActivated.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", hourActivated.getMerchantKey().getAccountTo());
    assertEquals(nextEpochHour, hourActivated.getEpochHour());

    state = testKit.getState();

    assertEquals("merchant-1", state.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", state.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", state.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", state.getMerchantKey().getAccountTo());
    assertEquals(epochDay, state.getEpochDay());
    assertEquals(2, state.getActiveHoursCount());

    activeHour = state.getActiveHours(1);

    assertEquals(nextEpochHour, activeHour.getEpochHour());
  }

  @Test
  public void aggregateDayTest() {
    DayTestKit testKit = DayTestKit.of(Day::new);

    var epochDay = TimeTo.fromNow().toEpochDay();
    var epochHour = TimeTo.fromEpochDay(epochDay).toEpochHour();
    var nextEpochHour = TimeTo.fromEpochHour(epochHour).plus().hours(1).toEpochHour();
    var now = TimeTo.fromEpochDay(epochDay).toTimestamp();

    testKit.activateHour(
        DayApi.ActivateHourCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay)
            .setEpochHour(epochHour)
            .build());

    testKit.activateHour(
        DayApi.ActivateHourCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay)
            .setEpochHour(nextEpochHour)
            .build());

    var response = testKit.aggregateDay(
        DayApi.AggregateDayCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay)
            .setAggregateRequestTimestamp(now)
            .setPaymentId("payment-1")
            .build());

    var dayAggregationRequested = response.getNextEventOfType(DayEntity.DayAggregationRequested.class);

    assertEquals("merchant-1", dayAggregationRequested.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", dayAggregationRequested.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", dayAggregationRequested.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", dayAggregationRequested.getMerchantKey().getAccountTo());
    assertEquals(epochDay, dayAggregationRequested.getEpochDay());
    assertEquals(now, dayAggregationRequested.getAggregateRequestTimestamp());
    assertEquals(2, dayAggregationRequested.getEpochHoursCount());
    assertEquals(epochHour, dayAggregationRequested.getEpochHours(0));
    assertEquals(nextEpochHour, dayAggregationRequested.getEpochHours(1));
    assertEquals("payment-1", dayAggregationRequested.getPaymentId());

    var state = testKit.getState();

    var aggregateDay = state.getAggregateDaysList().stream()
        .filter(aggDay -> aggDay.getAggregateRequestTimestamp().equals(now))
        .findFirst();
    assertTrue(aggregateDay.isPresent());
    assertEquals(now, aggregateDay.get().getAggregateRequestTimestamp());
  }

  @Test
  public void aggregateDayWithNoHoursTest() {
    DayTestKit testKit = DayTestKit.of(Day::new);

    var epochDay = TimeTo.fromNow().toEpochDay();
    var now = TimeTo.fromEpochDay(epochDay).toTimestamp();

    var response = testKit.aggregateDay(
        DayApi.AggregateDayCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay)
            .setAggregateRequestTimestamp(now)
            .build());

    var dayAggregated = response.getNextEventOfType(DayEntity.DayAggregated.class);

    assertEquals("merchant-1", dayAggregated.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", dayAggregated.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", dayAggregated.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", dayAggregated.getMerchantKey().getAccountTo());
    assertEquals(epochDay, dayAggregated.getEpochDay());
    assertEquals(now, dayAggregated.getLastUpdateTimestamp());
    assertEquals(now, dayAggregated.getAggregateRequestTimestamp());
    assertEquals(now, dayAggregated.getAggregationCompletedTimestamp());
    assertEquals(0, dayAggregated.getTransactionCount());
    assertEquals(0.0, dayAggregated.getTransactionTotalAmount(), 0.0);
  }

  @Test
  public void hourAggregationTest() {
    DayTestKit testKit = DayTestKit.of(Day::new);

    var epochDay = TimeTo.fromNow().toEpochDay();
    var epochHour = TimeTo.fromEpochDay(epochDay).toEpochHour();
    var nextEpochHour = TimeTo.fromEpochHour(epochHour).plus().hours(1).toEpochHour();
    var aggregateRequestTimestamp = TimeTo.fromEpochDay(epochDay).toTimestamp();

    testKit.activateHour(
        DayApi.ActivateHourCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay)
            .setEpochHour(epochHour)
            .build());

    testKit.activateHour(
        DayApi.ActivateHourCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay)
            .setEpochHour(nextEpochHour)
            .build());

    testKit.aggregateDay(
        DayApi.AggregateDayCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay)
            .setAggregateRequestTimestamp(aggregateRequestTimestamp)
            .setPaymentId("payment-1")
            .build());

    var response = testKit.hourAggregation(
        DayApi.HourAggregationCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay)
            .setEpochHour(epochHour)
            .setTransactionTotalAmount(123.45)
            .setTransactionCount(10)
            .setLastUpdateTimestamp(aggregateRequestTimestamp)
            .setAggregateRequestTimestamp(aggregateRequestTimestamp)
            .setPaymentId("payment-1")
            .build());

    var activeHourAggregated = response.getNextEventOfType(DayEntity.ActiveHourAggregated.class);

    assertEquals("merchant-1", activeHourAggregated.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", activeHourAggregated.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", activeHourAggregated.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", activeHourAggregated.getMerchantKey().getAccountTo());
    assertEquals(epochHour, activeHourAggregated.getEpochHour());
    assertEquals(123.45, activeHourAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(10, activeHourAggregated.getTransactionCount());
    assertEquals(aggregateRequestTimestamp, activeHourAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, activeHourAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeHourAggregated.getPaymentId());

    response = testKit.hourAggregation(
        DayApi.HourAggregationCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay)
            .setEpochHour(nextEpochHour)
            .setTransactionTotalAmount(678.90)
            .setTransactionCount(20)
            .setLastUpdateTimestamp(aggregateRequestTimestamp)
            .setAggregateRequestTimestamp(aggregateRequestTimestamp)
            .setPaymentId("payment-1")
            .build());

    var dayAggregated = response.getNextEventOfType(DayEntity.DayAggregated.class);
    activeHourAggregated = response.getNextEventOfType(DayEntity.ActiveHourAggregated.class);

    assertEquals("merchant-1", activeHourAggregated.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", activeHourAggregated.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", activeHourAggregated.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", activeHourAggregated.getMerchantKey().getAccountTo());
    assertEquals(nextEpochHour, activeHourAggregated.getEpochHour());
    assertEquals(678.90, activeHourAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(20, activeHourAggregated.getTransactionCount());
    assertEquals(aggregateRequestTimestamp, activeHourAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, activeHourAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeHourAggregated.getPaymentId());

    assertEquals("merchant-1", dayAggregated.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", dayAggregated.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", dayAggregated.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", dayAggregated.getMerchantKey().getAccountTo());
    assertEquals(epochDay, dayAggregated.getEpochDay());
    assertEquals(123.45 + 678.90, dayAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(10 + 20, dayAggregated.getTransactionCount());
    assertEquals(aggregateRequestTimestamp, dayAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, dayAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", dayAggregated.getPaymentId());

    // this sequence re-activates the day and hour aggregation
    aggregateRequestTimestamp = TimeTo.fromEpochDay(epochDay).plus().minutes(1).toTimestamp();

    response = testKit.activateHour(
        DayApi.ActivateHourCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay)
            .setEpochHour(epochHour)
            .build());

    response.getNextEventOfType(DayEntity.DayActivated.class);
    response.getNextEventOfType(DayEntity.HourActivated.class);

    testKit.aggregateDay(
        DayApi.AggregateDayCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay)
            .setAggregateRequestTimestamp(aggregateRequestTimestamp)
            .setPaymentId("payment-1")
            .build());

    response = testKit.hourAggregation(
        DayApi.HourAggregationCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay)
            .setEpochHour(epochHour)
            .setTransactionTotalAmount(543.21)
            .setTransactionCount(321)
            .setLastUpdateTimestamp(aggregateRequestTimestamp)
            .setAggregateRequestTimestamp(aggregateRequestTimestamp)
            .setPaymentId("payment-1")
            .build());

    dayAggregated = response.getNextEventOfType(DayEntity.DayAggregated.class);
    activeHourAggregated = response.getNextEventOfType(DayEntity.ActiveHourAggregated.class);

    assertEquals("merchant-1", activeHourAggregated.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", activeHourAggregated.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", activeHourAggregated.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", activeHourAggregated.getMerchantKey().getAccountTo());
    assertEquals(epochHour, activeHourAggregated.getEpochHour());
    assertEquals(543.21, activeHourAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(321, activeHourAggregated.getTransactionCount());
    assertEquals(aggregateRequestTimestamp, activeHourAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, activeHourAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeHourAggregated.getPaymentId());

    assertEquals("merchant-1", dayAggregated.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", dayAggregated.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", dayAggregated.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", dayAggregated.getMerchantKey().getAccountTo());
    assertEquals(epochDay, dayAggregated.getEpochDay());
    assertEquals(543.21, dayAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(321, dayAggregated.getTransactionCount());
    assertEquals(aggregateRequestTimestamp, dayAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, dayAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", dayAggregated.getPaymentId());
  }
}

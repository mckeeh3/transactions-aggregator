package io.aggregator.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.protobuf.Timestamp;

import org.junit.Test;

import io.aggregator.TimeTo;
import io.aggregator.api.DayApi;

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

    var response = testKit.activateHour(activateHourCommand(epochHour));

    var dayActivated = response.getNextEventOfType(DayEntity.DayActivated.class);
    var hourAdded = response.getNextEventOfType(DayEntity.HourAdded.class);

    assertEquals("merchant-1", dayActivated.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", dayActivated.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", dayActivated.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", dayActivated.getMerchantKey().getAccountTo());
    assertEquals(epochDay, dayActivated.getEpochDay());

    assertEquals("merchant-1", hourAdded.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", hourAdded.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", hourAdded.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", hourAdded.getMerchantKey().getAccountTo());
    assertEquals(epochHour, hourAdded.getEpochHour());

    var state = testKit.getState();

    assertEquals("merchant-1", state.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", state.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", state.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", state.getMerchantKey().getAccountTo());
    assertEquals(epochDay, state.getEpochDay());
    assertEquals(1, state.getActiveHoursCount());

    var activeHour = state.getActiveHours(0);

    assertEquals(epochHour, activeHour.getEpochHour());

    response = testKit.activateHour(activateHourCommand(nextEpochHour));

    hourAdded = response.getNextEventOfType(DayEntity.HourAdded.class);

    assertEquals("merchant-1", hourAdded.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", hourAdded.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", hourAdded.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", hourAdded.getMerchantKey().getAccountTo());
    assertEquals(nextEpochHour, hourAdded.getEpochHour());

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

    testKit.activateHour(activateHourCommand(epochHour));
    testKit.activateHour(activateHourCommand(nextEpochHour));

    var response = testKit.aggregateDay(aggregateDayCommand(epochDay, now));

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

    var response = testKit.aggregateDay(aggregateDayCommand(epochDay, now));

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

    testKit.activateHour(activateHourCommand(epochHour));
    testKit.activateHour(activateHourCommand(nextEpochHour));

    testKit.aggregateDay(aggregateDayCommand(epochDay, aggregateRequestTimestamp));

    var response = testKit.hourAggregation(hourAggregationCommand(epochHour, 123.45, 10, aggregateRequestTimestamp));

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

    response = testKit.hourAggregation(hourAggregationCommand(nextEpochHour, 678.90, 20, aggregateRequestTimestamp));

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

    response = testKit.activateHour(activateHourCommand(epochHour));

    response.getNextEventOfType(DayEntity.DayActivated.class);
    response.getNextEventOfType(DayEntity.HourAdded.class);

    testKit.aggregateDay(aggregateDayCommand(epochDay, aggregateRequestTimestamp));

    response = testKit.hourAggregation(hourAggregationCommand(epochHour, 543.21, 321, aggregateRequestTimestamp));

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

  @Test
  public void hourAggregationSameHourMultipleAggregations() {
    DayTestKit testKit = DayTestKit.of(Day::new);

    var epochDay = TimeTo.fromNow().toEpochDay();
    var epochHour = TimeTo.fromEpochDay(epochDay).toEpochHour();
    var aggregateRequestTimestamp1 = TimeTo.fromEpochHour(epochHour).toTimestamp();
    var aggregateRequestTimestamp2 = TimeTo.fromEpochHour(epochHour).plus().seconds(1).toTimestamp();

    testKit.activateHour(activateHourCommand(epochHour));
    testKit.aggregateDay(aggregateDayCommand(epochDay, aggregateRequestTimestamp1));

    testKit.activateHour(activateHourCommand(epochHour));
    testKit.aggregateDay(aggregateDayCommand(epochDay, aggregateRequestTimestamp2));

    var response = testKit.hourAggregation(hourAggregationCommand(epochHour, 123.45, 10, aggregateRequestTimestamp1));

    response.getNextEventOfType(DayEntity.DayAggregated.class);
    response.getNextEventOfType(DayEntity.ActiveHourAggregated.class);

    response = testKit.hourAggregation(hourAggregationCommand(epochHour, 678.90, 20, aggregateRequestTimestamp2));

    response.getNextEventOfType(DayEntity.DayAggregated.class);
    response.getNextEventOfType(DayEntity.ActiveHourAggregated.class);
  }

  static DayApi.ActivateHourCommand activateHourCommand(long epochHour) {
    return DayApi.ActivateHourCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setServiceCode("service-code-1")
        .setAccountFrom("account-from-1")
        .setAccountTo("account-to-1")
        .setEpochDay(TimeTo.fromEpochHour(epochHour).toEpochDay())
        .setEpochHour(epochHour)
        .build();
  }

  static DayApi.AggregateDayCommand aggregateDayCommand(long epochDay, Timestamp timestamp) {
    return DayApi.AggregateDayCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setServiceCode("service-code-1")
        .setAccountFrom("account-from-1")
        .setAccountTo("account-to-1")
        .setEpochDay(epochDay)
        .setAggregateRequestTimestamp(timestamp)
        .setPaymentId("payment-1")
        .build();
  }

  static DayApi.HourAggregationCommand hourAggregationCommand(long epochHour, double amount, int count, Timestamp timestamp) {
    return DayApi.HourAggregationCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setServiceCode("service-code-1")
        .setAccountFrom("account-from-1")
        .setAccountTo("account-to-1")
        .setEpochDay(TimeTo.fromEpochHour(epochHour).toEpochDay())
        .setEpochHour(epochHour)
        .setTransactionTotalAmount(amount)
        .setTransactionCount(count)
        .setLastUpdateTimestamp(timestamp)
        .setAggregateRequestTimestamp(timestamp)
        .setPaymentId("payment-1")
        .build();
  }
}

package io.aggregator.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.protobuf.Timestamp;

import org.junit.Test;

import io.aggregator.TimeTo;
import io.aggregator.api.DayApi;

import java.util.Collection;
import java.util.List;

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

    var response = testKit.addHour(activateHourCommand(epochHour));

    var dayActivated = response.getNextEventOfType(DayEntity.DayActivated.class);
    var hourAdded = response.getNextEventOfType(DayEntity.HourAdded.class);

    assertEquals("merchant-1", dayActivated.getMerchantKey().getMerchantId());
    assertEquals(epochDay, dayActivated.getEpochDay());

    assertEquals("merchant-1", hourAdded.getMerchantKey().getMerchantId());
    assertEquals(epochHour, hourAdded.getEpochHour());

    var state = testKit.getState();

    assertEquals("merchant-1", state.getMerchantKey().getMerchantId());
    assertEquals(epochDay, state.getEpochDay());
    assertEquals(1, state.getActiveHoursCount());

    var activeHour = state.getActiveHours(0);

    assertEquals(epochHour, activeHour.getEpochHour());

    response = testKit.addHour(activateHourCommand(nextEpochHour));

    hourAdded = response.getNextEventOfType(DayEntity.HourAdded.class);

    assertEquals("merchant-1", hourAdded.getMerchantKey().getMerchantId());
    assertEquals(nextEpochHour, hourAdded.getEpochHour());

    state = testKit.getState();

    assertEquals("merchant-1", state.getMerchantKey().getMerchantId());
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

    testKit.addHour(activateHourCommand(epochHour));
    testKit.addHour(activateHourCommand(nextEpochHour));

    var response = testKit.aggregateDay(aggregateDayCommand(epochDay, now));

    var dayAggregationRequested = response.getNextEventOfType(DayEntity.DayAggregationRequested.class);

    assertEquals("merchant-1", dayAggregationRequested.getMerchantKey().getMerchantId());
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
    assertEquals(epochDay, dayAggregated.getEpochDay());
    assertEquals(now, dayAggregated.getLastUpdateTimestamp());
    assertEquals(now, dayAggregated.getAggregateRequestTimestamp());
    assertEquals(now, dayAggregated.getAggregationCompletedTimestamp());
    assertEquals(0, dayAggregated.getMoneyMovementsCount());
    assertEquals("payment-1", dayAggregated.getPaymentId());
  }

  @Test
  public void hourAggregationTest() {
    DayTestKit testKit = DayTestKit.of(Day::new);

    var epochDay = TimeTo.fromNow().toEpochDay();
    var epochHour = TimeTo.fromEpochDay(epochDay).toEpochHour();
    var nextEpochHour = TimeTo.fromEpochHour(epochHour).plus().hours(1).toEpochHour();
    var aggregateRequestTimestamp = TimeTo.fromEpochDay(epochDay).toTimestamp();

    testKit.addHour(activateHourCommand(epochHour));
    testKit.addHour(activateHourCommand(nextEpochHour));

    testKit.aggregateDay(aggregateDayCommand(epochDay, aggregateRequestTimestamp));

    Collection<TransactionMerchantKey.MoneyMovement> moneyMovements = List.of(
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("BBB").setAmount(1.22).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("BBB").setAmount(2.20).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("AAA").setAmount(1.22).build()
    );
    var response = testKit.hourAggregation(hourAggregationCommand(epochHour, moneyMovements, aggregateRequestTimestamp));

    var activeHourAggregated = response.getNextEventOfType(DayEntity.ActiveHourAggregated.class);

    assertEquals("merchant-1", activeHourAggregated.getMerchantKey().getMerchantId());
    assertEquals(epochHour, activeHourAggregated.getEpochHour());
    assertEquals(moneyMovements.size(), activeHourAggregated.getMoneyMovementsCount());
    assertTrue(activeHourAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp, activeHourAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, activeHourAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeHourAggregated.getPaymentId());

    moneyMovements = List.of(
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("AAA").setAmount(3.33).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("DDD").setAmount(4.44).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("BBB").setAmount(1.55).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("CCC").setAmount(2.55).build()
    );
    response = testKit.hourAggregation(hourAggregationCommand(nextEpochHour, moneyMovements, aggregateRequestTimestamp));

    var dayAggregated = response.getNextEventOfType(DayEntity.DayAggregated.class);
    activeHourAggregated = response.getNextEventOfType(DayEntity.ActiveHourAggregated.class);

    assertEquals("merchant-1", activeHourAggregated.getMerchantKey().getMerchantId());
    assertEquals(nextEpochHour, activeHourAggregated.getEpochHour());
    assertEquals(moneyMovements.size(), activeHourAggregated.getMoneyMovementsCount());
    assertTrue(activeHourAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp, activeHourAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, activeHourAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeHourAggregated.getPaymentId());

    moneyMovements = List.of(
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("BBB").setAmount(1.22).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("DDD").setAmount(4.44).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("AAA").setAmount(1.22).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("CCC").setAmount(2.55).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("AAA").setAmount(3.33).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("BBB").setAmount(3.75).build()
    );

    assertEquals("merchant-1", dayAggregated.getMerchantKey().getMerchantId());
    assertEquals(epochDay, dayAggregated.getEpochDay());
    assertEquals(moneyMovements.size(), dayAggregated.getMoneyMovementsCount());
    assertTrue(dayAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp, dayAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, dayAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", dayAggregated.getPaymentId());

    // this sequence re-activates the day and hour aggregation
    aggregateRequestTimestamp = TimeTo.fromEpochDay(epochDay).plus().minutes(1).toTimestamp();

    response = testKit.addHour(activateHourCommand(epochHour));

    response.getNextEventOfType(DayEntity.DayActivated.class);
    response.getNextEventOfType(DayEntity.HourAdded.class);

    testKit.aggregateDay(aggregateDayCommand(epochDay, aggregateRequestTimestamp));

    moneyMovements = List.of(
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("BBB").setAmount(6.11).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("DDD").setAmount(3.11).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("AAA").setAmount(1.22).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("CCC").setAmount(4.33).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("BBB").setAmount(5.44).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("DDD").setAccountTo("BBB").setAmount(6.55).build()
    );
    response = testKit.hourAggregation(hourAggregationCommand(epochHour, moneyMovements, aggregateRequestTimestamp));

    dayAggregated = response.getNextEventOfType(DayEntity.DayAggregated.class);
    activeHourAggregated = response.getNextEventOfType(DayEntity.ActiveHourAggregated.class);

    assertEquals("merchant-1", activeHourAggregated.getMerchantKey().getMerchantId());
    assertEquals(epochHour, activeHourAggregated.getEpochHour());
    assertEquals(moneyMovements.size(), activeHourAggregated.getMoneyMovementsCount());
    assertTrue(activeHourAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp, activeHourAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, activeHourAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeHourAggregated.getPaymentId());

    assertEquals("merchant-1", dayAggregated.getMerchantKey().getMerchantId());
    assertEquals(epochDay, dayAggregated.getEpochDay());
    assertEquals(moneyMovements.size(), dayAggregated.getMoneyMovementsCount());
    assertTrue(dayAggregated.getMoneyMovementsList().containsAll(moneyMovements));
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

    testKit.addHour(activateHourCommand(epochHour));
    testKit.aggregateDay(aggregateDayCommand(epochDay, aggregateRequestTimestamp1));

    testKit.addHour(activateHourCommand(epochHour));
    testKit.aggregateDay(aggregateDayCommand(epochDay, aggregateRequestTimestamp2));

    Collection<TransactionMerchantKey.MoneyMovement> moneyMovements = List.of(
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("BBB").setAmount(1.22).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("BBB").setAmount(2.20).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("AAA").setAmount(1.22).build()
    );
    var response = testKit.hourAggregation(hourAggregationCommand(epochHour, moneyMovements, aggregateRequestTimestamp1));

    response.getNextEventOfType(DayEntity.DayAggregated.class);
    response.getNextEventOfType(DayEntity.ActiveHourAggregated.class);

    moneyMovements = List.of(
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("BBB").setAmount(1.22).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("DDD").setAmount(4.44).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("AAA").setAmount(1.22).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("CCC").setAmount(2.55).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("AAA").setAmount(3.33).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("BBB").setAmount(3.75).build()
    );
    response = testKit.hourAggregation(hourAggregationCommand(epochHour, moneyMovements, aggregateRequestTimestamp2));

    response.getNextEventOfType(DayEntity.DayAggregated.class);
    response.getNextEventOfType(DayEntity.ActiveHourAggregated.class);
  }

  static DayApi.AddHourCommand activateHourCommand(long epochHour) {
    return DayApi.AddHourCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setEpochDay(TimeTo.fromEpochHour(epochHour).toEpochDay())
        .setEpochHour(epochHour)
        .build();
  }

  static DayApi.AggregateDayCommand aggregateDayCommand(long epochDay, Timestamp timestamp) {
    return DayApi.AggregateDayCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setEpochDay(epochDay)
        .setAggregateRequestTimestamp(timestamp)
        .setPaymentId("payment-1")
        .build();
  }

  static DayApi.HourAggregationCommand hourAggregationCommand(long epochHour, Collection<TransactionMerchantKey.MoneyMovement> moneyMovements, Timestamp timestamp) {
    return DayApi.HourAggregationCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setEpochDay(TimeTo.fromEpochHour(epochHour).toEpochDay())
        .setEpochHour(epochHour)
        .addAllMoneyMovements(moneyMovements)
        .setLastUpdateTimestamp(timestamp)
        .setAggregateRequestTimestamp(timestamp)
        .setPaymentId("payment-1")
        .build();
  }
}

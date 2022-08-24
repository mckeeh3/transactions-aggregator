package io.aggregator.entity;

import io.aggregator.TimeTo;
import io.aggregator.api.HourApi;

import org.junit.Test;

import static org.junit.Assert.*;

import com.google.protobuf.Timestamp;

import java.util.Collection;
import java.util.List;

// This class was initially generated based on the .proto definition by Kalix tooling.
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

    var response = testKit.addMinute(addMinuteCommand(epochMinute));

    var hourActivated = response.getNextEventOfType(HourEntity.HourActivated.class);
    var minuteAdded = response.getNextEventOfType(HourEntity.MinuteAdded.class);

    assertEquals("merchant-1", hourActivated.getMerchantKey().getMerchantId());
    assertEquals(epochHour, hourActivated.getEpochHour());

    assertEquals("merchant-1", minuteAdded.getMerchantKey().getMerchantId());
    assertEquals(epochMinute, minuteAdded.getEpochMinute());

    var state = testKit.getState();

    assertEquals("merchant-1", state.getMerchantKey().getMerchantId());
    assertEquals(epochHour, state.getEpochHour());
    assertEquals(1, state.getActiveMinutesCount());

    var activeMinute = state.getActiveMinutes(0);

    assertEquals(epochMinute, activeMinute.getEpochMinute());

    response = testKit.addMinute(addMinuteCommand(nextEpochMinute));

    minuteAdded = response.getNextEventOfType(HourEntity.MinuteAdded.class);

    assertEquals("merchant-1", minuteAdded.getMerchantKey().getMerchantId());
    assertEquals(nextEpochMinute, minuteAdded.getEpochMinute());

    state = testKit.getState();

    assertEquals("merchant-1", state.getMerchantKey().getMerchantId());
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

    testKit.addMinute(addMinuteCommand(epochMinute));
    testKit.addMinute(addMinuteCommand(nextEpochMinute));

    var response = testKit.aggregateHour(aggregateHourCommand(epochHour, now));

    var hourAggregationRequested = response.getNextEventOfType(HourEntity.HourAggregationRequested.class);

    assertEquals("merchant-1", hourAggregationRequested.getMerchantKey().getMerchantId());
    assertEquals(epochHour, hourAggregationRequested.getEpochHour());
    assertEquals(now, hourAggregationRequested.getAggregateRequestTimestamp());
    assertEquals(2, hourAggregationRequested.getEpochMinutesCount());
    assertEquals(epochMinute, hourAggregationRequested.getEpochMinutes(0));
    assertEquals(nextEpochMinute, hourAggregationRequested.getEpochMinutes(1));
    assertEquals("payment-1", hourAggregationRequested.getPaymentId());

    var state = testKit.getState();

    var aggregateHour = state.getAggregateHoursList().stream()
        .filter(aggHr -> aggHr.getAggregateRequestTimestamp().equals(now))
        .findFirst();
    assertTrue(aggregateHour.isPresent());
    assertEquals(now, aggregateHour.get().getAggregateRequestTimestamp());
  }

  @Test
  public void aggregateHourWithNoHoursTest() {
    HourTestKit testKit = HourTestKit.of(Hour::new);

    var epochHour = TimeTo.fromNow().toEpochHour();
    var now = TimeTo.fromEpochHour(epochHour).toTimestamp();

    var response = testKit.aggregateHour(aggregateHourCommand(epochHour, now));

    var hourAggregated = response.getNextEventOfType(HourEntity.HourAggregated.class);

    assertEquals("merchant-1", hourAggregated.getMerchantKey().getMerchantId());
    assertEquals(epochHour, hourAggregated.getEpochHour());
    assertEquals(now, hourAggregated.getAggregateRequestTimestamp());
    assertEquals(0, hourAggregated.getMoneyMovementsCount());
    assertEquals("payment-1", hourAggregated.getPaymentId());
  }

  @Test
  public void minuteAggregationTest() {
    HourTestKit testKit = HourTestKit.of(Hour::new);

    var epochHour = TimeTo.fromNow().toEpochHour();
    var epochMinute = TimeTo.fromEpochHour(epochHour).toEpochMinute();
    var nextEpochMinute = TimeTo.fromEpochMinute(epochMinute).plus().minutes(1).toEpochMinute();
    var aggregateRequestTimestamp = TimeTo.fromEpochHour(epochHour).toTimestamp();

    testKit.addMinute(addMinuteCommand(epochMinute));
    testKit.addMinute(addMinuteCommand(nextEpochMinute));

    testKit.aggregateHour(aggregateHourCommand(epochHour, aggregateRequestTimestamp));

    Collection<TransactionMerchantKey.MoneyMovement> moneyMovements = List.of(
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("BBB").setAmount(1.22).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("BBB").setAmount(2.20).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("AAA").setAmount(1.22).build()
    );
    var response = testKit.minuteAggregation(minuteAggregationCommand(epochMinute, moneyMovements, aggregateRequestTimestamp));

    var activeMinuteAggregated = response.getNextEventOfType(HourEntity.ActiveMinuteAggregated.class);

    assertEquals("merchant-1", activeMinuteAggregated.getMerchantKey().getMerchantId());
    assertEquals(epochMinute, activeMinuteAggregated.getEpochMinute());
    assertEquals(moneyMovements.size(), activeMinuteAggregated.getMoneyMovementsCount());
    assertTrue(activeMinuteAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp, activeMinuteAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, activeMinuteAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeMinuteAggregated.getPaymentId());

    moneyMovements = List.of(
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("AAA").setAmount(3.33).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("DDD").setAmount(4.44).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("BBB").setAmount(1.55).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("CCC").setAmount(2.55).build()
    );
    response = testKit.minuteAggregation(minuteAggregationCommand(nextEpochMinute, moneyMovements, aggregateRequestTimestamp));

    var hourAggregated = response.getNextEventOfType(HourEntity.HourAggregated.class);
    activeMinuteAggregated = response.getNextEventOfType(HourEntity.ActiveMinuteAggregated.class);

    assertEquals("merchant-1", activeMinuteAggregated.getMerchantKey().getMerchantId());
    assertEquals(nextEpochMinute, activeMinuteAggregated.getEpochMinute());
    assertEquals(moneyMovements.size(), activeMinuteAggregated.getMoneyMovementsCount());
    assertTrue(activeMinuteAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp, activeMinuteAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, activeMinuteAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeMinuteAggregated.getPaymentId());

    moneyMovements = List.of(
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("BBB").setAmount(1.22).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("DDD").setAmount(4.44).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("AAA").setAmount(1.22).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("CCC").setAmount(2.55).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("AAA").setAmount(3.33).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("BBB").setAmount(3.75).build()
    );

    assertEquals("merchant-1", hourAggregated.getMerchantKey().getMerchantId());
    assertEquals(epochHour, hourAggregated.getEpochHour());
    assertEquals(moneyMovements.size(), hourAggregated.getMoneyMovementsCount());
    assertTrue(hourAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp, hourAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, hourAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", hourAggregated.getPaymentId());

    // this sequence re-activates the hour and minute aggregation
    aggregateRequestTimestamp = TimeTo.fromEpochHour(epochHour).plus().minutes(1).toTimestamp();

    response = testKit.addMinute(addMinuteCommand(epochMinute));

    response.getNextEventOfType(HourEntity.HourActivated.class);
    response.getNextEventOfType(HourEntity.MinuteAdded.class);

    testKit.aggregateHour(aggregateHourCommand(epochHour, aggregateRequestTimestamp));

    moneyMovements = List.of(
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("BBB").setAmount(6.11).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("DDD").setAmount(3.11).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("AAA").setAmount(1.22).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("CCC").setAmount(4.33).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("BBB").setAmount(5.44).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("DDD").setAccountTo("BBB").setAmount(6.55).build()
    );
    response = testKit.minuteAggregation(minuteAggregationCommand(epochMinute, moneyMovements, aggregateRequestTimestamp));

    hourAggregated = response.getNextEventOfType(HourEntity.HourAggregated.class);
    activeMinuteAggregated = response.getNextEventOfType(HourEntity.ActiveMinuteAggregated.class);

    assertEquals("merchant-1", activeMinuteAggregated.getMerchantKey().getMerchantId());
    assertEquals(epochMinute, activeMinuteAggregated.getEpochMinute());
    assertEquals(moneyMovements.size(), activeMinuteAggregated.getMoneyMovementsCount());
    assertTrue(activeMinuteAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp, activeMinuteAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, activeMinuteAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeMinuteAggregated.getPaymentId());

    assertEquals("merchant-1", hourAggregated.getMerchantKey().getMerchantId());
    assertEquals(epochHour, hourAggregated.getEpochHour());
    assertEquals(moneyMovements.size(), hourAggregated.getMoneyMovementsCount());
    assertTrue(hourAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp, hourAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, hourAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", hourAggregated.getPaymentId());
  }

  static HourApi.AddMinuteCommand addMinuteCommand(long epochMinute) {
    return HourApi.AddMinuteCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setEpochHour(TimeTo.fromEpochMinute(epochMinute).toEpochHour())
        .setEpochMinute(epochMinute)
        .build();
  }

  static HourApi.AggregateHourCommand aggregateHourCommand(long epochHour, Timestamp timestamp) {
    return HourApi.AggregateHourCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setEpochHour(epochHour)
        .setAggregateRequestTimestamp(timestamp)
        .setPaymentId("payment-1")
        .build();
  }

  static HourApi.MinuteAggregationCommand minuteAggregationCommand(long epochMinute, Collection<TransactionMerchantKey.MoneyMovement> moneyMovements, Timestamp timestamp) {
    return HourApi.MinuteAggregationCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setEpochHour(TimeTo.fromEpochMinute(epochMinute).toEpochHour())
        .setEpochMinute(epochMinute)
        .addAllMoneyMovements(moneyMovements)
        .setLastUpdateTimestamp(timestamp)
        .setAggregateRequestTimestamp(timestamp)
        .setPaymentId("payment-1")
        .build();
  }
}

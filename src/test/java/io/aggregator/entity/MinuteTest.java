package io.aggregator.entity;

import io.aggregator.TimeTo;
import io.aggregator.api.MinuteApi;

import org.junit.Test;

import static org.junit.Assert.*;

import com.google.protobuf.Timestamp;

import java.util.Collection;
import java.util.List;

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

    var epochMinute = TimeTo.fromNow().toEpochMinute();
    var epochSecond = TimeTo.fromEpochMinute(epochMinute).toEpochSecond();
    var nextEpochSecond = TimeTo.fromEpochSecond(epochSecond).plus().seconds(1).toEpochSecond();

    var response = testKit.addSecond(addSecondCommand(epochSecond));

    var minuteActivated = response.getNextEventOfType(MinuteEntity.MinuteActivated.class);
    var secondAdded = response.getNextEventOfType(MinuteEntity.SecondAdded.class);

    assertEquals("merchant-1", minuteActivated.getMerchantKey().getMerchantId());
    assertEquals(epochMinute, minuteActivated.getEpochMinute());

    assertEquals("merchant-1", secondAdded.getMerchantKey().getMerchantId());
    assertEquals(epochSecond, secondAdded.getEpochSecond());

    var state = testKit.getState();

    assertEquals("merchant-1", state.getMerchantKey().getMerchantId());
    assertEquals(epochMinute, state.getEpochMinute());
    assertEquals(1, state.getActiveSecondsCount());

    var activeSecond = state.getActiveSeconds(0);

    assertEquals(epochSecond, activeSecond.getEpochSecond());

    response = testKit.addSecond(addSecondCommand(nextEpochSecond));

    secondAdded = response.getNextEventOfType(MinuteEntity.SecondAdded.class);

    assertEquals("merchant-1", secondAdded.getMerchantKey().getMerchantId());
    assertEquals(nextEpochSecond, secondAdded.getEpochSecond());

    state = testKit.getState();

    assertEquals("merchant-1", state.getMerchantKey().getMerchantId());
    assertEquals(epochMinute, state.getEpochMinute());
    assertEquals(2, state.getActiveSecondsCount());

    activeSecond = state.getActiveSeconds(1);

    assertEquals(nextEpochSecond, activeSecond.getEpochSecond());
  }

  @Test
  public void aggregateMinuteTest() {
    MinuteTestKit testKit = MinuteTestKit.of(Minute::new);

    var epochMinute = TimeTo.fromNow().toEpochMinute();
    var epochSecond = TimeTo.fromEpochMinute(epochMinute).toEpochSecond();
    var nextEpochSecond = TimeTo.fromEpochSecond(epochSecond).plus().seconds(1).toEpochSecond();
    var now = TimeTo.fromEpochSecond(epochSecond).toTimestamp();

    testKit.addSecond(addSecondCommand(epochSecond));
    testKit.addSecond(addSecondCommand(nextEpochSecond));

    var response = testKit.aggregateMinute(aggregateMinuteCommand(epochMinute, now));

    var minuteAggregationRequested = response.getNextEventOfType(MinuteEntity.MinuteAggregationRequested.class);

    assertEquals("merchant-1", minuteAggregationRequested.getMerchantKey().getMerchantId());
    assertEquals(epochMinute, minuteAggregationRequested.getEpochMinute());
    assertEquals(now, minuteAggregationRequested.getAggregateRequestTimestamp());
    assertEquals(2, minuteAggregationRequested.getEpochSecondsCount());
    assertEquals(epochSecond, minuteAggregationRequested.getEpochSeconds(0));
    assertEquals(nextEpochSecond, minuteAggregationRequested.getEpochSeconds(1));
    assertEquals("payment-1", minuteAggregationRequested.getPaymentId());

    var state = testKit.getState();

    var aggregateMinute = state.getAggregateMinutesList().stream()
        .filter(aggMin -> aggMin.getAggregateRequestTimestamp().equals(now))
        .findFirst();
    assertTrue(aggregateMinute.isPresent());
    assertEquals(now, aggregateMinute.get().getAggregateRequestTimestamp());
  }

  @Test
  public void aggregateMinuteWithNoSecondsTest() {
    MinuteTestKit testKit = MinuteTestKit.of(Minute::new);

    var epochMinute = TimeTo.fromNow().toEpochMinute();
    var epochSecond = TimeTo.fromEpochMinute(epochMinute).toEpochSecond();
    var now = TimeTo.fromEpochSecond(epochSecond).toTimestamp();

    var response = testKit.aggregateMinute(aggregateMinuteCommand(epochMinute, now));

    var minuteAggregated = response.getNextEventOfType(MinuteEntity.MinuteAggregated.class);

    assertEquals("merchant-1", minuteAggregated.getMerchantKey().getMerchantId());
    assertEquals(epochMinute, minuteAggregated.getEpochMinute());
    assertEquals(now, minuteAggregated.getAggregateRequestTimestamp());
    assertEquals(0, minuteAggregated.getMoneyMovementsCount());
    assertEquals("payment-1", minuteAggregated.getPaymentId());
  }

  @Test
  public void secondAggregationTest() {
    MinuteTestKit testKit = MinuteTestKit.of(Minute::new);

    var epochMinute = TimeTo.fromNow().toEpochMinute();
    var epochSecond = TimeTo.fromEpochMinute(epochMinute).toEpochSecond();
    var nextEpochSecond = TimeTo.fromEpochSecond(epochSecond).plus().seconds(1).toEpochSecond();
    var aggregateRequestTimestamp = TimeTo.fromEpochSecond(epochSecond).toTimestamp();

    testKit.addSecond(addSecondCommand(epochSecond));
    testKit.addSecond(addSecondCommand(nextEpochSecond));

    testKit.aggregateMinute(aggregateMinuteCommand(epochMinute, aggregateRequestTimestamp));

    Collection<TransactionMerchantKey.MoneyMovement> moneyMovements = List.of(
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("BBB").setAmount(1.22).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("BBB").setAmount(2.20).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("AAA").setAmount(1.22).build()
    );
    var response = testKit.secondAggregation(secondAggregationCommand(epochSecond, moneyMovements, aggregateRequestTimestamp));

    var activeSecondAggregated = response.getNextEventOfType(MinuteEntity.ActiveSecondAggregated.class);

    assertEquals("merchant-1", activeSecondAggregated.getMerchantKey().getMerchantId());
    assertEquals(epochSecond, activeSecondAggregated.getEpochSecond());
    assertEquals(moneyMovements.size(), activeSecondAggregated.getMoneyMovementsCount());
    assertTrue(activeSecondAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp, activeSecondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, activeSecondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeSecondAggregated.getPaymentId());

    moneyMovements = List.of(
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("AAA").setAmount(3.33).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("DDD").setAmount(4.44).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("BBB").setAmount(1.55).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("CCC").setAmount(2.55).build()
    );
    response = testKit.secondAggregation(secondAggregationCommand(nextEpochSecond, moneyMovements, aggregateRequestTimestamp));

    var minuteAggregated = response.getNextEventOfType(MinuteEntity.MinuteAggregated.class);
    activeSecondAggregated = response.getNextEventOfType(MinuteEntity.ActiveSecondAggregated.class);

    assertEquals("merchant-1", activeSecondAggregated.getMerchantKey().getMerchantId());
    assertEquals(nextEpochSecond, activeSecondAggregated.getEpochSecond());
    assertEquals(moneyMovements.size(), activeSecondAggregated.getMoneyMovementsCount());
    assertTrue(activeSecondAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp, activeSecondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, activeSecondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeSecondAggregated.getPaymentId());

    moneyMovements = List.of(
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("BBB").setAmount(1.22).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("DDD").setAmount(4.44).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("AAA").setAmount(1.22).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("CCC").setAmount(2.55).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("AAA").setAmount(3.33).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("BBB").setAmount(3.75).build()
    );

    assertEquals("merchant-1", minuteAggregated.getMerchantKey().getMerchantId());
    assertEquals(epochMinute, minuteAggregated.getEpochMinute());
    assertEquals(moneyMovements.size(), minuteAggregated.getMoneyMovementsCount());
    assertTrue(minuteAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp, minuteAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, minuteAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", minuteAggregated.getPaymentId());

    // this sequence re-activates the second and minute aggregation
    aggregateRequestTimestamp = TimeTo.fromEpochSecond(epochSecond).plus().minutes(1).toTimestamp();

    response = testKit.addSecond(addSecondCommand(epochSecond));

    response.getNextEventOfType(MinuteEntity.MinuteActivated.class);
    response.getNextEventOfType(MinuteEntity.SecondAdded.class);

    testKit.aggregateMinute(aggregateMinuteCommand(epochMinute, aggregateRequestTimestamp));

    moneyMovements = List.of(
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("BBB").setAmount(6.11).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("DDD").setAmount(3.11).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("AAA").setAmount(1.22).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("CCC").setAmount(4.33).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("BBB").setAmount(5.44).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("DDD").setAccountTo("BBB").setAmount(6.55).build()
    );
    response = testKit.secondAggregation(secondAggregationCommand(epochSecond, moneyMovements, aggregateRequestTimestamp));

    minuteAggregated = response.getNextEventOfType(MinuteEntity.MinuteAggregated.class);
    activeSecondAggregated = response.getNextEventOfType(MinuteEntity.ActiveSecondAggregated.class);

    assertEquals("merchant-1", activeSecondAggregated.getMerchantKey().getMerchantId());
    assertEquals(epochSecond, activeSecondAggregated.getEpochSecond());
    assertEquals(moneyMovements.size(), activeSecondAggregated.getMoneyMovementsCount());
    assertTrue(activeSecondAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp, activeSecondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, activeSecondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeSecondAggregated.getPaymentId());

    assertEquals("merchant-1", minuteAggregated.getMerchantKey().getMerchantId());
    assertEquals(epochMinute, minuteAggregated.getEpochMinute());
    assertEquals(moneyMovements.size(), minuteAggregated.getMoneyMovementsCount());
    assertTrue(minuteAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp, minuteAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, minuteAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", minuteAggregated.getPaymentId());
  }

  static MinuteApi.AddSecondCommand addSecondCommand(long epochSecond) {
    return MinuteApi.AddSecondCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setEpochMinute(TimeTo.fromEpochSecond(epochSecond).toEpochMinute())
        .setEpochSecond(epochSecond)
        .build();
  }

  static MinuteApi.AggregateMinuteCommand aggregateMinuteCommand(long epochMinute, Timestamp timestamp) {
    return MinuteApi.AggregateMinuteCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setEpochMinute(epochMinute)
        .setAggregateRequestTimestamp(timestamp)
        .setPaymentId("payment-1")
        .build();
  }

  static MinuteApi.SecondAggregationCommand secondAggregationCommand(long epochSecond, Collection<TransactionMerchantKey.MoneyMovement> moneyMovements, Timestamp timestamp) {
    return MinuteApi.SecondAggregationCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setEpochMinute(TimeTo.fromEpochSecond(epochSecond).toEpochMinute())
        .setEpochSecond(epochSecond)
        .addAllMoneyMovements(moneyMovements)
        .setLastUpdateTimestamp(timestamp)
        .setAggregateRequestTimestamp(timestamp)
        .setPaymentId("payment-1")
        .build();
  }
}

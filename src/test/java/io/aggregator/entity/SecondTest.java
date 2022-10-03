package io.aggregator.entity;

import io.aggregator.TimeTo;
import io.aggregator.api.SecondApi;

import org.junit.Test;

import static org.junit.Assert.*;

import com.google.protobuf.Timestamp;

import java.util.Collection;
import java.util.List;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class SecondTest {

  @Test
  public void exampleTest() {
    // SecondTestKit testKit = SecondTestKit.of(StripedSecond::new);
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
  public void addStripedSecondTest() {
    SecondTestKit testKit = SecondTestKit.of(Second::new);

    var epochSecond = TimeTo.fromNow().toEpochSecond();
    var nextEpochSecond = TimeTo.fromEpochSecond(epochSecond).plus().seconds(1).toEpochSecond();
    var stripe = 3;

    var response = testKit.addStripedSecond(addStripedSecondCommand(epochSecond, stripe));

    var secondActivated = response.getNextEventOfType(SecondEntity.SecondActivated.class);
    var stripedSecondAdded = response.getNextEventOfType(SecondEntity.StripedSecondAdded.class);

    assertEquals("merchant-1", secondActivated.getMerchantKey().getMerchantId());
    assertEquals(epochSecond, secondActivated.getEpochSecond());

    assertEquals("merchant-1", stripedSecondAdded.getMerchantKey().getMerchantId());
    assertEquals(epochSecond, stripedSecondAdded.getEpochSecond());
    assertEquals(stripe, stripedSecondAdded.getStripe());

    var state = testKit.getState();

    assertEquals("merchant-1", state.getMerchantKey().getMerchantId());
    assertEquals(epochSecond, state.getEpochSecond());
    assertEquals(1, state.getActiveStripedSecondsCount());

    var activeStripedSecond = state.getActiveStripedSeconds(0);

    assertEquals(epochSecond, activeStripedSecond.getEpochSecond());

    response = testKit.addStripedSecond(addStripedSecondCommand(nextEpochSecond, stripe));

    stripedSecondAdded = response.getNextEventOfType(SecondEntity.StripedSecondAdded.class);

    assertEquals("merchant-1", stripedSecondAdded.getMerchantKey().getMerchantId());
    assertEquals(nextEpochSecond, stripedSecondAdded.getEpochSecond());

    state = testKit.getState();

    assertEquals("merchant-1", state.getMerchantKey().getMerchantId());
    assertEquals(epochSecond, state.getEpochSecond());
    assertEquals(2, state.getActiveStripedSecondsCount());

    activeStripedSecond = state.getActiveStripedSeconds(1);

    assertEquals(nextEpochSecond, activeStripedSecond.getEpochSecond());
  }

  @Test
  public void aggregateSecondTest() {
    SecondTestKit testKit = SecondTestKit.of(Second::new);

    var epochSecond = TimeTo.fromNow().toEpochSecond();
    var now = TimeTo.fromEpochSecond(epochSecond).toTimestamp();
    var stripe1 = 3;
    var stripe2 = 5;

    testKit.addStripedSecond(addStripedSecondCommand(epochSecond, stripe1));
    testKit.addStripedSecond(addStripedSecondCommand(epochSecond, stripe2));

    var response = testKit.aggregateSecond(aggregateSecondCommand(epochSecond, now));

    var secondAggregationRequested = response.getNextEventOfType(SecondEntity.SecondAggregationRequested.class);

    assertEquals("merchant-1", secondAggregationRequested.getMerchantKey().getMerchantId());
    assertEquals(epochSecond, secondAggregationRequested.getEpochSecond());
    assertEquals(now, secondAggregationRequested.getAggregateRequestTimestamp());
    assertEquals(2, secondAggregationRequested.getStripesCount());
    assertEquals(stripe1, secondAggregationRequested.getStripes(0));
    assertEquals(stripe2, secondAggregationRequested.getStripes(1));
    assertEquals("payment-1", secondAggregationRequested.getPaymentId());

    var state = testKit.getState();

    var aggregateSecond = state.getAggregateSecondsList().stream()
        .filter(aggSec -> aggSec.getAggregateRequestTimestamp().equals(now))
        .findFirst();
    assertTrue(aggregateSecond.isPresent());
    assertEquals(now, aggregateSecond.get().getAggregateRequestTimestamp());
  }

  @Test
  public void aggregateSecondWithNoSecondsTest() {
    SecondTestKit testKit = SecondTestKit.of(Second::new);

    var epochSecond = TimeTo.fromNow().toEpochSecond();
    var now = TimeTo.fromEpochSecond(epochSecond).toTimestamp();

    var response = testKit.aggregateSecond(aggregateSecondCommand(epochSecond, now));

    var secondAggregated = response.getNextEventOfType(SecondEntity.SecondAggregated.class);

    assertEquals("merchant-1", secondAggregated.getMerchantKey().getMerchantId());
    assertEquals(epochSecond, secondAggregated.getEpochSecond());
    assertEquals(now, secondAggregated.getAggregateRequestTimestamp());
    assertEquals(0, secondAggregated.getMoneyMovementsCount());
    assertEquals("payment-1", secondAggregated.getPaymentId());
  }

  @Test
  public void stripedSecondAggregationTest() {
    SecondTestKit testKit = SecondTestKit.of(Second::new);

    var timestamp = TimeTo.now();
    var epochSecond = TimeTo.fromTimestamp(timestamp).toEpochSecond();
    var nextEpochSecond = TimeTo.fromTimestamp(timestamp).plus().seconds(1).toEpochSecond();
    var stripe = 3;
    var aggregateRequestTimestamp = timestamp;

//    var epochSecond = TimeTo.fromNow().toEpochSecond();
//    var nextEpochSecond = TimeTo.fromEpochSecond(epochSecond).plus().seconds(1).toEpochSecond();
//    var aggregateRequestTimestamp = TimeTo.fromEpochSecond(epochSecond).toTimestamp();
//    var stripe = 3;

    testKit.addStripedSecond(addStripedSecondCommand(epochSecond, stripe));
    testKit.addStripedSecond(addStripedSecondCommand(nextEpochSecond, stripe));

    testKit.aggregateSecond(aggregateSecondCommand(epochSecond, aggregateRequestTimestamp));

    Collection<TransactionMerchantKey.MoneyMovement> moneyMovements = List.of(
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("BBB").setAmount("1.22").build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("BBB").setAmount("2.20").build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("AAA").setAmount("1.22").build()
    );
    var response = testKit.stripedSecondAggregation(stripedSecondAggregationCommand(epochSecond, stripe, moneyMovements, aggregateRequestTimestamp));

    var activeStripedSecondAggregated = response.getNextEventOfType(SecondEntity.ActiveStripedSecondAggregated.class);

    assertEquals("merchant-1", activeStripedSecondAggregated.getMerchantKey().getMerchantId());
    assertEquals(epochSecond, activeStripedSecondAggregated.getEpochSecond());
    assertEquals(moneyMovements.size(), activeStripedSecondAggregated.getMoneyMovementsCount());
    assertTrue(activeStripedSecondAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp, activeStripedSecondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, activeStripedSecondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeStripedSecondAggregated.getPaymentId());

    moneyMovements = List.of(
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("AAA").setAmount("3.33").build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("DDD").setAmount("4.44").build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("BBB").setAmount("1.55").build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("CCC").setAmount("2.55").build()
    );
    response = testKit.stripedSecondAggregation(stripedSecondAggregationCommand(nextEpochSecond, stripe, moneyMovements, aggregateRequestTimestamp));

    var secondAggregated = response.getNextEventOfType(SecondEntity.SecondAggregated.class);
    activeStripedSecondAggregated = response.getNextEventOfType(SecondEntity.ActiveStripedSecondAggregated.class);

    assertEquals("merchant-1", activeStripedSecondAggregated.getMerchantKey().getMerchantId());
    assertEquals(nextEpochSecond, activeStripedSecondAggregated.getEpochSecond());
    assertEquals(moneyMovements.size(), activeStripedSecondAggregated.getMoneyMovementsCount());
    assertTrue(activeStripedSecondAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp, activeStripedSecondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, activeStripedSecondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeStripedSecondAggregated.getPaymentId());

    moneyMovements = List.of(
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("BBB").setAmount("1.22").build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("DDD").setAmount("4.44").build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("AAA").setAmount("1.22").build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("CCC").setAmount("2.55").build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("AAA").setAmount("3.33").build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("BBB").setAmount("3.75").build()
    );

    assertEquals("merchant-1", secondAggregated.getMerchantKey().getMerchantId());
    assertEquals(nextEpochSecond, secondAggregated.getEpochSecond());
    assertEquals(moneyMovements.size(), secondAggregated.getMoneyMovementsCount());
    assertTrue(secondAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp, secondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, secondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", secondAggregated.getPaymentId());

    // this sequence re-activates the striped-second and second aggregation
    aggregateRequestTimestamp = TimeTo.fromEpochSecond(epochSecond).plus().minutes(1).toTimestamp();

    response = testKit.addStripedSecond(addStripedSecondCommand(epochSecond, stripe));

    response.getNextEventOfType(SecondEntity.SecondActivated.class);
    response.getNextEventOfType(SecondEntity.StripedSecondAdded.class);

    testKit.aggregateSecond(aggregateSecondCommand(epochSecond, aggregateRequestTimestamp));

    moneyMovements = List.of(
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("BBB").setAmount("6.11").build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("DDD").setAmount("3.11").build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("AAA").setAmount("1.22").build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("CCC").setAmount("4.33").build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("BBB").setAmount("5.44").build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("DDD").setAccountTo("BBB").setAmount("6.55").build()
    );
    response = testKit.stripedSecondAggregation(stripedSecondAggregationCommand(epochSecond, stripe, moneyMovements, aggregateRequestTimestamp));

    secondAggregated = response.getNextEventOfType(SecondEntity.SecondAggregated.class);
    activeStripedSecondAggregated = response.getNextEventOfType(SecondEntity.ActiveStripedSecondAggregated.class);

    assertEquals("merchant-1", activeStripedSecondAggregated.getMerchantKey().getMerchantId());
    assertEquals(epochSecond, activeStripedSecondAggregated.getEpochSecond());
    assertEquals(moneyMovements.size(), activeStripedSecondAggregated.getMoneyMovementsCount());
    assertTrue(activeStripedSecondAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp, activeStripedSecondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, activeStripedSecondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeStripedSecondAggregated.getPaymentId());

    assertEquals("merchant-1", secondAggregated.getMerchantKey().getMerchantId());
    assertEquals(epochSecond, secondAggregated.getEpochSecond());
    assertEquals(moneyMovements.size(), secondAggregated.getMoneyMovementsCount());
    assertTrue(secondAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp, secondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, secondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", secondAggregated.getPaymentId());
  }

  @Test
  public void multipleAggregationRequestsWithStaggeredResponses() {
    SecondTestKit testKit = SecondTestKit.of(Second::new);

    var timestamp = TimeTo.now();
    var epochSecond1 = TimeTo.fromTimestamp(timestamp).toEpochSecond();
    var epochSecond2 = TimeTo.fromTimestamp(timestamp).plus().nanos(1).toEpochSecond();
    var aggregateRequestTimestamp1 = TimeTo.fromEpochSecond(epochSecond1).toTimestamp();
    var aggregateRequestTimestamp2 = TimeTo.fromTimestamp(timestamp).plus().nanos(1).toTimestamp();
    var stripe = 3;

    testKit.addStripedSecond(addStripedSecondCommand(epochSecond1, stripe));
    testKit.aggregateSecond(aggregateSecondCommand(epochSecond1, aggregateRequestTimestamp1));

    testKit.addStripedSecond(addStripedSecondCommand(epochSecond2, stripe));
    testKit.aggregateSecond(aggregateSecondCommand(epochSecond1, aggregateRequestTimestamp2));

    Collection<TransactionMerchantKey.MoneyMovement> moneyMovements = List.of(
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("BBB").setAmount("1.22").build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("BBB").setAmount("2.20").build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("AAA").setAmount("1.22").build()
    );
    var response = testKit.stripedSecondAggregation(stripedSecondAggregationCommand(epochSecond2, stripe, moneyMovements, aggregateRequestTimestamp2));

    var secondAggregated = response.getNextEventOfType(SecondEntity.SecondAggregated.class);
    var activeStripedSecondAggregated = response.getNextEventOfType(SecondEntity.ActiveStripedSecondAggregated.class);

    assertEquals(epochSecond2, secondAggregated.getEpochSecond());
    assertEquals(moneyMovements.size(), secondAggregated.getMoneyMovementsCount());
    assertTrue(secondAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp2, secondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp2, secondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", secondAggregated.getPaymentId());

    assertEquals(epochSecond2, activeStripedSecondAggregated.getEpochSecond());
    assertEquals(moneyMovements.size(), activeStripedSecondAggregated.getMoneyMovementsCount());
    assertTrue(activeStripedSecondAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp2, activeStripedSecondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp2, activeStripedSecondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeStripedSecondAggregated.getPaymentId());

    moneyMovements = List.of(
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("CCC").setAmount("3.20").build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("DDD").setAmount("4.30").build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("DDD").setAmount("4.50").build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("DDD").setAccountTo("AAA").setAmount("5.80").build()
    );
    response = testKit.stripedSecondAggregation(stripedSecondAggregationCommand(epochSecond1, stripe, moneyMovements, aggregateRequestTimestamp1));

    secondAggregated = response.getNextEventOfType(SecondEntity.SecondAggregated.class);
    activeStripedSecondAggregated = response.getNextEventOfType(SecondEntity.ActiveStripedSecondAggregated.class);

    assertEquals(epochSecond1, secondAggregated.getEpochSecond());
    assertEquals(moneyMovements.size(), secondAggregated.getMoneyMovementsCount());
    assertTrue(secondAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp1, secondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp1, secondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", secondAggregated.getPaymentId());

    assertEquals(epochSecond1, activeStripedSecondAggregated.getEpochSecond());
    assertEquals(moneyMovements.size(), activeStripedSecondAggregated.getMoneyMovementsCount());
    assertTrue(activeStripedSecondAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp1, activeStripedSecondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp1, activeStripedSecondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeStripedSecondAggregated.getPaymentId());
  }

  static SecondApi.AddStripedSecondCommand addStripedSecondCommand(long epochSecond, int stripe) {
    return SecondApi.AddStripedSecondCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setEpochSecond(epochSecond)
        .setStripe(stripe)
        .build();
  }

  static SecondApi.AggregateSecondCommand aggregateSecondCommand(long epochSecond, Timestamp aggregationRequestTimestamp) {
    return SecondApi.AggregateSecondCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setEpochSecond(epochSecond)
        .setAggregateRequestTimestamp(aggregationRequestTimestamp)
        .setPaymentId("payment-1")
        .build();
  }

  static SecondApi.StripedSecondAggregationCommand stripedSecondAggregationCommand(long epochSecond, int stripe, Collection<TransactionMerchantKey.MoneyMovement> moneyMovements, Timestamp aggregateRequestTimestamp) {
    return SecondApi.StripedSecondAggregationCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setEpochSecond(epochSecond)
        .setStripe(stripe)
        .addAllMoneyMovements(moneyMovements)
        .setLastUpdateTimestamp(aggregateRequestTimestamp)
        .setAggregateRequestTimestamp(aggregateRequestTimestamp)
        .setPaymentId("payment-1")
        .build();
  }
}

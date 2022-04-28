package io.aggregator.entity;

import io.aggregator.TimeTo;
import io.aggregator.api.SecondApi;

import org.junit.Test;

import static org.junit.Assert.*;

import com.google.protobuf.Timestamp;

import java.util.Collection;
import java.util.List;

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
  public void addSubSecondTest() {
    SecondTestKit testKit = SecondTestKit.of(Second::new);

    var epochSubSecond = TimeTo.fromNow().toEpochSubSecond();
    var nextEpochSubSecond = TimeTo.fromEpochSubSecond(epochSubSecond).plus().seconds(1).toEpochSubSecond();
    var epochSecond = TimeTo.fromEpochSubSecond(epochSubSecond).toEpochSecond();

    var response = testKit.addSubSecond(addSubSecondCommand(epochSubSecond));

    var secondActivated = response.getNextEventOfType(SecondEntity.SecondActivated.class);
    var subSecondAdded = response.getNextEventOfType(SecondEntity.SubSecondAdded.class);

    assertEquals("merchant-1", secondActivated.getMerchantKey().getMerchantId());
    assertEquals(epochSecond, secondActivated.getEpochSecond());

    assertEquals("merchant-1", subSecondAdded.getMerchantKey().getMerchantId());
    assertEquals(epochSubSecond, subSecondAdded.getEpochSubSecond());

    var state = testKit.getState();

    assertEquals("merchant-1", state.getMerchantKey().getMerchantId());
    assertEquals(epochSecond, state.getEpochSecond());
    assertEquals(1, state.getActiveSubSecondsCount());

    var activeSubSecond = state.getActiveSubSeconds(0);

    assertEquals(epochSubSecond, activeSubSecond.getEpochSubSecond());

    response = testKit.addSubSecond(addSubSecondCommand(nextEpochSubSecond));

    subSecondAdded = response.getNextEventOfType(SecondEntity.SubSecondAdded.class);

    assertEquals("merchant-1", subSecondAdded.getMerchantKey().getMerchantId());
    assertEquals(nextEpochSubSecond, subSecondAdded.getEpochSubSecond());

    state = testKit.getState();

    assertEquals("merchant-1", state.getMerchantKey().getMerchantId());
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

    testKit.addSubSecond(addSubSecondCommand(epochSubSecond));
    testKit.addSubSecond(addSubSecondCommand(nextEpochSubSecond));

    var response = testKit.aggregateSecond(aggregateSecondCommand(epochSecond, now));

    var secondAggregationRequested = response.getNextEventOfType(SecondEntity.SecondAggregationRequested.class);

    assertEquals("merchant-1", secondAggregationRequested.getMerchantKey().getMerchantId());
    assertEquals(epochSecond, secondAggregationRequested.getEpochSecond());
    assertEquals(now, secondAggregationRequested.getAggregateRequestTimestamp());
    assertEquals(2, secondAggregationRequested.getEpochSubSecondsCount());
    assertEquals(epochSubSecond, secondAggregationRequested.getEpochSubSeconds(0));
    assertEquals(nextEpochSubSecond, secondAggregationRequested.getEpochSubSeconds(1));
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

    var epochSubSecond = TimeTo.fromNow().toEpochSubSecond();
    var epochSecond = TimeTo.fromEpochSubSecond(epochSubSecond).toEpochSecond();
    var now = TimeTo.fromEpochSubSecond(epochSubSecond).toTimestamp();

    var response = testKit.aggregateSecond(aggregateSecondCommand(epochSecond, now));

    var secondAggregationRequested = response.getNextEventOfType(SecondEntity.SecondAggregated.class);

    assertEquals("merchant-1", secondAggregationRequested.getMerchantKey().getMerchantId());
    assertEquals(epochSecond, secondAggregationRequested.getEpochSecond());
    assertEquals(now, secondAggregationRequested.getAggregateRequestTimestamp());
    assertEquals(0, secondAggregationRequested.getMoneyMovementsCount());
    assertEquals("payment-1", secondAggregationRequested.getPaymentId());
  }

  @Test
  public void subSecondAggregationTest() {
    SecondTestKit testKit = SecondTestKit.of(Second::new);

    var epochSubSecond = TimeTo.fromNow().toEpochSubSecond();
    var nextEpochSubSecond = TimeTo.fromEpochSubSecond(epochSubSecond).plus().subSeconds(1).toEpochSubSecond();
    var epochSecond = TimeTo.fromEpochSubSecond(epochSubSecond).toEpochSecond();
    var aggregateRequestTimestamp = TimeTo.fromEpochSubSecond(epochSubSecond).toTimestamp();

    testKit.addSubSecond(addSubSecondCommand(epochSubSecond));
    testKit.addSubSecond(addSubSecondCommand(nextEpochSubSecond));

    testKit.aggregateSecond(aggregateSecondCommand(epochSecond, aggregateRequestTimestamp));

    Collection<TransactionMerchantKey.MoneyMovement> moneyMovements = List.of(
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("BBB").setAmount(1.22).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("BBB").setAmount(2.20).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("AAA").setAmount(1.22).build()
    );
    var response = testKit.subSecondAggregation(subSecondAggregationCommand(epochSubSecond, moneyMovements, aggregateRequestTimestamp));

    var activeSubSecondAggregated = response.getNextEventOfType(SecondEntity.ActiveSubSecondAggregated.class);

    assertEquals("merchant-1", activeSubSecondAggregated.getMerchantKey().getMerchantId());
    assertEquals(epochSubSecond, activeSubSecondAggregated.getEpochSubSecond());
    assertEquals(moneyMovements.size(), activeSubSecondAggregated.getMoneyMovementsCount());
    assertTrue(activeSubSecondAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp, activeSubSecondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, activeSubSecondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeSubSecondAggregated.getPaymentId());

    moneyMovements = List.of(
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("AAA").setAmount(3.33).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("DDD").setAmount(4.44).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("BBB").setAmount(1.55).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("CCC").setAmount(2.55).build()
    );
    response = testKit.subSecondAggregation(subSecondAggregationCommand(nextEpochSubSecond, moneyMovements, aggregateRequestTimestamp));

    var secondAggregated = response.getNextEventOfType(SecondEntity.SecondAggregated.class);
    activeSubSecondAggregated = response.getNextEventOfType(SecondEntity.ActiveSubSecondAggregated.class);

    assertEquals("merchant-1", activeSubSecondAggregated.getMerchantKey().getMerchantId());
    assertEquals(nextEpochSubSecond, activeSubSecondAggregated.getEpochSubSecond());
    assertEquals(moneyMovements.size(), activeSubSecondAggregated.getMoneyMovementsCount());
    assertTrue(activeSubSecondAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp, activeSubSecondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, activeSubSecondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeSubSecondAggregated.getPaymentId());

    moneyMovements = List.of(
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("BBB").setAmount(1.22).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("DDD").setAmount(4.44).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("AAA").setAmount(1.22).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("CCC").setAmount(2.55).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("AAA").setAmount(3.33).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("BBB").setAmount(3.75).build()
    );

    assertEquals("merchant-1", secondAggregated.getMerchantKey().getMerchantId());
    assertEquals(epochSecond, secondAggregated.getEpochSecond());
    assertEquals(moneyMovements.size(), secondAggregated.getMoneyMovementsCount());
    assertTrue(secondAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp, secondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, secondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", secondAggregated.getPaymentId());

    // this sequence re-activates the sub-second and second aggregation
    aggregateRequestTimestamp = TimeTo.fromEpochSubSecond(epochSubSecond).plus().minutes(1).toTimestamp();

    response = testKit.addSubSecond(addSubSecondCommand(epochSubSecond));

    response.getNextEventOfType(SecondEntity.SecondActivated.class);
    response.getNextEventOfType(SecondEntity.SubSecondAdded.class);

    testKit.aggregateSecond(aggregateSecondCommand(epochSecond, aggregateRequestTimestamp));

    moneyMovements = List.of(
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("BBB").setAmount(6.11).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("DDD").setAmount(3.11).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("AAA").setAmount(1.22).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("CCC").setAmount(4.33).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("BBB").setAmount(5.44).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("DDD").setAccountTo("BBB").setAmount(6.55).build()
    );
    response = testKit.subSecondAggregation(subSecondAggregationCommand(epochSubSecond, moneyMovements, aggregateRequestTimestamp));

    secondAggregated = response.getNextEventOfType(SecondEntity.SecondAggregated.class);
    activeSubSecondAggregated = response.getNextEventOfType(SecondEntity.ActiveSubSecondAggregated.class);

    assertEquals("merchant-1", activeSubSecondAggregated.getMerchantKey().getMerchantId());
    assertEquals(epochSubSecond, activeSubSecondAggregated.getEpochSubSecond());
    assertEquals(moneyMovements.size(), activeSubSecondAggregated.getMoneyMovementsCount());
    assertTrue(activeSubSecondAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp, activeSubSecondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, activeSubSecondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeSubSecondAggregated.getPaymentId());

    assertEquals("merchant-1", secondAggregated.getMerchantKey().getMerchantId());
    assertEquals(epochSecond, secondAggregated.getEpochSecond());
    assertEquals(moneyMovements.size(), activeSubSecondAggregated.getMoneyMovementsCount());
    assertTrue(activeSubSecondAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp, secondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, secondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", secondAggregated.getPaymentId());
  }

  @Test
  public void multipleAggregationRequestsWithStaggeredResponses() {
    SecondTestKit testKit = SecondTestKit.of(Second::new);

    var epochSecond = TimeTo.fromNow().toEpochSecond();
    var epochSubSecond1 = TimeTo.fromEpochSecond(epochSecond).toEpochSubSecond();
    var epochSubSecond2 = TimeTo.fromEpochSubSecond(epochSubSecond1).plus().subSeconds(1).toEpochSubSecond();
    var aggregateRequestTimestamp1 = TimeTo.fromEpochSubSecond(epochSubSecond1).toTimestamp();
    var aggregateRequestTimestamp2 = TimeTo.fromEpochSubSecond(epochSubSecond1).plus().seconds(1).toTimestamp();

    testKit.addSubSecond(addSubSecondCommand(epochSubSecond1));
    testKit.aggregateSecond(aggregateSecondCommand(epochSecond, aggregateRequestTimestamp1));

    testKit.addSubSecond(addSubSecondCommand(epochSubSecond2));
    testKit.aggregateSecond(aggregateSecondCommand(epochSecond, aggregateRequestTimestamp2));

    Collection<TransactionMerchantKey.MoneyMovement> moneyMovements = List.of(
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("BBB").setAmount(1.22).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("BBB").setAmount(2.20).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("AAA").setAmount(1.22).build()
    );
    var response = testKit.subSecondAggregation(subSecondAggregationCommand(epochSubSecond2, moneyMovements, aggregateRequestTimestamp2));

    var secondAggregated = response.getNextEventOfType(SecondEntity.SecondAggregated.class);
    var activeSubSecondAggregated = response.getNextEventOfType(SecondEntity.ActiveSubSecondAggregated.class);

    assertEquals(epochSecond, secondAggregated.getEpochSecond());
    assertEquals(moneyMovements.size(), secondAggregated.getMoneyMovementsCount());
    assertTrue(secondAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp2, secondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp2, secondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", secondAggregated.getPaymentId());

    assertEquals(epochSubSecond2, activeSubSecondAggregated.getEpochSubSecond());
    assertEquals(moneyMovements.size(), activeSubSecondAggregated.getMoneyMovementsCount());
    assertTrue(activeSubSecondAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp2, activeSubSecondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp2, activeSubSecondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeSubSecondAggregated.getPaymentId());

    moneyMovements = List.of(
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("CCC").setAmount(3.20).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("DDD").setAmount(4.30).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("DDD").setAmount(4.50).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("DDD").setAccountTo("AAA").setAmount(5.80).build()
    );
    response = testKit.subSecondAggregation(subSecondAggregationCommand(epochSubSecond1, moneyMovements, aggregateRequestTimestamp1));

    secondAggregated = response.getNextEventOfType(SecondEntity.SecondAggregated.class);
    activeSubSecondAggregated = response.getNextEventOfType(SecondEntity.ActiveSubSecondAggregated.class);

    assertEquals(epochSecond, secondAggregated.getEpochSecond());
    assertEquals(moneyMovements.size(), secondAggregated.getMoneyMovementsCount());
    assertTrue(secondAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp1, secondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp1, secondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", secondAggregated.getPaymentId());

    assertEquals(epochSubSecond1, activeSubSecondAggregated.getEpochSubSecond());
    assertEquals(moneyMovements.size(), activeSubSecondAggregated.getMoneyMovementsCount());
    assertTrue(activeSubSecondAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp1, activeSubSecondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp1, activeSubSecondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeSubSecondAggregated.getPaymentId());
  }

  static SecondApi.AddSubSecondCommand addSubSecondCommand(long epochSubSecond) {
    return SecondApi.AddSubSecondCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setEpochSecond(TimeTo.fromEpochSubSecond(epochSubSecond).toEpochSecond())
        .setEpochSubSecond(epochSubSecond)
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

  static SecondApi.SubSecondAggregationCommand subSecondAggregationCommand(long epochSubSecond, Collection<TransactionMerchantKey.MoneyMovement> moneyMovements, Timestamp aggregateRequestTimestamp) {
    return SecondApi.SubSecondAggregationCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setEpochSecond(TimeTo.fromEpochSubSecond(epochSubSecond).toEpochSecond())
        .setEpochSubSecond(epochSubSecond)
        .addAllMoneyMovements(moneyMovements)
        .setLastUpdateTimestamp(aggregateRequestTimestamp)
        .setAggregateRequestTimestamp(aggregateRequestTimestamp)
        .setPaymentId("payment-1")
        .build();
  }
}

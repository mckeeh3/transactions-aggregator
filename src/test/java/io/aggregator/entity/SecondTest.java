package io.aggregator.entity;

import io.aggregator.TimeTo;
import io.aggregator.api.SecondApi;

import org.junit.Test;

import static org.junit.Assert.*;

import com.google.protobuf.Timestamp;

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
  public void addSubSubSecondTest() {
    SecondTestKit testKit = SecondTestKit.of(Second::new);

    var epochSubSecond = TimeTo.fromNow().toEpochSubSecond();
    var nextEpochSubSecond = TimeTo.fromEpochSubSecond(epochSubSecond).plus().seconds(1).toEpochSubSecond();
    var epochSecond = TimeTo.fromEpochSubSecond(epochSubSecond).toEpochSecond();

    var response = testKit.activateSubSecond(activateSubSecondCommand(epochSubSecond));

    var secondActivated = response.getNextEventOfType(SecondEntity.SecondActivated.class);
    var subSecondAdded = response.getNextEventOfType(SecondEntity.SubSecondAdded.class);

    assertEquals("merchant-1", secondActivated.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", secondActivated.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", secondActivated.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", secondActivated.getMerchantKey().getAccountTo());
    assertEquals(epochSecond, secondActivated.getEpochSecond());

    assertEquals("merchant-1", subSecondAdded.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", subSecondAdded.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", subSecondAdded.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", subSecondAdded.getMerchantKey().getAccountTo());
    assertEquals(epochSubSecond, subSecondAdded.getEpochSubSecond());

    var state = testKit.getState();

    assertEquals("merchant-1", state.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", state.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", state.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", state.getMerchantKey().getAccountTo());
    assertEquals(epochSecond, state.getEpochSecond());
    assertEquals(1, state.getActiveSubSecondsCount());

    var activeSubSecond = state.getActiveSubSeconds(0);

    assertEquals(epochSubSecond, activeSubSecond.getEpochSubSecond());

    response = testKit.activateSubSecond(activateSubSecondCommand(nextEpochSubSecond));

    subSecondAdded = response.getNextEventOfType(SecondEntity.SubSecondAdded.class);

    assertEquals("merchant-1", subSecondAdded.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", subSecondAdded.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", subSecondAdded.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", subSecondAdded.getMerchantKey().getAccountTo());
    assertEquals(nextEpochSubSecond, subSecondAdded.getEpochSubSecond());

    state = testKit.getState();

    assertEquals("merchant-1", state.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", state.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", state.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", state.getMerchantKey().getAccountTo());
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

    testKit.activateSubSecond(activateSubSecondCommand(epochSubSecond));
    testKit.activateSubSecond(activateSubSecondCommand(nextEpochSubSecond));

    var response = testKit.aggregateSecond(aggregateSecondCommand(epochSecond, now));

    var secondAggregationRequested = response.getNextEventOfType(SecondEntity.SecondAggregationRequested.class);

    assertEquals("merchant-1", secondAggregationRequested.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", secondAggregationRequested.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", secondAggregationRequested.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", secondAggregationRequested.getMerchantKey().getAccountTo());
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
    assertEquals("service-code-1", secondAggregationRequested.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", secondAggregationRequested.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", secondAggregationRequested.getMerchantKey().getAccountTo());
    assertEquals(epochSecond, secondAggregationRequested.getEpochSecond());
    assertEquals(now, secondAggregationRequested.getAggregateRequestTimestamp());
    assertEquals(0, secondAggregationRequested.getTransactionCount());
    assertEquals(0.0, secondAggregationRequested.getTransactionTotalAmount(), 0.0);
    assertEquals("payment-1", secondAggregationRequested.getPaymentId());
  }

  @Test
  public void subSubSecondAggregationTest() {
    SecondTestKit testKit = SecondTestKit.of(Second::new);

    var epochSubSecond = TimeTo.fromNow().toEpochSubSecond();
    var nextEpochSubSecond = TimeTo.fromEpochSubSecond(epochSubSecond).plus().subSeconds(1).toEpochSubSecond();
    var epochSecond = TimeTo.fromEpochSubSecond(epochSubSecond).toEpochSecond();
    var aggregateRequestTimestamp = TimeTo.fromEpochSubSecond(epochSubSecond).toTimestamp();

    testKit.activateSubSecond(activateSubSecondCommand(epochSubSecond));
    testKit.activateSubSecond(activateSubSecondCommand(nextEpochSubSecond));

    testKit.aggregateSecond(aggregateSecondCommand(epochSecond, aggregateRequestTimestamp));

    var response = testKit.subSecondAggregation(subSecondAggregationCommand(epochSubSecond, 123.45, 10, aggregateRequestTimestamp));

    var activeSubSecondAggregated = response.getNextEventOfType(SecondEntity.ActiveSubSecondAggregated.class);

    assertEquals("merchant-1", activeSubSecondAggregated.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", activeSubSecondAggregated.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", activeSubSecondAggregated.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", activeSubSecondAggregated.getMerchantKey().getAccountTo());
    assertEquals(epochSubSecond, activeSubSecondAggregated.getEpochSubSecond());
    assertEquals(123.45, activeSubSecondAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(10, activeSubSecondAggregated.getTransactionCount());
    assertEquals(aggregateRequestTimestamp, activeSubSecondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, activeSubSecondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeSubSecondAggregated.getPaymentId());

    response = testKit.subSecondAggregation(subSecondAggregationCommand(nextEpochSubSecond, 678.90, 20, aggregateRequestTimestamp));

    var secondAggregated = response.getNextEventOfType(SecondEntity.SecondAggregated.class);
    activeSubSecondAggregated = response.getNextEventOfType(SecondEntity.ActiveSubSecondAggregated.class);

    assertEquals("merchant-1", activeSubSecondAggregated.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", activeSubSecondAggregated.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", activeSubSecondAggregated.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", activeSubSecondAggregated.getMerchantKey().getAccountTo());
    assertEquals(nextEpochSubSecond, activeSubSecondAggregated.getEpochSubSecond());
    assertEquals(678.90, activeSubSecondAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(20, activeSubSecondAggregated.getTransactionCount());
    assertEquals(aggregateRequestTimestamp, activeSubSecondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, activeSubSecondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeSubSecondAggregated.getPaymentId());

    assertEquals("merchant-1", secondAggregated.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", secondAggregated.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", secondAggregated.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", secondAggregated.getMerchantKey().getAccountTo());
    assertEquals(epochSecond, secondAggregated.getEpochSecond());
    assertEquals(123.45 + 678.90, secondAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(10 + 20, secondAggregated.getTransactionCount());
    assertEquals(aggregateRequestTimestamp, secondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, secondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", secondAggregated.getPaymentId());

    // this sequence re-activates the sub-second and second aggregation
    aggregateRequestTimestamp = TimeTo.fromEpochSubSecond(epochSubSecond).plus().minutes(1).toTimestamp();

    response = testKit.activateSubSecond(activateSubSecondCommand(epochSubSecond));

    response.getNextEventOfType(SecondEntity.SecondActivated.class);
    response.getNextEventOfType(SecondEntity.SubSecondAdded.class);

    testKit.aggregateSecond(aggregateSecondCommand(epochSecond, aggregateRequestTimestamp));

    response = testKit.subSecondAggregation(subSecondAggregationCommand(epochSubSecond, 543.21, 321, aggregateRequestTimestamp));

    secondAggregated = response.getNextEventOfType(SecondEntity.SecondAggregated.class);
    activeSubSecondAggregated = response.getNextEventOfType(SecondEntity.ActiveSubSecondAggregated.class);

    assertEquals("merchant-1", activeSubSecondAggregated.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", activeSubSecondAggregated.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", activeSubSecondAggregated.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", activeSubSecondAggregated.getMerchantKey().getAccountTo());
    assertEquals(epochSubSecond, activeSubSecondAggregated.getEpochSubSecond());
    assertEquals(543.21, activeSubSecondAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(321, activeSubSecondAggregated.getTransactionCount());
    assertEquals(aggregateRequestTimestamp, activeSubSecondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, activeSubSecondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeSubSecondAggregated.getPaymentId());

    assertEquals("merchant-1", secondAggregated.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", secondAggregated.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", secondAggregated.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", secondAggregated.getMerchantKey().getAccountTo());
    assertEquals(epochSecond, secondAggregated.getEpochSecond());
    assertEquals(543.21, secondAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(321, secondAggregated.getTransactionCount());
    assertEquals(aggregateRequestTimestamp, secondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, secondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", secondAggregated.getPaymentId());
  }

  static SecondApi.ActivateSubSecondCommand activateSubSecondCommand(long epochSubSecond) {
    return SecondApi.ActivateSubSecondCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setServiceCode("service-code-1")
        .setAccountFrom("account-from-1")
        .setAccountTo("account-to-1")
        .setEpochSecond(TimeTo.fromEpochSubSecond(epochSubSecond).toEpochSecond())
        .setEpochSubSecond(epochSubSecond)
        .build();

  }

  static SecondApi.AggregateSecondCommand aggregateSecondCommand(long epochSecond, Timestamp aggregationRequestTimestamp) {
    return SecondApi.AggregateSecondCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setServiceCode("service-code-1")
        .setAccountFrom("account-from-1")
        .setAccountTo("account-to-1")
        .setEpochSecond(epochSecond)
        .setAggregateRequestTimestamp(aggregationRequestTimestamp)
        .setPaymentId("payment-1")
        .build();
  }

  static SecondApi.SubSecondAggregationCommand subSecondAggregationCommand(long epochSubSecond, double amount, int count, Timestamp aggregateRequestTimestamp) {
    return SecondApi.SubSecondAggregationCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setServiceCode("service-code-1")
        .setAccountFrom("account-from-1")
        .setAccountTo("account-to-1")
        .setEpochSecond(TimeTo.fromEpochSubSecond(epochSubSecond).toEpochSecond())
        .setEpochSubSecond(epochSubSecond)
        .setTransactionTotalAmount(amount)
        .setTransactionCount(count)
        .setLastUpdateTimestamp(aggregateRequestTimestamp)
        .setAggregateRequestTimestamp(aggregateRequestTimestamp)
        .setPaymentId("payment-1")
        .build();
  }
}

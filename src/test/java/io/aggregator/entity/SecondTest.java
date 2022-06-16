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
  public void addSubStripedSecondTest() {
    SecondTestKit testKit = SecondTestKit.of(Second::new);

    var timestamp = TimeTo.now();
    var epochSecond = TimeTo.fromTimestamp(timestamp).toEpochSecond();
    var nextEpochSecond = TimeTo.fromTimestamp(timestamp).plus().seconds(1).toEpochSecond();
    var stripe = 3;

    var response = testKit.activateStripedSecond(activateStripedSecondCommand(epochSecond, stripe));

    var secondActivated = response.getNextEventOfType(SecondEntity.SecondActivated.class);
    var stripedSecondAdded = response.getNextEventOfType(SecondEntity.StripedSecondAdded.class);

    assertEquals("merchant-1", secondActivated.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", secondActivated.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", secondActivated.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", secondActivated.getMerchantKey().getAccountTo());
    assertEquals(epochSecond, secondActivated.getEpochSecond());

    assertEquals("merchant-1", stripedSecondAdded.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", stripedSecondAdded.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", stripedSecondAdded.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", stripedSecondAdded.getMerchantKey().getAccountTo());
    assertEquals(epochSecond, stripedSecondAdded.getEpochSecond());
    assertEquals(stripe, stripedSecondAdded.getStripe());

    var state = testKit.getState();

    assertEquals("merchant-1", state.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", state.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", state.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", state.getMerchantKey().getAccountTo());
    assertEquals(epochSecond, state.getEpochSecond());
    assertEquals(1, state.getActiveStripedSecondsCount());

    var activeStripedSecond = state.getActiveStripedSeconds(0);

    assertEquals(epochSecond, activeStripedSecond.getEpochSecond());
    assertEquals(stripe, activeStripedSecond.getStripe());

    response = testKit.activateStripedSecond(activateStripedSecondCommand(nextEpochSecond, stripe));

    stripedSecondAdded = response.getNextEventOfType(SecondEntity.StripedSecondAdded.class);

    assertEquals("merchant-1", stripedSecondAdded.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", stripedSecondAdded.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", stripedSecondAdded.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", stripedSecondAdded.getMerchantKey().getAccountTo());
    assertEquals(nextEpochSecond, stripedSecondAdded.getEpochSecond());
    assertEquals(stripe, stripedSecondAdded.getStripe());

    state = testKit.getState();

    assertEquals("merchant-1", state.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", state.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", state.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", state.getMerchantKey().getAccountTo());
    assertEquals(epochSecond, state.getEpochSecond());
    assertEquals(2, state.getActiveStripedSecondsCount());

    activeStripedSecond = state.getActiveStripedSeconds(1);

    assertEquals(nextEpochSecond, activeStripedSecond.getEpochSecond());
    assertEquals(stripe, stripedSecondAdded.getStripe());
  }

  @Test
  public void aggregateSecondTest() {
    SecondTestKit testKit = SecondTestKit.of(Second::new);

    var timestamp = TimeTo.now();
    var epochSecond = TimeTo.fromTimestamp(timestamp).toEpochSecond();
    var stripe1 = 3;
    var stripe2 = 5;

    testKit.activateStripedSecond(activateStripedSecondCommand(epochSecond, stripe1));
    testKit.activateStripedSecond(activateStripedSecondCommand(epochSecond, stripe2));

    var response = testKit.aggregateSecond(aggregateSecondCommand(epochSecond, timestamp));

    var secondAggregationRequested = response.getNextEventOfType(SecondEntity.SecondAggregationRequested.class);

    assertEquals("merchant-1", secondAggregationRequested.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", secondAggregationRequested.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", secondAggregationRequested.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", secondAggregationRequested.getMerchantKey().getAccountTo());
    assertEquals(epochSecond, secondAggregationRequested.getEpochSecond());
    assertEquals(timestamp, secondAggregationRequested.getAggregateRequestTimestamp());
    assertEquals(2, secondAggregationRequested.getStripesCount());
    assertEquals(stripe1, secondAggregationRequested.getStripes(0));
    assertEquals(stripe2, secondAggregationRequested.getStripes(1));
    assertEquals("payment-1", secondAggregationRequested.getPaymentId());

    var state = testKit.getState();

    var aggregateSecond = state.getAggregateSecondsList().stream()
        .filter(aggSec -> aggSec.getAggregateRequestTimestamp().equals(timestamp))
        .findFirst();
    assertTrue(aggregateSecond.isPresent());
    assertEquals(timestamp, aggregateSecond.get().getAggregateRequestTimestamp());
  }

  @Test
  public void aggregateSecondWithNoSecondsTest() {
    SecondTestKit testKit = SecondTestKit.of(Second::new);

    var timestamp = TimeTo.now();
    var epochSecond = TimeTo.fromTimestamp(timestamp).toEpochSecond();

    var response = testKit.aggregateSecond(aggregateSecondCommand(epochSecond, timestamp));

    var secondAggregationRequested = response.getNextEventOfType(SecondEntity.SecondAggregated.class);

    assertEquals("merchant-1", secondAggregationRequested.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", secondAggregationRequested.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", secondAggregationRequested.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", secondAggregationRequested.getMerchantKey().getAccountTo());
    assertEquals(epochSecond, secondAggregationRequested.getEpochSecond());
    assertEquals(timestamp, secondAggregationRequested.getAggregateRequestTimestamp());
    assertEquals(0, secondAggregationRequested.getTransactionCount());
    assertEquals(0.0, secondAggregationRequested.getTransactionTotalAmount(), 0.0);
    assertEquals("payment-1", secondAggregationRequested.getPaymentId());
  }

  @Test
  public void stripedSecondAggregationTest() {
    SecondTestKit testKit = SecondTestKit.of(Second::new);

    var timestamp = TimeTo.now();
    var epochSecond = TimeTo.fromTimestamp(timestamp).toEpochSecond();
    var nextEpochSecond = TimeTo.fromTimestamp(timestamp).plus().seconds(1).toEpochSecond();
    var stripe = 3;
    var aggregateRequestTimestamp = timestamp;

    testKit.activateStripedSecond(activateStripedSecondCommand(epochSecond, stripe));
    testKit.activateStripedSecond(activateStripedSecondCommand(nextEpochSecond, stripe));

    testKit.aggregateSecond(aggregateSecondCommand(epochSecond, aggregateRequestTimestamp));

    var response = testKit.stripedSecondAggregation(stripedSecondAggregationCommand(epochSecond, stripe, 123.45, 10, aggregateRequestTimestamp));

    var activeStripedSecondAggregated = response.getNextEventOfType(SecondEntity.ActiveStripedSecondAggregated.class);

    assertEquals("merchant-1", activeStripedSecondAggregated.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", activeStripedSecondAggregated.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", activeStripedSecondAggregated.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", activeStripedSecondAggregated.getMerchantKey().getAccountTo());
    assertEquals(epochSecond, activeStripedSecondAggregated.getEpochSecond());
    assertEquals(stripe, activeStripedSecondAggregated.getStripe());
    assertEquals(123.45, activeStripedSecondAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(10, activeStripedSecondAggregated.getTransactionCount());
    assertEquals(aggregateRequestTimestamp, activeStripedSecondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, activeStripedSecondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeStripedSecondAggregated.getPaymentId());

    response = testKit.stripedSecondAggregation(stripedSecondAggregationCommand(nextEpochSecond, stripe, 678.90, 20, aggregateRequestTimestamp));

    var secondAggregated = response.getNextEventOfType(SecondEntity.SecondAggregated.class);
    activeStripedSecondAggregated = response.getNextEventOfType(SecondEntity.ActiveStripedSecondAggregated.class);

    assertEquals("merchant-1", activeStripedSecondAggregated.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", activeStripedSecondAggregated.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", activeStripedSecondAggregated.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", activeStripedSecondAggregated.getMerchantKey().getAccountTo());
    assertEquals(nextEpochSecond, activeStripedSecondAggregated.getEpochSecond());
    assertEquals(stripe, activeStripedSecondAggregated.getStripe());
    assertEquals(678.90, activeStripedSecondAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(20, activeStripedSecondAggregated.getTransactionCount());
    assertEquals(aggregateRequestTimestamp, activeStripedSecondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, activeStripedSecondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeStripedSecondAggregated.getPaymentId());

    assertEquals("merchant-1", secondAggregated.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", secondAggregated.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", secondAggregated.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", secondAggregated.getMerchantKey().getAccountTo());
    assertEquals(nextEpochSecond, secondAggregated.getEpochSecond());
    assertEquals(123.45 + 678.90, secondAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(10 + 20, secondAggregated.getTransactionCount());
    assertEquals(aggregateRequestTimestamp, secondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, secondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", secondAggregated.getPaymentId());

    // this sequence re-activates the striped-second and second aggregation
    aggregateRequestTimestamp = TimeTo.fromTimestamp(timestamp).plus().minutes(1).toTimestamp();

    response = testKit.activateStripedSecond(activateStripedSecondCommand(epochSecond, stripe));

    response.getNextEventOfType(SecondEntity.SecondActivated.class);
    response.getNextEventOfType(SecondEntity.StripedSecondAdded.class);

    testKit.aggregateSecond(aggregateSecondCommand(epochSecond, aggregateRequestTimestamp));

    response = testKit.stripedSecondAggregation(stripedSecondAggregationCommand(epochSecond, stripe, 543.21, 321, aggregateRequestTimestamp));

    secondAggregated = response.getNextEventOfType(SecondEntity.SecondAggregated.class);
    activeStripedSecondAggregated = response.getNextEventOfType(SecondEntity.ActiveStripedSecondAggregated.class);

    assertEquals("merchant-1", activeStripedSecondAggregated.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", activeStripedSecondAggregated.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", activeStripedSecondAggregated.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", activeStripedSecondAggregated.getMerchantKey().getAccountTo());
    assertEquals(epochSecond, activeStripedSecondAggregated.getEpochSecond());
    assertEquals(stripe, activeStripedSecondAggregated.getStripe());
    assertEquals(543.21, activeStripedSecondAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(321, activeStripedSecondAggregated.getTransactionCount());
    assertEquals(aggregateRequestTimestamp, activeStripedSecondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, activeStripedSecondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeStripedSecondAggregated.getPaymentId());

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

  @Test
  public void multipleAggregationRequestsWithStaggeredResponses() {
    SecondTestKit testKit = SecondTestKit.of(Second::new);

    var timestamp = TimeTo.now();
    var epochSecond1 = TimeTo.fromTimestamp(timestamp).toEpochSecond();
    var epochSecond2 = TimeTo.fromTimestamp(timestamp).plus().nanos(1).toEpochSecond();
    var aggregateRequestTimestamp1 = timestamp;
    var aggregateRequestTimestamp2 = TimeTo.fromTimestamp(timestamp).plus().nanos(1).toTimestamp();
    var stripe = 3;

    testKit.activateStripedSecond(activateStripedSecondCommand(epochSecond1, stripe));
    testKit.aggregateSecond(aggregateSecondCommand(epochSecond1, aggregateRequestTimestamp1));

    testKit.activateStripedSecond(activateStripedSecondCommand(epochSecond2, stripe));
    testKit.aggregateSecond(aggregateSecondCommand(epochSecond1, aggregateRequestTimestamp2));

    var response = testKit.stripedSecondAggregation(stripedSecondAggregationCommand(epochSecond2, stripe, 123.45, 10, aggregateRequestTimestamp2));

    var secondAggregated = response.getNextEventOfType(SecondEntity.SecondAggregated.class);
    var activeStripedSecondAggregated = response.getNextEventOfType(SecondEntity.ActiveStripedSecondAggregated.class);

    assertEquals(epochSecond1, secondAggregated.getEpochSecond());
    assertEquals(123.45, secondAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(10, secondAggregated.getTransactionCount());
    assertEquals(aggregateRequestTimestamp2, secondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp2, secondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", secondAggregated.getPaymentId());

    assertEquals(epochSecond2, activeStripedSecondAggregated.getEpochSecond());
    assertEquals(123.45, activeStripedSecondAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(10, activeStripedSecondAggregated.getTransactionCount());
    assertEquals(aggregateRequestTimestamp2, activeStripedSecondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp2, activeStripedSecondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeStripedSecondAggregated.getPaymentId());

    response = testKit.stripedSecondAggregation(stripedSecondAggregationCommand(epochSecond1, stripe, 678.90, 20, aggregateRequestTimestamp1));

    secondAggregated = response.getNextEventOfType(SecondEntity.SecondAggregated.class);
    activeStripedSecondAggregated = response.getNextEventOfType(SecondEntity.ActiveStripedSecondAggregated.class);

    assertEquals(epochSecond1, secondAggregated.getEpochSecond());
    assertEquals(678.90, secondAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(20, secondAggregated.getTransactionCount());
    assertEquals(aggregateRequestTimestamp1, secondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp1, secondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", secondAggregated.getPaymentId());

    assertEquals(epochSecond1, activeStripedSecondAggregated.getEpochSecond());
    assertEquals(678.90, activeStripedSecondAggregated.getTransactionTotalAmount(), 0.0);
    assertEquals(20, activeStripedSecondAggregated.getTransactionCount());
    assertEquals(aggregateRequestTimestamp1, activeStripedSecondAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp1, activeStripedSecondAggregated.getAggregateRequestTimestamp());
    assertEquals("payment-1", activeStripedSecondAggregated.getPaymentId());
  }

  static SecondApi.ActivateStripedSecondCommand activateStripedSecondCommand(long epochSecond, int stripe) {
    return SecondApi.ActivateStripedSecondCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setServiceCode("service-code-1")
        .setAccountFrom("account-from-1")
        .setAccountTo("account-to-1")
        .setEpochSecond(epochSecond)
        .setStripe(stripe)
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

  static SecondApi.StripedSecondAggregationCommand stripedSecondAggregationCommand(long epochSecond, int stripe, double amount, int count, Timestamp aggregateRequestTimestamp) {
    return SecondApi.StripedSecondAggregationCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setServiceCode("service-code-1")
        .setAccountFrom("account-from-1")
        .setAccountTo("account-to-1")
        .setEpochSecond(epochSecond)
        .setStripe(stripe)
        .setTransactionTotalAmount(amount)
        .setTransactionCount(count)
        .setLastUpdateTimestamp(aggregateRequestTimestamp)
        .setAggregateRequestTimestamp(aggregateRequestTimestamp)
        .setPaymentId("payment-1")
        .build();
  }
}

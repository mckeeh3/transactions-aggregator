package io.aggregator.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.protobuf.Timestamp;

import org.junit.Test;

import io.aggregator.TimeTo;
import io.aggregator.api.PaymentApi;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class PaymentTest {

  @Test
  public void exampleTest() {
    // PaymentTestKit testKit = PaymentTestKit.of(Payment::new);
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
  public void aggregationRequestWithNoDaysTest() {
    PaymentTestKit testKit = PaymentTestKit.of(Payment::new);

    var response = testKit.aggregationRequest(aggregationRequestCommand(TimeTo.now()));

    assertEquals(0, response.getAllEvents().size());
    assertEquals(0, testKit.getState().getAggregationsList().size());
  }

  @Test
  public void aggregationRequestWithOneDayTest() {
    PaymentTestKit testKit = PaymentTestKit.of(Payment::new);

    var now = TimeTo.now();
    var epochDay = TimeTo.fromTimestamp(now).toEpochDay();

    var response = testKit.aggregationRequest(aggregationRequestCommand(now, epochDay));

    assertEquals(1, response.getAllEvents().size());
    var event = response.getNextEventOfType(PaymentEntity.PaymentDayAggregationRequested.class);

    assertEquals("merchant-1", event.getMerchantKey().getMerchantId());
    assertEquals(epochDay, event.getEpochDay());
    assertEquals("payment-1", event.getPaymentId());
    assertEquals(now, event.getAggregateRequestTimestamp());

    assertEquals("merchant-1", testKit.getState().getMerchantKey().getMerchantId());
    assertEquals("payment-1", testKit.getState().getPaymentId());

    assertEquals(1, testKit.getState().getAggregationsList().size());
    assertEquals(now, testKit.getState().getAggregationsList().get(0).getAggregateRequestTimestamp());
    assertEquals(1, testKit.getState().getAggregationsList().get(0).getAggregationDaysList().size());
    assertEquals(epochDay, testKit.getState().getAggregationsList().get(0).getAggregationDaysList().get(0).getEpochDay());
    assertEquals(now, testKit.getState().getAggregationsList().get(0).getAggregationDaysList().get(0).getAggregateRequestTimestamp());
  }

  @Test
  public void aggregationRequestWithMultipleDaysTest() {
    PaymentTestKit testKit = PaymentTestKit.of(Payment::new);

    var now = TimeTo.now();
    var epochDay1 = TimeTo.fromTimestamp(now).minus().days(3).toEpochDay();
    var epochDay2 = TimeTo.fromTimestamp(now).minus().days(2).toEpochDay();
    var epochDay3 = TimeTo.fromTimestamp(now).minus().days(1).toEpochDay();

    var response = testKit.aggregationRequest(aggregationRequestCommand(now, epochDay1, epochDay2, epochDay3));

    assertEquals(3, response.getAllEvents().size());
    var event1 = response.getNextEventOfType(PaymentEntity.PaymentDayAggregationRequested.class);
    var event2 = response.getNextEventOfType(PaymentEntity.PaymentDayAggregationRequested.class);
    var event3 = response.getNextEventOfType(PaymentEntity.PaymentDayAggregationRequested.class);

    assertEquals(epochDay1, event1.getEpochDay());
    assertEquals(epochDay2, event2.getEpochDay());
    assertEquals(epochDay3, event3.getEpochDay());
  }

  @Test
  public void aggregationRequestWithMultipleRequestsAndMultipleDaysTest() {
    PaymentTestKit testKit = PaymentTestKit.of(Payment::new);

    var now = TimeTo.now();
    var epochDay1 = TimeTo.fromTimestamp(now).minus().days(3).toEpochDay();
    var epochDay2 = TimeTo.fromTimestamp(now).minus().days(2).toEpochDay();
    var epochDay3 = TimeTo.fromTimestamp(now).minus().days(1).toEpochDay();
    var aggregateRequestTimestamp1 = TimeTo.fromTimestamp(now).plus().minutes(5).toTimestamp();
    var aggregateRequestTimestamp2 = TimeTo.fromTimestamp(now).plus().minutes(10).toTimestamp();
    var aggregateRequestTimestamp3 = TimeTo.fromTimestamp(now).plus().minutes(15).toTimestamp();

    testKit.aggregationRequest(aggregationRequestCommand(aggregateRequestTimestamp1, epochDay1));
    testKit.aggregationRequest(aggregationRequestCommand(aggregateRequestTimestamp2, epochDay1, epochDay2));
    testKit.aggregationRequest(aggregationRequestCommand(aggregateRequestTimestamp3, epochDay1, epochDay2, epochDay3));

    var state = testKit.getState();

    assertEquals(3, state.getAggregationsList().size());
    assertEquals(1, state.getAggregationsList().get(0).getAggregationDaysList().size());
    assertEquals(2, state.getAggregationsList().get(1).getAggregationDaysList().size());
    assertEquals(3, state.getAggregationsList().get(2).getAggregationDaysList().size());
  }

  @Test
  public void aggregationRequestWithMultipleIdempotentRequestsTest() {
    PaymentTestKit testKit = PaymentTestKit.of(Payment::new);

    var now = TimeTo.now();
    var epochDay1 = TimeTo.fromTimestamp(now).minus().days(3).toEpochDay();
    var epochDay2 = TimeTo.fromTimestamp(now).minus().days(2).toEpochDay();
    var epochDay3 = TimeTo.fromTimestamp(now).minus().days(1).toEpochDay();

    var response = testKit.aggregationRequest(aggregationRequestCommand(now, epochDay1, epochDay2, epochDay3));

    assertEquals(3, response.getAllEvents().size());

    response = testKit.aggregationRequest(aggregationRequestCommand(now, epochDay1, epochDay2, epochDay3)); // test for idempotent behavior

    assertEquals(0, response.getAllEvents().size());
  }

  @Test
  public void paymentRequestWithZeroDays() {
    PaymentTestKit testKit = PaymentTestKit.of(Payment::new);

    var now = TimeTo.now();
    var epochDay = TimeTo.fromTimestamp(now).toEpochDay();

    var response = testKit.paymentRequest(paymentRequestCommand(now, epochDay));

    assertEquals(2, response.getAllEvents().size());
    // var event = response.getNextEventOfType(PaymentEntity.PaymentDayPaymentRequested.class);

    // assertEquals("merchant-1", event.getMerchantKey().getMerchantId());
    // assertEquals("service-code-1", event.getMerchantKey().getServiceCode());
    // assertEquals("account-from-1", event.getMerchantKey().getAccountFrom());
    // assertEquals("account-to-1", event.getMerchantKey().getAccountTo());
    // assertEquals(epochDay, event.getEpochDay());
    // assertEquals("payment-1", event.getPaymentId());
    // assertEquals(now, event.getPaymentRequestTimestamp());

    // assertEquals("merchant-1", testKit.getState().getMerchantKey().getMerchantId());
    // assertEquals("service-code-1", testKit.getState().getMerchantKey().getServiceCode());
    // assertEquals("account-from-1", testKit.getState().getMerchantKey().getAccountFrom());
    // assertEquals("account-to-1", testKit.getState().getMerchantKey().getAccountTo());
    // assertEquals("payment-1", testKit.getState().getPaymentId());

    // assertEquals(1, testKit.getState().getPaymentsList().size());
    // assertEquals(now, testKit.getState().getPaymentsList().get(0).getPaymentRequestTimestamp());
    // assertEquals(1, testKit.getState().getPaymentsList().get(0).getPaymentDaysList().size());
    // assertEquals(epochDay, testKit.getState().getPaymentsList().get(0).getPaymentDaysList().get(0
  }

  @Test
  public void paymentRequestTestWithOneDay() {
    PaymentTestKit testKit = PaymentTestKit.of(Payment::new);

    var now = TimeTo.now();
    var epochDay = TimeTo.fromTimestamp(now).toEpochDay();

    var response = testKit.paymentRequest(paymentRequestCommand(now, epochDay));

    assertEquals(2, response.getAllEvents().size());

    var event = response.getNextEventOfType(PaymentEntity.PaymentRequested.class);

    response.getNextEventOfType(PaymentEntity.PaymentDayAggregationRequested.class);

    assertEquals("merchant-1", event.getMerchantKey().getMerchantId());
    assertEquals("payment-1", event.getPaymentId());
  }

  @Test
  public void paymentRequestTestWithMultipleDays() {
    PaymentTestKit testKit = PaymentTestKit.of(Payment::new);

    var now = TimeTo.now();
    var epochDay1 = TimeTo.fromTimestamp(now).minus().days(3).toEpochDay();
    var epochDay2 = TimeTo.fromTimestamp(now).minus().days(2).toEpochDay();
    var epochDay3 = TimeTo.fromTimestamp(now).minus().days(1).toEpochDay();

    var response = testKit.paymentRequest(paymentRequestCommand(now, epochDay1, epochDay2, epochDay3));

    assertEquals(4, response.getAllEvents().size());

    response.getNextEventOfType(PaymentEntity.PaymentRequested.class);
    response.getNextEventOfType(PaymentEntity.PaymentDayAggregationRequested.class);
    response.getNextEventOfType(PaymentEntity.PaymentDayAggregationRequested.class);
    response.getNextEventOfType(PaymentEntity.PaymentDayAggregationRequested.class);

    var state = testKit.getState();

    assertEquals(1, state.getAggregationsList().size());
    assertEquals(3, state.getAggregationsList().get(0).getAggregationDaysList().size());
  }

  @Test
  public void dayAggregationTestOneDayWithOneAggregationRequest() {
    PaymentTestKit testKit = PaymentTestKit.of(Payment::new);

    var now = TimeTo.now();
    var epochDay = TimeTo.fromTimestamp(now).toEpochDay();

    testKit.aggregationRequest(aggregationRequestCommand(now, epochDay));

    Collection<TransactionMerchantKey.MoneyMovement> moneyMovements = List.of(
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("BBB").setAmount(1.22).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("BBB").setAmount(2.20).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("AAA").setAmount(1.22).build()
    );
    var response = testKit.dayAggregation(dayAggregationCommand(epochDay, moneyMovements, now));

    assertEquals(1, response.getAllEvents().size());

    var event = response.getNextEventOfType(PaymentEntity.ActiveDayAggregated.class);

    assertEquals("merchant-1", event.getMerchantKey().getMerchantId());
    assertEquals("payment-1", event.getPaymentId());
    assertEquals(epochDay, event.getEpochDay());
    assertEquals(moneyMovements.size(), event.getMoneyMovementsCount());
    assertTrue(event.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(now, event.getLastUpdateTimestamp());
    assertEquals(now, event.getAggregateRequestTimestamp());

    var state = testKit.getState();

    assertEquals(1, state.getAggregationsList().size());
    assertEquals(1, state.getAggregationsList().get(0).getAggregationDaysList().size());

    var aggregationDay = state.getAggregationsList().get(0).getAggregationDaysList().get(0);

    assertEquals(epochDay, aggregationDay.getEpochDay());
    assertEquals(moneyMovements.size(), aggregationDay.getMoneyMovementsCount());
    assertTrue(aggregationDay.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(now, aggregationDay.getLastUpdateTimestamp());
    assertEquals(now, aggregationDay.getAggregateRequestTimestamp());
  }

  @Test
  public void dayAggregationTestOneDayWithOnePaymentRequest() {
    PaymentTestKit testKit = PaymentTestKit.of(Payment::new);

    var now = TimeTo.now();
    var epochDay = TimeTo.fromTimestamp(now).toEpochDay();

    testKit.paymentRequest(paymentRequestCommand(now, epochDay));

    Collection<TransactionMerchantKey.MoneyMovement> moneyMovements = List.of(
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("BBB").setAmount(1.22).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("BBB").setAmount(2.20).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("AAA").setAmount(1.22).build()
    );
    var response = testKit.dayAggregation(dayAggregationCommand(epochDay, moneyMovements, now));

    assertEquals(2, response.getAllEvents().size());

    var paymentAggregated = response.getNextEventOfType(PaymentEntity.PaymentAggregated.class);
    var activeDayAggregated = response.getNextEventOfType(PaymentEntity.ActiveDayAggregated.class);

    assertEquals("merchant-1", paymentAggregated.getMerchantKey().getMerchantId());
    assertEquals("payment-1", paymentAggregated.getPaymentId());
    assertEquals(moneyMovements.size(), paymentAggregated.getMoneyMovementsCount());
    assertTrue(paymentAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(now, paymentAggregated.getLastUpdateTimestamp());
    assertEquals(now, paymentAggregated.getAggregateRequestTimestamp());

    assertEquals("merchant-1", activeDayAggregated.getMerchantKey().getMerchantId());
    assertEquals("payment-1", activeDayAggregated.getPaymentId());
    assertEquals(epochDay, activeDayAggregated.getEpochDay());
    assertEquals(moneyMovements.size(), activeDayAggregated.getMoneyMovementsCount());
    assertTrue(activeDayAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(now, activeDayAggregated.getLastUpdateTimestamp());
    assertEquals(now, activeDayAggregated.getAggregateRequestTimestamp());

    var state = testKit.getState();

    assertEquals(1, state.getAggregationsList().size());
    assertEquals(1, state.getAggregationsList().get(0).getAggregationDaysList().size());

    var aggregationDay = state.getAggregationsList().get(0).getAggregationDaysList().get(0);

    assertEquals(epochDay, aggregationDay.getEpochDay());
    assertEquals(moneyMovements.size(), aggregationDay.getMoneyMovementsCount());
    assertTrue(aggregationDay.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(now, aggregationDay.getLastUpdateTimestamp());
    assertEquals(now, aggregationDay.getAggregateRequestTimestamp());
  }

// TODO fix the test below
//  @Test
//  public void dayAggregationTestMultipleDaysWithOnePaymentRequest() {
//    PaymentTestKit testKit = PaymentTestKit.of(Payment::new);
//
//    var now = TimeTo.now();
//    var epochDay1 = TimeTo.fromTimestamp(now).minus().days(3).toEpochDay();
//    var epochDay2 = TimeTo.fromTimestamp(now).minus().days(2).toEpochDay();
//    var epochDay3 = TimeTo.fromTimestamp(now).minus().days(1).toEpochDay();
//    var aggregateRequestTimestamp1 = TimeTo.fromTimestamp(now).plus().minutes(5).toTimestamp();
//    var aggregateRequestTimestamp2 = TimeTo.fromTimestamp(now).plus().minutes(10).toTimestamp();
//    var aggregateRequestTimestamp3 = TimeTo.fromTimestamp(now).plus().minutes(15).toTimestamp();
//
//    testKit.aggregationRequest(aggregationRequestCommand(aggregateRequestTimestamp1, epochDay1));
//    testKit.dayAggregation(dayAggregationCommand(epochDay1, 12.34, 12, aggregateRequestTimestamp1));
//
//    testKit.aggregationRequest(aggregationRequestCommand(aggregateRequestTimestamp2, epochDay1, epochDay2));
//    testKit.dayAggregation(dayAggregationCommand(epochDay1, 23.45, 23, aggregateRequestTimestamp2));
//    testKit.dayAggregation(dayAggregationCommand(epochDay2, 34.56, 34, aggregateRequestTimestamp2));
//
//    testKit.paymentRequest(paymentRequestCommand(aggregateRequestTimestamp3, epochDay1, epochDay2, epochDay3));
//    testKit.dayAggregation(dayAggregationCommand(epochDay2, 56.78, 56, aggregateRequestTimestamp3));
//    testKit.dayAggregation(dayAggregationCommand(epochDay1, 45.67, 45, aggregateRequestTimestamp3));
//    testKit.dayAggregation(dayAggregationCommand(epochDay1, 45.67, 45, aggregateRequestTimestamp3)); // test for idempotent behavior
//    var response = testKit.dayAggregation(dayAggregationCommand(epochDay3, 67.89, 67, aggregateRequestTimestamp3));
//
//    assertEquals(2, response.getAllEvents().size());
//
//    var paymentAggregated = response.getNextEventOfType(PaymentEntity.PaymentAggregated.class);
//    response.getNextEventOfType(PaymentEntity.ActiveDayAggregated.class);
//
//    assertEquals("merchant-1", paymentAggregated.getMerchantKey().getMerchantId());
//    assertEquals("payment-1", paymentAggregated.getPaymentId());
//    assertEquals(12.34 + 23.45 + 34.56 + 45.67 + 56.78 + 67.89, paymentAggregated.getTransactionTotalAmount(), 0.0);
//    assertEquals(12 + 23 + 34 + 45 + 56 + 67, paymentAggregated.getTransactionCount());
//    assertEquals(aggregateRequestTimestamp3, paymentAggregated.getLastUpdateTimestamp());
//    assertEquals(aggregateRequestTimestamp3, paymentAggregated.getAggregateRequestTimestamp());
//
//    // after payment has been completed, further aggregation requests should be ignored
//    response = testKit.aggregationRequest(aggregationRequestCommand(aggregateRequestTimestamp2, epochDay1, epochDay2));
//    assertEquals(0, response.getAllEvents().size());
//
//    response = testKit.paymentRequest(paymentRequestCommand(aggregateRequestTimestamp3, epochDay1, epochDay2, epochDay3));
//    assertEquals(0, response.getAllEvents().size());
//  }

  @Test
  public void aggregationWithOneAggregationOneDayAndOnePaymentNoDaysRequest() {
    PaymentTestKit testKit = PaymentTestKit.of(Payment::new);

    var now = TimeTo.now();
    var epochDay = TimeTo.fromTimestamp(now).minus().days(1).toEpochDay();
    var aggregateRequestTimestamp = TimeTo.fromTimestamp(now).plus().minutes(5).toTimestamp();

    testKit.aggregationRequest(aggregationRequestCommand(aggregateRequestTimestamp, epochDay));
    Collection<TransactionMerchantKey.MoneyMovement> moneyMovements = List.of(
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("AAA").setAccountTo("BBB").setAmount(1.22).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("CCC").setAccountTo("BBB").setAmount(2.20).build(),
        TransactionMerchantKey.MoneyMovement.newBuilder().setAccountFrom("BBB").setAccountTo("AAA").setAmount(1.22).build()
    );
    testKit.dayAggregation(dayAggregationCommand(epochDay, moneyMovements, aggregateRequestTimestamp));

    var response = testKit.paymentRequest(paymentRequestCommand(aggregateRequestTimestamp));

    var paymentAggregated = response.getNextEventOfType(PaymentEntity.PaymentAggregated.class);

    assertEquals("merchant-1", paymentAggregated.getMerchantKey().getMerchantId());
    assertEquals("payment-1", paymentAggregated.getPaymentId());
    assertEquals(moneyMovements.size(), paymentAggregated.getMoneyMovementsCount());
    assertTrue(paymentAggregated.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp, paymentAggregated.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, paymentAggregated.getAggregateRequestTimestamp());

    var state = testKit.getState();

    assertEquals(1, state.getAggregationsList().size());
    assertEquals(1, state.getAggregationsList().get(0).getAggregationDaysList().size());

    var aggregationDay = state.getAggregationsList().get(0).getAggregationDaysList().get(0);

    assertEquals(epochDay, aggregationDay.getEpochDay());
    assertEquals(moneyMovements.size(), aggregationDay.getMoneyMovementsCount());
    assertTrue(aggregationDay.getMoneyMovementsList().containsAll(moneyMovements));
    assertEquals(aggregateRequestTimestamp, aggregationDay.getLastUpdateTimestamp());
    assertEquals(aggregateRequestTimestamp, aggregationDay.getAggregateRequestTimestamp());
  }

  static PaymentApi.DayAggregationCommand dayAggregationCommand(long epochDay, Collection<TransactionMerchantKey.MoneyMovement> moneyMovements, Timestamp timestamp) {
    return PaymentApi.DayAggregationCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setPaymentId("payment-1")
        .setEpochDay(epochDay)
        .addAllMoneyMovements(moneyMovements)
        .setLastUpdateTimestamp(timestamp)
        .setAggregateRequestTimestamp(timestamp)
        .build();
  }

  static PaymentApi.AggregationRequestCommand aggregationRequestCommand(Timestamp timestamp, Long... epochDays) {
    return PaymentApi.AggregationRequestCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setPaymentId("payment-1")
        .setAggregateRequestTimestamp(timestamp)
        .addAllEpochDays(Arrays.asList(epochDays))
        .build();
  }

  static PaymentApi.PaymentRequestCommand paymentRequestCommand(Timestamp timestamp, Long... epochDays) {
    return PaymentApi.PaymentRequestCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setPaymentId("payment-1")
        .setAggregateRequestTimestamp(timestamp)
        .addAllEpochDays(Arrays.asList(epochDays))
        .build();
  }
}

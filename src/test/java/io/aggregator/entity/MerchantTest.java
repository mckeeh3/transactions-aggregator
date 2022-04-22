package io.aggregator.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import io.aggregator.TimeTo;
import io.aggregator.api.MerchantApi;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class MerchantTest {

  @Test
  public void exampleTest() {
    // MerchantTestKit testKit = MerchantTestKit.of(Merchant::new);
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
  public void activateDayTest() {
    MerchantTestKit testKit = MerchantTestKit.of(Merchant::new);

    var now = TimeTo.now();
    var epochDay = TimeTo.fromTimestamp(now).toEpochDay();

    var response = testKit.activateDay(activateDayCommand(epochDay));

    assertEquals(1, response.getAllEvents().size());
    var dayActivated = response.getNextEventOfType(MerchantEntity.MerchantDayActivated.class);

    assertEquals("merchant-1", dayActivated.getMerchantKey().getMerchantId());
    assertEquals(epochDay, dayActivated.getEpochDay());

    var state = testKit.getState();

    assertEquals("merchant-1", state.getMerchantKey().getMerchantId());
    assertEquals(0, state.getPaymentIdSequenceNumber());
    assertEquals(1, state.getActiveDaysCount());
  }

  @Test
  public void addDuplicateDayTest() {
    MerchantTestKit testKit = MerchantTestKit.of(Merchant::new);

    var now = TimeTo.now();
    var epochDay = TimeTo.fromTimestamp(now).toEpochDay();

    var response = testKit.activateDay(activateDayCommand(epochDay));
    response.getNextEventOfType(MerchantEntity.MerchantDayActivated.class);

    response = testKit.activateDay(activateDayCommand(epochDay));
    response.getNextEventOfType(MerchantEntity.MerchantDayActivated.class);

    // var state = testKit.getState();

    // assertEquals(1, state.getActiveDaysCount());
    // assertEquals(epochDay, state.getActiveDays(0));
  }

  @Test
  public void merchantAggregationRequestTestWithNoDays() {
    MerchantTestKit testKit = MerchantTestKit.of(Merchant::new);

    var response = testKit.merchantAggregationRequest(merchantAggregationRequestCommand());

    assertEquals(0, response.getAllEvents().size());
  }

  @Test
  @Ignore
  public void merchantAggregationRequestTestWithOneDay() {
    MerchantTestKit testKit = MerchantTestKit.of(Merchant::new);

    var now = TimeTo.now();
    var epochDay = TimeTo.fromTimestamp(now).toEpochDay();

    var response = testKit.activateDay(activateDayCommand(epochDay));

    response.getNextEventOfType(MerchantEntity.MerchantDayActivated.class);

    // assertEquals(1, testKit.getState().getActiveDaysCount());
    // assertEquals(epochDay, testKit.getState().getActiveDays(0));

    response = testKit.merchantAggregationRequest(merchantAggregationRequestCommand());

    var merchantAggregationRequested = response.getNextEventOfType(MerchantEntity.MerchantAggregationRequested.class);

    assertEquals("merchant-1", merchantAggregationRequested.getMerchantKey().getMerchantId());
    assertEquals("payment-1", merchantAggregationRequested.getPaymentId());
    // assertEquals(epochDay, merchantAggregationRequested.getEpochDay());
    assertTrue(merchantAggregationRequested.getAggregateRequestTimestamp().getSeconds() > 0);

    assertEquals(0, testKit.getState().getActiveDaysCount());
  }

  @Test
  @Ignore
  public void merchantAggregationRequestTestWithMultipleDays() {
    MerchantTestKit testKit = MerchantTestKit.of(Merchant::new);

    var now = TimeTo.now();
    var epochDay1 = TimeTo.fromTimestamp(now).toEpochDay();
    var epochDay2 = TimeTo.fromEpochDay(epochDay1).plus().days(1).toEpochDay();
    var epochDay3 = TimeTo.fromEpochDay(epochDay1).plus().days(2).toEpochDay();

    testKit.activateDay(activateDayCommand(epochDay1));
    testKit.activateDay(activateDayCommand(epochDay2));
    testKit.activateDay(activateDayCommand(epochDay3));

    assertEquals(3, testKit.getState().getActiveDaysCount());
    assertEquals(epochDay1, testKit.getState().getActiveDays(0));
    assertEquals(epochDay2, testKit.getState().getActiveDays(1));
    assertEquals(epochDay3, testKit.getState().getActiveDays(2));

    var response = testKit.merchantAggregationRequest(merchantAggregationRequestCommand());

    assertEquals(1, response.getAllEvents().size());
    var merchantAggregationRequestedDay1 = response.getNextEventOfType(MerchantEntity.MerchantAggregationRequested.class);

    // assertEquals(epochDay1, merchantAggregationRequestedDay1.getEpochDay());
    assertTrue(merchantAggregationRequestedDay1.getAggregateRequestTimestamp().getSeconds() > 0);

    assertEquals(0, testKit.getState().getActiveDaysCount());
    assertEquals(0, testKit.getState().getPaymentIdSequenceNumber());
  }

  @Test
  @Ignore
  public void merchantAggregationRequestTestWithMultiplePayments() {
    MerchantTestKit testKit = MerchantTestKit.of(Merchant::new);

    var now = TimeTo.now();
    var epochDay1 = TimeTo.fromTimestamp(now).toEpochDay();
    var epochDay2 = TimeTo.fromEpochDay(epochDay1).plus().days(1).toEpochDay();
    var epochDay3 = TimeTo.fromEpochDay(epochDay1).plus().days(2).toEpochDay();

    testKit.activateDay(activateDayCommand(epochDay1));
    testKit.activateDay(activateDayCommand(epochDay2));
    testKit.activateDay(activateDayCommand(epochDay3));

    testKit.merchantAggregationRequest(merchantAggregationRequestCommand());

    // start a new payment
    testKit.activateDay(activateDayCommand(epochDay2));
    testKit.activateDay(activateDayCommand(epochDay3));

    assertEquals(2, testKit.getState().getActiveDaysCount());
    assertEquals(epochDay2, testKit.getState().getActiveDays(0));
    assertEquals(epochDay3, testKit.getState().getActiveDays(1));

    var response = testKit.merchantAggregationRequest(merchantAggregationRequestCommand());

    var event = response.getNextEventOfType(MerchantEntity.MerchantAggregationRequested.class);

    assertEquals(2, event.getActiveDaysCount());
    assertEquals(epochDay2, event.getActiveDays(0));
    assertEquals(epochDay3, event.getActiveDays(1));

    assertEquals(0, testKit.getState().getPaymentIdSequenceNumber());
  }

  @Test
  public void merchantPaymentRequestTestWithNoDays() {
    MerchantTestKit testKit = MerchantTestKit.of(Merchant::new);

    var response = testKit.merchantPaymentRequest(merchantPaymentRequestCommand());

    var merchantPaymentRequested = response.getNextEventOfType(MerchantEntity.MerchantPaymentRequested.class);

    assertEquals("merchant-1", merchantPaymentRequested.getMerchantKey().getMerchantId());
    assertEquals("payment-1", merchantPaymentRequested.getPaymentId());
  }

  @Test
  public void merchantPaymentRequestTestWithOneDay() {
    MerchantTestKit testKit = MerchantTestKit.of(Merchant::new);

    var now = TimeTo.now();
    var epochDay = TimeTo.fromTimestamp(now).toEpochDay();

    var response = testKit.activateDay(activateDayCommand(epochDay));

    response.getNextEventOfType(MerchantEntity.MerchantDayActivated.class);

    // assertEquals(1, testKit.getState().getActiveDaysCount());
    // assertEquals(epochDay, testKit.getState().getActiveDays(0));

    response = testKit.merchantPaymentRequest(merchantPaymentRequestCommand());

    var event = response.getNextEventOfType(MerchantEntity.MerchantPaymentRequested.class);

    assertEquals("merchant-1", event.getMerchantKey().getMerchantId());
    assertEquals("payment-1", event.getPaymentId());
    // assertEquals(1, event.getActiveDaysCount());
    // assertEquals(epochDay, event.getActiveDays(0));
    assertTrue(event.getAggregateRequestTimestamp().getSeconds() > 0);

    assertEquals(0, testKit.getState().getActiveDaysCount());
    assertEquals(1, testKit.getState().getPaymentIdSequenceNumber());
  }

  @Test
  @Ignore
  public void merchantPaymentRequestTestWithMultipleDays() {
    MerchantTestKit testKit = MerchantTestKit.of(Merchant::new);

    var now = TimeTo.now();
    var epochDay1 = TimeTo.fromTimestamp(now).toEpochDay();
    var epochDay2 = TimeTo.fromEpochDay(epochDay1).plus().days(1).toEpochDay();
    var epochDay3 = TimeTo.fromEpochDay(epochDay1).plus().days(2).toEpochDay();

    testKit.activateDay(activateDayCommand(epochDay1));
    testKit.activateDay(activateDayCommand(epochDay2));
    testKit.activateDay(activateDayCommand(epochDay3));

    assertEquals(3, testKit.getState().getActiveDaysCount());
    assertEquals(epochDay1, testKit.getState().getActiveDays(0));
    assertEquals(epochDay2, testKit.getState().getActiveDays(1));
    assertEquals(epochDay3, testKit.getState().getActiveDays(2));

    var response = testKit.merchantPaymentRequest(merchantPaymentRequestCommand());

    response.getNextEventOfType(MerchantEntity.MerchantPaymentRequested.class);

    assertEquals(0, testKit.getState().getActiveDaysCount());
    assertEquals(1, testKit.getState().getPaymentIdSequenceNumber());
  }

  @Test
  @Ignore
  public void merchantPaymentRequestTestWithMultipleRequestsOnePayments() {
    MerchantTestKit testKit = MerchantTestKit.of(Merchant::new);

    var now = TimeTo.now();
    var epochDay1 = TimeTo.fromTimestamp(now).toEpochDay();
    var epochDay2 = TimeTo.fromEpochDay(epochDay1).plus().days(1).toEpochDay();
    var epochDay3 = TimeTo.fromEpochDay(epochDay1).plus().days(2).toEpochDay();

    testKit.activateDay(activateDayCommand(epochDay1));
    testKit.activateDay(activateDayCommand(epochDay2));
    testKit.activateDay(activateDayCommand(epochDay3));

    var response = testKit.merchantPaymentRequest(merchantPaymentRequestCommand());

    response.getNextEventOfType(MerchantEntity.MerchantPaymentRequested.class);

    // start a new aggregation and payment
    testKit.activateDay(activateDayCommand(epochDay2));
    testKit.activateDay(activateDayCommand(epochDay3));

    assertEquals(2, testKit.getState().getActiveDaysCount());
    assertEquals(epochDay2, testKit.getState().getActiveDays(0));
    assertEquals(epochDay3, testKit.getState().getActiveDays(1));

    response = testKit.merchantPaymentRequest(merchantPaymentRequestCommand());

    var event = response.getNextEventOfType(MerchantEntity.MerchantPaymentRequested.class);

    assertEquals(2, testKit.getState().getPaymentIdSequenceNumber());
    assertEquals("payment-2", event.getPaymentId());
  }

  static MerchantApi.ActivateDayCommand activateDayCommand(long epochDay) {
    return MerchantApi.ActivateDayCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .setEpochDay(epochDay)
        .build();
  }

  static MerchantApi.MerchantAggregationRequestCommand merchantAggregationRequestCommand() {
    return MerchantApi.MerchantAggregationRequestCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .build();
  }

  static MerchantApi.MerchantPaymentRequestCommand merchantPaymentRequestCommand() {
    return MerchantApi.MerchantPaymentRequestCommand
        .newBuilder()
        .setMerchantId("merchant-1")
        .build();
  }
}

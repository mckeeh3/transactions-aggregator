package io.aggregator.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
  public void addDayTest() {
    MerchantTestKit testKit = MerchantTestKit.of(Merchant::new);

    var now = TimeTo.now();
    var epochDay = TimeTo.fromTimestamp(now).toEpochDay();

    var response = testKit.addDay(
        MerchantApi.AddDayCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay)
            .build());

    var dayAdded = response.getNextEventOfType(MerchantEntity.MerchantDayAdded.class);

    assertEquals("merchant-1", dayAdded.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", dayAdded.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", dayAdded.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", dayAdded.getMerchantKey().getAccountTo());
    assertEquals(epochDay, dayAdded.getEpochDay());

    var state = testKit.getState();

    assertEquals("merchant-1", state.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", state.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", state.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", state.getMerchantKey().getAccountTo());
    assertEquals(0, state.getPaymentCount());
    assertEquals(1, state.getActiveDaysCount());
    assertEquals(epochDay, state.getActiveDays(0));
  }

  @Test
  public void addDuplicateDayTest() {
    MerchantTestKit testKit = MerchantTestKit.of(Merchant::new);

    var now = TimeTo.now();
    var epochDay = TimeTo.fromTimestamp(now).toEpochDay();

    var response = testKit.addDay(
        MerchantApi.AddDayCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay)
            .build());

    response.getNextEventOfType(MerchantEntity.MerchantDayAdded.class);

    response = testKit.addDay(
        MerchantApi.AddDayCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay)
            .build());

    response.getNextEventOfType(MerchantEntity.MerchantDayAdded.class);

    var state = testKit.getState();

    assertEquals(1, state.getActiveDaysCount());
    assertEquals(epochDay, state.getActiveDays(0));
  }

  @Test
  public void merchantAggregationRequestTestWithNoDays() {
    MerchantTestKit testKit = MerchantTestKit.of(Merchant::new);

    var response = testKit.merchantAggregationRequest(
        MerchantApi.MerchantAggregationRequestCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .build());

    var allEvents = response.getAllEvents();

    assertEquals(0, allEvents.size());
  }

  @Test
  public void merchantAggregationRequestTestWithOneDay() {
    MerchantTestKit testKit = MerchantTestKit.of(Merchant::new);

    var now = TimeTo.now();
    var epochDay = TimeTo.fromTimestamp(now).toEpochDay();

    var response = testKit.addDay(
        MerchantApi.AddDayCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay)
            .build());

    response.getNextEventOfType(MerchantEntity.MerchantDayAdded.class);

    assertEquals(1, testKit.getState().getActiveDaysCount());
    assertEquals(epochDay, testKit.getState().getActiveDays(0));

    response = testKit.merchantAggregationRequest(
        MerchantApi.MerchantAggregationRequestCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .build());

    var merchantAggregationRequested = response.getNextEventOfType(MerchantEntity.MerchantAggregationRequested.class);

    assertEquals("merchant-1", merchantAggregationRequested.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", merchantAggregationRequested.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", merchantAggregationRequested.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", merchantAggregationRequested.getMerchantKey().getAccountTo());
    assertEquals("payment-1", merchantAggregationRequested.getPaymentId());
    assertEquals(epochDay, merchantAggregationRequested.getEpochDay());
    assertTrue(merchantAggregationRequested.getAggregateRequestTimestamp().getSeconds() > 0);

    assertEquals(0, testKit.getState().getActiveDaysCount());
  }

  @Test
  public void merchantAggregationRequestTestWithMultipleDays() {
    MerchantTestKit testKit = MerchantTestKit.of(Merchant::new);

    var now = TimeTo.now();
    var epochDay1 = TimeTo.fromTimestamp(now).toEpochDay();
    var epochDay2 = TimeTo.fromEpochDay(epochDay1).plus().days(1).toEpochDay();
    var epochDay3 = TimeTo.fromEpochDay(epochDay1).plus().days(2).toEpochDay();

    testKit.addDay(
        MerchantApi.AddDayCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay1)
            .build());

    testKit.addDay(
        MerchantApi.AddDayCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay2)
            .build());

    testKit.addDay(
        MerchantApi.AddDayCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay3)
            .build());

    assertEquals(3, testKit.getState().getActiveDaysCount());
    assertEquals(epochDay1, testKit.getState().getActiveDays(0));
    assertEquals(epochDay2, testKit.getState().getActiveDays(1));
    assertEquals(epochDay3, testKit.getState().getActiveDays(2));

    var response = testKit.merchantAggregationRequest(
        MerchantApi.MerchantAggregationRequestCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .build());

    var merchantAggregationRequestedDay1 = response.getNextEventOfType(MerchantEntity.MerchantAggregationRequested.class);
    var merchantAggregationRequestedDay2 = response.getNextEventOfType(MerchantEntity.MerchantAggregationRequested.class);
    var merchantAggregationRequestedDay3 = response.getNextEventOfType(MerchantEntity.MerchantAggregationRequested.class);

    assertEquals(epochDay1, merchantAggregationRequestedDay1.getEpochDay());
    assertTrue(merchantAggregationRequestedDay1.getAggregateRequestTimestamp().getSeconds() > 0);

    assertEquals(epochDay2, merchantAggregationRequestedDay2.getEpochDay());
    assertTrue(merchantAggregationRequestedDay2.getAggregateRequestTimestamp().getSeconds() > 0);

    assertEquals(epochDay3, merchantAggregationRequestedDay3.getEpochDay());
    assertTrue(merchantAggregationRequestedDay3.getAggregateRequestTimestamp().getSeconds() > 0);

    assertEquals(0, testKit.getState().getActiveDaysCount());
    assertEquals(0, testKit.getState().getPaymentCount());
  }

  @Test
  public void merchantAggregationRequestTestWithMultiplePayments() {
    MerchantTestKit testKit = MerchantTestKit.of(Merchant::new);

    var now = TimeTo.now();
    var epochDay1 = TimeTo.fromTimestamp(now).toEpochDay();
    var epochDay2 = TimeTo.fromEpochDay(epochDay1).plus().days(1).toEpochDay();
    var epochDay3 = TimeTo.fromEpochDay(epochDay1).plus().days(2).toEpochDay();

    testKit.addDay(
        MerchantApi.AddDayCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay1)
            .build());

    testKit.addDay(
        MerchantApi.AddDayCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay2)
            .build());

    testKit.addDay(
        MerchantApi.AddDayCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay3)
            .build());

    testKit.merchantAggregationRequest(
        MerchantApi.MerchantAggregationRequestCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .build());

    // start a new payment
    testKit.addDay(
        MerchantApi.AddDayCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay2)
            .build());

    testKit.addDay(
        MerchantApi.AddDayCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay3)
            .build());

    assertEquals(2, testKit.getState().getActiveDaysCount());
    assertEquals(epochDay2, testKit.getState().getActiveDays(0));
    assertEquals(epochDay3, testKit.getState().getActiveDays(1));

    var response = testKit.merchantAggregationRequest(
        MerchantApi.MerchantAggregationRequestCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .build());

    var merchantAggregationRequestedDay2 = response.getNextEventOfType(MerchantEntity.MerchantAggregationRequested.class);
    var merchantAggregationRequestedDay3 = response.getNextEventOfType(MerchantEntity.MerchantAggregationRequested.class);

    assertEquals("payment-1", merchantAggregationRequestedDay2.getPaymentId());
    assertEquals(epochDay2, merchantAggregationRequestedDay2.getEpochDay());
    assertTrue(merchantAggregationRequestedDay2.getAggregateRequestTimestamp().getSeconds() > 0);

    assertEquals("payment-1", merchantAggregationRequestedDay3.getPaymentId());
    assertEquals(epochDay3, merchantAggregationRequestedDay3.getEpochDay());
    assertTrue(merchantAggregationRequestedDay3.getAggregateRequestTimestamp().getSeconds() > 0);

    assertEquals(0, testKit.getState().getPaymentCount());
  }

  @Test
  public void merchantPaymentRequestTestWithNoDays() {
    MerchantTestKit testKit = MerchantTestKit.of(Merchant::new);

    var response = testKit.merchantPaymentRequest(
        MerchantApi.MerchantPaymentRequestCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .build());

    var merchantPaymentRequested = response.getNextEventOfType(MerchantEntity.MerchantPaymentRequested.class);

    assertEquals("merchant-1", merchantPaymentRequested.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", merchantPaymentRequested.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", merchantPaymentRequested.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", merchantPaymentRequested.getMerchantKey().getAccountTo());
    assertEquals("payment-1", merchantPaymentRequested.getPaymentId());
  }

  @Test
  public void merchantPaymentRequestTestWithOneDay() {
    MerchantTestKit testKit = MerchantTestKit.of(Merchant::new);

    var now = TimeTo.now();
    var epochDay = TimeTo.fromTimestamp(now).toEpochDay();

    var response = testKit.addDay(
        MerchantApi.AddDayCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay)
            .build());

    response.getNextEventOfType(MerchantEntity.MerchantDayAdded.class);

    assertEquals(1, testKit.getState().getActiveDaysCount());
    assertEquals(epochDay, testKit.getState().getActiveDays(0));

    response = testKit.merchantPaymentRequest(
        MerchantApi.MerchantPaymentRequestCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .build());

    response.getNextEventOfType(MerchantEntity.MerchantPaymentRequested.class);
    var merchantAggregationRequested = response.getNextEventOfType(MerchantEntity.MerchantAggregationRequested.class);

    assertEquals("merchant-1", merchantAggregationRequested.getMerchantKey().getMerchantId());
    assertEquals("service-code-1", merchantAggregationRequested.getMerchantKey().getServiceCode());
    assertEquals("account-from-1", merchantAggregationRequested.getMerchantKey().getAccountFrom());
    assertEquals("account-to-1", merchantAggregationRequested.getMerchantKey().getAccountTo());
    assertEquals("payment-1", merchantAggregationRequested.getPaymentId());
    assertEquals(epochDay, merchantAggregationRequested.getEpochDay());
    assertTrue(merchantAggregationRequested.getAggregateRequestTimestamp().getSeconds() > 0);

    assertEquals(0, testKit.getState().getActiveDaysCount());
    assertEquals(1, testKit.getState().getPaymentCount());
  }

  @Test
  public void merchantPaymentRequestTestWithMultipleDays() {
    MerchantTestKit testKit = MerchantTestKit.of(Merchant::new);

    var now = TimeTo.now();
    var epochDay1 = TimeTo.fromTimestamp(now).toEpochDay();
    var epochDay2 = TimeTo.fromEpochDay(epochDay1).plus().days(1).toEpochDay();
    var epochDay3 = TimeTo.fromEpochDay(epochDay1).plus().days(2).toEpochDay();

    testKit.addDay(
        MerchantApi.AddDayCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay1)
            .build());

    testKit.addDay(
        MerchantApi.AddDayCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay2)
            .build());

    testKit.addDay(
        MerchantApi.AddDayCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay3)
            .build());

    assertEquals(3, testKit.getState().getActiveDaysCount());
    assertEquals(epochDay1, testKit.getState().getActiveDays(0));
    assertEquals(epochDay2, testKit.getState().getActiveDays(1));
    assertEquals(epochDay3, testKit.getState().getActiveDays(2));

    var response = testKit.merchantPaymentRequest(
        MerchantApi.MerchantPaymentRequestCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .build());

    response.getNextEventOfType(MerchantEntity.MerchantPaymentRequested.class);
    var merchantAggregationRequestedDay1 = response.getNextEventOfType(MerchantEntity.MerchantAggregationRequested.class);
    var merchantAggregationRequestedDay2 = response.getNextEventOfType(MerchantEntity.MerchantAggregationRequested.class);
    var merchantAggregationRequestedDay3 = response.getNextEventOfType(MerchantEntity.MerchantAggregationRequested.class);

    assertEquals(epochDay1, merchantAggregationRequestedDay1.getEpochDay());
    assertTrue(merchantAggregationRequestedDay1.getAggregateRequestTimestamp().getSeconds() > 0);

    assertEquals(epochDay2, merchantAggregationRequestedDay2.getEpochDay());
    assertTrue(merchantAggregationRequestedDay2.getAggregateRequestTimestamp().getSeconds() > 0);

    assertEquals(epochDay3, merchantAggregationRequestedDay3.getEpochDay());
    assertTrue(merchantAggregationRequestedDay3.getAggregateRequestTimestamp().getSeconds() > 0);

    assertEquals(0, testKit.getState().getActiveDaysCount());
    assertEquals(1, testKit.getState().getPaymentCount());
  }

  @Test
  public void merchantPaymentRequestTestWithMultipleRequestsOnePayments() {
    MerchantTestKit testKit = MerchantTestKit.of(Merchant::new);

    var now = TimeTo.now();
    var epochDay1 = TimeTo.fromTimestamp(now).toEpochDay();
    var epochDay2 = TimeTo.fromEpochDay(epochDay1).plus().days(1).toEpochDay();
    var epochDay3 = TimeTo.fromEpochDay(epochDay1).plus().days(2).toEpochDay();

    testKit.addDay(
        MerchantApi.AddDayCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay1)
            .build());

    testKit.addDay(
        MerchantApi.AddDayCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay2)
            .build());

    testKit.addDay(
        MerchantApi.AddDayCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay3)
            .build());

    var response = testKit.merchantAggregationRequest(
        MerchantApi.MerchantAggregationRequestCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .build());

    response.getNextEventOfType(MerchantEntity.MerchantAggregationRequested.class);
    response.getNextEventOfType(MerchantEntity.MerchantAggregationRequested.class);
    response.getNextEventOfType(MerchantEntity.MerchantAggregationRequested.class);

    // start a new aggregation and payment
    testKit.addDay(
        MerchantApi.AddDayCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay2)
            .build());

    testKit.addDay(
        MerchantApi.AddDayCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setEpochDay(epochDay3)
            .build());

    assertEquals(2, testKit.getState().getActiveDaysCount());
    assertEquals(epochDay2, testKit.getState().getActiveDays(0));
    assertEquals(epochDay3, testKit.getState().getActiveDays(1));

    response = testKit.merchantPaymentRequest(
        MerchantApi.MerchantPaymentRequestCommand
            .newBuilder()
            .setMerchantId("merchant-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .build());

    response.getNextEventOfType(MerchantEntity.MerchantPaymentRequested.class);
    var merchantAggregationRequestedDay2 = response.getNextEventOfType(MerchantEntity.MerchantAggregationRequested.class);
    var merchantAggregationRequestedDay3 = response.getNextEventOfType(MerchantEntity.MerchantAggregationRequested.class);

    assertEquals("payment-1", merchantAggregationRequestedDay2.getPaymentId());
    assertEquals(epochDay2, merchantAggregationRequestedDay2.getEpochDay());
    assertTrue(merchantAggregationRequestedDay2.getAggregateRequestTimestamp().getSeconds() > 0);

    assertEquals("payment-1", merchantAggregationRequestedDay3.getPaymentId());
    assertEquals(epochDay3, merchantAggregationRequestedDay3.getEpochDay());
    assertTrue(merchantAggregationRequestedDay3.getAggregateRequestTimestamp().getSeconds() > 0);

    assertEquals(1, testKit.getState().getPaymentCount());
  }
}

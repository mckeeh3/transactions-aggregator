package io.aggregator.entity;

import io.aggregator.TimeTo;
import io.aggregator.api.TransactionApi;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class TransactionTest {

  @Ignore
  @Test
  public void exampleTest() {
    // TransactionTestKit testKit = TransactionTestKit.of(Transaction::new);
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
  public void paymentPricedTest() {
    TransactionTestKit testKit = TransactionTestKit.of(Transaction::new);

    var now = TimeTo.now();
    var response = testKit.paymentPriced(
        TransactionApi.PaymentPricedCommand
            .newBuilder()
            .setTransactionId("transaction-1")
            .setShopId("shop-1")
            .setEventType("event-type-1")
                .addPricedItem(TransactionApi.PricedItem.newBuilder()
                        .setServiceCode("service-code-1")
                        .setPricedItemAmount(123.45)
                        .build())
            .setTimestamp(now)
            .build());

    var incidentAdded = response.getNextEventOfType(TransactionEntity.IncidentAdded.class);
    assertEquals("transaction-1", incidentAdded.getTransactionId());
    assertEquals("shop-1", incidentAdded.getShopId());
    assertEquals("event-type-1", incidentAdded.getEventType());
    assertEquals(now, incidentAdded.getIncidentTimestamp());
    assertEquals(1, incidentAdded.getTransactionIncidentList().size());
    assertEquals("service-code-1", incidentAdded.getTransactionIncidentList().get(0).getServiceCode());
    assertEquals(123.45, incidentAdded.getTransactionIncidentList().get(0).getIncidentAmount(), 0);
    assertEquals("from", incidentAdded.getTransactionIncidentList().get(0).getAccountFrom());
    assertEquals("to", incidentAdded.getTransactionIncidentList().get(0).getAccountTo());

    var state = testKit.getState();
    assertEquals("transaction-1", state.getTransactionId());
    assertEquals("shop", state.getMerchantId());
    assertEquals("shop-1", state.getShopId());
    assertEquals(1, state.getTransactionIncidentList().size());
    assertEquals("service-code-1", state.getTransactionIncidentList().get(0).getServiceCode());
    assertEquals(123.45, state.getTransactionIncidentList().get(0).getIncidentAmount(), 0);
    assertEquals("from", state.getTransactionIncidentList().get(0).getAccountFrom());
    assertEquals("to", state.getTransactionIncidentList().get(0).getAccountTo());
  }

  @Ignore
  @Test
  public void getTransactionTest() {
    TransactionTestKit testKit = TransactionTestKit.of(Transaction::new);

    testKit.paymentPriced(
        TransactionApi.PaymentPricedCommand
            .newBuilder()
            .setTransactionId("transaction-1")
            .setTimestamp(TimeTo.now())
            .build());

    var response = testKit.getTransaction(
        TransactionApi.GetTransactionRequest
            .newBuilder()
            .setTransactionId("transaction-1")
            .build());

    var transaction = response.getReply();

    assertNotNull(transaction);
    assertEquals("transaction-1", transaction.getTransactionId());
    assertEquals("merchant-1", transaction.getMerchantId());
    assertEquals(123.45, transaction.getTransactionAmount(), 0.0);
    assertTrue(transaction.getTransactionTimestamp().getSeconds() > 0);
  }
}

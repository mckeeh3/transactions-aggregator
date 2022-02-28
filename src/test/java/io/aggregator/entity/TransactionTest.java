package io.aggregator.entity;

import io.aggregator.TimeTo;
import io.aggregator.api.TransactionApi;
import org.junit.Test;

import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class TransactionTest {

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
  public void createTransactionTest() {
    TransactionTestKit testKit = TransactionTestKit.of(Transaction::new);

    var now = TimeTo.now();
    var response = testKit.createTransaction(
        TransactionApi.CreateTransactionCommand
            .newBuilder()
            .setTransactionId("transaction-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setMerchantId("merchant-1")
            .setShopId("shop-1")
            .setTransactionAmount(123.45)
            .setTransactionTimestamp(now)
            .build());

    var transactionCreated = response.getNextEventOfType(TransactionEntity.TransactionCreated.class);

    assertEquals("transaction-1", transactionCreated.getTransactionKey().getTransactionId());
    assertEquals("service-code-1", transactionCreated.getTransactionKey().getServiceCode());
    assertEquals("account-from-1", transactionCreated.getTransactionKey().getAccountFrom());
    assertEquals("account-to-1", transactionCreated.getTransactionKey().getAccountTo());
    assertEquals("merchant-1", transactionCreated.getMerchantId());
    assertEquals("shop-1", transactionCreated.getShopId());
    assertEquals(123.45, transactionCreated.getTransactionAmount(), 0.0);
    assertEquals(now, transactionCreated.getTransactionTimestamp());

    var state = testKit.getState();

    assertEquals("transaction-1", state.getTransactionKey().getTransactionId());
    assertEquals("service-code-1", state.getTransactionKey().getServiceCode());
    assertEquals("account-from-1", state.getTransactionKey().getAccountFrom());
    assertEquals("account-to-1", state.getTransactionKey().getAccountTo());
    assertEquals("merchant-1", state.getMerchantId());
    assertEquals("shop-1", state.getShopId());
    assertEquals(123.45, state.getTransactionAmount(), 0.0);
    assertEquals(now, state.getTransactionTimestamp());
  }

  @Test
  public void addPaymentTest() {
    TransactionTestKit testKit = TransactionTestKit.of(Transaction::new);

    var now = TimeTo.now();
    testKit.createTransaction(
        TransactionApi.CreateTransactionCommand
            .newBuilder()
            .setTransactionId("transaction-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setMerchantId("merchant-1")
            .setShopId("shop-1")
            .setTransactionAmount(123.45)
            .setTransactionTimestamp(now)
            .build());

    var response = testKit.addPayment(
        TransactionApi.AddPaymentCommand
            .newBuilder()
            .setTransactionId("transaction-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setPaymentId("payment-1")
            .build());

    var paymentAdded = response.getNextEventOfType(TransactionEntity.PaymentAdded.class);

    assertEquals("transaction-1", paymentAdded.getTransactionKey().getTransactionId());
    assertEquals("service-code-1", paymentAdded.getTransactionKey().getServiceCode());
    assertEquals("account-from-1", paymentAdded.getTransactionKey().getAccountFrom());
    assertEquals("account-to-1", paymentAdded.getTransactionKey().getAccountTo());
    assertEquals("payment-1", paymentAdded.getPaymentId());

    var state = testKit.getState();

    assertEquals("transaction-1", state.getTransactionKey().getTransactionId());
    assertEquals("service-code-1", state.getTransactionKey().getServiceCode());
    assertEquals("account-from-1", state.getTransactionKey().getAccountFrom());
    assertEquals("account-to-1", state.getTransactionKey().getAccountTo());
    assertEquals("merchant-1", state.getMerchantId());
    assertEquals("shop-1", state.getShopId());
    assertEquals(123.45, state.getTransactionAmount(), 0.0);
    assertEquals(now, state.getTransactionTimestamp());
    assertEquals("payment-1", state.getPaymentId());
  }

  @Test
  public void getTransactionTest() {
    TransactionTestKit testKit = TransactionTestKit.of(Transaction::new);

    testKit.createTransaction(
        TransactionApi.CreateTransactionCommand
            .newBuilder()
            .setTransactionId("transaction-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .setMerchantId("merchant-1")
            .setTransactionAmount(123.45)
            .setTransactionTimestamp(TimeTo.now())
            .build());

    var response = testKit.getTransaction(
        TransactionApi.GetTransactionRequest
            .newBuilder()
            .setTransactionId("transaction-1")
            .setServiceCode("service-code-1")
            .setAccountFrom("account-from-1")
            .setAccountTo("account-to-1")
            .build());

    var transaction = response.getReply();

    assertNotNull(transaction);
    assertEquals("transaction-1", transaction.getTransactionKey().getTransactionId());
    assertEquals("service-code-1", transaction.getTransactionKey().getServiceCode());
    assertEquals("account-from-1", transaction.getTransactionKey().getAccountFrom());
    assertEquals("account-to-1", transaction.getTransactionKey().getAccountTo());
    assertEquals("merchant-1", transaction.getMerchantId());
    assertEquals(123.45, transaction.getTransactionAmount(), 0.0);
    assertTrue(transaction.getTransactionTimestamp().getSeconds() > 0);
  }
}

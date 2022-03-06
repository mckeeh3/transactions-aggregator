package io.aggregator.action;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.aggregator.TimeTo;
import io.aggregator.api.SubSecondApi;
import io.aggregator.entity.TransactionEntity;
import io.aggregator.entity.TransactionMerchantKey;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class TransactionToSubSecondActionTest {

  @Test
  public void exampleTest() {
    // TransactionToSubSecondActionTestKit testKit = TransactionToSubSecondActionTestKit.of(TransactionToSubSecondAction::new);
    // use the testkit to execute a command
    // ActionResult<SomeResponse> result = testKit.someOperation(SomeRequest);
    // verify the response
    // SomeResponse actualResponse = result.getReply();
    // assertEquals(expectedResponse, actualResponse);
  }

  @Test
  public void onTransactionCreatedTest() {
    TransactionToSubSecondActionTestKit testKit = TransactionToSubSecondActionTestKit.of(TransactionToSubSecondAction::new);
    var timestamp = TimeTo.now();
    var epochSubSecond = TimeTo.fromTimestamp(timestamp).toEpochSubSecond();

    var result = testKit.onTransactionCreated(
        TransactionEntity.TransactionCreated
            .newBuilder()
            .setTransactionKey(
                TransactionMerchantKey.TransactionKey
                    .newBuilder()
                    .setTransactionId("transaction-1")
                    .setServiceCode("service-code-1")
                    .setAccountFrom("account-from-1")
                    .setAccountTo("account-to-1")
                    .build())
            .setMerchantId("merchant-1")
            .setTransactionAmount(1)
            .setTransactionTimestamp(timestamp)
            .setMerchantId("merchant-1")
            .setShopId("shop-1")
            .build());

    var reply = (SubSecondApi.AddTransactionCommand) result.getForward().getMessage();

    assertEquals("merchant-1", reply.getMerchantId());
    assertEquals("service-code-1", reply.getServiceCode());
    assertEquals("account-from-1", reply.getAccountFrom());
    assertEquals("account-to-1", reply.getAccountTo());
    assertEquals(epochSubSecond, reply.getEpochSubSecond());
    assertEquals(1, reply.getAmount(), 0.0);
    assertEquals("transaction-1", reply.getTransactionId());
    assertEquals(timestamp, reply.getTimestamp());
  }

  @Test
  public void ignoreOtherEventsTest() {
    // TransactionToSubSecondActionTestKit testKit = TransactionToSubSecondActionTestKit.of(TransactionToSubSecondAction::new);
    // ActionResult<Empty> result = testKit.ignoreOtherEvents(Any.newBuilder()...build());
  }
}

package io.aggregator.action;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.aggregator.TimeTo;
import io.aggregator.api.TransactionApi;
import io.aggregator.entity.SubSecondEntity;
import io.aggregator.entity.TransactionMerchantKey;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class SubSecondToTransactionActionTest {

  @Test
  public void exampleTest() {
    // SubSecondToTransactionActionTestKit testKit = SubSecondToTransactionActionTestKit.of(SubSecondToTransactionAction::new);
    // use the testkit to execute a command
    // ActionResult<SomeResponse> result = testKit.someOperation(SomeRequest);
    // verify the response
    // SomeResponse actualResponse = result.getReply();
    // assertEquals(expectedResponse, actualResponse);
  }

  @Test
  public void onTransactionPaidTest() {
    SubSecondToTransactionActionTestKit testKit = SubSecondToTransactionActionTestKit.of(SubSecondToTransactionAction::new);

    var result = testKit.onTransactionPaid(
        SubSecondEntity.TransactionPaid
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
            .setEpochSubSecond(TimeTo.fromNow().toEpochSubSecond())
            .setPaymentId("payment-1")
            .build());

    var reply = (TransactionApi.AddPaymentCommand) result.getForward().getMessage();

    assertEquals("transaction-1", reply.getTransactionId());
    assertEquals("service-code-1", reply.getServiceCode());
    assertEquals("account-from-1", reply.getAccountFrom());
    assertEquals("account-to-1", reply.getAccountTo());
    assertEquals("payment-1", reply.getPaymentId());
  }

  @Test
  public void ignoreOtherEventsTest() {
    // SubSecondToTransactionActionTestKit testKit = SubSecondToTransactionActionTestKit.of(SubSecondToTransactionAction::new);
    // ActionResult<Empty> result = testKit.ignoreOtherEvents(Any.newBuilder()...build());
  }
}

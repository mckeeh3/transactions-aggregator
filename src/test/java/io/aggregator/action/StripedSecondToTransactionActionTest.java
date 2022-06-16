package io.aggregator.action;

import akka.stream.javadsl.Source;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import io.aggregator.TimeTo;
import io.aggregator.action.StripedSecondToTransactionAction;
import io.aggregator.action.StripedSecondToTransactionActionTestKit;
import io.aggregator.api.TransactionApi;
import io.aggregator.entity.StripedSecondEntity;
import io.aggregator.entity.TransactionMerchantKey;
import kalix.javasdk.testkit.ActionResult;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class StripedSecondToTransactionActionTest {

  @Test
  @Ignore("to be implemented")
  public void exampleTest() {
    StripedSecondToTransactionActionTestKit service = StripedSecondToTransactionActionTestKit.of(StripedSecondToTransactionAction::new);
    // // use the testkit to execute a command
    // SomeCommand command = SomeCommand.newBuilder()...build();
    // ActionResult<SomeResponse> result = service.someOperation(command);
    // // verify the reply
    // SomeReply reply = result.getReply();
    // assertEquals(expectedReply, reply);
  }

  @Test
  public void onTransactionPaidTest() {
    StripedSecondToTransactionActionTestKit testKit = StripedSecondToTransactionActionTestKit.of(StripedSecondToTransactionAction::new);

    var result = testKit.onTransactionPaid(
        StripedSecondEntity.TransactionPaid
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
            .setEpochSecond(TimeTo.fromNow().toEpochSecond())
            .setStripe(TimeTo.stripe("transaction-1"))
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
  @Ignore("to be implemented")
  public void ignoreOtherEventsTest() {
    StripedSecondToTransactionActionTestKit testKit = StripedSecondToTransactionActionTestKit.of(StripedSecondToTransactionAction::new);
    // ActionResult<Empty> result = testKit.ignoreOtherEvents(Any.newBuilder()...build());
  }

}

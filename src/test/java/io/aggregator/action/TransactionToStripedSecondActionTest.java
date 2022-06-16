package io.aggregator.action;

import akka.stream.javadsl.Source;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import io.aggregator.TimeTo;
import io.aggregator.action.TransactionToStripedSecondAction;
import io.aggregator.action.TransactionToStripedSecondActionTestKit;
import io.aggregator.api.StripedSecondApi;
import io.aggregator.entity.TransactionEntity;
import io.aggregator.entity.TransactionMerchantKey;
import kalix.javasdk.testkit.ActionResult;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class TransactionToStripedSecondActionTest {

  @Test
  @Ignore("to be implemented")
  public void exampleTest() {
    TransactionToStripedSecondActionTestKit service = TransactionToStripedSecondActionTestKit.of(TransactionToStripedSecondAction::new);
    // // use the testkit to execute a command
    // SomeCommand command = SomeCommand.newBuilder()...build();
    // ActionResult<SomeResponse> result = service.someOperation(command);
    // // verify the reply
    // SomeReply reply = result.getReply();
    // assertEquals(expectedReply, reply);
  }

  @Test
  public void onTransactionCreatedTest() {
    TransactionToStripedSecondActionTestKit testKit = TransactionToStripedSecondActionTestKit.of(TransactionToStripedSecondAction::new);

    var timestamp = TimeTo.now();
    var stripe = TimeTo.stripe("transaction-1");

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

    var reply = (StripedSecondApi.AddTransactionCommand) result.getForward().getMessage();

    assertEquals("merchant-1", reply.getMerchantId());
    assertEquals("service-code-1", reply.getServiceCode());
    assertEquals("account-from-1", reply.getAccountFrom());
    assertEquals("account-to-1", reply.getAccountTo());
    assertEquals(timestamp.getSeconds(), reply.getEpochSecond());
    assertEquals(stripe, reply.getStripe());
    assertEquals(1, reply.getAmount(), 0.0);
    assertEquals("transaction-1", reply.getTransactionId());
    assertEquals(timestamp, reply.getTimestamp());
  }

  @Test
  @Ignore("to be implemented")
  public void ignoreOtherEventsTest() {
    TransactionToStripedSecondActionTestKit testKit = TransactionToStripedSecondActionTestKit.of(TransactionToStripedSecondAction::new);
    // ActionResult<Empty> result = testKit.ignoreOtherEvents(Any.newBuilder()...build());
  }

}

package io.aggregator.action;

import io.aggregator.TimeTo;
import io.aggregator.api.StripedSecondApi;
import io.aggregator.entity.TransactionEntity;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
  @Ignore("to be implemented")
  public void onIncidentAddedTest() {
    TransactionToStripedSecondActionTestKit testKit = TransactionToStripedSecondActionTestKit.of(TransactionToStripedSecondAction::new);

    var timestamp = TimeTo.now();
    var epochSecond = TimeTo.fromTimestamp(timestamp).toEpochSecond();

    var result = testKit.onIncidentAdded(
        TransactionEntity.IncidentAdded
            .newBuilder()
            .setTransactionId("transaction-1")
            .setEventType("event-type-1")
            .setShopId("merchant-shop")
            .setMerchantId("merchant")
            .setIncidentTimestamp(timestamp)
            .addTransactionIncident(TransactionEntity.TransactionIncident.newBuilder()
                .setServiceCode("service-code-1")
                .setIncidentAmount("123.45")
                .setAccountFrom("from")
                .setAccountTo("to")
                .build())
            .build());

    var reply = (StripedSecondApi.AddLedgerItemsCommand) result.getForward().getMessage();

    assertEquals("merchant", reply.getMerchantId());
    assertEquals(epochSecond, reply.getEpochSecond());
    assertEquals("transaction-1", reply.getTransactionId());
    assertEquals(timestamp, reply.getTimestamp());
    assertEquals(1, reply.getLedgerItemList().size());
    assertEquals("service-code-1", reply.getLedgerItemList().get(0).getServiceCode());
    assertEquals("123.45", reply.getLedgerItemList().get(0).getAmount());
    assertEquals("from", reply.getLedgerItemList().get(0).getAccountFrom());
    assertEquals("to", reply.getLedgerItemList().get(0).getAccountTo());
  }

  @Test
  @Ignore("to be implemented")
  public void ignoreOtherEventsTest() {
    TransactionToStripedSecondActionTestKit testKit = TransactionToStripedSecondActionTestKit.of(TransactionToStripedSecondAction::new);
    // ActionResult<Empty> result = testKit.ignoreOtherEvents(Any.newBuilder()...build());
  }

}

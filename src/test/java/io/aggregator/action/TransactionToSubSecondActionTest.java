package io.aggregator.action;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.aggregator.TimeTo;
import io.aggregator.api.SubSecondApi;
import io.aggregator.entity.TransactionEntity;

// This class was initially generated based on the .proto definition by Kalix tooling.
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
  public void onIncidentAddedTest() {
    TransactionToSubSecondActionTestKit testKit = TransactionToSubSecondActionTestKit.of(TransactionToSubSecondAction::new);

    var timestamp = TimeTo.now();
    var epochSubSecond = TimeTo.fromTimestamp(timestamp).toEpochSubSecond();

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

    var reply = (SubSecondApi.AddLedgerItemsCommand) result.getForward().getMessage();

    assertEquals("merchant", reply.getMerchantId());
    assertEquals(epochSubSecond, reply.getEpochSubSecond());
    assertEquals("transaction-1", reply.getTransactionId());
    assertEquals(timestamp, reply.getTimestamp());
    assertEquals(1, reply.getLedgerItemList().size());
    assertEquals("service-code-1", reply.getLedgerItemList().get(0).getServiceCode());
    assertEquals("123.45", reply.getLedgerItemList().get(0).getAmount());
    assertEquals("from", reply.getLedgerItemList().get(0).getAccountFrom());
    assertEquals("to", reply.getLedgerItemList().get(0).getAccountTo());
  }

  @Test
  public void ignoreOtherEventsTest() {
    // TransactionToSubSecondActionTestKit testKit = TransactionToSubSecondActionTestKit.of(TransactionToSubSecondAction::new);
    // ActionResult<Empty> result = testKit.ignoreOtherEvents(Any.newBuilder()...build());
  }
}

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

    testKit.createTransaction(TransactionApi.CreateTransactionCommand
        .newBuilder()
        .setTransactionKey(
            TransactionApi.TransactionKey
                .newBuilder()
                .setTransactionId("123")
                .setService("456")
                .setAccount("789")
                .build())
        .setTransactionAmount(123.45)
        .setMerchantId("merchant-1")
        .setTransactionTimestamp(TimeTo.now())
        .build());

    var state = testKit.getState();

    assertEquals(state.getTransactionKey().getTransactionId(), "123");
    assertEquals(state.getTransactionKey().getService(), "456");
    assertEquals(state.getTransactionKey().getAccount(), "789");
  }
}

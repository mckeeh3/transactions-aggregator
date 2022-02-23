package io.aggregator.action;

import akka.stream.javadsl.Source;
import com.akkaserverless.javasdk.testkit.ActionResult;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import io.aggregator.action.TransactionToSubSecondAction;
import io.aggregator.action.TransactionToSubSecondActionTestKit;
import io.aggregator.entity.TransactionEntity;
import org.junit.Test;
import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class TransactionToSubSecondActionTest {

  @Test
  public void exampleTest() {
    TransactionToSubSecondActionTestKit testKit = TransactionToSubSecondActionTestKit.of(TransactionToSubSecondAction::new);
    // use the testkit to execute a command
    // ActionResult<SomeResponse> result = testKit.someOperation(SomeRequest);
    // verify the response
    // SomeResponse actualResponse = result.getReply();
    // assertEquals(expectedResponse, actualResponse);
  }

  @Test
  public void onTransactionCreatedTest() {
    TransactionToSubSecondActionTestKit testKit = TransactionToSubSecondActionTestKit.of(TransactionToSubSecondAction::new);
    // ActionResult<Empty> result = testKit.onTransactionCreated(TransactionEntity.TransactionCreated.newBuilder()...build());
  }

  @Test
  public void ignoreOtherEventsTest() {
    TransactionToSubSecondActionTestKit testKit = TransactionToSubSecondActionTestKit.of(TransactionToSubSecondAction::new);
    // ActionResult<Empty> result = testKit.ignoreOtherEvents(Any.newBuilder()...build());
  }

}

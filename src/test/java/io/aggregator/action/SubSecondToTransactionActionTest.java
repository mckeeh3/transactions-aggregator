package io.aggregator.action;

import akka.stream.javadsl.Source;
import com.akkaserverless.javasdk.testkit.ActionResult;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import io.aggregator.action.SubSecondToTransactionAction;
import io.aggregator.action.SubSecondToTransactionActionTestKit;
import io.aggregator.entity.SubSecondEntity;
import org.junit.Test;
import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class SubSecondToTransactionActionTest {

  @Test
  public void exampleTest() {
    SubSecondToTransactionActionTestKit testKit = SubSecondToTransactionActionTestKit.of(SubSecondToTransactionAction::new);
    // use the testkit to execute a command
    // ActionResult<SomeResponse> result = testKit.someOperation(SomeRequest);
    // verify the response
    // SomeResponse actualResponse = result.getReply();
    // assertEquals(expectedResponse, actualResponse);
  }

  @Test
  public void onTransactionPaidTest() {
    SubSecondToTransactionActionTestKit testKit = SubSecondToTransactionActionTestKit.of(SubSecondToTransactionAction::new);
    // ActionResult<Empty> result = testKit.onTransactionPaid(SubSecondEntity.TransactionPaid.newBuilder()...build());
  }

  @Test
  public void ignoreOtherEventsTest() {
    SubSecondToTransactionActionTestKit testKit = SubSecondToTransactionActionTestKit.of(SubSecondToTransactionAction::new);
    // ActionResult<Empty> result = testKit.ignoreOtherEvents(Any.newBuilder()...build());
  }

}

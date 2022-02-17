package io.aggregator.action;

import akka.stream.javadsl.Source;
import com.akkaserverless.javasdk.testkit.ActionResult;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import io.aggregator.action.SubSecondToSecondAction;
import io.aggregator.action.SubSecondToSecondActionTestKit;
import io.aggregator.entity.SubSecondEntity;
import org.junit.Test;
import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class SubSecondToSecondActionTest {

  @Test
  public void exampleTest() {
    SubSecondToSecondActionTestKit testKit = SubSecondToSecondActionTestKit.of(SubSecondToSecondAction::new);
    // use the testkit to execute a command
    // ActionResult<SomeResponse> result = testKit.someOperation(SomeRequest);
    // verify the response
    // SomeResponse actualResponse = result.getReply();
    // assertEquals(expectedResponse, actualResponse);
  }

  @Test
  public void onSubSecondCreatedTest() {
    SubSecondToSecondActionTestKit testKit = SubSecondToSecondActionTestKit.of(SubSecondToSecondAction::new);
    // ActionResult<Empty> result = testKit.onSubSecondCreated(SubSecondEntity.SubSecondCreated.newBuilder()...build());
  }

  @Test
  public void onSubSecondAggregatedTest() {
    SubSecondToSecondActionTestKit testKit = SubSecondToSecondActionTestKit.of(SubSecondToSecondAction::new);
    // ActionResult<Empty> result = testKit.onSubSecondAggregated(SubSecondEntity.SubSecondAggregated.newBuilder()...build());
  }

  @Test
  public void ignoreOtherEventsTest() {
    SubSecondToSecondActionTestKit testKit = SubSecondToSecondActionTestKit.of(SubSecondToSecondAction::new);
    // ActionResult<Empty> result = testKit.ignoreOtherEvents(Any.newBuilder()...build());
  }

}

package io.aggregator.action;

import akka.stream.javadsl.Source;
import kalix.javasdk.testkit.ActionResult;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import io.aggregator.action.MinuteToSecondAction;
import io.aggregator.action.MinuteToSecondActionTestKit;
import io.aggregator.entity.MinuteEntity;
import org.junit.Test;
import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class MinuteToSecondActionTest {

  @Test
  public void exampleTest() {
    MinuteToSecondActionTestKit testKit = MinuteToSecondActionTestKit.of(MinuteToSecondAction::new);
    // use the testkit to execute a command
    // ActionResult<SomeResponse> result = testKit.someOperation(SomeRequest);
    // verify the response
    // SomeResponse actualResponse = result.getReply();
    // assertEquals(expectedResponse, actualResponse);
  }

  @Test
  public void onMinuteAggregationRequestedTest() {
    MinuteToSecondActionTestKit testKit = MinuteToSecondActionTestKit.of(MinuteToSecondAction::new);
    // ActionResult<Empty> result = testKit.onMinuteAggregationRequested(MinuteEntity.MinuteAggregationRequested.newBuilder()...build());
  }

  @Test
  public void ignoreOtherEventsTest() {
    MinuteToSecondActionTestKit testKit = MinuteToSecondActionTestKit.of(MinuteToSecondAction::new);
    // ActionResult<Empty> result = testKit.ignoreOtherEvents(Any.newBuilder()...build());
  }

}

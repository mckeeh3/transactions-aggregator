package io.aggregator.action;

import akka.stream.javadsl.Source;
import kalix.javasdk.testkit.ActionResult;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import io.aggregator.action.SecondToMinuteAction;
import io.aggregator.action.SecondToMinuteActionTestKit;
import io.aggregator.entity.SecondEntity;
import org.junit.Test;
import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class SecondToMinuteActionTest {

  @Test
  public void exampleTest() {
    SecondToMinuteActionTestKit testKit = SecondToMinuteActionTestKit.of(SecondToMinuteAction::new);
    // use the testkit to execute a command
    // ActionResult<SomeResponse> result = testKit.someOperation(SomeRequest);
    // verify the response
    // SomeResponse actualResponse = result.getReply();
    // assertEquals(expectedResponse, actualResponse);
  }

  @Test
  public void onSecondCreatedTest() {
    SecondToMinuteActionTestKit testKit = SecondToMinuteActionTestKit.of(SecondToMinuteAction::new);
    // ActionResult<Empty> result = testKit.onSecondCreated(SecondEntity.SecondCreated.newBuilder()...build());
  }

  @Test
  public void onSecondAggregatedTest() {
    SecondToMinuteActionTestKit testKit = SecondToMinuteActionTestKit.of(SecondToMinuteAction::new);
    // ActionResult<Empty> result = testKit.onSecondAggregated(SecondEntity.SecondAggregated.newBuilder()...build());
  }

  @Test
  public void ignoreOtherEventsTest() {
    SecondToMinuteActionTestKit testKit = SecondToMinuteActionTestKit.of(SecondToMinuteAction::new);
    // ActionResult<Empty> result = testKit.ignoreOtherEvents(Any.newBuilder()...build());
  }

}

package io.aggregator.action;

import akka.stream.javadsl.Source;
import kalix.javasdk.testkit.ActionResult;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import io.aggregator.action.SecondToSubSecondAction;
import io.aggregator.action.SecondToSubSecondActionTestKit;
import io.aggregator.entity.SecondEntity;
import org.junit.Test;
import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class SecondToSubSecondActionTest {

  @Test
  public void exampleTest() {
    SecondToSubSecondActionTestKit testKit = SecondToSubSecondActionTestKit.of(SecondToSubSecondAction::new);
    // use the testkit to execute a command
    // ActionResult<SomeResponse> result = testKit.someOperation(SomeRequest);
    // verify the response
    // SomeResponse actualResponse = result.getReply();
    // assertEquals(expectedResponse, actualResponse);
  }

  @Test
  public void onSecondAggregationRequestedTest() {
    SecondToSubSecondActionTestKit testKit = SecondToSubSecondActionTestKit.of(SecondToSubSecondAction::new);
    // ActionResult<Empty> result = testKit.onSecondAggregationRequested(SecondEntity.SecondAggregationRequested.newBuilder()...build());
  }

  @Test
  public void ignoreOtherEventsTest() {
    SecondToSubSecondActionTestKit testKit = SecondToSubSecondActionTestKit.of(SecondToSubSecondAction::new);
    // ActionResult<Empty> result = testKit.ignoreOtherEvents(Any.newBuilder()...build());
  }

}

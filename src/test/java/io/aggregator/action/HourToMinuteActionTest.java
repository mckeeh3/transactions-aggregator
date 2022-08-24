package io.aggregator.action;

import akka.stream.javadsl.Source;
import kalix.javasdk.testkit.ActionResult;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import io.aggregator.action.HourToMinuteAction;
import io.aggregator.action.HourToMinuteActionTestKit;
import io.aggregator.entity.HourEntity;
import org.junit.Test;
import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class HourToMinuteActionTest {

  @Test
  public void exampleTest() {
    HourToMinuteActionTestKit testKit = HourToMinuteActionTestKit.of(HourToMinuteAction::new);
    // use the testkit to execute a command
    // ActionResult<SomeResponse> result = testKit.someOperation(SomeRequest);
    // verify the response
    // SomeResponse actualResponse = result.getReply();
    // assertEquals(expectedResponse, actualResponse);
  }

  @Test
  public void onHourAggregationRequestedTest() {
    HourToMinuteActionTestKit testKit = HourToMinuteActionTestKit.of(HourToMinuteAction::new);
    // ActionResult<Empty> result = testKit.onHourAggregationRequested(HourEntity.HourAggregationRequested.newBuilder()...build());
  }

  @Test
  public void ignoreOtherEventsTest() {
    HourToMinuteActionTestKit testKit = HourToMinuteActionTestKit.of(HourToMinuteAction::new);
    // ActionResult<Empty> result = testKit.ignoreOtherEvents(Any.newBuilder()...build());
  }

}

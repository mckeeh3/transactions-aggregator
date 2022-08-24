package io.aggregator.action;

import akka.stream.javadsl.Source;
import kalix.javasdk.testkit.ActionResult;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import io.aggregator.action.DayToHourAction;
import io.aggregator.action.DayToHourActionTestKit;
import io.aggregator.entity.DayEntity;
import org.junit.Test;
import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class DayToHourActionTest {

  @Test
  public void exampleTest() {
    DayToHourActionTestKit testKit = DayToHourActionTestKit.of(DayToHourAction::new);
    // use the testkit to execute a command
    // ActionResult<SomeResponse> result = testKit.someOperation(SomeRequest);
    // verify the response
    // SomeResponse actualResponse = result.getReply();
    // assertEquals(expectedResponse, actualResponse);
  }

  @Test
  public void onDayAggregationRequestedTest() {
    DayToHourActionTestKit testKit = DayToHourActionTestKit.of(DayToHourAction::new);
    // ActionResult<Empty> result = testKit.onDayAggregationRequested(DayEntity.DayAggregationRequested.newBuilder()...build());
  }

  @Test
  public void ignoreOtherEventsTest() {
    DayToHourActionTestKit testKit = DayToHourActionTestKit.of(DayToHourAction::new);
    // ActionResult<Empty> result = testKit.ignoreOtherEvents(Any.newBuilder()...build());
  }

}

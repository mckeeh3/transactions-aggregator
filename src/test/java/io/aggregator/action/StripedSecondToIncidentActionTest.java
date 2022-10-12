package io.aggregator.action;

import akka.stream.javadsl.Source;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import io.aggregator.action.StripedSecondToIncidentAction;
import io.aggregator.action.StripedSecondToIncidentActionTestKit;
import io.aggregator.entity.StripedSecondEntity;
import kalix.javasdk.testkit.ActionResult;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class StripedSecondToIncidentActionTest {

  @Test
  @Ignore("to be implemented")
  public void exampleTest() {
    StripedSecondToIncidentActionTestKit service = StripedSecondToIncidentActionTestKit.of(StripedSecondToIncidentAction::new);
    // // use the testkit to execute a command
    // SomeCommand command = SomeCommand.newBuilder()...build();
    // ActionResult<SomeResponse> result = service.someOperation(command);
    // // verify the reply
    // SomeReply reply = result.getReply();
    // assertEquals(expectedReply, reply);
  }

  @Test
  @Ignore("to be implemented")
  public void onStripedSecondAggregatedTest() {
    StripedSecondToIncidentActionTestKit testKit = StripedSecondToIncidentActionTestKit.of(StripedSecondToIncidentAction::new);
    // ActionResult<Empty> result = testKit.onStripedSecondAggregated(StripedSecondEntity.StripedSecondAggregated.newBuilder()...build());
  }

  @Test
  @Ignore("to be implemented")
  public void ignoreOtherEventsTest() {
    StripedSecondToIncidentActionTestKit testKit = StripedSecondToIncidentActionTestKit.of(StripedSecondToIncidentAction::new);
    // ActionResult<Empty> result = testKit.ignoreOtherEvents(Any.newBuilder()...build());
  }

}

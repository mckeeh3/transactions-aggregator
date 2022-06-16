package io.aggregator.action;

import akka.stream.javadsl.Source;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import io.aggregator.action.SecondToStripedSecondAction;
import io.aggregator.action.SecondToStripedSecondActionTestKit;
import io.aggregator.entity.SecondEntity;
import kalix.javasdk.testkit.ActionResult;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class SecondToStripedSecondActionTest {

  @Test
  @Ignore("to be implemented")
  public void exampleTest() {
    SecondToStripedSecondActionTestKit service = SecondToStripedSecondActionTestKit.of(SecondToStripedSecondAction::new);
    // // use the testkit to execute a command
    // SomeCommand command = SomeCommand.newBuilder()...build();
    // ActionResult<SomeResponse> result = service.someOperation(command);
    // // verify the reply
    // SomeReply reply = result.getReply();
    // assertEquals(expectedReply, reply);
  }

  @Test
  @Ignore("to be implemented")
  public void onSecondAggregationRequestedTest() {
    SecondToStripedSecondActionTestKit testKit = SecondToStripedSecondActionTestKit.of(SecondToStripedSecondAction::new);
    // ActionResult<Empty> result = testKit.onSecondAggregationRequested(SecondEntity.SecondAggregationRequested.newBuilder()...build());
  }

  @Test
  @Ignore("to be implemented")
  public void ignoreOtherEventsTest() {
    SecondToStripedSecondActionTestKit testKit = SecondToStripedSecondActionTestKit.of(SecondToStripedSecondAction::new);
    // ActionResult<Empty> result = testKit.ignoreOtherEvents(Any.newBuilder()...build());
  }

}

package io.aggregator.action;

import akka.stream.javadsl.Source;
import com.akkaserverless.javasdk.testkit.ActionResult;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import io.aggregator.action.DayToPaymentAction;
import io.aggregator.action.DayToPaymentActionTestKit;
import io.aggregator.entity.DayEntity;
import org.junit.Test;
import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class DayToPaymentActionTest {

  @Test
  public void exampleTest() {
    DayToPaymentActionTestKit testKit = DayToPaymentActionTestKit.of(DayToPaymentAction::new);
    // use the testkit to execute a command
    // ActionResult<SomeResponse> result = testKit.someOperation(SomeRequest);
    // verify the response
    // SomeResponse actualResponse = result.getReply();
    // assertEquals(expectedResponse, actualResponse);
  }

  @Test
  public void onDayAggregatedTest() {
    DayToPaymentActionTestKit testKit = DayToPaymentActionTestKit.of(DayToPaymentAction::new);
    // ActionResult<Empty> result = testKit.onDayAggregated(DayEntity.DayAggregated.newBuilder()...build());
  }

  @Test
  public void ignoreOtherEventsTest() {
    DayToPaymentActionTestKit testKit = DayToPaymentActionTestKit.of(DayToPaymentAction::new);
    // ActionResult<Empty> result = testKit.ignoreOtherEvents(Any.newBuilder()...build());
  }

}

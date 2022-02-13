package io.aggregator.entity;

import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntity;
import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.akkaserverless.javasdk.testkit.EventSourcedResult;
import com.google.protobuf.Empty;
import io.aggregator.api.MinuteApi;
import org.junit.Test;

import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class MinuteTest {

  @Test
  public void exampleTest() {
    MinuteTestKit testKit = MinuteTestKit.of(Minute::new);
    // use the testkit to execute a command
    // of events emitted, or a final updated state:
    // EventSourcedResult<SomeResponse> result = testKit.someOperation(SomeRequest);
    // verify the emitted events
    // ExpectedEvent actualEvent = result.getNextEventOfType(ExpectedEvent.class);
    // assertEquals(expectedEvent, actualEvent)
    // verify the final state after applying the events
    // assertEquals(expectedState, testKit.getState());
    // verify the response
    // SomeResponse actualResponse = result.getReply();
    // assertEquals(expectedResponse, actualResponse);
  }

  @Test
  public void addSecondTest() {
    MinuteTestKit testKit = MinuteTestKit.of(Minute::new);
    // EventSourcedResult<Empty> result = testKit.addSecond(AddSecondCommand.newBuilder()...build());
  }


  @Test
  public void aggregateMinuteTest() {
    MinuteTestKit testKit = MinuteTestKit.of(Minute::new);
    // EventSourcedResult<Empty> result = testKit.aggregateMinute(AggregateMinuteCommand.newBuilder()...build());
  }


  @Test
  public void secondAggregationTest() {
    MinuteTestKit testKit = MinuteTestKit.of(Minute::new);
    // EventSourcedResult<Empty> result = testKit.secondAggregation(SecondAggregationCommand.newBuilder()...build());
  }

}

package io.aggregator.action;

import akka.stream.javadsl.Source;
import kalix.javasdk.testkit.ActionResult;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import io.aggregator.action.MerchantToPaymentAction;
import io.aggregator.action.MerchantToPaymentActionTestKit;
import io.aggregator.entity.MerchantEntity;
import org.junit.Test;
import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class MerchantToPaymentActionTest {

  @Test
  public void exampleTest() {
    MerchantToPaymentActionTestKit testKit = MerchantToPaymentActionTestKit.of(MerchantToPaymentAction::new);
    // use the testkit to execute a command
    // ActionResult<SomeResponse> result = testKit.someOperation(SomeRequest);
    // verify the response
    // SomeResponse actualResponse = result.getReply();
    // assertEquals(expectedResponse, actualResponse);
  }

  @Test
  public void onMerchantAggregationRequestedTest() {
    MerchantToPaymentActionTestKit testKit = MerchantToPaymentActionTestKit.of(MerchantToPaymentAction::new);
    // ActionResult<Empty> result = testKit.onMerchantAggregationRequested(MerchantEntity.MerchantAggregationRequested.newBuilder()...build());
  }

  @Test
  public void onMerchantPaymentRequestedTest() {
    MerchantToPaymentActionTestKit testKit = MerchantToPaymentActionTestKit.of(MerchantToPaymentAction::new);
    // ActionResult<Empty> result = testKit.onMerchantPaymentRequested(MerchantEntity.MerchantPaymentRequested.newBuilder()...build());
  }

  @Test
  public void ignoreOtherEventsTest() {
    MerchantToPaymentActionTestKit testKit = MerchantToPaymentActionTestKit.of(MerchantToPaymentAction::new);
    // ActionResult<Empty> result = testKit.ignoreOtherEvents(Any.newBuilder()...build());
  }

}

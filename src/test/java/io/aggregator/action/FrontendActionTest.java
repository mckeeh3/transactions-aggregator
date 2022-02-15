package io.aggregator.action;

import akka.stream.javadsl.Source;
import com.akkaserverless.javasdk.testkit.ActionResult;
import com.google.protobuf.Empty;
import io.aggregator.action.FrontendAction;
import io.aggregator.action.FrontendActionTestKit;
import io.aggregator.action.FrontendService;
import org.junit.Test;
import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class FrontendActionTest {

  @Test
  public void exampleTest() {
    FrontendActionTestKit testKit = FrontendActionTestKit.of(FrontendAction::new);
    // use the testkit to execute a command
    // ActionResult<SomeResponse> result = testKit.someOperation(SomeRequest);
    // verify the response
    // SomeResponse actualResponse = result.getReply();
    // assertEquals(expectedResponse, actualResponse);
  }

  @Test
  public void generatrTransactionsTest() {
    FrontendActionTestKit testKit = FrontendActionTestKit.of(FrontendAction::new);
    // ActionResult<Empty> result = testKit.generatrTransactions(FrontendService.GenerateTransactuionsRequest.newBuilder()...build());
  }

  @Test
  public void collectMerchantTotalsTest() {
    FrontendActionTestKit testKit = FrontendActionTestKit.of(FrontendAction::new);
    // ActionResult<Empty> result = testKit.collectMerchantTotals(FrontendService.CollectMerchantTotalsRequest.newBuilder()...build());
  }

}

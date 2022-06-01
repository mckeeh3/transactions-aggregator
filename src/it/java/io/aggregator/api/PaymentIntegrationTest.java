package io.aggregator.api;

import com.google.protobuf.Empty;
import io.aggregator.Main;
import io.aggregator.entity.PaymentEntity;
import kalix.javasdk.testkit.junit.KalixTestKitResource;
import org.junit.ClassRule;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.*;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

// Example of an integration test calling our service via the Kalix proxy
// Run all test classes ending with "IntegrationTest" using `mvn verify -Pit`
public class PaymentIntegrationTest {

  /**
   * The test kit starts both the service container and the Kalix proxy.
   */
  @ClassRule
  public static final KalixTestKitResource testKit =
    new KalixTestKitResource(Main.createKalix());

  /**
   * Use the generated gRPC client to call the service through the Kalix proxy.
   */
  private final Payment client;

  public PaymentIntegrationTest() {
    client = testKit.getGrpcClient(Payment.class);
  }

  @Test
  public void aggregationRequestOnNonExistingEntity() throws Exception {
    // TODO: set fields in command, and provide assertions to match replies
    // client.aggregationRequest(PaymentApi.AggregationRequestCommand.newBuilder().build())
    //         .toCompletableFuture().get(5, SECONDS);
  }

  @Test
  public void paymentRequestOnNonExistingEntity() throws Exception {
    // TODO: set fields in command, and provide assertions to match replies
    // client.paymentRequest(PaymentApi.PaymentRequestCommand.newBuilder().build())
    //         .toCompletableFuture().get(5, SECONDS);
  }

  @Test
  public void dayAggregationOnNonExistingEntity() throws Exception {
    // TODO: set fields in command, and provide assertions to match replies
    // client.dayAggregation(PaymentApi.DayAggregationCommand.newBuilder().build())
    //         .toCompletableFuture().get(5, SECONDS);
  }
}

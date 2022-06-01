package io.aggregator.api;

import com.google.protobuf.Empty;
import io.aggregator.Main;
import io.aggregator.entity.SecondEntity;
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
public class SecondIntegrationTest {

  /**
   * The test kit starts both the service container and the Kalix proxy.
   */
  @ClassRule
  public static final KalixTestKitResource testKit =
    new KalixTestKitResource(Main.createKalix());

  /**
   * Use the generated gRPC client to call the service through the Kalix proxy.
   */
  private final Second client;

  public SecondIntegrationTest() {
    client = testKit.getGrpcClient(Second.class);
  }

  @Test
  public void activateSubSecondOnNonExistingEntity() throws Exception {
    // TODO: set fields in command, and provide assertions to match replies
    // client.activateSubSecond(SecondApi.ActivateSubSecondCommand.newBuilder().build())
    //         .toCompletableFuture().get(5, SECONDS);
  }

  @Test
  public void aggregateSecondOnNonExistingEntity() throws Exception {
    // TODO: set fields in command, and provide assertions to match replies
    // client.aggregateSecond(SecondApi.AggregateSecondCommand.newBuilder().build())
    //         .toCompletableFuture().get(5, SECONDS);
  }

  @Test
  public void subSecondAggregationOnNonExistingEntity() throws Exception {
    // TODO: set fields in command, and provide assertions to match replies
    // client.subSecondAggregation(SecondApi.SubSecondAggregationCommand.newBuilder().build())
    //         .toCompletableFuture().get(5, SECONDS);
  }
}

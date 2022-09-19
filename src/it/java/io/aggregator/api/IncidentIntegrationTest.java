package io.aggregator.api;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.aggregator.Main;
import io.aggregator.entity.IncidentEntity;
import io.aggregator.entity.IncidentTestKit;
import io.aggregator.entity.TransactionMerchantKey;
import kalix.javasdk.testkit.junit.KalixTestKitResource;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;

import static java.util.concurrent.TimeUnit.*;
import static org.junit.Assert.assertEquals;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

// Example of an integration test calling our service via the Kalix proxy
// Run all test classes ending with "IntegrationTest" using `mvn verify -Pit`
public class IncidentIntegrationTest {

  /**
   * The test kit starts both the service container and the Kalix proxy.
   */
  @ClassRule
  public static final KalixTestKitResource testKit =
    new KalixTestKitResource(Main.createKalix());

  /**
   * Use the generated gRPC client to call the service through the Kalix proxy.
   */
  private final Incident client;

  public IncidentIntegrationTest() {
    client = testKit.getGrpcClient(Incident.class);
  }

  @Test
  public void happyPath() throws Exception {
    String incidentAmount = "100";
    String paymentId = "1234";
    Timestamp t = Timestamps.fromDate(new Date());

    TransactionMerchantKey.TransactionKey key = TransactionMerchantKey.TransactionKey.newBuilder()
            .setTransactionId("transId1")
            .setServiceCode("srv1")
            .setAccountFrom("accFrom")
            .setAccountTo("accTo")
            .build();
    var createCommand = IncidentApi.CreateIncidentCommand.newBuilder()
            .setTransactionId(key.getTransactionId())
            .setServiceCode(key.getServiceCode())
            .setAccountFrom(key.getAccountFrom())
            .setAccountTo(key.getAccountTo())
            .setIncidentAmount(incidentAmount)
            .setTimestamp(t)
            .build();
    var createResponse = client.createIncident(createCommand).toCompletableFuture().get(5,SECONDS);

    var getCommand = IncidentApi.GetIncidentRequest.newBuilder()
            .setTransactionId(key.getTransactionId())
            .setServiceCode(key.getServiceCode())
            .setAccountFrom(key.getAccountFrom())
            .setAccountTo(key.getAccountTo())
            .build();
    var get = client.getIncident(getCommand).toCompletableFuture().get(5,SECONDS);

    assertEquals(incidentAmount, get.getIncidentAmount());
    assertEquals(true,get.getPaymentId().isEmpty());
    var addPaymentCommand = IncidentApi.AddPaymentCommand.newBuilder()
            .setTransactionId(key.getTransactionId())
            .setServiceCode(key.getServiceCode())
            .setAccountFrom(key.getAccountFrom())
            .setAccountTo(key.getAccountTo())
            .setPaymentId(paymentId)
            .setTimestamp(t).build();
    var addPaymentResponse = client.addPayment(addPaymentCommand).toCompletableFuture().get(5,SECONDS);
    get = client.getIncident(getCommand).toCompletableFuture().get(5,SECONDS);
    assertEquals(incidentAmount, get.getIncidentAmount());
    assertEquals(paymentId,get.getPaymentId());


  }


}

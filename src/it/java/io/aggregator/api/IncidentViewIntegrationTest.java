package io.aggregator.api;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.aggregator.Main;
import io.aggregator.entity.TransactionMerchantKey;
import io.aggregator.view.IncidentsByDate;
import io.aggregator.view.IncidentsByDateClient;
import io.aggregator.view.IncidentsByDateModel;
import io.aggregator.view.IncidentsByDateView;
import kalix.javasdk.testkit.junit.KalixTestKitResource;
import org.junit.ClassRule;
import org.junit.Test;

import java.time.Instant;
import java.util.Date;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

// Example of an integration test calling our service via the Kalix proxy
// Run all test classes ending with "IntegrationTest" using `mvn verify -Pit`
public class IncidentViewIntegrationTest {

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
  private final IncidentsByDate view;

  public IncidentViewIntegrationTest() {
    client = testKit.getGrpcClient(Incident.class);
    view = testKit.getGrpcClient(IncidentsByDate.class);
  }

  @Test
  public void happyPath() throws Exception {
    String incidentAmount = "100";
    String paymentId = "1234";
    String merchantId = "merchant1";
    String shopId = "merchant1Shop1";

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
            .setMerchantId(merchantId)
            .setShopId(shopId)
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

    Thread.sleep(5000);

    Timestamp from = Timestamps.fromMillis(Instant.now().minusSeconds(1000).toEpochMilli());
    Timestamp to = Timestamps.fromMillis(Instant.now().toEpochMilli());
    var unPayedIncidentsByMerchantAndDateReq = IncidentsByDateModel.IncidentsByDateRequest.newBuilder()
            .setFromDate(from)
            .setToDate(to)
            .setMerchantId(merchantId)
            .setPaymentId("0")
            .build();
    var unPayedIncidentsByMerchantAndDateRes = view.getIncidentsByDate(unPayedIncidentsByMerchantAndDateReq).toCompletableFuture().get(5,SECONDS);
    assertEquals(1,unPayedIncidentsByMerchantAndDateRes.getResultsCount());


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


    Thread.sleep(5000);

    //check
    unPayedIncidentsByMerchantAndDateRes = view.getIncidentsByDate(unPayedIncidentsByMerchantAndDateReq).toCompletableFuture().get(5,SECONDS);
    assertEquals(0,unPayedIncidentsByMerchantAndDateRes.getResultsCount());

    var payedIncidentsByMerchantAndDateReq = unPayedIncidentsByMerchantAndDateReq
            .toBuilder()
            .setPaymentId(paymentId)
            .build();
    var payedIncidentsByMerchantAndDateRes = view.getIncidentsByDate(payedIncidentsByMerchantAndDateReq).toCompletableFuture().get(5,SECONDS);
    assertEquals(1,payedIncidentsByMerchantAndDateRes.getResultsCount());
    assertEquals(paymentId, payedIncidentsByMerchantAndDateRes.getResults(0).getPaymentId());

  }


}

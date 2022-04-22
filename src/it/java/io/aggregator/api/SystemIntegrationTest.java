package io.aggregator.api;

import com.akkaserverless.javasdk.testkit.junit.AkkaServerlessTestKitResource;
import io.aggregator.Main;
import io.aggregator.TimeTo;
import org.junit.ClassRule;
import org.junit.Test;

import java.time.Instant;

import static java.util.concurrent.TimeUnit.SECONDS;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

// Example of an integration test calling our service via the Akka Serverless proxy
// Run all test classes ending with "IntegrationTest" using `mvn verify -Pit`
public class SystemIntegrationTest {

  /**
   * The test kit starts both the service container and the Akka Serverless proxy.
   */
  @ClassRule
  public static final AkkaServerlessTestKitResource testKit =
    new AkkaServerlessTestKitResource(Main.createAkkaServerless());

  /**
   * Use the generated gRPC client to call the service through the Akka Serverless proxy.
   */
  private final Transaction transactionClient;
  private final Merchant merchantClient;

  public SystemIntegrationTest() {
    transactionClient = testKit.getGrpcClient(Transaction.class);
    merchantClient = testKit.getGrpcClient(Merchant.class);
  }

  @Test
  public void transactionToMerchant() throws Exception {
    System.out.println(Thread.currentThread().getName() + " - CALL TIME: " + Instant.now().toString());

    transactionClient.paymentPriced(TransactionApi.PaymentPricedCommand.newBuilder()
        .setTransactionId("txn-1")
        .setShopId("tesco-chelsea")
        .setEventType("approved")
        .setTimestamp(TimeTo.now())
        .addPricedItem(TransactionApi.PricedItem.newBuilder()
            .setPricedItemAmount(1.01)
            .setServiceCode("ABC")
            .build())
        .build()).toCompletableFuture().get(10, SECONDS);

    Thread.sleep(10000);

    merchantClient.merchantAggregationRequest(MerchantApi.MerchantAggregationRequestCommand.newBuilder()
        .setMerchantId("tesco")
        .build()).toCompletableFuture().get(10, SECONDS);

    Thread.sleep(10000);

//    merchantClient.merchantPaymentRequest(MerchantApi.MerchantPaymentRequestCommand.newBuilder()
//        .setMerchantId("tesco")
//        .build()).toCompletableFuture().get(10, SECONDS);
  }

  @Test
  public void merchantToTransaction() throws Exception {
    System.out.println(Thread.currentThread().getName() + " - CALL TIME: " + Instant.now().toString());

    merchantClient.merchantAggregationRequest(MerchantApi.MerchantAggregationRequestCommand.newBuilder()
        .setMerchantId("tesco")
        .build()).toCompletableFuture().get(10, SECONDS);

    merchantClient.merchantPaymentRequest(MerchantApi.MerchantPaymentRequestCommand.newBuilder()
        .setMerchantId("tesco")
        .build()).toCompletableFuture().get(10, SECONDS);

    Thread.sleep(15000);

//    merchantClient.merchantAggregationRequest(MerchantApi.MerchantAggregationRequestCommand.newBuilder()
//        .setMerchantId("tesco")
//        .build()).toCompletableFuture().get(10, SECONDS);
//
//    merchantClient.merchantPaymentRequest(MerchantApi.MerchantPaymentRequestCommand.newBuilder()
//        .setMerchantId("tesco")
//        .build()).toCompletableFuture().get(10, SECONDS);
  }
}

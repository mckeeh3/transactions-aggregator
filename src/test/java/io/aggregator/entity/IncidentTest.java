package io.aggregator.entity;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.aggregator.api.IncidentApi;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity;
import kalix.javasdk.eventsourcedentity.EventSourcedEntityContext;
import kalix.javasdk.testkit.EventSourcedResult;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class IncidentTest {

  @Test
  public void happyPath() {

    String incidentAmount = "100";
    String paymentId = "1234";
    String merchantId = "merchant1";
    String shopId = "merchant1Shop1";

    Timestamp t = Timestamps.fromDate(new Date());

    IncidentTestKit service = IncidentTestKit.of(Incident::new);
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
            .setMerchantId(merchantId)
            .setShopId(shopId)
            .setTimestamp(t)
            .build();
    var createResponse = service.createIncident(createCommand);
    var incidentCreated = createResponse.getNextEventOfType(IncidentEntity.IncidentCreated.class);
    assertEquals(key.getTransactionId(), incidentCreated.getTransactionKey().getTransactionId());
    assertEquals(key.getServiceCode(), incidentCreated.getTransactionKey().getServiceCode());
    assertEquals(key.getAccountFrom(), incidentCreated.getTransactionKey().getAccountFrom());
    assertEquals(key.getAccountTo(), incidentCreated.getTransactionKey().getAccountTo());
    assertEquals(incidentAmount,incidentCreated.getIncidentAmount());
    assertEquals(merchantId,incidentCreated.getMerchantId());
    assertEquals(shopId,incidentCreated.getShopId());

    var addPaymentCommand = IncidentApi.AddPaymentCommand.newBuilder()
            .setTransactionId(key.getTransactionId())
            .setServiceCode(key.getServiceCode())
            .setAccountFrom(key.getAccountFrom())
            .setAccountTo(key.getAccountTo())
            .setPaymentId(paymentId)
            .setTimestamp(t).build();
    var addPaymentResponse = service.addPayment(addPaymentCommand);
    assertEquals(false, addPaymentResponse.isError());
    var incidentPaymentAdded = addPaymentResponse.getNextEventOfType(IncidentEntity.IncidentPaymentAdded.class);

    assertEquals(paymentId,incidentPaymentAdded.getPaymentId());
  }

}

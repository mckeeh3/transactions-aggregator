package io.aggregator.action;

import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import com.google.protobuf.util.Timestamps;
import io.aggregator.api.IncidentApi;
import io.aggregator.entity.TransactionEntity;
import kalix.javasdk.action.ActionCreationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

// This class was initially generated based on the .proto definition by Kalix tooling.
// This is the implementation for the Action Service described in your io/aggregator/action/transaction_to_incident_action.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class TransactionToIncidentAction extends AbstractTransactionToIncidentAction {
  static final Logger log = LoggerFactory.getLogger(TransactionToIncidentAction.class);
  public TransactionToIncidentAction(ActionCreationContext creationContext) {}

  @Override
  public Effect<Empty> onIncidentAdded(TransactionEntity.IncidentAdded incidentAdded) {
    log.debug("IncidentAdded: {}", incidentAdded);
    TransactionEntity.TransactionIncident ti = incidentAdded.getTransactionIncident(incidentAdded.getTransactionIncidentCount()-1);
    IncidentApi.CreateIncidentCommand cmd = IncidentApi.CreateIncidentCommand.newBuilder()
            .setTransactionId(incidentAdded.getTransactionId())
            .setServiceCode(ti.getServiceCode())
            .setAccountFrom(ti.getAccountFrom())
            .setAccountTo(ti.getAccountTo())
            .setMerchantId(incidentAdded.getMerchantId())
            .setShopId(incidentAdded.getShopId())
            .setIncidentAmount(ti.getIncidentAmount())
            .setTimestamp(incidentAdded.getIncidentTimestamp())
            .build();
    return effects().asyncEffect(components().incident().createIncident(cmd).execute().exceptionally(e -> {
      e.printStackTrace();
      return Empty.getDefaultInstance();
    }).thenApply(r -> {
      log.debug("IncidentAdded: DONE!!");
      return effects().reply(Empty.getDefaultInstance());
    }));
  }
  @Override
  public Effect<Empty> onPaymentAdded(TransactionEntity.PaymentAdded paymentAdded) {
    log.debug("PaymentAdded: {}", paymentAdded);
    IncidentApi.AddPaymentCommand cmd = IncidentApi.AddPaymentCommand.newBuilder()
            .setTransactionId(paymentAdded.getTransactionKey().getTransactionId())
            .setServiceCode(paymentAdded.getTransactionKey().getServiceCode())
            .setAccountFrom(paymentAdded.getTransactionKey().getAccountFrom())
            .setAccountTo(paymentAdded.getTransactionKey().getAccountTo())
            .setTimestamp(Timestamps.fromDate(new Date()))
            .setPaymentId(paymentAdded.getPaymentId())
            .build();
    return effects().forward(components().incident().addPayment(cmd));
  }
  @Override
  public Effect<Empty> ignoreOtherEvents(Any any) {
    return effects().reply(Empty.getDefaultInstance());
  }
}

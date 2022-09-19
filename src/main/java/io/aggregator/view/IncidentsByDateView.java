package io.aggregator.view;

import com.google.protobuf.Any;
import io.aggregator.entity.IncidentEntity;
import kalix.javasdk.view.View;
import kalix.javasdk.view.ViewContext;

// This class was initially generated based on the .proto definition by Kalix tooling.
// This is the implementation for the View Service described in your io/aggregator/view/incidents_by_date.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class IncidentsByDateView extends AbstractIncidentsByDateView {

  public IncidentsByDateView(ViewContext context) {}

  @Override
  public IncidentsByDateModel.IncidentByDateViewState emptyState() {
    return IncidentsByDateModel.IncidentByDateViewState.getDefaultInstance();
  }

  @Override
  public View.UpdateEffect<IncidentsByDateModel.IncidentByDateViewState> onIncidentCreated(
    IncidentsByDateModel.IncidentByDateViewState state, IncidentEntity.IncidentCreated incidentCreated) {
    IncidentsByDateModel.IncidentByDateViewState newState = IncidentsByDateModel.IncidentByDateViewState.newBuilder()
            .setTransactionId(incidentCreated.getTransactionKey().getTransactionId())
            .setServiceCode(incidentCreated.getTransactionKey().getServiceCode())
            .setAccountFrom(incidentCreated.getTransactionKey().getAccountFrom())
            .setAccountTo(incidentCreated.getTransactionKey().getAccountTo())
            .setIncidentTimestamp(incidentCreated.getIncidentTimestamp())
            .setIncidentAmount(incidentCreated.getIncidentAmount())
            .setPaymentId("0")
            .setMerchantId(incidentCreated.getMerchantId())
            .setShopId(incidentCreated.getShopId())
            .build();
    return effects().updateState(newState);
  }
  @Override
  public View.UpdateEffect<IncidentsByDateModel.IncidentByDateViewState> onIncidentPaymentAdded(
    IncidentsByDateModel.IncidentByDateViewState state, IncidentEntity.IncidentPaymentAdded incidentPaymentAdded) {
    return effects().updateState(state.toBuilder().setPaymentId(incidentPaymentAdded.getPaymentId()).build());
  }
  @Override
  public View.UpdateEffect<IncidentsByDateModel.IncidentByDateViewState> ignoreOtherEvents(
    IncidentsByDateModel.IncidentByDateViewState state, Any any) {
    return effects().ignore();
  }
}


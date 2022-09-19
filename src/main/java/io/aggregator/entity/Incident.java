package io.aggregator.entity;

import com.google.protobuf.Empty;
import io.aggregator.api.IncidentApi;
import io.grpc.Status;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity.Effect;
import kalix.javasdk.eventsourcedentity.EventSourcedEntityContext;

// This class was initially generated based on the .proto definition by Kalix tooling.
// This is the implementation for the Event Sourced Entity Service described in your io/aggregator/api/incident_api.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class Incident extends AbstractIncident {

  @SuppressWarnings("unused")
  private final String entityId;

  public Incident(EventSourcedEntityContext context) {
    this.entityId = context.entityId();
  }

  @Override
  public IncidentEntity.IncidentState emptyState() {
    return IncidentEntity.IncidentState.getDefaultInstance();
  }

  @Override
  public Effect<Empty> createIncident(IncidentEntity.IncidentState currentState, IncidentApi.CreateIncidentCommand createIncidentCommand) {

    if(!currentState.equals(IncidentEntity.IncidentState.getDefaultInstance()))
      return effects().reply(Empty.getDefaultInstance());

    TransactionMerchantKey.TransactionKey key = TransactionMerchantKey.TransactionKey.newBuilder()
            .setTransactionId(createIncidentCommand.getTransactionId())
            .setServiceCode(createIncidentCommand.getServiceCode())
            .setAccountFrom(createIncidentCommand.getAccountFrom())
            .setAccountTo(createIncidentCommand.getAccountTo())
            .build();
    IncidentEntity.IncidentCreated event = IncidentEntity.IncidentCreated.newBuilder()
            .setTransactionKey(key)
            .setMerchantId(createIncidentCommand.getMerchantId())
            .setShopId(createIncidentCommand.getShopId())
            .setIncidentTimestamp(createIncidentCommand.getTimestamp())
            .setIncidentAmount(createIncidentCommand.getIncidentAmount())
            .build();

    return effects().emitEvent(event).thenReply(newState -> Empty.getDefaultInstance());
  }

  @Override
  public Effect<Empty> addPayment(IncidentEntity.IncidentState currentState, IncidentApi.AddPaymentCommand addPaymentCommand) {

    if(currentState.equals(IncidentEntity.IncidentState.getDefaultInstance()))
      return effects().error("Incident not created");

    if(!currentState.getPaymentId().isEmpty())
      return effects().reply(Empty.getDefaultInstance());

    IncidentEntity.IncidentPaymentAdded event = IncidentEntity.IncidentPaymentAdded.newBuilder()
            .setTransactionKey(currentState.getTransactionKey())
            .setPaymentTimestamp(addPaymentCommand.getTimestamp())
            .setPaymentId(addPaymentCommand.getPaymentId())
            .build();

    return effects().emitEvent(event).thenReply(newState -> Empty.getDefaultInstance());
  }

  @Override
  public Effect<IncidentApi.GetIncidentResponse> getIncident(IncidentEntity.IncidentState currentState, IncidentApi.GetIncidentRequest getIncidentRequest) {
    if(currentState.equals(IncidentEntity.IncidentState.getDefaultInstance()))
      return effects().error("Not found", Status.Code.NOT_FOUND);
    return effects().reply(IncidentApi.GetIncidentResponse.newBuilder()
                    .setIncidentAmount(currentState.getIncidentAmount())
                    .setPaymentId(currentState.getPaymentId())
                    .build());
  }

  @Override
  public IncidentEntity.IncidentState incidentCreated(IncidentEntity.IncidentState currentState, IncidentEntity.IncidentCreated incidentCreated) {
    return IncidentEntity.IncidentState.newBuilder()
            .setTransactionKey(incidentCreated.getTransactionKey())
            .setIncidentAmount(incidentCreated.getIncidentAmount())
            .build();
  }
  @Override
  public IncidentEntity.IncidentState incidentPaymentAdded(IncidentEntity.IncidentState currentState, IncidentEntity.IncidentPaymentAdded incidentPaymentAdded) {
    return currentState.toBuilder().setPaymentId(incidentPaymentAdded.getPaymentId()).build();
  }

}

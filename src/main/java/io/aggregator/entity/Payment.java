package io.aggregator.entity;

import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntity;
import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntity.Effect;
import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.google.protobuf.Empty;
import io.aggregator.api.PaymentApi;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
// This is the implementation for the Event Sourced Entity Service described in your io/aggregator/api/payment_api.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class Payment extends AbstractPayment {

  @SuppressWarnings("unused")
  private final String entityId;

  public Payment(EventSourcedEntityContext context) {
    this.entityId = context.entityId();
  }

  @Override
  public PaymentEntity.PaymentState emptyState() {
    throw new UnsupportedOperationException("Not implemented yet, replace with your empty entity state");
  }

  @Override
  public Effect<Empty> paymentAggregationRequest(PaymentEntity.PaymentState currentState, PaymentApi.PaymentAggregationRequestCommand paymentAggregationRequestCommand) {
    return effects().error("The command handler for `PaymentAggregationRequest` is not implemented, yet");
  }

  @Override
  public Effect<Empty> dayAggregation(PaymentEntity.PaymentState currentState, PaymentApi.DayAggregationCommand dayAggregationCommand) {
    return effects().error("The command handler for `DayAggregation` is not implemented, yet");
  }

  @Override
  public PaymentEntity.PaymentState paymentCreated(PaymentEntity.PaymentState currentState, PaymentEntity.PaymentCreated paymentCreated) {
    throw new RuntimeException("The event handler for `PaymentCreated` is not implemented, yet");
  }
  @Override
  public PaymentEntity.PaymentState paymentAggregationRequested(PaymentEntity.PaymentState currentState, PaymentEntity.PaymentAggregationRequested paymentAggregationRequested) {
    throw new RuntimeException("The event handler for `PaymentAggregationRequested` is not implemented, yet");
  }
  @Override
  public PaymentEntity.PaymentState paymentDayAggregated(PaymentEntity.PaymentState currentState, PaymentEntity.PaymentDayAggregated paymentDayAggregated) {
    throw new RuntimeException("The event handler for `PaymentDayAggregated` is not implemented, yet");
  }

}

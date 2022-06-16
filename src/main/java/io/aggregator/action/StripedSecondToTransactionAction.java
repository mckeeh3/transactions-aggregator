package io.aggregator.action;

import com.google.protobuf.Any;
import com.google.protobuf.Empty;

import io.aggregator.api.TransactionApi;
import io.aggregator.entity.StripedSecondEntity;
import kalix.javasdk.action.ActionCreationContext;

// This class was initially generated based on the .proto definition by Kalix tooling.
// This is the implementation for the Action Service described in your io/aggregator/action/striped_second_to_transaction_action.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class StripedSecondToTransactionAction extends AbstractStripedSecondToTransactionAction {

  public StripedSecondToTransactionAction(ActionCreationContext creationContext) {
  }

  @Override
  public Effect<Empty> onTransactionPaid(StripedSecondEntity.TransactionPaid event) {
    return effects().forward(components().transaction().addPayment(
        TransactionApi.AddPaymentCommand
            .newBuilder()
            .setTransactionId(event.getTransactionKey().getTransactionId())
            .setServiceCode(event.getTransactionKey().getServiceCode())
            .setAccountFrom(event.getTransactionKey().getAccountFrom())
            .setAccountTo(event.getTransactionKey().getAccountTo())
            .setPaymentId(event.getPaymentId())
            .build()));
  }

  @Override
  public Effect<Empty> ignoreOtherEvents(Any any) {
    return effects().reply(Empty.getDefaultInstance());
  }
}

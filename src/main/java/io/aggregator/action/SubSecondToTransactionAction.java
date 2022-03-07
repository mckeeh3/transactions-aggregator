package io.aggregator.action;

import com.akkaserverless.javasdk.action.ActionCreationContext;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.aggregator.api.TransactionApi;
import io.aggregator.entity.SubSecondEntity;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
// This is the implementation for the Action Service described in your io/aggregator/action/sub_second_to_transaction_action.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class SubSecondToTransactionAction extends AbstractSubSecondToTransactionAction {
  static Logger log = LoggerFactory.getLogger(SubSecondToTransactionAction.class);

  public SubSecondToTransactionAction(ActionCreationContext creationContext) {
  }

  @Override
  public Effect<Empty> onTransactionPaid(SubSecondEntity.TransactionPaid event) {
    log.info("onTransactionPaid: {}", event);

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

package io.aggregator.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Any;
import com.google.protobuf.Empty;

import io.aggregator.TimeTo;
import io.aggregator.api.StripedSecondApi;
import io.aggregator.entity.TransactionEntity;
import kalix.javasdk.action.ActionCreationContext;

// This class was initially generated based on the .proto definition by Kalix tooling.
// This is the implementation for the Action Service described in your io/aggregator/action/transaction_to_striped_second_action.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class TransactionToStripedSecondAction extends AbstractTransactionToStripedSecondAction {

  private static final Logger log = LoggerFactory.getLogger(TransactionToStripedSecondAction.class);

  public TransactionToStripedSecondAction(ActionCreationContext creationContext) {
  }

  @Override
  public Effect<Empty> onTransactionCreated(TransactionEntity.TransactionCreated event) {
    log.debug("onTransactionCreated: {}", event);

    var timestamp = event.getTransactionTimestamp();
    var stripe = TimeTo.stripe(event.getTransactionKey().getTransactionId());

    return effects().forward(components().stripedSecond().addTransaction(
        StripedSecondApi.AddTransactionCommand
            .newBuilder()
            .setMerchantId(event.getMerchantId())
            .setServiceCode(event.getTransactionKey().getServiceCode())
            .setAccountFrom(event.getTransactionKey().getAccountFrom())
            .setAccountTo(event.getTransactionKey().getAccountTo())
            .setEpochSecond(timestamp.getSeconds())
            .setStripe(stripe)
            .setTransactionId(event.getTransactionKey().getTransactionId())
            .setAmount(event.getTransactionAmount())
            .setTimestamp(timestamp)
            .build()));
  }

  @Override
  public Effect<Empty> ignoreOtherEvents(Any any) {
    return effects().reply(Empty.getDefaultInstance());
  }
}

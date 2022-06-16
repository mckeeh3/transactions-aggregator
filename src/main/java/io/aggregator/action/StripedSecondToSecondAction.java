package io.aggregator.action;

import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import io.aggregator.TimeTo;
import io.aggregator.api.SecondApi;
import io.aggregator.entity.StripedSecondEntity;
import kalix.javasdk.action.ActionCreationContext;

// This class was initially generated based on the .proto definition by Kalix tooling.
// This is the implementation for the Action Service described in your io/aggregator/action/striped_second_to_second_action.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class StripedSecondToSecondAction extends AbstractStripedSecondToSecondAction {

  public StripedSecondToSecondAction(ActionCreationContext creationContext) {}

  @Override
  public Effect<Empty> onStripedSecondActivated(StripedSecondEntity.StripedSecondActivated event) {
    return effects().forward(components().second().activateStripedSecond(
        SecondApi.ActivateStripedSecondCommand
            .newBuilder()
            .setMerchantId(event.getMerchantKey().getMerchantId())
            .setServiceCode(event.getMerchantKey().getServiceCode())
            .setAccountFrom(event.getMerchantKey().getAccountFrom())
            .setAccountTo(event.getMerchantKey().getAccountTo())
            .setEpochSecond(event.getEpochSecond())
            .setStripe(event.getStripe())
            .build()));
  }
  @Override
  public Effect<Empty> onStripedSecondAggregated(StripedSecondEntity.StripedSecondAggregated event) {
    return effects().forward(components().second().stripedSecondAggregation(
        SecondApi.StripedSecondAggregationCommand
            .newBuilder()
            .setMerchantId(event.getMerchantKey().getMerchantId())
            .setServiceCode(event.getMerchantKey().getServiceCode())
            .setAccountFrom(event.getMerchantKey().getAccountFrom())
            .setAccountTo(event.getMerchantKey().getAccountTo())
            .setEpochSecond(event.getEpochSecond())
            .setStripe(event.getStripe())
            .setTransactionTotalAmount(event.getTransactionTotalAmount())
            .setTransactionCount(event.getTransactionCount())
            .setLastUpdateTimestamp(event.getLastUpdateTimestamp())
            .setAggregateRequestTimestamp(event.getAggregateRequestTimestamp())
            .setPaymentId(event.getPaymentId())
            .build()));
  }
  @Override
  public Effect<Empty> ignoreOtherEvents(Any any) {
    return effects().reply(Empty.getDefaultInstance());
  }
}

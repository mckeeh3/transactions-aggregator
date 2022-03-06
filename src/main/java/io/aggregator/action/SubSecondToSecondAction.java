package io.aggregator.action;

import com.akkaserverless.javasdk.action.ActionCreationContext;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;

import io.aggregator.TimeTo;
import io.aggregator.api.SecondApi;
import io.aggregator.entity.SubSecondEntity;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class SubSecondToSecondAction extends AbstractSubSecondToSecondAction {

  public SubSecondToSecondAction(ActionCreationContext creationContext) {
  }

  @Override
  public Effect<Empty> onSubSecondActivated(SubSecondEntity.SubSecondActivated event) {
    return effects().forward(components().second().activateSubSecond(
        SecondApi.ActivateSubSecondCommand
            .newBuilder()
            .setMerchantId(event.getMerchantKey().getMerchantId())
            .setServiceCode(event.getMerchantKey().getServiceCode())
            .setAccountFrom(event.getMerchantKey().getAccountFrom())
            .setAccountTo(event.getMerchantKey().getAccountTo())
            .setEpochSecond(TimeTo.fromEpochSubSecond(event.getEpochSubSecond()).toEpochSecond())
            .setEpochSubSecond(event.getEpochSubSecond())
            .build()));
  }

  @Override
  public Effect<Empty> onSubSecondAggregated(SubSecondEntity.SubSecondAggregated event) {
    return effects().forward(components().second().subSecondAggregation(
        SecondApi.SubSecondAggregationCommand
            .newBuilder()
            .setMerchantId(event.getMerchantKey().getMerchantId())
            .setServiceCode(event.getMerchantKey().getServiceCode())
            .setAccountFrom(event.getMerchantKey().getAccountFrom())
            .setAccountTo(event.getMerchantKey().getAccountTo())
            .setEpochSecond(TimeTo.fromEpochSubSecond(event.getEpochSubSecond()).toEpochSecond())
            .setEpochSubSecond(event.getEpochSubSecond())
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

package io.aggregator.action;

import com.akkaserverless.javasdk.action.ActionCreationContext;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;

import io.aggregator.TimeTo;
import io.aggregator.api.MinuteApi;
import io.aggregator.entity.SecondEntity;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class SecondToMinuteAction extends AbstractSecondToMinuteAction {

  public SecondToMinuteAction(ActionCreationContext creationContext) {
  }

  @Override
  public Effect<Empty> onSecondActivated(SecondEntity.SecondActivated event) {
    return effects().forward(components().minute().addSecond(
        MinuteApi.AddSecondCommand
            .newBuilder()
            .setMerchantId(event.getMerchantKey().getMerchantId())
            .setServiceCode(event.getMerchantKey().getServiceCode())
            .setAccountFrom(event.getMerchantKey().getAccountFrom())
            .setAccountTo(event.getMerchantKey().getAccountTo())
            .setEpochMinute(TimeTo.fromEpochSecond(event.getEpochSecond()).toEpochMinute())
            .setEpochSecond(event.getEpochSecond())
            .build()));
  }

  @Override
  public Effect<Empty> onSecondAggregated(SecondEntity.SecondAggregated event) {
    return effects().forward(components().minute().secondAggregation(
        MinuteApi.SecondAggregationCommand
            .newBuilder()
            .setMerchantId(event.getMerchantKey().getMerchantId())
            .setServiceCode(event.getMerchantKey().getServiceCode())
            .setAccountFrom(event.getMerchantKey().getAccountFrom())
            .setAccountTo(event.getMerchantKey().getAccountTo())
            .setEpochMinute(TimeTo.fromEpochSecond(event.getEpochSecond()).toEpochMinute())
            .setEpochSecond(event.getEpochSecond())
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

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
  public Effect<Empty> onSecondCreated(SecondEntity.SecondCreated secondCreated) {
    return effects().forward(components().minute().addSecond(
        MinuteApi.AddSecondCommand
            .newBuilder()
            .setMerchantId(secondCreated.getMerchantKey().getMerchantId())
            .setServiceCode(secondCreated.getMerchantKey().getServiceCode())
            .setAccountFrom(secondCreated.getMerchantKey().getAccountFrom())
            .setAccountTo(secondCreated.getMerchantKey().getAccountTo())
            .setEpochMinute(TimeTo.fromEpochSecond(secondCreated.getEpochSecond()).toEpochMinute())
            .setEpochSecond(secondCreated.getEpochSecond())
            .build()));
  }

  @Override
  public Effect<Empty> onSecondAggregated(SecondEntity.SecondAggregated secondAggregated) {
    return effects().forward(components().minute().secondAggregation(
        MinuteApi.SecondAggregationCommand
            .newBuilder()
            .setMerchantId(secondAggregated.getMerchantKey().getMerchantId())
            .setServiceCode(secondAggregated.getMerchantKey().getServiceCode())
            .setAccountFrom(secondAggregated.getMerchantKey().getAccountFrom())
            .setAccountTo(secondAggregated.getMerchantKey().getAccountTo())
            .setEpochMinute(TimeTo.fromEpochSecond(secondAggregated.getEpochSecond()).toEpochMinute())
            .setEpochSecond(secondAggregated.getEpochSecond())
            .setTransactionTotalAmount(secondAggregated.getTransactionTotalAmount())
            .setTransactionCount(secondAggregated.getTransactionCount())
            .setLastUpdateTimestamp(secondAggregated.getLastUpdateTimestamp())
            .setAggregateRequestTimestamp(secondAggregated.getAggregateRequestTimestamp())
            .build()));
  }

  @Override
  public Effect<Empty> ignoreOtherEvents(Any any) {
    return effects().reply(Empty.getDefaultInstance());
  }
}

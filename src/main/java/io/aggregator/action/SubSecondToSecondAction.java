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
  public Effect<Empty> onSubSecondCreated(SubSecondEntity.SubSecondCreated subSecondCreated) {
    return effects().forward(components().second().addSubSecond(
        SecondApi.AddSubSecondCommand
            .newBuilder()
            .setMerchantId(subSecondCreated.getMerchantId())
            .setEpochSecond(TimeTo.fromEpochSubSecond(subSecondCreated.getEpochSubSecond()).toEpochSecond())
            .setEpochSubSecond(subSecondCreated.getEpochSubSecond())
            .build()));
  }

  @Override
  public Effect<Empty> onSubSecondAggregated(SubSecondEntity.SubSecondAggregated subSecondAggregated) {
    return effects().forward(components().second().subSecondAggregation(
        SecondApi.SubSecondAggregationCommand
            .newBuilder()
            .setMerchantId(subSecondAggregated.getMerchantId())
            .setEpochSecond(TimeTo.fromEpochSecond(subSecondAggregated.getEpochSubSecond()).toEpochSecond())
            .setEpochSubSecond(subSecondAggregated.getEpochSubSecond())
            .setTransactionTotalAmount(subSecondAggregated.getTransactionTotalAmount())
            .setTransactionCount(subSecondAggregated.getTransactionCount())
            .setLastUpdateTimestamp(subSecondAggregated.getLastUpdateTimestamp())
            .setAggregateRequestTimestamp(subSecondAggregated.getAggregateRequestTimestamp())
            .build()));
  }

  @Override
  public Effect<Empty> ignoreOtherEvents(Any any) {
    return effects().reply(Empty.getDefaultInstance());
  }
}

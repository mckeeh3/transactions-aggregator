package io.aggregator.action;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.akkaserverless.javasdk.action.ActionCreationContext;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;

import io.aggregator.api.SubSecondApi;
import io.aggregator.entity.SecondEntity;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class SecondToSubSecondAction extends AbstractSecondToSubSecondAction {

  public SecondToSubSecondAction(ActionCreationContext creationContext) {
  }

  @Override
  public Effect<Empty> onSecondAggregationRequested(SecondEntity.SecondAggregationRequested secondAggregationRequested) {
    var results = secondAggregationRequested.getEpochSubSecondsList().stream()
        .map(epochSubSecond -> SubSecondApi.AggregateSubSecondCommand
            .newBuilder()
            .setMerchantId(secondAggregationRequested.getMerchantId())
            .setEpochSubSecond(epochSubSecond)
            .setAggregateRequestTimestamp(secondAggregationRequested.getAggregateRequestTimestamp())
            .build())
        .map(command -> components().subSecond().aggregateSubSecond(command).execute())
        .collect(Collectors.toList());

    var result = CompletableFuture.allOf(results.toArray(new CompletableFuture[results.size()]))
        .thenApply(reply -> effects().reply(Empty.getDefaultInstance()));

    return effects().asyncEffect(result);
  }

  @Override
  public Effect<Empty> ignoreOtherEvents(Any any) {
    return effects().reply(Empty.getDefaultInstance());
  }
}

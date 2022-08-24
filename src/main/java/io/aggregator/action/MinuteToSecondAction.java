package io.aggregator.action;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import kalix.javasdk.action.ActionCreationContext;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;

import io.aggregator.api.SecondApi;
import io.aggregator.entity.MinuteEntity;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class MinuteToSecondAction extends AbstractMinuteToSecondAction {

  public MinuteToSecondAction(ActionCreationContext creationContext) {
  }

  @Override
  public Effect<Empty> onMinuteAggregationRequested(MinuteEntity.MinuteAggregationRequested event) {
    var results = event.getEpochSecondsList().stream()
        .map(epochSecond -> SecondApi.AggregateSecondCommand
            .newBuilder()
            .setMerchantId(event.getMerchantKey().getMerchantId())
            .setEpochSecond(epochSecond)
            .setAggregateRequestTimestamp(event.getAggregateRequestTimestamp())
            .setPaymentId(event.getPaymentId())
            .build())
        .map(command -> components().second().aggregateSecond(command).execute())
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

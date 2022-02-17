package io.aggregator.action;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.akkaserverless.javasdk.action.ActionCreationContext;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;

import io.aggregator.api.HourApi;
import io.aggregator.entity.DayEntity;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class DayToHourAction extends AbstractDayToHourAction {

  public DayToHourAction(ActionCreationContext creationContext) {
  }

  @Override
  public Effect<Empty> onDayAggregationRequested(DayEntity.DayAggregationRequested dayAggregationRequested) {
    var results = dayAggregationRequested.getEpochHoursList().stream()
        .map(epochHour -> HourApi.AggregateHourCommand
            .newBuilder()
            .setMerchantId(dayAggregationRequested.getMerchantId())
            .setEpochHour(epochHour)
            .setAggregateRequestTimestamp(dayAggregationRequested.getAggregateRequestTimestamp())
            .build())
        .map(command -> components().hour().aggregateHour(command).execute())
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

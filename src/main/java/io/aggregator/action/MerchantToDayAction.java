package io.aggregator.action;

import java.util.concurrent.CompletableFuture;

import com.akkaserverless.javasdk.action.ActionCreationContext;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;

import io.aggregator.api.DayApi;
import io.aggregator.entity.MerchantEntity;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
// This is the implementation for the Action Service described in your io/aggregator/action/merchant_to_day_action.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class MerchantToDayAction extends AbstractMerchantToDayAction {

  public MerchantToDayAction(ActionCreationContext creationContext) {
  }

  @Override
  public Effect<Empty> onMerchantAggregationRequested(MerchantEntity.MerchantAggregationRequested event) {
    var results = event.getActiveDaysList().stream()
        .map(epochDay -> DayApi.AggregateDayCommand
            .newBuilder()
            .setMerchantId(event.getMerchantKey().getMerchantId())
            .setEpochDay(epochDay)
            .setAggregateRequestTimestamp(event.getAggregateRequestTimestamp())
            .setPaymentId(event.getPaymentId())
            .build())
        .map(command -> components().day().aggregateDay(command).execute())
        .toList();

    var result = CompletableFuture.allOf(results.toArray(new CompletableFuture[results.size()]))
        .thenApply(reply -> effects().reply(Empty.getDefaultInstance()));

    return effects().asyncEffect(result);
  }

  @Override
  public Effect<Empty> onMerchantPaymentRequested(MerchantEntity.MerchantPaymentRequested event) {
    var results = event.getActiveDaysList().stream()
        .map(epochDay -> DayApi.AggregateDayCommand
            .newBuilder()
            .setMerchantId(event.getMerchantKey().getMerchantId())
            .setPaymentId(event.getPaymentId())
            .setEpochDay(epochDay)
            .setAggregateRequestTimestamp(event.getAggregateRequestTimestamp())
            .setPaymentId(event.getPaymentId())
            .build())
        .map(command -> components().day().aggregateDay(command).execute())
        .toList();

    var result = CompletableFuture.allOf(results.toArray(new CompletableFuture[results.size()]))
        .thenApply(reply -> effects().reply(Empty.getDefaultInstance()));

    return effects().asyncEffect(result);
  }

  @Override
  public Effect<Empty> ignoreOtherEvents(Any any) {
    return effects().reply(Empty.getDefaultInstance());
  }
}

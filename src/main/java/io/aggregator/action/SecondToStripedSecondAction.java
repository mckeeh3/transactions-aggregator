package io.aggregator.action;

import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import io.aggregator.api.StripedSecondApi;
import io.aggregator.entity.SecondEntity;
import kalix.javasdk.action.ActionCreationContext;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

// This class was initially generated based on the .proto definition by Kalix tooling.
// This is the implementation for the Action Service described in your io/aggregator/action/second_to_striped_second_action.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class SecondToStripedSecondAction extends AbstractSecondToStripedSecondAction {

  public SecondToStripedSecondAction(ActionCreationContext creationContext) {}

  @Override
  public Effect<Empty> onSecondAggregationRequested(SecondEntity.SecondAggregationRequested event) {
    var results = event.getStripesList().stream()
        .map(stripe -> StripedSecondApi.AggregateStripedSecondCommand
            .newBuilder()
            .setMerchantId(event.getMerchantKey().getMerchantId())
            .setEpochSecond(event.getEpochSecond())
            .setStripe(stripe)
            .setAggregateRequestTimestamp(event.getAggregateRequestTimestamp())
            .setPaymentId(event.getPaymentId())
            .build())
        .map(command -> components().stripedSecond().aggregateStripedSecond(command).execute())
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

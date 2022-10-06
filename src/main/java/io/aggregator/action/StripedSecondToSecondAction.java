package io.aggregator.action;

import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import io.aggregator.api.SecondApi;
import io.aggregator.entity.StripedSecondEntity;
import kalix.javasdk.action.ActionCreationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This class was initially generated based on the .proto definition by Kalix tooling.
// This is the implementation for the Action Service described in your io/aggregator/action/striped_second_to_second_action.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class StripedSecondToSecondAction extends AbstractStripedSecondToSecondAction {
  static final Logger log = LoggerFactory.getLogger(StripedSecondToSecondAction.class);

  public StripedSecondToSecondAction(ActionCreationContext creationContext) {}

  @Override
  public Effect<Empty> onStripedSecondActivated(StripedSecondEntity.StripedSecondActivated event) {
    log.debug(Thread.currentThread().getName() + " - StripedSecondActivated: {}", event);
    log.info(Thread.currentThread().getName() + " - ON EVENT: StripedSecondActivated");

    return effects().forward(components().second().addStripedSecond(
        SecondApi.AddStripedSecondCommand
            .newBuilder()
            .setMerchantId(event.getMerchantKey().getMerchantId())
            .setEpochSecond(event.getEpochSecond())
            .setStripe(event.getStripe())
            .build()));
  }

  @Override
  public Effect<Empty> onStripedSecondAggregated(StripedSecondEntity.StripedSecondAggregated event) {
    log.debug("{} - ON EVENT: StripedSecondAggregated: {}", Thread.currentThread().getName(), event);

    return effects().forward(components().second().stripedSecondAggregation(
        SecondApi.StripedSecondAggregationCommand
            .newBuilder()
            .setMerchantId(event.getMerchantKey().getMerchantId())
            .setEpochSecond(event.getEpochSecond())
            .setStripe(event.getStripe())
            .addAllMoneyMovements(event.getMoneyMovementsList())
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

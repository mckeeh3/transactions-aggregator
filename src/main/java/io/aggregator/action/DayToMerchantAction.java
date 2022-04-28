package io.aggregator.action;

import kalix.javasdk.action.ActionCreationContext;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;

import io.aggregator.api.MerchantApi;
import io.aggregator.entity.DayEntity;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
// This is the implementation for the Action Service described in your io/aggregator/action/day_to_merchant_action.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class DayToMerchantAction extends AbstractDayToMerchantAction {

  public DayToMerchantAction(ActionCreationContext creationContext) {
  }

  @Override
  public Effect<Empty> onDayActivated(DayEntity.DayActivated event) {
    return effects().forward(components().merchant().activateDay(
        MerchantApi.ActivateDayCommand
            .newBuilder()
            .setMerchantId(event.getMerchantKey().getMerchantId())
            .setServiceCode(event.getMerchantKey().getServiceCode())
            .setAccountFrom(event.getMerchantKey().getAccountFrom())
            .setAccountTo(event.getMerchantKey().getAccountTo())
            .setEpochDay(event.getEpochDay())
            .build()));
  }

  @Override
  public Effect<Empty> ignoreOtherEvents(Any any) {
    return effects().reply(Empty.getDefaultInstance());
  }
}

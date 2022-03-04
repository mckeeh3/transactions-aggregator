package io.aggregator.action;

import com.akkaserverless.javasdk.action.ActionCreationContext;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;

import io.aggregator.api.PaymentApi;
import io.aggregator.entity.MerchantEntity;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
// This is the implementation for the Action Service described in your io/aggregator/action/merchant_to_payment_action.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class MerchantToPaymentAction extends AbstractMerchantToPaymentAction {

  public MerchantToPaymentAction(ActionCreationContext creationContext) {
  }

  @Override
  public Effect<Empty> onMerchantAggregationRequested(MerchantEntity.MerchantAggregationRequested event) {
    return effects().forward(components().payment().aggregationRequest(
        PaymentApi.AggregationRequestCommand
            .newBuilder()
            .setMerchantId(event.getMerchantKey().getMerchantId())
            .setServiceCode(event.getMerchantKey().getServiceCode())
            .setAccountFrom(event.getMerchantKey().getAccountFrom())
            .setAccountTo(event.getMerchantKey().getAccountTo())
            .setPaymentId(event.getPaymentId())
            .addAllEpochDays(event.getActiveDaysList())
            .build()));
  }

  @Override
  public Effect<Empty> onMerchantPaymentRequested(MerchantEntity.MerchantPaymentRequested event) {
    return effects().forward(components().payment().paymentRequest(
        PaymentApi.PaymentRequestCommand
            .newBuilder()
            .setMerchantId(event.getMerchantKey().getMerchantId())
            .setServiceCode(event.getMerchantKey().getServiceCode())
            .setAccountFrom(event.getMerchantKey().getAccountFrom())
            .setAccountTo(event.getMerchantKey().getAccountTo())
            .setPaymentId(event.getPaymentId())
            .addAllEpochDays(event.getActiveDaysList())
            .build()));
  }

  @Override
  public Effect<Empty> ignoreOtherEvents(Any any) {
    return effects().reply(Empty.getDefaultInstance());
  }
}

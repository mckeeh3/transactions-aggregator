package io.aggregator.view;

import kalix.javasdk.view.View;
import kalix.javasdk.view.ViewContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Any;
import io.aggregator.entity.PaymentEntity;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
// This is the implementation for the View Service described in your io/aggregator/view/merchant_payments_by_merchant_by_date.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class MerchantPaymentsByMerchantByDateView extends AbstractMerchantPaymentsByMerchantByDateView {
  private static final Logger log = LoggerFactory.getLogger(MerchantPaymentsByMerchantByDateView.class);

  public MerchantPaymentsByMerchantByDateView(ViewContext context) {
  }

  @Override
  public MerchantPaymentsModel.MerchantPayment emptyState() {
    return MerchantPaymentsModel.MerchantPayment.getDefaultInstance();
  }

  @Override
  public View.UpdateEffect<MerchantPaymentsModel.MerchantPayment> onPaymentAggregated(MerchantPaymentsModel.MerchantPayment state, PaymentEntity.PaymentAggregated event) {
    log.info("onPaymentAggregated: state: {}\nPaymentAggregated: {}", state, event);

    return effects().updateState(MerchantPaymentsEventHandler.handle(state, event));
  }

  @Override
  public View.UpdateEffect<MerchantPaymentsModel.MerchantPayment> ignoreOtherEvents(MerchantPaymentsModel.MerchantPayment state, Any any) {
    return effects().ignore();
  }
}

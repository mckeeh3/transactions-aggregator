package io.aggregator.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Any;

import io.aggregator.entity.PaymentEntity.PaymentAggregated;
import io.aggregator.view.MerchantPaymentsModel.MerchantPayment;
import kalix.javasdk.view.View;
import kalix.javasdk.view.ViewContext;

// This class was initially generated based on the .proto definition by Kalix tooling.
// This is the implementation for the View Service described in your io/aggregator/view/merchant_payments_all.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class MerchantPaymentsAllView extends AbstractMerchantPaymentsAllView {
  private static final Logger log = LoggerFactory.getLogger(MerchantPaymentsAllView.class);

  public MerchantPaymentsAllView(ViewContext context) {
  }

  @Override
  public MerchantPaymentsModel.MerchantPayment emptyState() {
    return MerchantPaymentsModel.MerchantPayment.getDefaultInstance();
  }

  @Override
  public View.UpdateEffect<MerchantPayment> onPaymentAggregated(MerchantPayment state, PaymentAggregated event) {
    log.info("onPaymentAggregated: state: {}\nPaymentAggregated: {}", state, event);

    return effects().updateState(MerchantPaymentsEventHandler.handle(state, event));
  }

  @Override
  public View.UpdateEffect<MerchantPayment> ignoreOtherEvents(MerchantPayment state, Any any) {
    return effects().ignore();
  }
}

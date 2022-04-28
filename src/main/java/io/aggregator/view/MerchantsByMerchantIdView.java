package io.aggregator.view;

import kalix.javasdk.view.View;
import kalix.javasdk.view.ViewContext;
import com.google.protobuf.Any;
import io.aggregator.entity.MerchantEntity;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
// This is the implementation for the View Service described in your io/aggregator/view/merchants_by_merchant_id.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class MerchantsByMerchantIdView extends AbstractMerchantsByMerchantIdView {

  public MerchantsByMerchantIdView(ViewContext context) {
  }

  @Override
  public MerchantModel.Merchant emptyState() {
    return MerchantModel.Merchant.getDefaultInstance();
  }

  @Override
  public View.UpdateEffect<MerchantModel.Merchant> onMerchantDayActivated(MerchantModel.Merchant state, MerchantEntity.MerchantDayActivated event) {
    return effects().updateState(MerchantsEventHandler.handle(state, event));
  }

  @Override
  public View.UpdateEffect<MerchantModel.Merchant> onMerchantPaymentRequested(MerchantModel.Merchant state, MerchantEntity.MerchantPaymentRequested event) {
    return effects().updateState(MerchantsEventHandler.handle(state, event));
  }

  @Override
  public View.UpdateEffect<MerchantModel.Merchant> ignoreOtherEvents(MerchantModel.Merchant state, Any any) {
    return effects().ignore();
  }
}

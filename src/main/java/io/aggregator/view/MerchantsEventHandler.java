package io.aggregator.view;

import io.aggregator.entity.MerchantEntity;
import io.aggregator.view.MerchantModel.Merchant;

public class MerchantsEventHandler {

  public static Merchant handle(Merchant state, MerchantEntity.MerchantDayActivated event) {
    return state.toBuilder()
        .setMerchantId(event.getMerchantKey().getMerchantId())
        .setPaymentId(event.getPaymentId())
        .setIsPaid(false)
        .setStatus("Pending")
        .build();
  }

  public static Merchant handle(Merchant state, MerchantEntity.MerchantPaymentRequested event) {
    return state.toBuilder()
        .setMerchantId(event.getMerchantKey().getMerchantId())
        .setPaymentId(event.getPaymentId())
        .setIsPaid(true)
        .setStatus("Paid")
        .build();
  }
}

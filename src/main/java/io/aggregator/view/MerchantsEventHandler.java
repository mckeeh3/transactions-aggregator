package io.aggregator.view;

import io.aggregator.entity.MerchantEntity;
import io.aggregator.view.MerchantModel.Merchant;

public class MerchantsEventHandler {

  public static Merchant handle(Merchant state, MerchantEntity.MerchantDayActivated event) {
    return state.toBuilder()
        .setMerchantId(event.getMerchantKey().getMerchantId())
        .setServiceCode(event.getMerchantKey().getServiceCode())
        .setAccountFrom(event.getMerchantKey().getAccountFrom())
        .setAccountTo(event.getMerchantKey().getAccountTo())
        .setPaymentId(event.getPaymentId())
        .setIsPaid(false)
        .setStatus("Pending")
        .build();
  }

  public static Merchant handle(Merchant state, MerchantEntity.MerchantPaymentRequested event) {
    return state.toBuilder()
        .setMerchantId(event.getMerchantKey().getMerchantId())
        .setServiceCode(event.getMerchantKey().getServiceCode())
        .setAccountFrom(event.getMerchantKey().getAccountFrom())
        .setAccountTo(event.getMerchantKey().getAccountTo())
        .setPaymentId(event.getPaymentId())
        .setIsPaid(true)
        .setStatus("Paid")
        .build();
  }
}

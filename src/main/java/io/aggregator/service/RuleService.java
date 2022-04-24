package io.aggregator.service;

import io.aggregator.api.TransactionApi;
import io.aggregator.entity.TransactionEntity;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RuleService {

  public static final String JPMC_ACCOUNT = "JPMC";
  public static final String TAX_ACCOUNT = "TAX";
  public static final String CARD_SCHEME_ACCOUNT = "CARD-SCHEME";

  public static Iterable<TransactionEntity.TransactionIncident> applyRules(TransactionEntity.TransactionState state, String merchant, TransactionApi.PaymentPricedCommand command) {
    return command.getPricedItemList().stream()
        .flatMap(pricedItem -> applyRules(state, merchant, pricedItem))
        .collect(Collectors.toList());
  }

  static Stream<TransactionEntity.TransactionIncident> applyRules(TransactionEntity.TransactionState state, String merchant, TransactionApi.PricedItem pricedItem) {
    String merchantAccount = findMerchantAccount(merchant);

    return switch (pricedItem.getServiceCode()) {
      case "SVC1" -> Stream.of(
          TransactionEntity.TransactionIncident.newBuilder()
              .setServiceCode(pricedItem.getServiceCode())
              .setIncidentAmount(pricedItem.getPricedItemAmount())
              .setAccountFrom(JPMC_ACCOUNT)
              .setAccountTo(merchantAccount)
              .build()
      );
      case "SVC2" -> Stream.of(
          TransactionEntity.TransactionIncident.newBuilder()
              .setServiceCode(pricedItem.getServiceCode())
              .setIncidentAmount(pricedItem.getPricedItemAmount())
              .setAccountFrom(merchantAccount)
              .setAccountTo(JPMC_ACCOUNT)
              .build()
      );
      case "SVC3" -> Stream.of(
          TransactionEntity.TransactionIncident.newBuilder()
              .setServiceCode(pricedItem.getServiceCode())
              .setIncidentAmount(pricedItem.getPricedItemAmount())
              .setAccountFrom(merchantAccount)
              .setAccountTo(TAX_ACCOUNT)
              .build()
      );
      case "SVC4" -> Stream.of(
          TransactionEntity.TransactionIncident.newBuilder()
              .setServiceCode(pricedItem.getServiceCode())
              .setIncidentAmount(pricedItem.getPricedItemAmount())
              .setAccountFrom(merchantAccount)
              .setAccountTo(CARD_SCHEME_ACCOUNT)
              .build()
      );
      default -> Stream.empty();
    };
  }

  private static String findMerchantAccount(String merchantId) {
    return "MERCHANT-" + merchantId.toUpperCase();
  }
}

//RULES BY SERVICE:
//JPMC      MERCHANT
//MERCHANT  JPMC
//MERCHANT  TAX
//MERCHANT  CARD-SCHEME

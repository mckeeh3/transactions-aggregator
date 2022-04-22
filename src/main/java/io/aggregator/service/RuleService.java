package io.aggregator.service;

import io.aggregator.api.TransactionApi;
import io.aggregator.entity.TransactionEntity;

import java.util.stream.Collectors;

public class RuleService {

  static Iterable<TransactionEntity.TransactionIncident> toTransactionIncidents(TransactionEntity.TransactionState state, TransactionApi.PaymentPricedCommand command) {
    return command.getPricedItemList().stream()
        .map(pricedItem -> TransactionEntity.TransactionIncident.newBuilder()
            .setServiceCode(pricedItem.getServiceCode())
            .setIncidentAmount(pricedItem.getPricedItemAmount())
            .setAccountFrom("from")
            .setAccountTo("to")
            .build())
        .collect(Collectors.toList());
  }
}

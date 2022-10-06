package io.aggregator.action;

import io.aggregator.api.StripedSecondApi;
import kalix.javasdk.action.ActionCreationContext;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;

import io.aggregator.TimeTo;
import io.aggregator.entity.TransactionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

// This class was initially generated based on the .proto definition by Kalix tooling.
// This is the implementation for the Action Service described in your io/aggregator/action/transaction_to_striped_second_action.proto.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class TransactionToStripedSecondAction extends AbstractTransactionToStripedSecondAction {
  static final Logger log = LoggerFactory.getLogger(TransactionToStripedSecondAction.class);

  public TransactionToStripedSecondAction(ActionCreationContext creationContext) {
  }

  @Override
  public Effect<Empty> onIncidentAdded(TransactionEntity.IncidentAdded event) {
    var timestamp = event.getIncidentTimestamp();
    var stripe = TimeTo.stripe(event.getTransactionId());

    log.debug(Thread.currentThread().getName() + " - IncidentAdded: {}", event);
    log.info(Thread.currentThread().getName() + " - ON EVENT: IncidentAdded");
    return effects().forward(components().stripedSecond().addLedgerItems(
        StripedSecondApi.AddLedgerItemsCommand
            .newBuilder()
            .setMerchantId(event.getMerchantId())
            .setTransactionId(event.getTransactionId())
            .setEpochSecond(timestamp.getSeconds())
            .setTimestamp(timestamp)
            .setStripe(stripe)
            .addAllLedgerItem(event.getTransactionIncidentList().stream()
                .map(TransactionToStripedSecondAction::toLedgerItem)
                .collect(Collectors.toList()))
            .build()));
  }

  static StripedSecondApi.LedgerItem toLedgerItem(TransactionEntity.TransactionIncident transactionIncident) {
    return StripedSecondApi.LedgerItem.newBuilder()
            .setServiceCode(transactionIncident.getServiceCode())
            .setAmount(transactionIncident.getIncidentAmount())
            .setAccountFrom(transactionIncident.getAccountFrom())
            .setAccountTo(transactionIncident.getAccountTo())
            .build();
  }

  @Override
  public Effect<Empty> ignoreOtherEvents(Any any) {
    return effects().reply(Empty.getDefaultInstance());
  }
}

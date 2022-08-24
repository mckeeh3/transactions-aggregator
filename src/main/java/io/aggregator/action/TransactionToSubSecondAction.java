package io.aggregator.action;

import kalix.javasdk.action.ActionCreationContext;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;

import io.aggregator.TimeTo;
import io.aggregator.api.SubSecondApi;
import io.aggregator.entity.TransactionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

// This class was initially generated based on the .proto definition by Kalix tooling.
// This is the implementation for the Action Service described in your io/aggregator/action/transaction_to_sub_second_action.proto.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class TransactionToSubSecondAction extends AbstractTransactionToSubSecondAction {
  static final Logger log = LoggerFactory.getLogger(TransactionToSubSecondAction.class);

  public TransactionToSubSecondAction(ActionCreationContext creationContext) {
  }

  @Override
  public Effect<Empty> onIncidentAdded(TransactionEntity.IncidentAdded event) {
    var timestamp = event.getIncidentTimestamp();
    var epochSubSecond = TimeTo.fromTimestamp(timestamp).toEpochSubSecond();

    log.debug(Thread.currentThread().getName() + " - IncidentAdded: {}", event);
    log.info(Thread.currentThread().getName() + " - ON EVENT: IncidentAdded");
    return effects().forward(components().subSecond().addLedgerItems(
            SubSecondApi.AddLedgerItemsCommand
                    .newBuilder()
                    .setMerchantId(event.getMerchantId())
                    .setEpochSubSecond(epochSubSecond)
                    .setTransactionId(event.getTransactionId())
                    .setTimestamp(timestamp)
                    .addAllLedgerItem(event.getTransactionIncidentList().stream()
                            .map(TransactionToSubSecondAction::toLedgerItem)
                            .collect(Collectors.toList()))
                    .build()));
  }

  static SubSecondApi.LedgerItem toLedgerItem(TransactionEntity.TransactionIncident transactionIncident) {
    return SubSecondApi.LedgerItem.newBuilder()
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

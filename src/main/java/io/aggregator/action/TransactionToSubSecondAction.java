package io.aggregator.action;

import com.akkaserverless.javasdk.action.ActionCreationContext;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;

import io.aggregator.TimeTo;
import io.aggregator.api.SubSecondApi;
import io.aggregator.entity.TransactionEntity;

import java.util.stream.Collectors;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
// This is the implementation for the Action Service described in your io/aggregator/action/transaction_to_sub_second_action.proto.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class TransactionToSubSecondAction extends AbstractTransactionToSubSecondAction {

  public TransactionToSubSecondAction(ActionCreationContext creationContext) {
  }

  @Override
  public Effect<Empty> onTransactionCreated(TransactionEntity.TransactionCreated event) {
    var timestamp = event.getTransactionTimestamp();
    var epochSubSecond = TimeTo.fromTimestamp(timestamp).toEpochSubSecond();

    return effects().forward(components().subSecond().addTransaction(
        SubSecondApi.AddTransactionCommand
            .newBuilder()
            .setMerchantId(event.getMerchantId())
            .setTransactionId(event.getTransactionId())
//            .setServiceCode(event.getTransactionKey().getServiceCode())
//            .setAccountFrom(event.getTransactionKey().getAccountFrom())
//            .setAccountTo(event.getTransactionKey().getAccountTo())
            .setEpochSubSecond(epochSubSecond)
//            .setTransactionId(event.getTransactionKey().getTransactionId())
            .setAmount(event.getTransactionAmount())
            .setTimestamp(timestamp)
            .build()));
  }

  @Override
  public Effect<Empty> onIncidentAdded(TransactionEntity.IncidentAdded event) {
    // TODO apply what is in onTransactionCreated here
    var timestamp = event.getIncidentTimestamp();
    var epochSubSecond = TimeTo.fromTimestamp(timestamp).toEpochSubSecond();

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
            // TODO
            .build();
  }

  @Override
  public Effect<Empty> ignoreOtherEvents(Any any) {
    return effects().reply(Empty.getDefaultInstance());
  }
}

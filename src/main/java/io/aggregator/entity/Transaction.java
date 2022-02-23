package io.aggregator.entity;

import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.google.protobuf.Empty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.aggregator.api.TransactionApi;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
// This is the implementation for the Event Sourced Entity Service described in your io/aggregator/api/tranaaction_api.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class Transaction extends AbstractTransaction {
  static final Logger log = LoggerFactory.getLogger(Transaction.class);

  public Transaction(EventSourcedEntityContext context) {
    log.info("Transaction instance started, entityKey {}", context.entityId());
  }

  @Override
  public TransactionEntity.TransactionState emptyState() {
    return TransactionEntity.TransactionState.getDefaultInstance();
  }

  @Override
  public Effect<Empty> createTransaction(TransactionEntity.TransactionState state, TransactionApi.CreateTransactionCommand command) {
    return handle(state, command);
  }

  @Override
  public TransactionEntity.TransactionState transactionCreated(TransactionEntity.TransactionState state, TransactionEntity.TransactionCreated event) {
    return handle(state, event);
  }

  private Effect<Empty> handle(TransactionEntity.TransactionState state, TransactionApi.CreateTransactionCommand command) {
    log.info("state: {}\nCreateTransactionCommand: {}", state, command);

    if (state.getTransactionKey() != null && !state.getTransactionKey().getTransactionId().isEmpty()) {
      return effects().reply(Empty.getDefaultInstance()); // already created
    }
    return effects()
        .emitEvent(eventFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private TransactionEntity.TransactionState handle(TransactionEntity.TransactionState state, TransactionEntity.TransactionCreated event) {
    return TransactionEntity.TransactionState
        .newBuilder()
        .setTransactionKey(
            TransactionEntity.TransactionKey
                .newBuilder()
                .setTransactionId(event.getTransactionKey().getTransactionId())
                .setService(event.getTransactionKey().getService())
                .setAccount(event.getTransactionKey().getAccount())
                .build())
        .setTransactionAmount(event.getTransactionAmount())
        .setMerchantId(event.getMerchantId())
        .setTransactionTimestamp(event.getTransactionTimestamp())
        .build();
  }

  private TransactionEntity.TransactionCreated eventFor(TransactionEntity.TransactionState state, TransactionApi.CreateTransactionCommand command) {
    return TransactionEntity.TransactionCreated
        .newBuilder()
        .setTransactionKey(
            TransactionEntity.TransactionKey
                .newBuilder()
                .setTransactionId(command.getTransactionKey().getTransactionId())
                .setService(command.getTransactionKey().getService())
                .setAccount(command.getTransactionKey().getAccount())
                .build())
        .setTransactionAmount(command.getTransactionAmount())
        .setMerchantId(command.getMerchantId())
        .setTransactionTimestamp(command.getTransactionTimestamp())
        .build();
  }
}

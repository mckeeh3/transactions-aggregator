package io.aggregator.entity;

import java.util.Optional;

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
  public Effect<Empty> addPayment(TransactionEntity.TransactionState state, TransactionApi.AddPaymentCommand command) {
    return handle(state, command);
  }

  @Override
  public Effect<TransactionApi.GetTransactionResponse> getTransaction(TransactionEntity.TransactionState state, TransactionApi.GetTransactionRequest request) {
    return reject(state, request).orElseGet((() -> handle(state, request)));
  }

  @Override
  public TransactionEntity.TransactionState transactionCreated(TransactionEntity.TransactionState state, TransactionEntity.TransactionCreated event) {
    return handle(state, event);
  }

  @Override
  public TransactionEntity.TransactionState paymentAdded(TransactionEntity.TransactionState state, TransactionEntity.PaymentAdded event) {
    return handle(state, event);
  }

  private Effect<Empty> handle(TransactionEntity.TransactionState state, TransactionApi.CreateTransactionCommand command) {
    log.info("state: {}\nCreateTransactionCommand: {}", state, command);

    if (state.getTransactionKey().getTransactionId() != null && !state.getTransactionKey().getTransactionId().isEmpty()) {
      return effects().reply(Empty.getDefaultInstance()); // already created
    }
    return effects()
        .emitEvent(eventFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(TransactionEntity.TransactionState state, TransactionApi.AddPaymentCommand command) {
    log.info("state: {}\nAddPaymentCommand: {}", state, command);

    return effects()
        .emitEvent(eventFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Optional<Effect<TransactionApi.GetTransactionResponse>> reject(TransactionEntity.TransactionState state, TransactionApi.GetTransactionRequest request) {
    if (state.getTransactionKey() == null || state.getTransactionKey().getTransactionId().isEmpty()) {
      return Optional.of(effects().error(String.format("Transaction %s not found", request)));
    }
    return Optional.empty();
  }

  private Effect<TransactionApi.GetTransactionResponse> handle(TransactionEntity.TransactionState state, TransactionApi.GetTransactionRequest request) {
    return effects().reply(
        TransactionApi.GetTransactionResponse
            .newBuilder()
            .setTransactionKey(
                TransactionApi.TransactionKey
                    .newBuilder()
                    .setTransactionId(state.getTransactionKey().getTransactionId())
                    .setServiceCode(state.getTransactionKey().getServiceCode())
                    .setAccountFrom(state.getTransactionKey().getAccountFrom())
                    .setAccountTo(state.getTransactionKey().getAccountTo())
                    .build())
            .setMerchantId(state.getMerchantId())
            .setShopId(state.getShopId())
            .setTransactionAmount(state.getTransactionAmount())
            .setTransactionTimestamp(state.getTransactionTimestamp())
            .setPaymentId(state.getPaymentId())
            .build());
  }

  static TransactionEntity.TransactionState handle(TransactionEntity.TransactionState state, TransactionEntity.TransactionCreated event) {
    return TransactionEntity.TransactionState
        .newBuilder()
        .setTransactionKey(
            TransactionMerchantKey.TransactionKey
                .newBuilder()
                .setTransactionId(event.getTransactionKey().getTransactionId())
                .setServiceCode(event.getTransactionKey().getServiceCode())
                .setAccountFrom(event.getTransactionKey().getAccountFrom())
                .setAccountTo(event.getTransactionKey().getAccountTo())
                .build())
        .setTransactionAmount(event.getTransactionAmount())
        .setMerchantId(event.getMerchantId())
        .setShopId(event.getShopId())
        .setTransactionTimestamp(event.getTransactionTimestamp())
        .build();
  }

  static TransactionEntity.TransactionState handle(TransactionEntity.TransactionState state, TransactionEntity.PaymentAdded event) {
    return state.toBuilder()
        .setPaymentId(event.getPaymentId())
        .build();
  }

  static TransactionEntity.TransactionCreated eventFor(TransactionEntity.TransactionState state, TransactionApi.CreateTransactionCommand command) {
    return TransactionEntity.TransactionCreated
        .newBuilder()
        .setTransactionKey(
            TransactionMerchantKey.TransactionKey
                .newBuilder()
                .setTransactionId(command.getTransactionId())
                .setServiceCode(command.getServiceCode())
                .setAccountFrom(command.getAccountFrom())
                .setAccountTo(command.getAccountTo())
                .build())
        .setTransactionAmount(command.getTransactionAmount())
        .setMerchantId(command.getMerchantId())
        .setShopId(command.getShopId())
        .setTransactionTimestamp(command.getTransactionTimestamp())
        .build();
  }

  static TransactionEntity.PaymentAdded eventFor(TransactionEntity.TransactionState state, TransactionApi.AddPaymentCommand command) {
    return TransactionEntity.PaymentAdded
        .newBuilder()
        .setTransactionKey(
            TransactionMerchantKey.TransactionKey
                .newBuilder()
                .setTransactionId(command.getTransactionId())
                .setServiceCode(command.getServiceCode())
                .setAccountFrom(command.getAccountFrom())
                .setAccountTo(command.getAccountTo())
                .build())
        .setPaymentId(command.getPaymentId())
        .build();
  }
}

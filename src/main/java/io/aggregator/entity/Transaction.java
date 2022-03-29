package io.aggregator.entity;

import java.util.Optional;

import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.google.protobuf.Empty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.aggregator.api.TransactionApi;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
// This is the implementation for the Event Sourced Entity Service described in your io/aggregator/api/transaction_api.proto file.
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
  public Effect<Empty> paymentPriced(TransactionEntity.TransactionState state, TransactionApi.PaymentPricedCommand command) {
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
  public TransactionEntity.TransactionState incidentAdded(TransactionEntity.TransactionState state, TransactionEntity.IncidentAdded event) {
    return handle(state, event);
  }

  @Override
  public TransactionEntity.TransactionState paymentAdded(TransactionEntity.TransactionState state, TransactionEntity.PaymentAdded event) {
    return handle(state, event);
  }

  private Effect<Empty> handle(TransactionEntity.TransactionState state, TransactionApi.PaymentPricedCommand command) {
    log.debug("state: {}\nPaymentPricedCommand: {}", state, command);

    return effects()
        .emitEvent(eventFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(TransactionEntity.TransactionState state, TransactionApi.AddPaymentCommand command) {
    log.debug("state: {}\nAddPaymentCommand: {}", state, command);

    return effects()
        .emitEvent(eventFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Optional<Effect<TransactionApi.GetTransactionResponse>> reject(TransactionEntity.TransactionState state, TransactionApi.GetTransactionRequest request) {
    if (state.getTransactionId().isEmpty()) {
      return Optional.of(effects().error(String.format("Transaction %s not found", request)));
    }
    return Optional.empty();
  }

  private Effect<TransactionApi.GetTransactionResponse> handle(TransactionEntity.TransactionState state, TransactionApi.GetTransactionRequest request) {
    return effects().reply(
        TransactionApi.GetTransactionResponse
            .newBuilder()
            .setTransactionId(state.getTransactionId())
            .setMerchantId(state.getMerchantId())
            .setShopId(state.getShopId())
            .setTransactionAmount(state.getTransactionAmount())
            .setTransactionTimestamp(state.getTransactionTimestamp())
            .setPaymentId(state.getPaymentId())
            .build());
  }

  static TransactionEntity.TransactionState handle(TransactionEntity.TransactionState state, TransactionEntity.IncidentAdded event) {
    return state.toBuilder()
        // TODO
        .build();
  }

  static TransactionEntity.TransactionState handle(TransactionEntity.TransactionState state, TransactionEntity.PaymentAdded event) {
    return state.toBuilder()
        .setPaymentId(event.getPaymentId())
        .build();
  }

  static TransactionEntity.IncidentAdded eventFor(TransactionEntity.TransactionState state, TransactionApi.PaymentPricedCommand command) {
    return TransactionEntity.IncidentAdded
            .newBuilder()
            // TODO
            .setTransactionId(command.getTransactionId())
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

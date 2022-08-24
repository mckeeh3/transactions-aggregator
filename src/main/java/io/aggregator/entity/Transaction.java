package io.aggregator.entity;

import java.util.Optional;

import kalix.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.google.protobuf.Empty;

import io.aggregator.service.MerchantService;
import io.aggregator.service.RuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.aggregator.api.TransactionApi;

// This class was initially generated based on the .proto definition by Kalix tooling.
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
  public Effect<TransactionApi.GetTransactionResponse> getTransaction(TransactionEntity.TransactionState state, TransactionApi.GetTransactionRequest request) {
    return reject(state, request).orElseGet((() -> handle(state, request)));
  }

  @Override
  public TransactionEntity.TransactionState incidentAdded(TransactionEntity.TransactionState state, TransactionEntity.IncidentAdded event) {
    return handle(state, event);
  }

  private Effect<Empty> handle(TransactionEntity.TransactionState state, TransactionApi.PaymentPricedCommand command) {
    log.debug(Thread.currentThread().getName() + " - state: {}\nPaymentPricedCommand: {}", state, command);
    log.info(Thread.currentThread().getName() + " - RECEIVED COMMAND: PaymentPricedCommand");

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
            .build());
  }

  static TransactionEntity.TransactionState handle(TransactionEntity.TransactionState state, TransactionEntity.IncidentAdded event) {
    log.info(Thread.currentThread().getName() + " - RECEIVED EVENT: IncidentAdded");
    return state.toBuilder()
        .setTransactionId(event.getTransactionId())
        .setMerchantId(event.getMerchantId())
        .setShopId(event.getShopId())
        .addAllTransactionIncident(event.getTransactionIncidentList())
        .build();
  }

  static TransactionEntity.IncidentAdded eventFor(TransactionEntity.TransactionState state, TransactionApi.PaymentPricedCommand command) {
    String merchant = MerchantService.findMerchant(command.getShopId());
    return TransactionEntity.IncidentAdded
            .newBuilder()
            .setTransactionId(command.getTransactionId())
            .setEventType(command.getEventType())
            .setShopId(command.getShopId())
            .setMerchantId(merchant)
            .setIncidentTimestamp(command.getTimestamp())
            .addAllTransactionIncident(toTransactionIncidents(state, merchant, command))
            .build();
  }

  static Iterable<TransactionEntity.TransactionIncident> toTransactionIncidents(TransactionEntity.TransactionState state, String merchant, TransactionApi.PaymentPricedCommand command) {
    return RuleService.applyRules(state, merchant, command);
  }
}

package io.aggregator.entity;

import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Empty;

import io.aggregator.TimeTo;
import io.aggregator.api.StripedSecondApi;
import kalix.javasdk.eventsourcedentity.EventSourcedEntityContext;

// This class was initially generated based on the .proto definition by Kalix tooling.
// This is the implementation for the Event Sourced Entity Service described in your io/aggregator/api/striped_second_api.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class StripedSecond extends AbstractStripedSecond {
  static final Logger log = LoggerFactory.getLogger(StripedSecond.class);

  public StripedSecond(EventSourcedEntityContext context) {
  }

  @Override
  public StripedSecondEntity.StripedSecondState emptyState() {
    return StripedSecondEntity.StripedSecondState.getDefaultInstance();
  }

  @Override
  public Effect<Empty> addTransaction(StripedSecondEntity.StripedSecondState state, StripedSecondApi.AddTransactionCommand command) {
    return handle(state, command);
  }

  @Override
  public Effect<Empty> aggregateStripedSecond(StripedSecondEntity.StripedSecondState state, StripedSecondApi.AggregateStripedSecondCommand command) {
    return handle(state, command);
  }

  @Override
  public StripedSecondEntity.StripedSecondState stripedSecondActivated(StripedSecondEntity.StripedSecondState state, StripedSecondEntity.StripedSecondActivated event) {
    return handle(state, event);
  }

  @Override
  public StripedSecondEntity.StripedSecondState stripedSecondTransactionAdded(StripedSecondEntity.StripedSecondState state, StripedSecondEntity.StripedSecondTransactionAdded event) {
    return handle(state, event);
  }

  @Override
  public StripedSecondEntity.StripedSecondState stripedSecondAggregated(StripedSecondEntity.StripedSecondState state, StripedSecondEntity.StripedSecondAggregated event) {
    return handle(state, event);
  }

  @Override
  public StripedSecondEntity.StripedSecondState transactionPaid(StripedSecondEntity.StripedSecondState state, StripedSecondEntity.TransactionPaid event) {
    return state;
  }

  private Effect<Empty> handle(StripedSecondEntity.StripedSecondState state, StripedSecondApi.AddTransactionCommand command) {
    log.debug("state: {}\nAddTransactionCommand: {}", state, command);

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(StripedSecondEntity.StripedSecondState state, StripedSecondApi.AggregateStripedSecondCommand command) {
    log.debug("state: {}\nAggregateCommand: {}", state, command);

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  static StripedSecondEntity.StripedSecondState handle(StripedSecondEntity.StripedSecondState state, StripedSecondEntity.StripedSecondActivated event) {
    return state.toBuilder()
        .setMerchantKey(
            TransactionMerchantKey.MerchantKey
                .newBuilder()
                .setMerchantId(event.getMerchantKey().getMerchantId())
                .setServiceCode(event.getMerchantKey().getServiceCode())
                .setAccountFrom(event.getMerchantKey().getAccountFrom())
                .setAccountTo(event.getMerchantKey().getAccountTo())
                .build())
        .setStripe(event.getStripe())
        .setEpochSecond(event.getEpochSecond())
        .setEpochMinute(TimeTo.fromEpochSecond(event.getEpochSecond()).toEpochMinute())
        .setEpochHour(TimeTo.fromEpochSecond(event.getEpochSecond()).toEpochHour())
        .setEpochDay(TimeTo.fromEpochSecond(event.getEpochSecond()).toEpochDay())
        .build();
  }

  static StripedSecondEntity.StripedSecondState handle(StripedSecondEntity.StripedSecondState state, StripedSecondEntity.StripedSecondTransactionAdded event) {
    var transactionAlreadyAdded = state.getTransactionsList().stream()
        .anyMatch(transaction -> transaction.getTransactionKey().equals(event.getTransactionKey()));

    if (transactionAlreadyAdded) {
      return state; // idempotent - no need to re-add the same transaction
    } else {
      return state.toBuilder()
          .addTransactions(
              StripedSecondEntity.Transaction
                  .newBuilder()
                  .setMerchantId(event.getMerchantKey().getMerchantId())
                  .setEpochSecond(event.getEpochSecond())
                  .setStripe(event.getStripe())
                  .setTransactionKey(event.getTransactionKey())
                  .setAmount(event.getAmount())
                  .setTimestamp(event.getTimestamp())
                  .build())
          .build();
    }
  }

  static StripedSecondEntity.StripedSecondState handle(StripedSecondEntity.StripedSecondState state, StripedSecondEntity.StripedSecondAggregated event) {
    var transactions = state.getTransactionsList().stream()
        .map(transaction -> {
          if (transaction.getAggregateRequestTimestamp().getSeconds() == 0) {
            return transaction.toBuilder()
                .setAggregateRequestTimestamp(event.getAggregateRequestTimestamp())
                .build();
          } else {
            return transaction;
          }
        })
        .toList();

    return state.toBuilder()
        .clearTransactions()
        .addAllTransactions(transactions)
        .build();
  }

  static List<?> eventsFor(StripedSecondEntity.StripedSecondState state, StripedSecondApi.AddTransactionCommand command) {
    var transactionAdded = StripedSecondEntity.StripedSecondTransactionAdded
        .newBuilder()
        .setMerchantKey(
            TransactionMerchantKey.MerchantKey
                .newBuilder()
                .setMerchantId(command.getMerchantId())
                .setServiceCode(command.getServiceCode())
                .setAccountFrom(command.getAccountFrom())
                .setAccountTo(command.getAccountTo())
                .build())
        .setEpochSecond(command.getEpochSecond())
        .setStripe(command.getStripe())
        .setTransactionKey(
            TransactionMerchantKey.TransactionKey
                .newBuilder()
                .setTransactionId(command.getTransactionId())
                .setServiceCode(command.getServiceCode())
                .setAccountFrom(command.getAccountFrom())
                .setAccountTo(command.getAccountTo())
                .build())
        .setAmount(command.getAmount())
        .setTimestamp(command.getTimestamp())
        .build();

    var isInactive = state.getTransactionsCount() == 0 || state.getTransactionsList().stream()
        .allMatch(transaction -> transaction.getAggregateRequestTimestamp().getSeconds() > 0);

    if (isInactive) {
      var secondCreated = StripedSecondEntity.StripedSecondActivated
          .newBuilder()
          .setMerchantKey(
              TransactionMerchantKey.MerchantKey
                  .newBuilder()
                  .setMerchantId(command.getMerchantId())
                  .setServiceCode(command.getServiceCode())
                  .setAccountFrom(command.getAccountFrom())
                  .setAccountTo(command.getAccountTo())
                  .build())
          .setEpochSecond(command.getEpochSecond())
          .setStripe(command.getStripe())
          .build();

      return List.of(secondCreated, transactionAdded);
    } else {
      return List.of(transactionAdded);
    }
  }

  static List<?> eventsFor(StripedSecondEntity.StripedSecondState state, StripedSecondApi.AggregateStripedSecondCommand command) {
    var transactions = state.getTransactionsList().stream()
        .filter(transaction -> transaction.getAggregateRequestTimestamp().getSeconds() == 0)
        .toList();

    if (transactions.size() == 0) {
      return List.of(StripedSecondEntity.StripedSecondAggregated
          .newBuilder()
          .setMerchantKey(state.getMerchantKey())
          .setEpochSecond(state.getEpochSecond())
          .setStripe(state.getStripe())
          .setTransactionTotalAmount(0.0)
          .setTransactionCount(0)
          .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
          .setPaymentId(command.getPaymentId())
          .build());
    } else {
      var total = transactions.stream()
          .reduce(0.0, (a, b) -> a + b.getAmount(), (a, b) -> a + b);
      var lastUpdate = transactions.stream()
          .map(transaction -> transaction.getTimestamp())
          .max(TimeTo.comparator())
          .get();

      var transactionsPaid = transactions.stream()
          .map(transaction -> StripedSecondEntity.TransactionPaid
              .newBuilder()
              .setTransactionKey(
                  TransactionMerchantKey.TransactionKey
                      .newBuilder()
                      .setTransactionId(transaction.getTransactionKey().getTransactionId())
                      .setServiceCode(transaction.getTransactionKey().getServiceCode())
                      .setAccountFrom(transaction.getTransactionKey().getAccountFrom())
                      .setAccountTo(transaction.getTransactionKey().getAccountTo())
                      .build())
              .setMerchantId(command.getMerchantId())
              .setEpochSecond(state.getEpochSecond())
              .setStripe(state.getStripe())
              .setPaymentId(command.getPaymentId())
              .build());

      var stripedSecondAggregated = StripedSecondEntity.StripedSecondAggregated
          .newBuilder()
          .setMerchantKey(state.getMerchantKey())
          .setEpochSecond(state.getEpochSecond())
          .setStripe(state.getStripe())
          .setTransactionTotalAmount(total)
          .setTransactionCount(transactions.size())
          .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
          .setLastUpdateTimestamp(lastUpdate)
          .setPaymentId(command.getPaymentId())
          .build();

      return Stream.concat(Stream.of(stripedSecondAggregated), transactionsPaid).toList();
    }
  }
}

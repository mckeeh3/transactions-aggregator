package io.aggregator.entity;

import java.util.List;
import java.util.stream.Stream;

import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.google.protobuf.Empty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.aggregator.TimeTo;
import io.aggregator.api.SubSecondApi;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class SubSecond extends AbstractSubSecond {
  static final Logger log = LoggerFactory.getLogger(SubSecond.class);

  public SubSecond(EventSourcedEntityContext context) {
  }

  @Override
  public SubSecondEntity.SubSecondState emptyState() {
    return SubSecondEntity.SubSecondState.getDefaultInstance();
  }

  @Override
  public Effect<Empty> addTransaction(SubSecondEntity.SubSecondState state, SubSecondApi.AddTransactionCommand command) {
    return handle(state, command);
  }

  @Override
  public Effect<Empty> addLedgerItems(SubSecondEntity.SubSecondState currentState, SubSecondApi.AddLedgerItemsCommand addLedgerItemsCommand) {
    // TODO
    return null;
  }

  @Override
  public Effect<Empty> aggregateSubSecond(SubSecondEntity.SubSecondState state, SubSecondApi.AggregateSubSecondCommand command) {
    return handle(state, command);
  }

  @Override
  public SubSecondEntity.SubSecondState subSecondActivated(SubSecondEntity.SubSecondState state, SubSecondEntity.SubSecondActivated event) {
    return handle(state, event);
  }

  @Override
  public SubSecondEntity.SubSecondState subSecondTransactionAdded(SubSecondEntity.SubSecondState state, SubSecondEntity.SubSecondTransactionAdded event) {
    return handle(state, event);
  }

  @Override
  public SubSecondEntity.SubSecondState subSecondAggregated(SubSecondEntity.SubSecondState state, SubSecondEntity.SubSecondAggregated event) {
    return handle(state, event);
  }

  @Override
  public SubSecondEntity.SubSecondState transactionPaid(SubSecondEntity.SubSecondState state, SubSecondEntity.TransactionPaid event) {
    return state;
  }

  private Effect<Empty> handle(SubSecondEntity.SubSecondState state, SubSecondApi.AddTransactionCommand command) {
    log.debug("state: {}\nAddTransactionCommand: {}", state, command);

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(SubSecondEntity.SubSecondState state, SubSecondApi.AggregateSubSecondCommand command) {
    log.debug("state: {}\nAggregateCommand: {}", state, command);

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  static SubSecondEntity.SubSecondState handle(SubSecondEntity.SubSecondState state, SubSecondEntity.SubSecondActivated event) {
    return state.toBuilder()
        .setMerchantKey(
            TransactionMerchantKey.MerchantKey
                .newBuilder()
                .setMerchantId(event.getMerchantKey().getMerchantId())
                .setServiceCode(event.getMerchantKey().getServiceCode())
                .setAccountFrom(event.getMerchantKey().getAccountFrom())
                .setAccountTo(event.getMerchantKey().getAccountTo())
                .build())
        .setEpochSubSecond(event.getEpochSubSecond())
        .setEpochSecond(TimeTo.fromEpochSubSecond(event.getEpochSubSecond()).toEpochSecond())
        .setEpochMinute(TimeTo.fromEpochSubSecond(event.getEpochSubSecond()).toEpochMinute())
        .setEpochHour(TimeTo.fromEpochSubSecond(event.getEpochSubSecond()).toEpochHour())
        .setEpochDay(TimeTo.fromEpochSubSecond(event.getEpochSubSecond()).toEpochDay())
        .build();
  }

  static SubSecondEntity.SubSecondState handle(SubSecondEntity.SubSecondState state, SubSecondEntity.SubSecondTransactionAdded event) {
    var transactionAlreadyAdded = state.getTransactionsList().stream()
        .anyMatch(transaction -> transaction.getTransactionKey().equals(event.getTransactionKey()));

    if (transactionAlreadyAdded) {
      return state; // idempotent - no need to re-add the same transaction
    } else {
      return state.toBuilder()
          .addTransactions(
              SubSecondEntity.Transaction
                  .newBuilder()
                  .setMerchantId(event.getMerchantKey().getMerchantId())
                  .setEpochSubSecond(event.getEpochSubSecond())
                  .setTransactionKey(event.getTransactionKey())
                  .setAmount(event.getAmount())
                  .setTimestamp(event.getTimestamp())
                  .build())
          .build();
    }
  }

  static SubSecondEntity.SubSecondState handle(SubSecondEntity.SubSecondState state, SubSecondEntity.SubSecondAggregated event) {
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

  static List<?> eventsFor(SubSecondEntity.SubSecondState state, SubSecondApi.AddTransactionCommand command) {
    var transactionAdded = SubSecondEntity.SubSecondTransactionAdded
        .newBuilder()
        .setMerchantKey(
            TransactionMerchantKey.MerchantKey
                .newBuilder()
                .setMerchantId(command.getMerchantId())
                .setServiceCode(command.getServiceCode())
                .setAccountFrom(command.getAccountFrom())
                .setAccountTo(command.getAccountTo())
                .build())
        .setEpochSubSecond(command.getEpochSubSecond())
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
      var secondCreated = SubSecondEntity.SubSecondActivated
          .newBuilder()
          .setMerchantKey(
              TransactionMerchantKey.MerchantKey
                  .newBuilder()
                  .setMerchantId(command.getMerchantId())
                  .setServiceCode(command.getServiceCode())
                  .setAccountFrom(command.getAccountFrom())
                  .setAccountTo(command.getAccountTo())
                  .build())
          .setEpochSubSecond(command.getEpochSubSecond())
          .build();

      return List.of(secondCreated, transactionAdded);
    } else {
      return List.of(transactionAdded);
    }
  }

  static List<?> eventsFor(SubSecondEntity.SubSecondState state, SubSecondApi.AggregateSubSecondCommand command) {
    var transactions = state.getTransactionsList().stream()
        .filter(transaction -> transaction.getAggregateRequestTimestamp().getSeconds() == 0)
        .toList();

    if (transactions.size() == 0) {
      return List.of(SubSecondEntity.SubSecondAggregated
          .newBuilder()
          .setMerchantKey(state.getMerchantKey())
          .setEpochSubSecond(state.getEpochSubSecond())
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
          .map(transaction -> SubSecondEntity.TransactionPaid
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
              .setEpochSubSecond(state.getEpochSubSecond())
              .setPaymentId(command.getPaymentId())
              .build());

      var subSecondAggregated = SubSecondEntity.SubSecondAggregated
          .newBuilder()
          .setMerchantKey(state.getMerchantKey())
          .setEpochSubSecond(state.getEpochSubSecond())
          .setTransactionTotalAmount(total)
          .setTransactionCount(transactions.size())
          .setAggregateRequestTimestamp(command.getAggregateRequestTimestamp())
          .setLastUpdateTimestamp(lastUpdate)
          .setPaymentId(command.getPaymentId())
          .build();

      return Stream.concat(Stream.of(subSecondAggregated), transactionsPaid).toList();
    }
  }
}

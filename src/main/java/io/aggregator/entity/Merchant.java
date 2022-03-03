package io.aggregator.entity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.google.protobuf.Empty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.aggregator.TimeTo;
import io.aggregator.api.MerchantApi;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
// This is the implementation for the Event Sourced Entity Service described in your io/aggregator/api/merchant_api.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class Merchant extends AbstractMerchant {
  static final Logger log = LoggerFactory.getLogger(Merchant.class);

  public Merchant(EventSourcedEntityContext context) {
  }

  @Override
  public MerchantEntity.MerchantState emptyState() {
    return MerchantEntity.MerchantState.getDefaultInstance();
  }

  @Override
  public Effect<Empty> addDay(MerchantEntity.MerchantState state, MerchantApi.AddDayCommand command) {
    return handle(state, command);
  }

  @Override
  public Effect<Empty> merchantAggregationRequest(MerchantEntity.MerchantState state, MerchantApi.MerchantAggregationRequestCommand command) {
    return reject(state, command).orElseGet(() -> handle(state, command));
  }

  @Override
  public Effect<Empty> merchantPaymentRequest(MerchantEntity.MerchantState state, MerchantApi.MerchantPaymentRequestCommand command) {
    return reject(state, command).orElseGet(() -> handle(state, command));
  }

  @Override
  public MerchantEntity.MerchantState merchantDayAdded(MerchantEntity.MerchantState state, MerchantEntity.MerchantDayAdded event) {
    return handle(state, event);
  }

  @Override
  public MerchantEntity.MerchantState merchantPaymentRequested(MerchantEntity.MerchantState state, MerchantEntity.MerchantPaymentRequested event) {
    return handle(state, event);
  }

  @Override
  public MerchantEntity.MerchantState merchantAggregationRequested(MerchantEntity.MerchantState state, MerchantEntity.MerchantAggregationRequested event) {
    return handle(state, event);
  }

  private Optional<Effect<Empty>> reject(MerchantEntity.MerchantState state, MerchantApi.MerchantAggregationRequestCommand command) {
    if (command.getMerchantId().isEmpty()) {
      return Optional.of(effects().error("MerchantId is required"));
    }
    if (command.getServiceCode().isEmpty()) {
      return Optional.of(effects().error("ServiceCode is required"));
    }
    if (command.getAccountFrom().isEmpty()) {
      return Optional.of(effects().error("AccountFrom is required"));
    }
    if (command.getAccountTo().isEmpty()) {
      return Optional.of(effects().error("AccountTo is required"));
    }
    return Optional.empty();
  }

  private Optional<Effect<Empty>> reject(MerchantEntity.MerchantState state, MerchantApi.MerchantPaymentRequestCommand command) {
    if (command.getMerchantId().isEmpty()) {
      return Optional.of(effects().error("MerchantId is required"));
    }
    if (command.getServiceCode().isEmpty()) {
      return Optional.of(effects().error("ServiceCode is required"));
    }
    if (command.getAccountFrom().isEmpty()) {
      return Optional.of(effects().error("AccountFrom is required"));
    }
    if (command.getAccountTo().isEmpty()) {
      return Optional.of(effects().error("AccountTo is required"));
    }
    return Optional.empty();
  }

  private Effect<Empty> handle(MerchantEntity.MerchantState state, MerchantApi.AddDayCommand command) {
    log.info("state: {}\nAddDayCommand: {}", state, command);

    return effects()
        .emitEvent(eventFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(MerchantEntity.MerchantState state, MerchantApi.MerchantAggregationRequestCommand command) {
    log.info("state: {}\nMerchantAggregationRequestCommand: {}", state, command);

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(MerchantEntity.MerchantState state, MerchantApi.MerchantPaymentRequestCommand command) {
    log.info("state: {}\nMerchantPaymentRequestCommand: {}", state, command);

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private MerchantEntity.MerchantState handle(MerchantEntity.MerchantState state, MerchantEntity.MerchantDayAdded event) {
    var alreadyAddedDay = state.getActiveDaysList().stream().anyMatch(epochDay -> epochDay == event.getEpochDay());

    if (!alreadyAddedDay) {
      state = state.toBuilder()
          .addActiveDays(event.getEpochDay())
          .build();
    }

    if (state.getMerchantKey().getMerchantId().isEmpty()) {
      return state.toBuilder()
          .setMerchantKey(
              TransactionMerchantKey.MerchantKey
                  .newBuilder()
                  .setMerchantId(event.getMerchantKey().getMerchantId())
                  .setServiceCode(event.getMerchantKey().getServiceCode())
                  .setAccountFrom(event.getMerchantKey().getAccountFrom())
                  .setAccountTo(event.getMerchantKey().getAccountTo())
                  .build())
          .build();
    } else {
      return state;
    }
  }

  private MerchantEntity.MerchantState handle(MerchantEntity.MerchantState state, MerchantEntity.MerchantPaymentRequested event) {
    return state.toBuilder()
        .clearActiveDays()
        .setPaymentCount(state.getPaymentCount() + 1)
        .build();
  }

  private MerchantEntity.MerchantState handle(MerchantEntity.MerchantState state, MerchantEntity.MerchantAggregationRequested event) {
    return state.toBuilder()
        .clearActiveDays()
        .build();
  }

  private MerchantEntity.MerchantDayAdded eventFor(MerchantEntity.MerchantState state, MerchantApi.AddDayCommand command) {
    return MerchantEntity.MerchantDayAdded
        .newBuilder()
        .setMerchantKey(
            TransactionMerchantKey.MerchantKey
                .newBuilder()
                .setMerchantId(command.getMerchantId())
                .setServiceCode(command.getServiceCode())
                .setAccountFrom(command.getAccountFrom())
                .setAccountTo(command.getAccountTo())
                .build())
        .setEpochDay(command.getEpochDay())
        .build();
  }

  private List<?> eventsFor(MerchantEntity.MerchantState state, MerchantApi.MerchantAggregationRequestCommand command) {
    if (state.getActiveDaysCount() == 0) {
      return List.of();
    }

    return state.getActiveDaysList().stream()
        .map(epochDay -> toMerchantAggregationRequested(state, toMerchantKey(command), epochDay))
        .toList();
  }

  private List<?> eventsFor(MerchantEntity.MerchantState state, MerchantApi.MerchantPaymentRequestCommand command) {
    if (state.getActiveDaysCount() == 0) {
      return List.of(toMerchantPaymentRequested(state, toMerchantKey(command)));
    }

    var requests = state.getActiveDaysList().stream()
        .map(epochDay -> toMerchantAggregationRequested(state, toMerchantKey(command), epochDay));

    var request = toMerchantPaymentRequested(state, toMerchantKey(command));

    return Stream.concat(Stream.of(request), requests).toList();
  }

  private MerchantEntity.MerchantAggregationRequested toMerchantAggregationRequested(MerchantEntity.MerchantState state, TransactionMerchantKey.MerchantKey merchantKey, Long epochDay) {
    return MerchantEntity.MerchantAggregationRequested
        .newBuilder()
        .setMerchantKey(merchantKey)
        .setEpochDay(epochDay)
        .setPaymentId(paymentIdNext(state))
        .setAggregateRequestTimestamp(TimeTo.now())
        .build();
  }

  private MerchantEntity.MerchantPaymentRequested toMerchantPaymentRequested(MerchantEntity.MerchantState state, TransactionMerchantKey.MerchantKey merchantKey) {
    return MerchantEntity.MerchantPaymentRequested
        .newBuilder()
        .setMerchantKey(merchantKey)
        .setPaymentId(paymentIdNext(state))
        .build();
  }

  private TransactionMerchantKey.MerchantKey toMerchantKey(MerchantApi.MerchantAggregationRequestCommand command) {
    return TransactionMerchantKey.MerchantKey
        .newBuilder()
        .setMerchantId(command.getMerchantId())
        .setServiceCode(command.getServiceCode())
        .setAccountFrom(command.getAccountFrom())
        .setAccountTo(command.getAccountTo())
        .build();
  }

  private TransactionMerchantKey.MerchantKey toMerchantKey(MerchantApi.MerchantPaymentRequestCommand command) {
    return TransactionMerchantKey.MerchantKey
        .newBuilder()
        .setMerchantId(command.getMerchantId())
        .setServiceCode(command.getServiceCode())
        .setAccountFrom(command.getAccountFrom())
        .setAccountTo(command.getAccountTo())
        .build();
  }

  static String paymentIdNext(MerchantEntity.MerchantState state) {
    return String.format("payment-%d", state.getPaymentCount() + 1);
  }
}
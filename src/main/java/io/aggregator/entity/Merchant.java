package io.aggregator.entity;

import java.util.List;
import java.util.Optional;

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
  public Effect<Empty> activateDay(MerchantEntity.MerchantState state, MerchantApi.ActivateDayCommand command) {
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
  public MerchantEntity.MerchantState merchantDayActivated(MerchantEntity.MerchantState state, MerchantEntity.MerchantDayActivated event) {
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

  private Effect<Empty> handle(MerchantEntity.MerchantState state, MerchantApi.ActivateDayCommand command) {
    log.info("state: {}\nActivateDayCommand: {}", state, command);

    return effects()
        .emitEvents(eventsFor(state, command))
        .thenReply(newState -> Empty.getDefaultInstance());
  }

  private Effect<Empty> handle(MerchantEntity.MerchantState state, MerchantApi.MerchantAggregationRequestCommand command) {
    log.info("state: {}\nMerchantAggregationRequestCommand: {}", state, command);

    var event = eventFor(state, command);

    if (event.isPresent()) {
      return effects()
          .emitEvent(event.get())
          .thenReply(newState -> Empty.getDefaultInstance());
    } else {
      return effects().reply(Empty.getDefaultInstance());
    }
  }

  private Effect<Empty> handle(MerchantEntity.MerchantState state, MerchantApi.MerchantPaymentRequestCommand command) {
    log.info("state: {}\nMerchantPaymentRequestCommand: {}", state, command);

    var event = eventFor(state, command);

    if (event.isPresent()) {
      return effects()
          .emitEvent(event.get())
          .thenReply(newState -> Empty.getDefaultInstance());
    } else {
      return effects().reply(Empty.getDefaultInstance());
    }
  }

  static MerchantEntity.MerchantState handle(MerchantEntity.MerchantState state, MerchantEntity.MerchantDayActivated event) {
    var alreadyActivatedDay = state.getActiveDaysList().stream().anyMatch(epochDay -> epochDay == event.getEpochDay());

    if (!alreadyActivatedDay) {
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

  static MerchantEntity.MerchantState handle(MerchantEntity.MerchantState state, MerchantEntity.MerchantPaymentRequested event) {
    return state.toBuilder()
        .clearActiveDays()
        .setPaymentIdSequenceNumber(state.getPaymentIdSequenceNumber() + 1)
        .build();
  }

  static MerchantEntity.MerchantState handle(MerchantEntity.MerchantState state, MerchantEntity.MerchantAggregationRequested event) {
    return state.toBuilder()
        .clearActiveDays()
        .build();
  }

  static List<?> eventsFor(MerchantEntity.MerchantState state, MerchantApi.ActivateDayCommand command) {
    var merchantKey = TransactionMerchantKey.MerchantKey
        .newBuilder()
        .setMerchantId(command.getMerchantId())
        .setServiceCode(command.getServiceCode())
        .setAccountFrom(command.getAccountFrom())
        .setAccountTo(command.getAccountTo())
        .build();

    var merchantDayActivated = MerchantEntity.MerchantDayActivated
        .newBuilder()
        .setMerchantKey(merchantKey)
        .setEpochDay(command.getEpochDay())
        .setPaymentId(paymentIdNext(state))
        .build();

    var merchantAggregationRequested = MerchantEntity.MerchantAggregationRequested
        .newBuilder()
        .setMerchantKey(merchantKey)
        .setPaymentId(paymentIdNext(state))
        .setAggregateRequestTimestamp(TimeTo.now())
        .addAllActiveDays(state.getActiveDaysList())
        .addActiveDays(command.getEpochDay())
        .build();

    return List.of(merchantDayActivated, merchantAggregationRequested);
  }

  static Optional<MerchantEntity.MerchantAggregationRequested> eventFor(MerchantEntity.MerchantState state, MerchantApi.MerchantAggregationRequestCommand command) {
    if (state.getActiveDaysCount() == 0) {
      return Optional.empty();
    }

    return Optional.of(toMerchantAggregationRequested(state, toMerchantKey(command)));
  }

  static Optional<MerchantEntity.MerchantPaymentRequested> eventFor(MerchantEntity.MerchantState state, MerchantApi.MerchantPaymentRequestCommand command) {
    return Optional.of(toMerchantPaymentRequested(state, toMerchantKey(command)));
  }

  static MerchantEntity.MerchantAggregationRequested toMerchantAggregationRequested(MerchantEntity.MerchantState state, TransactionMerchantKey.MerchantKey merchantKey) {
    return MerchantEntity.MerchantAggregationRequested
        .newBuilder()
        .setMerchantKey(merchantKey)
        .setPaymentId(paymentIdNext(state))
        .setAggregateRequestTimestamp(TimeTo.now())
        .addAllActiveDays(state.getActiveDaysList())
        .build();
  }

  static MerchantEntity.MerchantPaymentRequested toMerchantPaymentRequested(MerchantEntity.MerchantState state, TransactionMerchantKey.MerchantKey merchantKey) {
    return MerchantEntity.MerchantPaymentRequested
        .newBuilder()
        .setMerchantKey(merchantKey)
        .setPaymentId(paymentIdNext(state))
        .setAggregateRequestTimestamp(TimeTo.now())
        .addAllActiveDays(state.getActiveDaysList())
        .build();
  }

  static TransactionMerchantKey.MerchantKey toMerchantKey(MerchantApi.MerchantAggregationRequestCommand command) {
    return TransactionMerchantKey.MerchantKey
        .newBuilder()
        .setMerchantId(command.getMerchantId())
        .setServiceCode(command.getServiceCode())
        .setAccountFrom(command.getAccountFrom())
        .setAccountTo(command.getAccountTo())
        .build();
  }

  static TransactionMerchantKey.MerchantKey toMerchantKey(MerchantApi.MerchantPaymentRequestCommand command) {
    return TransactionMerchantKey.MerchantKey
        .newBuilder()
        .setMerchantId(command.getMerchantId())
        .setServiceCode(command.getServiceCode())
        .setAccountFrom(command.getAccountFrom())
        .setAccountTo(command.getAccountTo())
        .build();
  }

  static String paymentIdNext(MerchantEntity.MerchantState state) {
    return String.format("payment-%d", state.getPaymentIdSequenceNumber() + 1);
  }
}
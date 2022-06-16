package io.aggregator;

import io.aggregator.action.*;
import io.aggregator.entity.Day;
import io.aggregator.entity.DayProvider;
import io.aggregator.entity.Hour;
import io.aggregator.entity.HourProvider;
import io.aggregator.entity.Merchant;
import io.aggregator.entity.MerchantProvider;
import io.aggregator.entity.Minute;
import io.aggregator.entity.MinuteProvider;
import io.aggregator.entity.Payment;
import io.aggregator.entity.PaymentProvider;
import io.aggregator.entity.Second;
import io.aggregator.entity.SecondProvider;
import io.aggregator.entity.StripedSecond;
import io.aggregator.entity.StripedSecondProvider;
import io.aggregator.entity.Transaction;
import io.aggregator.entity.TransactionProvider;
import io.aggregator.view.MerchantPaymentsAllView;
import io.aggregator.view.MerchantPaymentsByDateView;
import io.aggregator.view.MerchantPaymentsByDateViewProvider;
import io.aggregator.view.MerchantPaymentsByMerchantByDateView;
import io.aggregator.view.MerchantsByMerchantIdView;
import io.aggregator.view.MerchantsNotPaidView;
import io.aggregator.view.TransactionsNotPaidByDateView;
import io.aggregator.view.TransactionsPaidByPaymentByDateView;
import kalix.javasdk.Kalix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public final class Main {

  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  public static Kalix createKalix() {
    Kalix kalix = new Kalix();
    return kalix
        .register(DayProvider.of(Day::new))
        .register(DayToHourActionProvider.of(DayToHourAction::new))
        .register(DayToMerchantActionProvider.of(DayToMerchantAction::new))
        .register(DayToPaymentActionProvider.of(DayToPaymentAction::new))
        .register(FrontendActionProvider.of(FrontendAction::new))
        .register(HourProvider.of(Hour::new))
        .register(HourToDayActionProvider.of(HourToDayAction::new))
        .register(HourToMinuteActionProvider.of(HourToMinuteAction::new))
//        .register(MerchantPaymentsAllViewProvider.of(MerchantPaymentsAllView::new))
//        .register(MerchantPaymentsByDateViewProvider.of(MerchantPaymentsByDateView::new))
//        .register(MerchantPaymentsByMerchantByDateViewProvider.of(MerchantPaymentsByMerchantByDateView::new))
        .register(MerchantProvider.of(Merchant::new))
        .register(MerchantToDayActionProvider.of(MerchantToDayAction::new))
        .register(MerchantToPaymentActionProvider.of(MerchantToPaymentAction::new))
//        .register(MerchantsByMerchantIdViewProvider.of(MerchantsByMerchantIdView::new))
//        .register(MerchantsNotPaidViewProvider.of(MerchantsNotPaidView::new))
        .register(MinuteProvider.of(Minute::new))
        .register(MinuteToHourActionProvider.of(MinuteToHourAction::new))
        .register(MinuteToSecondActionProvider.of(MinuteToSecondAction::new))
        .register(PaymentProvider.of(Payment::new))
        .register(SecondProvider.of(Second::new))
        .register(SecondToMinuteActionProvider.of(SecondToMinuteAction::new))
        .register(SecondToStripedSecondActionProvider.of(SecondToStripedSecondAction::new))
        .register(StripedSecondProvider.of(StripedSecond::new))
        .register(StripedSecondToSecondActionProvider.of(StripedSecondToSecondAction::new))
        .register(StripedSecondToTransactionActionProvider.of(StripedSecondToTransactionAction::new))
        .register(TransactionProvider.of(Transaction::new))
        .register(TransactionToStripedSecondActionProvider.of(TransactionToStripedSecondAction::new))
        .register(TransactionTopicConsumerActionProvider.of(TransactionTopicConsumerAction::new))
//        .register(TransactionsNotPaidByDateViewProvider.of(TransactionsNotPaidByDateView::new))
//        .register(TransactionsPaidByPaymentByDateViewProvider.of(TransactionsPaidByPaymentByDateView::new))
        ;
  }

  public static void main(String[] args) throws Exception {
    LOG.info("starting the Kalix service");
    createKalix().start();
  }
}

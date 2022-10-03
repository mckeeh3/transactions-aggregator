package io.aggregator;

import io.aggregator.action.*;
import io.aggregator.entity.*;
import kalix.javasdk.Kalix;
import io.aggregator.view.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public final class Main {

  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  public static Kalix createKalix() {
    // The KalixFactory automatically registers any generated Actions, Views or Entities,
    // and is kept up-to-date with any changes in your protobuf definitions.
    // If you prefer, you may remove this and manually register these components in a
    // `new Kalix()` instance.
    return KalixFactory.withComponents(
      Day::new,
      Hour::new,
      Merchant::new,
      Minute::new,
      Payment::new,
      Second::new,
      StripedSecond::new,
      Transaction::new,
      DayToHourAction::new,
      DayToMerchantAction::new,
      DayToPaymentAction::new,
      FrontendAction::new,
      HourToDayAction::new,
      HourToMinuteAction::new,
      MerchantPaymentsByDateView::new,
      MerchantPaymentsByMerchantByDateView::new,
      MerchantToDayAction::new,
      MerchantToPaymentAction::new,
      MerchantsByMerchantIdView::new,
      MerchantsNotPaidView::new,
      MinuteToHourAction::new,
      MinuteToSecondAction::new,
      SecondToMinuteAction::new,
      SecondToStripedSecondAction::new,
      StripedSecondToSecondAction::new,
      TransactionToStripedSecondAction::new,
      TransactionsNotPaidByDateView::new,
      TransactionsPaidByPaymentByDateView::new);
  }

  public static void main(String[] args) throws Exception {
    LOG.info("starting the Kalix service");
    createKalix().start();
  }
}

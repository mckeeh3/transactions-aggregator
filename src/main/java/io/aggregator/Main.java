package io.aggregator;

import io.aggregator.entity.*;
import kalix.javasdk.Kalix;
import io.aggregator.action.DayToHourAction;
import io.aggregator.action.DayToMerchantAction;
import io.aggregator.action.DayToPaymentAction;
import io.aggregator.action.FrontendAction;
import io.aggregator.action.HourToDayAction;
import io.aggregator.action.HourToMinuteAction;
import io.aggregator.action.MerchantToDayAction;
import io.aggregator.action.MerchantToPaymentAction;
import io.aggregator.action.MinuteToHourAction;
import io.aggregator.action.MinuteToSecondAction;
import io.aggregator.action.SecondToMinuteAction;
import io.aggregator.action.SecondToSubSecondAction;
import io.aggregator.action.SubSecondToSecondAction;
import io.aggregator.action.TransactionToSubSecondAction;
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
      Incident::new,
      Merchant::new,
      Minute::new,
      Payment::new,
      Second::new,
      SubSecond::new,
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
      SecondToSubSecondAction::new,
      SubSecondToSecondAction::new,
      TransactionToSubSecondAction::new,
      TransactionsNotPaidByDateView::new,
      TransactionsPaidByPaymentByDateView::new);
  }

  public static void main(String[] args) throws Exception {
    LOG.info("starting the Kalix service");
    createKalix().start();
  }
}

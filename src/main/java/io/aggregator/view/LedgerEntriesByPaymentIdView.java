package io.aggregator.view;

import com.akkaserverless.javasdk.view.View;
import com.akkaserverless.javasdk.view.ViewContext;
import com.google.protobuf.Any;
import io.aggregator.entity.TransactionEntity;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
// This is the implementation for the View Service described in your io/aggregator/view/ledger_entries_by_payment_id.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class LedgerEntriesByPaymentIdView extends AbstractLedgerEntriesByPaymentIdView {

  public LedgerEntriesByPaymentIdView(ViewContext context) {}

  @Override
  public TransactionModel.Transaction emptyState() {
    throw new UnsupportedOperationException("Not implemented yet, replace with your empty view state");
  }

  @Override
  public View.UpdateEffect<TransactionModel.Transaction> onIncidentAdded(
    TransactionModel.Transaction state, TransactionEntity.IncidentAdded incidentAdded) {
    throw new UnsupportedOperationException("Update handler for 'OnIncidentAdded' not implemented yet");
  }
  @Override
  public View.UpdateEffect<TransactionModel.Transaction> ignoreOtherEvents(
    TransactionModel.Transaction state, Any any) {
    throw new UnsupportedOperationException("Update handler for 'IgnoreOtherEvents' not implemented yet");
  }
}


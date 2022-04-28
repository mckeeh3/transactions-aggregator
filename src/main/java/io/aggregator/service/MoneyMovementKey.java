package io.aggregator.service;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MoneyMovementKey {
  String from;
  String to;
}

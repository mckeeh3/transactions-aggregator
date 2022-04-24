package io.aggregator.service;

public class MerchantService {
  public static String findMerchant(String shopId) {
    return shopId.split("-")[0];
  }
}

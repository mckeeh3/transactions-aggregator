package io.aggregator;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

import com.google.protobuf.Timestamp;

public class TimeTo {
  public static Timestamp zero() {
    return Timestamp
        .newBuilder()
        .setSeconds(0)
        .setNanos(0)
        .build();
  }

  public static Timestamp now() {
    var now = Instant.now();
    return Timestamp
        .newBuilder()
        .setSeconds(now.getEpochSecond())
        .setNanos(now.getNano())
        .build();
  }

  public static Timestamp timestampForEpochDayX(long epochDay) {
    return Timestamp
        .newBuilder()
        .setSeconds(epochDay * 60 * 60 * 24)
        .setNanos(0)
        .build();
  }

  public static int compare(Timestamp timestamp1, Timestamp timestamp2) {
    if (timestamp1.getSeconds() < timestamp2.getSeconds()) {
      return -1;
    } else if (timestamp1.getSeconds() > timestamp2.getSeconds()) {
      return 1;
    } else if (timestamp1.getNanos() < timestamp2.getNanos()) {
      return -1;
    } else if (timestamp1.getNanos() > timestamp2.getNanos()) {
      return 1;
    } else {
      return 0;
    }
  }

  public static Comparator<Timestamp> comparator() {
    return (timestamp1, timestamp2) -> compare(timestamp1, timestamp2);
  }

  public static From fromEpochSecond(long epochSecond) {
    return new From(Timestamp
        .newBuilder()
        .setSeconds(epochSecond)
        .setNanos(0)
        .build());
  }

  public static From fromEpochMinute(long epochMinute) {
    return new From(Timestamp
        .newBuilder()
        .setSeconds(epochMinute * 60)
        .setNanos(0)
        .build());
  }

  public static From fromEpochHour(long epochHour) {
    return new From(Timestamp
        .newBuilder()
        .setSeconds(epochHour * 60 * 60)
        .setNanos(0)
        .build());
  }

  public static From fromEpochDay(long epochDay) {
    return new From(Timestamp
        .newBuilder()
        .setSeconds(epochDay * 60 * 60 * 24)
        .setNanos(0)
        .build());
  }

  public static From fromTimestamp(Timestamp timestamp) {
    return new From(timestamp);
  }

  public static class From {
    private final Timestamp timestamp;

    private From(Timestamp timestamp) {
      this.timestamp = timestamp;
    }

    public long toEpochSecond() {
      return timestamp.getSeconds();
    }

    public long toEpochMinute() {
      return timestamp.getSeconds() / 60;
    }

    public long toEpochHour() {
      return timestamp.getSeconds() / 60 / 60;
    }

    public long toEpochDay() {
      return timestamp.getSeconds() / 60 / 60 / 24;
    }

    public Timestamp toTimestamp() {
      return timestamp;
    }

    public Instant toInstant() {
      return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }

    public String format() {
      return toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE_TIME);
    }
  }
}

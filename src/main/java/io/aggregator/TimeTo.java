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

  public static Timestamp max(Timestamp a, Timestamp b) {
    if (a.getSeconds() > b.getSeconds()) {
      return a;
    } else if (a.getSeconds() < b.getSeconds()) {
      return b;
    } else {
      if (a.getNanos() > b.getNanos()) {
        return a;
      } else {
        return b;
      }
    }
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

  public static From fromZero() {
    return fromEpochSecond(0);
  }

  public static From fromNow() {
    return fromTimestamp(now());
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

    public MathOp plus() {
      return new MathOp(timestamp, false);
    }

    public MathOp minus() {
      return new MathOp(timestamp, true);
    }
  }

  public static class MathOp {
    private final Timestamp timestamp;
    private final boolean subtract;

    public MathOp(Timestamp timestamp, boolean subtract) {
      this.timestamp = timestamp;
      this.subtract = subtract;
    }

    public From nanos(int nanos) {
      var nanosAdjusted = subtract ? timestamp.getNanos() - nanos : timestamp.getNanos() + nanos;
      if (nanosAdjusted < 0) {
        return new From(Timestamp
            .newBuilder()
            .setSeconds(timestamp.getSeconds() - 1)
            .setNanos(1_000_000_000 + nanosAdjusted)
            .build());
      } else if (nanosAdjusted >= 1000000000) {
        return new From(Timestamp
            .newBuilder()
            .setSeconds(timestamp.getSeconds() + 1)
            .setNanos(nanosAdjusted - 1_000_000_000)
            .build());
      } else {
        return new From(Timestamp
            .newBuilder()
            .setSeconds(timestamp.getSeconds())
            .setNanos(subtract ? timestamp.getNanos() - nanos : timestamp.getNanos() + nanos)
            .build());
      }

    }

    public From seconds(long seconds) {
      return fromTimestamp(Timestamp
          .newBuilder()
          .setSeconds(timestamp.getSeconds() + seconds * (subtract ? -1 : 1))
          .setNanos(timestamp.getNanos())
          .build());
    }

    public From minutes(long minutes) {
      return fromTimestamp(Timestamp
          .newBuilder()
          .setSeconds(timestamp.getSeconds() + minutes * 60 * (subtract ? -1 : 1))
          .setNanos(timestamp.getNanos())
          .build());
    }

    public From hours(long hours) {
      return fromTimestamp(Timestamp
          .newBuilder()
          .setSeconds(timestamp.getSeconds() + hours * 60 * 60 * (subtract ? -1 : 1))
          .setNanos(timestamp.getNanos())
          .build());
    }

    public From days(long days) {
      return fromTimestamp(Timestamp
          .newBuilder()
          .setSeconds(timestamp.getSeconds() + days * 60 * 60 * 24 * (subtract ? -1 : 1))
          .setNanos(timestamp.getNanos())
          .build());
    }
  }
}

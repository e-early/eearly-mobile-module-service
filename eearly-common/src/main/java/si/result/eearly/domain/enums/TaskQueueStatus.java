package si.result.eearly.domain.enums;

import lombok.Getter;

@Getter
public enum TaskQueueStatus {
  TODO("TODO"),
  PROCESSING("PROCESSING"),
  RETRY("RETRY"),
  FAILED("FAILED");

  private final String value;

  TaskQueueStatus(String value) {
    this.value = value;
  }
}

package si.result.eearly.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import si.result.eearly.domain.schedule.Schedule;
import si.result.eearly.exception.ServiceInvalidRequestException;

class ScheduleFiltersTest {

  @ParameterizedTest
  @ValueSource(strings = {
      "",
      "active:ISTRUE",
      "name:EQ:Morning",
      "name:EQ:Morning AND active:ISTRUE",
      "NOT name:EQ:Morning",
      "(name:EQ:A OR name:EQ:B) AND active:ISFALSE",
      "description:EQ:Take it at 8.30",
      "name:IN:[a.b,c.d]"})
  void acceptsFieldsOfTheQueriedRecord(String filter) {
    assertThat(ScheduleFilters.toSpecification(filter, Schedule.class)).isNotNull();
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "profiles.userId:ISNOTNULL",
      "profiles.notificationDaySummary:ISTRUE",
      "active:ISTRUE AND profiles.notificationNoInternet:ISTRUE",
      "name:EQ:A OR scheduleEntries.description:LIKE:x",
      "NOT profiles.notificationNoBluetooth:ISTRUE",
      "(active:ISTRUE AND (name:EQ:A OR profiles.userId:ISNULL))"})
  void rejectsFieldsOfRelatedRecords(String filter) {
    assertThatThrownBy(() -> ScheduleFilters.toSpecification(filter, Schedule.class))
        .isInstanceOf(ServiceInvalidRequestException.class)
        .hasMessageContaining("related records");
  }
}

package si.result.eearly.facade;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import si.result.eearly.domain.UserPrincipal;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.domain.profile.Profile;
import si.result.eearly.domain.schedule.Schedule;
import si.result.eearly.domain.schedule.ScheduleEntry;
import si.result.eearly.exception.ServiceInvalidRequestException;
import si.result.eearly.mapper.ScheduleMapper;
import si.result.eearly.service.ScheduleEntryService;
import si.result.eearly.service.ScheduleService;
import si.result.eearly.service.UserPrincipalService;
import si.result.eearly.genproto.GetScheduleEntriesResponse;
import si.result.eearly.genproto.GetSchedulesResponse;
import si.result.eearly.genproto.SendMeasurementReminderResponse;
import si.result.eearly.genproto.SendScheduleNotificationResponse;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScheduleServiceGrpcFacade {

  private final ScheduleService scheduleService;
  private final ScheduleEntryService scheduleEntryService;
  private final UserPrincipalService userPrincipalService;
  private final ScheduleMapper scheduleMapper;

  @Transactional(readOnly = true)
  public GetSchedulesResponse getSchedules(String filter) {
    // The filter comes from the client, so results are always limited to the caller's own schedules.
    final UUID userId = currentUserId();
    Specification<Schedule> ownSchedules = (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.join("profiles", JoinType.INNER).get("userId"), userId);
    Specification<Schedule> specification = ScheduleFilters.toSpecification(filter, Schedule.class);
    specification = specification == null ? ownSchedules : specification.and(ownSchedules);

    try {
        final var schedules = scheduleService.getScheduleList(specification);
        var responseBuilder = GetSchedulesResponse.newBuilder();
        List<si.result.eearly.genproto.Schedule> protoSchedules = new ArrayList<>();
        for (Schedule schedule : schedules) {
          protoSchedules.add(scheduleMapper.toGrpcSchedule(schedule));
        }
        responseBuilder.addAllSchedules(protoSchedules);
        return responseBuilder.build();
    } catch (ValidationException e) {
      throw new ServiceInvalidRequestException(e.getMessage(), si.result.eearly.genproto.ErrorCode.forNumber(e.getErrorCode().getNumVal()));
    }
  }

  @Transactional(readOnly = true)
  public GetScheduleEntriesResponse getScheduleEntries(String filter) {
    final UUID userId = currentUserId();
    Specification<ScheduleEntry> specification = ScheduleFilters.toSpecification(filter, ScheduleEntry.class);
    Specification<ScheduleEntry> specificationUserId = (root, query, criteriaBuilder) -> {
      Join<ScheduleEntry, Schedule> scheduleJoin = root.join("schedules", JoinType.INNER);

      Join<Schedule, Profile> profileJoin = scheduleJoin.join("profiles", JoinType.INNER);

      return criteriaBuilder.equal(profileJoin.get("userId"), userId);
    };
    specification = specification == null ? specificationUserId : specification.and(specificationUserId);

    List<ScheduleEntry> scheduleEntries = scheduleEntryService.getScheduleEntries(specification);

    var scheduleResponseBuilder = GetScheduleEntriesResponse.newBuilder();
    for (ScheduleEntry scheduleEntry : scheduleEntries) {
      scheduleResponseBuilder.addScheduleEntries(scheduleMapper.toGrpcScheduleEntry(scheduleEntry));
    }

    return scheduleResponseBuilder.build();
  }

  @Transactional
  public SendScheduleNotificationResponse sendScheduleNotificationResponse(String scheduleId) {
    scheduleService.notifyAboutNewSchedules(UUID.fromString(scheduleId));

    return SendScheduleNotificationResponse.newBuilder().build();
  }

  @Transactional
  public SendMeasurementReminderResponse sendMeasurementReminder(
      String scheduleId,
      String scheduledAt,
      String scheduleName) {
    scheduleService.sendMeasurementReminder(UUID.fromString(scheduleId), scheduledAt, scheduleName);
    return SendMeasurementReminderResponse.newBuilder().build();
  }

  /** Fails closed: without a user in the token, no schedule data is returned. */
  private UUID currentUserId() {
    UserPrincipal principal = userPrincipalService.getUserPrincipal();
    if (principal == null || principal.getId() == null) {
      throw new AccessDeniedException("No user in the access token");
    }
    return UUID.fromString(principal.getId());
  }
}

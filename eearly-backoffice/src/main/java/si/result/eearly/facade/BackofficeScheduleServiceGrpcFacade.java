package si.result.eearly.facade;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.genproto.*;
import si.result.eearly.domain.schedule.Schedule;
import si.result.eearly.domain.schedule.ScheduleEntry;
import si.result.eearly.exception.ServiceInvalidRequestException;
import si.result.eearly.mapper.BackofficeScheduleMapper;
import si.result.eearly.service.ScheduleEntryService;
import si.result.eearly.service.BackofficeScheduleService;
import si.result.eearly.service.UserPrincipalService;
import si.result.r3.filter.config.RestFilterGenerator;
import si.result.eearly.repository.MeasurementTypeRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BackofficeScheduleServiceGrpcFacade {

  private final BackofficeScheduleService scheduleService;
  private final UserPrincipalService userPrincipalService;
  private final ScheduleEntryService scheduleEntryService;
  private final BackofficeScheduleMapper scheduleMapper;
  private final MeasurementTypeRepository measurementTypeRepository;

  RestFilterGenerator restFilterGenerator = new RestFilterGenerator(new HashMap<>());

  @Transactional
  public UpsertScheduleResponse upsertSchedule (UpsertScheduleRequest request) {
    try {
      return scheduleMapper.toUpsertScheduleResponse(
              scheduleService.upsertSchedule(
                      scheduleMapper.toUpsertCommand(request, measurementTypeRepository)));
    } catch (ValidationException e) {
      throw new ServiceInvalidRequestException(e.getMessage(), si.result.eearly.genproto.ErrorCode.forNumber(e.getErrorCode().getNumVal()));
    }
  }

  @Transactional
  public DeleteScheduleResponse deleteSchedule(DeleteScheduleRequest request) {
    try {
      scheduleService.deleteSchedule(request.getScheduleId(), request.getKeycloakId());
      return DeleteScheduleResponse.newBuilder()
          .setError(si.result.eearly.genproto.Error.newBuilder()
              .setCode(ErrorCode.OK)
              .setMessage("Schedule deleted successfully")
              .build())
          .build();
    } catch (ValidationException e) {
      throw new ServiceInvalidRequestException(
          e.getMessage(),
          si.result.eearly.genproto.ErrorCode.forNumber(e.getErrorCode().getNumVal()));
    }
  }

  @Transactional
  public GetScheduleResponse createUpdateSchedule (CreateUpdateScheduleRequest request) {

    final var caretakerId = userPrincipalService.getUserPrincipal().getId();
    try {
      final var schedule = scheduleService.createUpdateSchedule(caretakerId, request.getId(), request.getName(), request.getDescription(), request.getActive());
      return scheduleMapper.toGrpcScheduleResponse(schedule);
    } catch (ValidationException e) {
      throw new ServiceInvalidRequestException(e.getMessage(), si.result.eearly.genproto.ErrorCode.forNumber(e.getErrorCode().getNumVal()));
    }
  }

  @Transactional
  public GetScheduleEntriesResponse createUpdateScheduleEntry (CreateUpdateScheduleEntryRequest request) {

    try {
      final var scheduleEntries = scheduleService.createUpdateScheduleEntry(scheduleMapper.toCreateUpdateScheduleEntryCommand(request));
      List<si.result.eearly.genproto.ScheduleEntry> scheduleEntriesGrpc = new ArrayList<>();
      for (ScheduleEntry scheduleEntry : scheduleEntries) {
        scheduleEntriesGrpc.add(scheduleMapper.toGrpcScheduleEntry(scheduleEntry));
      }
      return GetScheduleEntriesResponse.newBuilder().addAllScheduleEntries(scheduleEntriesGrpc).build();
    } catch (ValidationException e) {
      throw new ServiceInvalidRequestException(e.getMessage(), si.result.eearly.genproto.ErrorCode.forNumber(e.getErrorCode().getNumVal()));
    }
  }

  @Transactional
  public GetScheduleResponse addUserToSchedule(AddUserToScheduleRequest request)  {
    try {
      return scheduleMapper.toGrpcScheduleResponse(scheduleService.addUserToSchedule(request.getUserId(), request.getScheduleId()));
    } catch (ValidationException e) {
      throw new ServiceInvalidRequestException(e.getMessage(), si.result.eearly.genproto.ErrorCode.forNumber(e.getErrorCode().getNumVal()));
    }
  }

  @Transactional
  public GetScheduleEmptyResponse removeUserFromSchedule(RemoveUserFromScheduleRequest request) {
    scheduleService.removeUserFromSchedule(request.getUserId(), request.getScheduleId());
    return GetScheduleEmptyResponse.newBuilder().build();
  }

  @Transactional(readOnly = true)
  public GetSchedulesResponse getSchedulesForCaretaker(String filter) {
    Specification<Schedule> specification = restFilterGenerator.parse(filter).toSpecification(Schedule.class);
    Specification<Schedule> specificationUserId = (root, query, criteriaBuilder) -> {
      return criteriaBuilder.equal(root.get("caretakerId"), UUID.fromString(userPrincipalService.getUserPrincipal().getId()));
    };
    specification = specification == null ? specificationUserId : specification.and(specificationUserId);

    List<Schedule> schedules = scheduleService.getSchedulesForCaretaker(specification);

    var scheduleResponseBuilder = GetSchedulesResponse.newBuilder();
    for (Schedule schedule : schedules) {
      scheduleResponseBuilder.addSchedules(scheduleMapper.toGrpcSchedule(schedule));
    }
    return scheduleResponseBuilder.build();
  }

  @Transactional(readOnly = true)
  public GetScheduleEntriesResponse getScheduleEntriesForCaretaker(String scheduleId, String filter) {
    Specification<ScheduleEntry> specification = restFilterGenerator.parse(filter).toSpecification(ScheduleEntry.class);
    Specification<ScheduleEntry> specificationUserId = (root, query, criteriaBuilder) -> {
      Join<ScheduleEntry, Schedule> scheduleJoin = root.join("schedules", JoinType.INNER);
      criteriaBuilder.equal(scheduleJoin.get("caretakerId"), UUID.fromString(userPrincipalService.getUserPrincipal().getId()));
      return criteriaBuilder.equal(scheduleJoin.get("id"), UUID.fromString(scheduleId));
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
  public GetScheduleResponse deleteScheduleEntry(String scheduleId, String scheduleEntryId) {
    try {
      return scheduleMapper.toGrpcScheduleResponse(scheduleService.deleteScheduleEntry(scheduleId, scheduleEntryId));
    } catch (ValidationException e) {
      throw new ServiceInvalidRequestException(e.getMessage(), si.result.eearly.genproto.ErrorCode.forNumber(e.getErrorCode().getNumVal()));
    }
  }
}

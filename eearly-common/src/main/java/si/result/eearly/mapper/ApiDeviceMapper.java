package si.result.eearly.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import si.result.eearly.domain.api_device.CheckHeartbeatCommand;
import si.result.eearly.domain.api_device.GetMeasurementCommand;

@Mapper(uses = TimeMapper.class)
public interface ApiDeviceMapper {

	CheckHeartbeatCommand toCheckHeartbeatCommand(String deviceId, String userId);

	@Mapping(source = "startDate", target = "startTimestamp", qualifiedByName = "epochMillisToOffsetDateTime")
	@Mapping(source = "endDate", target = "endTimestamp", qualifiedByName = "epochMillisToOffsetDateTime")
	GetMeasurementCommand toGetMeasurementCommand(String deviceId, String userId, String measurementTypeId,
			Long startDate,
			Long endDate);
}

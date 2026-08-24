package si.result.eearly.service;

import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import si.result.eearly.domain.api_device.CheckHeartbeatCommand;
import si.result.eearly.domain.api_device.GetMeasurementCommand;
import si.result.eearly.domain.api_device.Heartbeat;
import si.result.eearly.domain.device.DeviceUser;
import si.result.eearly.domain.measurement.Measurement;
import si.result.eearly.domain.measurement.MeasurementType;

@Transactional
public interface ApiDeviceService {
	Heartbeat checkHeartbeat(CheckHeartbeatCommand command, DeviceUser deviceUser);

	List<Measurement> getMeasurements(GetMeasurementCommand command, DeviceUser deviceUser,
			MeasurementType measurementType);
}

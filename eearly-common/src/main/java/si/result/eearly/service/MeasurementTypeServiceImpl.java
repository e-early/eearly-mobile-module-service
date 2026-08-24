package si.result.eearly.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import si.result.eearly.domain.measurement.MeasurementType;
import si.result.eearly.repository.MeasurementTypeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeasurementTypeServiceImpl implements MeasurementTypeService {

	private final MeasurementTypeRepository measurementTypeRepository;

	@Override
	public List<MeasurementType> getMeasurementTypeList() {
		return measurementTypeRepository.findAll();
	}
}

package si.result.eearly.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import si.result.eearly.domain.enums.MeasurementSubTypeEnum;
import si.result.eearly.domain.measurement.MeasurementType;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MeasurementTypeRepository  extends JpaRepository<MeasurementType, UUID> {

    Optional<MeasurementType> findByObservationId(String observationId);

    Optional<MeasurementType> findByObservationIdAndMeasurementSubType(String observationId, MeasurementSubTypeEnum measurementSubType);
}

package si.result.eearly.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.domain.facility.Facility;
import si.result.eearly.domain.facility.UpsertFacilityCommand;
import si.result.eearly.repository.FacilityRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacilityServiceImpl implements FacilityService {

    private final FacilityRepository facilityRepository;


    @Override
    public List<Facility> getAllFacilities() {
        return facilityRepository.findAll();
    }

    @Override
    public Facility getFacilityById(String facilityId) {
        return facilityRepository.findById(UUID.fromString(facilityId)).orElse(null);
    }

    @Override
    public Facility upsertFacility(UpsertFacilityCommand command) throws ValidationException {
        final var facilityToUpsert = Facility.createFacilityFromUpsertCommand(command);
        facilityRepository.save(facilityToUpsert);

        return facilityToUpsert;
    }
}

package si.result.eearly.service;

import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.domain.facility.Facility;
import si.result.eearly.domain.facility.UpsertFacilityCommand;

import java.util.List;

public interface FacilityService {

    List<Facility> getAllFacilities();
    Facility getFacilityById(String facilityId);
    Facility upsertFacility(UpsertFacilityCommand command) throws ValidationException;
}

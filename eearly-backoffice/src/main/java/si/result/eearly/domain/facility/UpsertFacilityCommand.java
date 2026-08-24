package si.result.eearly.domain.facility;

public record UpsertFacilityCommand(
        String id,
        String name,
        String description
) {
}
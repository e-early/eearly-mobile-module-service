package si.result.eearly.domain.caretaker;

import si.result.eearly.domain.facility.Facility;

public record UpsertCaretakerCommand(
        String id,
        String name,
        Facility facility
) {
}

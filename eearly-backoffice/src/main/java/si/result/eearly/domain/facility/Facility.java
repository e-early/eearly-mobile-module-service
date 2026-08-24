package si.result.eearly.domain.facility;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "facility")
@Data
@NoArgsConstructor
public class Facility {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    public static Facility createFacilityFromUpsertCommand(UpsertFacilityCommand command) {
        Facility facilityToUpsert = new Facility();
        facilityToUpsert.setId(UUID.fromString(command.id()));
        facilityToUpsert.setName(command.name());
        facilityToUpsert.setDescription(command.description());
        return facilityToUpsert;
    }
}

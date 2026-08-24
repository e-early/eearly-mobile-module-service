package si.result.eearly.domain.caretaker;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import si.result.eearly.domain.facility.Facility;

import java.util.UUID;

@Entity
@Table(name = "caretaker")
@Data
@NoArgsConstructor
public class Caretaker {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    public static Caretaker createProfileFromUpsertCommand(UpsertCaretakerCommand command) {
        Caretaker caretakerToUpsert = new Caretaker();
        caretakerToUpsert.setId(UUID.fromString(command.id()));
        caretakerToUpsert.setName(command.name());
        caretakerToUpsert.setFacility(command.facility());

        return caretakerToUpsert;
    }

}

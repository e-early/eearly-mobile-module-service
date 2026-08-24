package si.result.eearly.domain.manufacturer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "manufacturer")
@Data
@NoArgsConstructor
public class Manufacturer {

    @Id
    @Column(name = "id")
    @UuidGenerator
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;
}

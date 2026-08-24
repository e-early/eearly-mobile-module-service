package si.result.eearly.domain.token;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import si.result.eearly.domain.profile.Profile;

import java.util.UUID;

@Entity
@Table(name = "mobile_token")
@Data
@NoArgsConstructor
public final class MobileToken {

    @Id
    @Column(name = "token")
    private String token;

    @Column(name = "user_id")
    private UUID userId;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Profile profile;

    public static MobileToken create(String mobileToken, String userId) {
        var token = new MobileToken();
        token.setToken(mobileToken);
        token.setUserId(UUID.fromString(userId));
        return token;
    }
}

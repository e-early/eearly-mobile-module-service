package si.result.eearly;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

class EearlyApplicationTest {

  @Test
  void applicationIsASpringBootApplication() {
    assertThat(EearlyApplication.class.isAnnotationPresent(SpringBootApplication.class)).isTrue();
  }
}

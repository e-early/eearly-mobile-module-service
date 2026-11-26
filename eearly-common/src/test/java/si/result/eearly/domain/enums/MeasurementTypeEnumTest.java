package si.result.eearly.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MeasurementTypeEnumTest {

  @Test
  @DisplayName("domain enum and protobuf enum have the same names in the same order")
  void domainEnumMatchesProtoEnum() {
    List<String> domain = Arrays.stream(MeasurementTypeEnum.values()).map(Enum::name).toList();
    List<String> proto = Arrays.stream(si.result.eearly.genproto.MeasurementTypeEnum.values())
        .filter(value -> value != si.result.eearly.genproto.MeasurementTypeEnum.UNRECOGNIZED)
        .map(Enum::name)
        .toList();

    assertThat(domain).isEqualTo(proto);
  }

  @Test
  @DisplayName("proto field numbers follow the domain declaration order")
  void protoNumbersFollowDeclarationOrder() {
    for (MeasurementTypeEnum value : MeasurementTypeEnum.values()) {
      assertThat(si.result.eearly.genproto.MeasurementTypeEnum.valueOf(value.name()).getNumber())
          .as(value.name()).isEqualTo(value.ordinal());
    }
  }
}

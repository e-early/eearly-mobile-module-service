package si.result.eearly.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper
public interface TimeMapper {

	@Named("epochMillisToOffsetDateTime")
	@SuppressWarnings("unused")
	default OffsetDateTime epochMillisToInstant(Long value) {
		return OffsetDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault());
	}

	@Named("offsetDateTimeToEpochMillis")
	@SuppressWarnings("unused")
	default Long instantToEpochMillis(OffsetDateTime value) {
		return value.toInstant().toEpochMilli();
	}

	@Named("stringToOffsetDateTime")
	@SuppressWarnings("unused")
	default OffsetDateTime stringToOffsetDateTime(String value) {
		try {
			LocalDateTime ldt = LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
			return ldt.atOffset(ZoneOffset.UTC);
		} catch (Exception e) {
			try {
				return OffsetDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(value)), ZoneOffset.UTC);
			} catch (Exception ex) {
				return OffsetDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(value)), ZoneOffset.UTC);
			}
		}
	}

	@Named("offsetDateTimeToString")
	@SuppressWarnings("unused")
	default String offsetDateTimeToString(OffsetDateTime value) {
		return value.toString();
	}
}

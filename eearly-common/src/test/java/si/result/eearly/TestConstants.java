package si.result.eearly;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class TestConstants {
    public static final String MEASUREMENT_TYPE_1 = "BODY_WEIGHT", MEASUREMENT_TYPE_2 = "BODY_HEIGHT";
    public static final String MEASUREMENT_TYPE_ID = "3d51f149-4eb2-42af-b217-8f5a754968eb";
    public static final String MEASUREMENT_TYPE_SUBTYPE = "UNKNOWN_MEASUREMENT_SUB_TYPE";
    public static final double MEASUREMENT_TYPE_MIN_VALUE_1 = 1.0, MEASUREMENT_TYPE_MAX_VALUE_1 = 10.0;
    public static final double MEASUREMENT_TYPE_MIN_VALUE_2 = 2.0, MEASUREMENT_TYPE_MAX_VALUE_2 = 20.0;
    public static final String MEASUREMENT_TYPE_UNIT = "kg";
    public static final UUID[] SORTED_UUIDS = {
            UUID.fromString("b893ea7e-40d0-4855-8219-a1fe29d1aa20"),
            UUID.fromString("5b0e9067-1b25-4fd3-b441-08ec4bc5bd5d"),
            UUID.fromString("730f3fc5-095c-4a1b-b1da-fc69f5e62fb2")};
    public static final byte[] PICTURE_1 = {1, 2, 3}, PICTURE_2 = {4, 5, 6};
    public static final boolean TRUE_VALUE = true, FALSE_VALUE = false;
    public static final OffsetDateTime TIMESTAMP = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS);
    public static final LocalTime TIME = LocalTime.of(12, 14, 15);
    public static final int MINUTES_DUE = 15;
    public static final String DEVICE_NAME_1 = "deviceName", DEVICE_NAME_2 = "deviceName2";
    public static final UUID DEVICE_ID = UUID.fromString("64c48771-aa92-4d64-a80c-0288b6f965ed");
    public static final String DEVICE_DESCRIPTION_1 = "deviceDescription1", DEVICE_DESCRIPTION_2 = "deviceDescription2";
    public static final String DEVICE_CONNECTION_TYPE_1 = "deviceConnectionType", DEVICE_CONNECTION_TYPE_2 = "deviceConnectionType2";
    public static final UUID DEVICE_MANUFACTURER_ID_1 = UUID.fromString("a6a4d1ad-2dd3-4b5f-bbe0-ad9b8ef8eaa6"), DEVICE_MANUFACTURER_ID_2 = UUID.fromString("d845c39a-1a03-4ed8-a11c-145bc487625f");
    public static final String API_KEY_1 = "apiKey", API_KEY_2 = "apiKey2", API_KEY_3 = "apiKey3";
    public static final double MEASUREMENT_VALUE_1 = 10.0, MEASUREMENT_VALUE_2 = 20.0, MEASUREMENT_VALUE_3 = 30.0;
    public static final String MEASUREMENT_BATCH_ID = "1";
    public static final int MEASUREMENT_GROUP_ID = 1;
    public static final String MEASUREMENT_MODE = "SINGLE";
}

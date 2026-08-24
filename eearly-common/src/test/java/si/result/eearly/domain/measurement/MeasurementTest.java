package si.result.eearly.domain.measurement;

//import org.jetbrains.annotations.NotNull;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//import si.result.eearly.domain.BasePostgresContainerInit;
//import si.result.eearly.domain.device.Device;
//import si.result.eearly.domain.enums.MeasurementMode;
//import si.result.eearly.domain.enums.MeasurementSubTypeEnum;
//import si.result.eearly.domain.enums.MeasurementTypeEnum;
//import si.result.eearly.domain.manufacturer.Manufacturer;
//import si.result.eearly.domain.profile.Profile;
//import si.result.eearly.repository.*;
//
//import java.sql.*;
//import java.time.Instant;
//import java.time.OffsetDateTime;
//import java.time.ZoneId;
//import java.time.ZoneOffset;
//import java.time.temporal.ChronoUnit;
//import java.util.Comparator;
//import java.util.List;
//import java.util.UUID;
//
//import static si.result.eearly.TestConstants.*;
//

//public class MeasurementTest extends BasePostgresContainerInit {
//
//    @Autowired
//    private ProfileRepository profileRepository;
//
//    @Autowired
//    private DeviceRepository deviceRepository;
//
//    @Autowired
//    private MeasurementTypeRepository measurementTypeRepository;
//
//    @Autowired
//    private MeasurementRepository measurementRepository;
//
//    @Autowired
//    private DeviceUserRepository deviceUserRepository;
//
//    @BeforeEach
//    void setUp() {
//        deviceUserRepository.deleteAll();
//        measurementRepository.deleteAll();
//        profileRepository.deleteAll();
//        deviceRepository.deleteAll();
//        measurementTypeRepository.deleteAll();
//    }
//
//    @Test
//    @Transactional(propagation = Propagation.NEVER)
//    public void createAndPersistMeasurementTest() throws SQLException {
//        Profile profile = createProfile();
//        profileRepository.saveAndFlush(profile);
//
//        MeasurementType measurementType = createMeasurementType();
//        measurementTypeRepository.saveAndFlush(measurementType);
//
//        Device device = createDevice();
//        deviceRepository.saveAndFlush(device);
//
//        Measurement measurement = createMeasurement(measurementType, device, profile, 10.0);
//        measurementRepository.saveAndFlush(measurement);
//
//        assertProfile(profile);
//        assertMeasurementType(measurementType);
//        assertDevice(device);
//        assertMeasurement(profile, measurementType, device, measurement);
//    }
//
//    @Test
//    @Transactional(propagation = Propagation.NEVER)
//    public void createAndPersistManyMeasurementsTest() throws SQLException {
//        Profile profile = createProfile();
//        profileRepository.saveAndFlush(profile);
//
//        MeasurementType measurementType = createMeasurementType();
//        measurementTypeRepository.saveAndFlush(measurementType);
//
//        Device device = createDevice();
//        deviceRepository.saveAndFlush(device);
//
//        Measurement measurement1 = createMeasurement(measurementType, device, profile, 10.0);
//        Measurement measurement2 = createMeasurement(measurementType, device, profile, 11.0);
//        Measurement measurement3 = createMeasurement(measurementType, device, profile, 12.0);
//        measurementRepository.saveAllAndFlush(List.of(measurement1, measurement2, measurement3));
//
//        assertProfile(profile);
//        assertMeasurementType(measurementType);
//        assertDevice(device);
//        assertMeasurement(profile, measurementType, device, measurement1);
//        assertMeasurement(profile, measurementType, device, measurement2);
//        assertMeasurement(profile, measurementType, device, measurement3);
//    }
//
//    @Test
//    public void getMeasurementFromDBTest() throws SQLException {
//        insertData();
//
//        List<Measurement> orderedMeasurementList = measurementRepository.findAll();
//        orderedMeasurementList.sort(Comparator.comparing(Measurement::getId));
//
//        Assertions.assertEquals(3, orderedMeasurementList.size());
//        Assertions.assertEquals(SORTED_UUIDS[0], orderedMeasurementList.getFirst().getId());
//        Assertions.assertEquals(SORTED_UUIDS[0], orderedMeasurementList.getFirst().getMeasurementType().getId());
//        Assertions.assertEquals(SORTED_UUIDS[0], orderedMeasurementList.getFirst().getDevice().getId());
//        Assertions.assertEquals(SORTED_UUIDS[0], orderedMeasurementList.getFirst().getProfile().getUserId());
//        Assertions.assertEquals(TIMESTAMP, orderedMeasurementList.getFirst().getMeasuredAt());
//        Assertions.assertEquals(MEASUREMENT_VALUE_1, orderedMeasurementList.getFirst().getValue());
//        Assertions.assertEquals(MEASUREMENT_BATCH_ID, orderedMeasurementList.get(0).getMeasurementBatchId());
//        Assertions.assertEquals(MEASUREMENT_GROUP_ID, orderedMeasurementList.get(0).getMeasurementGroupId());
//
//        Assertions.assertEquals(SORTED_UUIDS[1], orderedMeasurementList.get(1).getId());
//        Assertions.assertEquals(SORTED_UUIDS[0], orderedMeasurementList.get(1).getMeasurementType().getId());
//        Assertions.assertEquals(SORTED_UUIDS[0], orderedMeasurementList.get(1).getDevice().getId());
//        Assertions.assertEquals(SORTED_UUIDS[0], orderedMeasurementList.get(1).getProfile().getUserId());
//        Assertions.assertEquals(TIMESTAMP, orderedMeasurementList.get(1).getMeasuredAt());
//        Assertions.assertEquals(MEASUREMENT_VALUE_2, orderedMeasurementList.get(1).getValue());
//        Assertions.assertEquals(MEASUREMENT_BATCH_ID, orderedMeasurementList.get(1).getMeasurementBatchId());
//        Assertions.assertEquals(MEASUREMENT_GROUP_ID, orderedMeasurementList.get(1).getMeasurementGroupId());
//
//        Assertions.assertEquals(SORTED_UUIDS[2], orderedMeasurementList.get(2).getId());
//        Assertions.assertEquals(SORTED_UUIDS[0], orderedMeasurementList.get(2).getMeasurementType().getId());
//        Assertions.assertEquals(SORTED_UUIDS[0], orderedMeasurementList.get(2).getDevice().getId());
//        Assertions.assertEquals(SORTED_UUIDS[0], orderedMeasurementList.get(2).getProfile().getUserId());
//        Assertions.assertEquals(TIMESTAMP, orderedMeasurementList.get(2).getMeasuredAt());
//        Assertions.assertEquals(MEASUREMENT_VALUE_3, orderedMeasurementList.get(2).getValue());
//        Assertions.assertEquals(MEASUREMENT_BATCH_ID, orderedMeasurementList.get(2).getMeasurementBatchId());
//        Assertions.assertEquals(MEASUREMENT_GROUP_ID, orderedMeasurementList.get(2).getMeasurementGroupId());
//    }
//
//    private static void insertData() throws SQLException {
//        ConnectionAndStatement connectionAndStatement = getConnectionAndStatement();
//
//        insertMeasurementTypes(connectionAndStatement.statement());
//        insertProfiles(connectionAndStatement.connection());
//        insertDevices(connectionAndStatement.connection());
//        insertMeasurements(connectionAndStatement.statement());
//        insertDeviceMeasurementType(connectionAndStatement.statement());
//        insertDeviceUsers(connectionAndStatement.statement());
//    }
//
//    private static @NotNull ConnectionAndStatement getConnectionAndStatement() throws SQLException {
//        Connection connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
//        Statement statement = connection.createStatement();
//      return new ConnectionAndStatement(connection, statement);
//    }
//
//    private record ConnectionAndStatement(Connection connection, Statement statement) {
//    }
//
//    private Profile createProfile() {
//        UUID uuid = UUID.randomUUID();
//        byte[] picture = new byte[]{1, 2, 3};
//        boolean notificationDaySummary = true;
//        boolean notificationMeasurementDue = true;
//        boolean notificationNoInternet = true;
//        boolean notificationNoBluetooth = true;
//
//        Profile p = new Profile();
//        p.setUserId(uuid);
//        p.setPicture(picture);
//        p.setNotificationDaySummary(notificationDaySummary);
//        p.setNotificationDaySummaryTime(TIME);
//        p.setNotificationMeasurementDue(notificationMeasurementDue);
//        p.setNotificationMeasurementDueMinutes(MINUTES_DUE);
//        p.setNotificationNoInternet(notificationNoInternet);
//        p.setNotificationNoBluetooth(notificationNoBluetooth);
//
//        return p;
//    }
//
//    private MeasurementType createMeasurementType() {
//        MeasurementType measurementType = new MeasurementType();
//        measurementType.setMeasurementType(MeasurementTypeEnum.BLOOD_PRESSURE);
//        measurementType.setMeasurementSubType(MeasurementSubTypeEnum.DIASTOLIC);
//        measurementType.setMinValue(10.99);
//        measurementType.setMaxValue(20.99);
//        measurementType.setUnit("unit");
//
//        return measurementType;
//    }
//
//    private Device createDevice() {
//        String name = "device_name";
//        byte[] picture = new byte[]{4, 5, 6};
//        String description = "description";
//        String connectionType = "connection_type";
//
//        Device d = new Device();
//        d.setName(name);
//        d.setPicture(picture);
//        d.setDescription(description);
//        d.setConnectionType(connectionType);
//        d.setManufacturer(createManufacturer());
//
//        return d;
//    }
//
//    private Manufacturer createManufacturer() {
//        Manufacturer manufacturer = new Manufacturer();
//        manufacturer.setId(UUID.fromString("a6a4d1ad-2dd3-4b5f-bbe0-ad9b8ef8eaa6"));
//        return manufacturer;
//    }
//
//    private Measurement createMeasurement(MeasurementType measurementType, Device device, Profile profile, double value) {
//        Measurement measurement = new Measurement();
//        measurement.setMeasurementType(measurementType);
//        measurement.setDevice(device);
//        measurement.setProfile(profile);
//        measurement.setMeasuredAt(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS));
//        measurement.setValue(value);
//        measurement.setMeasurementBatchId("measurementBatchId");
//        measurement.setMeasurementMode(MeasurementMode.SINGLE);
//        return measurement;
//    }
//
//    private void assertProfile(Profile profile) throws SQLException {
//        ResultSet resultSet = getResultSet("SELECT * FROM common.profile WHERE user_id = '" + profile.getUserId() + "';");
//
//        if (resultSet.next()) {
//            Assertions.assertEquals(profile.getUserId().toString(), resultSet.getString("user_id"));
//            Assertions.assertArrayEquals(profile.getPicture(), resultSet.getBytes("picture"));
//            Assertions.assertEquals(profile.isNotificationDaySummary(), resultSet.getBoolean("notification_day_summary"));
//            Assertions.assertEquals(profile.getNotificationDaySummaryTime(), resultSet.getTime("notification_day_summary_time") != null ? resultSet.getTime("notification_day_summary_time").toLocalTime() : null);
//            Assertions.assertEquals(profile.isNotificationMeasurementDue(), resultSet.getBoolean("notification_measurement_due"));
//            Assertions.assertEquals(profile.getNotificationMeasurementDueMinutes(), resultSet.getInt("notification_measurement_due_minutes"));
//            Assertions.assertEquals(profile.isNotificationNoInternet(), resultSet.getBoolean("notification_no_internet"));
//            Assertions.assertEquals(profile.isNotificationNoBluetooth(), resultSet.getBoolean("notification_no_bluetooth"));
//        } else {
//            Assertions.fail("No profile row with id " + profile.getUserId() + " found");
//        }
//
//        resultSet.close();
//    }
//
//    private void assertMeasurementType(MeasurementType measurementType) throws SQLException {
//        ResultSet resultSet = getResultSet("SELECT * FROM common.measurement_type;");
//
//        if (resultSet.next()) {
//            Assertions.assertNotNull(measurementType.getId());
//            Assertions.assertEquals(measurementType.getMeasurementType(), MeasurementTypeEnum.valueOf(resultSet.getString("measurement_type")));
//            Assertions.assertEquals(measurementType.getMeasurementSubType(), MeasurementSubTypeEnum.valueOf(resultSet.getString("measurement_sub_type")));
//            Assertions.assertEquals(measurementType.getMinValue(), resultSet.getDouble("min_value"));
//            Assertions.assertEquals(measurementType.getMaxValue(), resultSet.getDouble("max_value"));
//            Assertions.assertEquals(measurementType.getUnit(), resultSet.getString("unit"));
//        } else {
//            Assertions.fail("No measurement type row with id  "+ measurementType.getId() +" found");
//        }
//
//        resultSet.close();
//    }
//
//    private void assertDevice(Device device) throws SQLException {
//        ResultSet resultSet = getResultSet("SELECT * FROM common.device WHERE id = '" + device.getId() + "';");
//
//        if (resultSet.next()) {
//            Assertions.assertEquals(device.getId().toString(), resultSet.getString("id"));
//            Assertions.assertEquals(device.getName(), resultSet.getString("name"));
//            Assertions.assertArrayEquals(device.getPicture(), resultSet.getBytes("picture"));
//            Assertions.assertEquals(device.getDescription(), resultSet.getString("description"));
//            Assertions.assertEquals(device.getConnectionType(), resultSet.getString("connection_type"));
//        } else {
//            Assertions.fail("No device row with id " + device.getId() + " found");
//        }
//
//        resultSet.close();
//    }
//
//    private void assertMeasurement(Profile profile, MeasurementType measurementType, Device device, Measurement measurement) throws SQLException {
//        ResultSet resultSet = getResultSet("SELECT * FROM common.measurement WHERE id = '" + measurement.getId() + "';");
//        if (resultSet.next()) {
//            Assertions.assertNotNull(resultSet.getString("id"));
//            Assertions.assertEquals(measurementType.getId().toString(), resultSet.getString("measurement_type_id"));
//            Assertions.assertEquals(device.getId().toString(), resultSet.getString("device_id"));
//            Assertions.assertEquals(profile.getUserId().toString(), resultSet.getString("user_id"));
//            Assertions.assertEquals(measurement.getMeasuredAt(), OffsetDateTime.of(resultSet.getTimestamp("measured_at").toLocalDateTime(), ZoneOffset.UTC));
//            Assertions.assertEquals(measurement.getValue(), resultSet.getDouble("value"));
//            Assertions.assertEquals(measurement.getMeasurementBatchId(), resultSet.getString("measurement_batch_id"));
//            Assertions.assertEquals(measurement.getMeasurementGroupId(), resultSet.getInt("measurement_group_id"));
//        } else {
//            Assertions.fail("No measurement row with id  "+ measurementType.getId() +" found");
//        }
//
//        resultSet.close();
//    }
//
//    private static ResultSet getResultSet(String query) throws SQLException {
//        Connection connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
//        Statement statement = connection.createStatement();
//      return statement.executeQuery(query);
//    }
//
//    private static void insertDevices(Connection connection) throws SQLException {
//        PreparedStatement devicePs = connection.prepareStatement(
//                "INSERT INTO common.device (id, name, picture, description, connection_type, manufacturer_id) VALUES "
//                        + "(?, ?, ?, ?, ?, ?),"
//                        + "(?, ?, ?, ?, ?, ?)"
//        );
//
//        devicePs.setObject(1, SORTED_UUIDS[0]);
//        devicePs.setString(2, DEVICE_NAME_1);
//        devicePs.setBytes(3, PICTURE_1);
//        devicePs.setString(4, DEVICE_DESCRIPTION_1);
//        devicePs.setString(5, DEVICE_CONNECTION_TYPE_1);
//        devicePs.setObject(6, DEVICE_MANUFACTURER_ID_1);
//
//
//        devicePs.setObject(7, SORTED_UUIDS[1]);
//        devicePs.setString(8, DEVICE_NAME_2);
//        devicePs.setBytes(9, PICTURE_2);
//        devicePs.setString(10, DEVICE_DESCRIPTION_2);
//        devicePs.setString(11, DEVICE_CONNECTION_TYPE_2);
//        devicePs.setObject(12, DEVICE_MANUFACTURER_ID_2);
//
//
//        devicePs.executeUpdate();
//        devicePs.close();
//    }
//
//    private static void insertMeasurements(Statement statement) throws SQLException {
//        String measurement = "INSERT INTO common.measurement (id, measurement_type_id, device_id, user_id, measured_at, value, measurement_batch_id, measurement_group_id, measurement_mode) VALUES "
//                + "('"
//                + SORTED_UUIDS[0] + "','"
//                + SORTED_UUIDS[0] + "','"
//                + SORTED_UUIDS[0] + "','"
//                + SORTED_UUIDS[0] + "','"
//                + TIMESTAMP + "',"
//                + MEASUREMENT_VALUE_1 + ","
//                + MEASUREMENT_BATCH_ID + ","
//                + MEASUREMENT_GROUP_ID + ",'"
//                + MEASUREMENT_MODE + "'"
//                + "),('"
//                + SORTED_UUIDS[1] + "','"
//                + SORTED_UUIDS[0] + "','"
//                + SORTED_UUIDS[0] + "','"
//                + SORTED_UUIDS[0] + "','"
//                + TIMESTAMP + "',"
//                + MEASUREMENT_VALUE_2 + ","
//                + MEASUREMENT_BATCH_ID + ","
//                + MEASUREMENT_GROUP_ID + ",'"
//                + MEASUREMENT_MODE + "'"
//                + "),('"
//                + SORTED_UUIDS[2] + "','"
//                + SORTED_UUIDS[0] + "','"
//                + SORTED_UUIDS[0] + "','"
//                + SORTED_UUIDS[0] + "','"
//                + TIMESTAMP + "',"
//                + MEASUREMENT_VALUE_3 + ","
//                + MEASUREMENT_BATCH_ID + ","
//                + MEASUREMENT_GROUP_ID + ",'"
//                + MEASUREMENT_MODE + "'"
//                + ")";
//
//        statement.execute(measurement);
//    }
//
//    private static void insertDeviceMeasurementType(Statement statement) throws SQLException {
//        String deviceMeasurementType = "INSERT INTO common.device_measurement_type (device_id, measurement_type_id) VALUES "
//                + "('"
//                + SORTED_UUIDS[0] + "','"
//                + SORTED_UUIDS[0]
//                + "'),('"
//                + SORTED_UUIDS[1] + "','"
//                + SORTED_UUIDS[0]
//                + "'),('"
//                + SORTED_UUIDS[1] + "','"
//                + SORTED_UUIDS[1]
//                + "')";
//        statement.execute(deviceMeasurementType);
//    }
//
//    private static void insertMeasurementTypes(Statement statement) throws SQLException {
//        String measurementType = "INSERT INTO common.measurement_type (id, measurement_type, measurement_sub_type, min_value, max_value, unit) VALUES "
//                + "('"
//                + SORTED_UUIDS[0] + "',"
//                + "'" + MEASUREMENT_TYPE_1 + "',"
//                + "'" + MEASUREMENT_TYPE_SUBTYPE + "',"
//                + MEASUREMENT_TYPE_MIN_VALUE_1 + ","
//                + MEASUREMENT_TYPE_MAX_VALUE_1 + ","
//                + "'" + MEASUREMENT_TYPE_UNIT
//                + "'),('"
//                + SORTED_UUIDS[1] + "',"
//                + "'" + MEASUREMENT_TYPE_2 + "',"
//                + "'" + MEASUREMENT_TYPE_SUBTYPE + "',"
//                + MEASUREMENT_TYPE_MIN_VALUE_2 + ","
//                + MEASUREMENT_TYPE_MAX_VALUE_2 + ","
//                + "'" + MEASUREMENT_TYPE_UNIT
//                + "')";
//        statement.execute(measurementType);
//    }
//
//    private static void insertProfiles(Connection connection) throws SQLException {
//        PreparedStatement profilePs = connection.prepareStatement(
//                "INSERT INTO common.profile (user_id, picture, notification_day_summary, notification_day_summary_time, notification_measurement_due, notification_measurement_due_minutes, notification_no_internet, notification_no_bluetooth) VALUES"
//                        + "(?, ?, ?, ?, ?, ?, ?, ?),"
//                        + "(?, ?, ?, ?, ?, ?, ?, ?)");
//        profilePs.setObject(1, SORTED_UUIDS[0]);
//        profilePs.setBytes(2, PICTURE_1);
//        profilePs.setBoolean(3, TRUE_VALUE);
//        profilePs.setTime(4, Time.valueOf(TIME));
//        profilePs.setBoolean(5, TRUE_VALUE);
//        profilePs.setInt(6, MINUTES_DUE);
//        profilePs.setBoolean(7, TRUE_VALUE);
//        profilePs.setBoolean(8, TRUE_VALUE);
//
//        profilePs.setObject(9, SORTED_UUIDS[1]);
//        profilePs.setBytes(10, PICTURE_2);
//        profilePs.setBoolean(11, TRUE_VALUE);
//        profilePs.setTime(12, Time.valueOf(TIME));
//        profilePs.setBoolean(13, TRUE_VALUE);
//        profilePs.setInt(14, MINUTES_DUE);
//        profilePs.setBoolean(15, FALSE_VALUE);
//        profilePs.setBoolean(16, FALSE_VALUE);
//
//        profilePs.executeUpdate();
//        profilePs.close();
//    }
//
//    private static void insertDeviceUsers(Statement statement) throws SQLException {
//        String deviceUser = "INSERT INTO common.device_user (profile_user_id, device_id, api_key) VALUES "
//                + "('"
//                + SORTED_UUIDS[0] + "',"
//                + "'" + SORTED_UUIDS[0] + "',"
//                + "'" + API_KEY_1
//                + "'),('"
//                + SORTED_UUIDS[1] + "',"
//                + "'" + SORTED_UUIDS[0] + "',"
//                + "'" + API_KEY_2
//                + "'),('"
//                + SORTED_UUIDS[1] + "',"
//                + "'" + SORTED_UUIDS[1] + "',"
//                + "'" + API_KEY_3
//                + "')";
//        statement.execute(deviceUser);
//    }
//}

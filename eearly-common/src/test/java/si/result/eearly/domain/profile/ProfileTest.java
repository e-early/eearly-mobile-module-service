package si.result.eearly.domain.profile;

//import org.jetbrains.annotations.NotNull;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//import si.result.eearly.domain.BasePostgresContainerInit;
//import si.result.eearly.domain.device.Device;
//import si.result.eearly.domain.device.DeviceUser;
//import si.result.eearly.domain.device.DeviceUserId;
//import si.result.eearly.domain.enums.MeasurementMode;
//import si.result.eearly.domain.enums.MeasurementSubTypeEnum;
//import si.result.eearly.domain.enums.MeasurementTypeEnum;
//import si.result.eearly.domain.measurement.Measurement;
//import si.result.eearly.domain.measurement.MeasurementType;
//import si.result.eearly.repository.*;
//
//import java.sql.*;
//import java.time.Instant;
//import java.time.OffsetDateTime;
//import java.time.temporal.ChronoUnit;
//import java.util.*;
//
//import static si.result.eearly.TestConstants.*;

//public class ProfileTest extends BasePostgresContainerInit {
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
//        //measurementTypeRepository.deleteAll();
//
//        //deviceRepository.deleteAll();
//        deviceUserRepository.deleteAll();
//        profileRepository.deleteAll();
//    }
//
//    @Test
//    @Transactional(propagation = Propagation.NEVER)
//    public void createAndPersistProfileTest() throws SQLException {
//        Profile profile = createProfile();
//        profileRepository.saveAndFlush(profile);
//
//        assertProfile(profile);
//    }
//
//    @Test
//    public void getProfileFromDBTest() throws SQLException {
//        insertData();
//
//        List<Profile> orderedProfileList = profileRepository.findAll();
//        orderedProfileList.sort(Comparator.comparing(Profile::getUserId));
//
//        Assertions.assertEquals(2, orderedProfileList.size());
//        Assertions.assertEquals(SORTED_UUIDS[0], orderedProfileList.getFirst().getUserId());
//        Assertions.assertArrayEquals(PICTURE_1, orderedProfileList.getFirst().getPicture());
//        Assertions.assertEquals(TRUE_VALUE, orderedProfileList.getFirst().isNotificationDaySummary());
//        Assertions.assertEquals(TIME, orderedProfileList.getFirst().getNotificationDaySummaryTime());
//        Assertions.assertEquals(TRUE_VALUE, orderedProfileList.getFirst().isNotificationMeasurementDue());
//        Assertions.assertEquals(MINUTES_DUE, orderedProfileList.getFirst().getNotificationMeasurementDueMinutes());
//        Assertions.assertEquals(TRUE_VALUE, orderedProfileList.getFirst().isNotificationNoInternet());
//        Assertions.assertEquals(TRUE_VALUE, orderedProfileList.getFirst().isNotificationNoBluetooth());
//
//        List<Measurement> orderedMeasurementListFromProfile0 = orderedProfileList.getFirst().getMeasurementList();
//        orderedMeasurementListFromProfile0.sort(Comparator.comparing(Measurement::getId));
//        Assertions.assertEquals(3, orderedMeasurementListFromProfile0.size());
//        Assertions.assertEquals(SORTED_UUIDS[0], orderedMeasurementListFromProfile0.getFirst().getId());
//        Assertions.assertEquals(SORTED_UUIDS[1], orderedMeasurementListFromProfile0.get(1).getId());
//        Assertions.assertEquals(SORTED_UUIDS[2], orderedMeasurementListFromProfile0.get(2).getId());
//
//        List<DeviceUser> deviceListFromProfile0 = orderedProfileList.getFirst().getDeviceUserList();
//        Assertions.assertEquals(1, deviceListFromProfile0.size());
//        Assertions.assertEquals(DEVICE_ID, deviceListFromProfile0.getFirst().getDevice().getId());
//
//        Assertions.assertEquals(SORTED_UUIDS[1], orderedProfileList.get(1).getUserId());
//        Assertions.assertArrayEquals(PICTURE_2, orderedProfileList.get(1).getPicture());
//        Assertions.assertEquals(TRUE_VALUE, orderedProfileList.get(1).isNotificationDaySummary());
//        Assertions.assertEquals(TIME, orderedProfileList.get(1).getNotificationDaySummaryTime());
//        Assertions.assertEquals(TRUE_VALUE, orderedProfileList.get(1).isNotificationMeasurementDue());
//        Assertions.assertEquals(MINUTES_DUE, orderedProfileList.get(1).getNotificationMeasurementDueMinutes());
//        Assertions.assertEquals(FALSE_VALUE, orderedProfileList.get(1).isNotificationNoInternet());
//        Assertions.assertEquals(FALSE_VALUE, orderedProfileList.get(1).isNotificationNoBluetooth());
//
//        removeData();
//    }
//
//    @Test
//    public void updateMeasurementListInsideProfileTest() {
//        Device device = getDevice();
//        deviceRepository.saveAndFlush(device);
//
//        Profile profile = createProfile();
//        profileRepository.saveAndFlush(profile);
//
//        MeasurementType measurementType = createMeasurementType();
//        measurementTypeRepository.saveAndFlush(measurementType);
//
//        Measurement measurement = createMeasurement(measurementType, device, profile);
//        measurementRepository.saveAndFlush(measurement);
//
//        profile.getMeasurementList().add(measurement);
//        profileRepository.saveAndFlush(profile);
//
//        Assertions.assertEquals(1, profile.getMeasurementList().size());
//        Assertions.assertEquals(measurement.getId(), profile.getMeasurementList().getFirst().getId());
//
//        Measurement newMeasurement1 = createMeasurement(measurementType, device, profile);
//        measurementRepository.saveAndFlush(newMeasurement1);
//
//        Measurement newMeasurement2 = createMeasurement(measurementType, device, profile);
//        measurementRepository.saveAndFlush(newMeasurement2);
//
//        profile.getMeasurementList().remove(measurement);
//        profile.getMeasurementList().add(newMeasurement1);
//        profile.getMeasurementList().add(newMeasurement2);
//        profileRepository.saveAndFlush(profile);
//        measurementRepository.delete(measurement);
//
//        Profile updatedProfile = profileRepository.getReferenceById(profile.getUserId());
//        Measurement updatedMeasurement1 = measurementRepository.getReferenceById(newMeasurement1.getId());
//        Measurement updatedMeasurement2 = measurementRepository.getReferenceById(newMeasurement2.getId());
//
//        Assertions.assertEquals(2, updatedProfile.getMeasurementList().size());
//        Assertions.assertTrue(updatedProfile.getMeasurementList().containsAll(List.of(newMeasurement1, newMeasurement2)));
//        Assertions.assertTrue(List.of(newMeasurement1, newMeasurement2).containsAll(updatedProfile.getMeasurementList()));
//        Assertions.assertEquals(updatedProfile.getUserId(), updatedMeasurement1.getProfile().getUserId());
//        Assertions.assertEquals(updatedProfile.getUserId(), updatedMeasurement2.getProfile().getUserId());
//    }
//
//    @Test
//    public void updateDeviceUserListInsideProfileTest() {
//        Device device = getDevice();
//        deviceRepository.saveAndFlush(device);
//
//        Profile profile = createProfile();
//        profileRepository.saveAndFlush(profile);
//
//        DeviceUser deviceUser = createDeviceUser(device, profile);
//        deviceUserRepository.saveAndFlush(deviceUser);
//
//        profile.getDeviceUserList().add(deviceUser);
//        profileRepository.saveAndFlush(profile);
//
//        Profile refreshedProfile = profileRepository.getReferenceById(profile.getUserId());
//        Assertions.assertEquals(1, refreshedProfile.getDeviceUserList().size());
//        Assertions.assertEquals(device.getId(), refreshedProfile.getDeviceUserList().getFirst().getDevice().getId());
//
//        Device device1 = getDevice();
//        Device device2 = getDevice();
//        deviceRepository.saveAllAndFlush(List.of(device1, device2));
//
//        DeviceUser deviceUser1 = createDeviceUser(device1, profile);
//        DeviceUser deviceUser2 = createDeviceUser(device2, profile);
//        deviceUserRepository.saveAllAndFlush(List.of(deviceUser1, deviceUser2));
//
//        profile.getDeviceUserList().remove(deviceUser);
//        profile.getDeviceUserList().add(deviceUser1);
//        profile.getDeviceUserList().add(deviceUser2);
//        profileRepository.saveAndFlush(profile);
//        deviceUserRepository.delete(deviceUser);
//
//        Profile updatedProfile = profileRepository.getReferenceById(profile.getUserId());
//
//        Assertions.assertEquals(2, updatedProfile.getDeviceUserList().size());
//        Assertions.assertTrue(updatedProfile.getDeviceUserList().containsAll(List.of(deviceUser1, deviceUser2)));
//        Assertions.assertTrue(List.of(deviceUser1, deviceUser2).containsAll(updatedProfile.getDeviceUserList()));
//        Assertions.assertEquals(updatedProfile.getUserId(), deviceUser1.getProfile().getUserId());
//        Assertions.assertEquals(updatedProfile.getUserId(), deviceUser2.getProfile().getUserId());
//    }
//
//    private Profile createProfile() {
//        UUID uuid = SORTED_UUIDS[0];
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
//        p.setMeasurementList(new ArrayList<>());
//        p.setDeviceUserList(new ArrayList<>());
//
//        return p;
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
//    private static ResultSet getResultSet(String query) throws SQLException {
//        Connection connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
//        Statement statement = connection.createStatement();
//      return statement.executeQuery(query);
//    }
//
//    private static void insertData() throws SQLException {
//        ConnectionAndStatement connectionAndStatement = getConnectionAndStatement();
//        insertProfiles(connectionAndStatement.connection());
//        insertMeasurements(connectionAndStatement.statement());
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
//    private static void removeData() throws SQLException {
//        ConnectionAndStatement connectionAndStatement = getConnectionAndStatement();
//
//        connectionAndStatement.statement().execute("DELETE FROM common.device_user");
//        connectionAndStatement.statement().execute("DELETE FROM common.device_measurement_type");
//        connectionAndStatement.statement().execute("DELETE FROM common.measurement");
//        connectionAndStatement.statement().execute("DELETE FROM common.profile");
//
//        connectionAndStatement.statement().close();
//        connectionAndStatement.connection().close();
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
//    private static void insertMeasurements(Statement statement) throws SQLException {
//        String measurement = "INSERT INTO common.measurement (id, measurement_type_id, device_id, user_id, measured_at, value, measurement_batch_id, measurement_group_id, measurement_mode) VALUES "
//                + "('"
//                + SORTED_UUIDS[0] + "','"
//                + MEASUREMENT_TYPE_ID + "','"
//                + DEVICE_ID + "','"
//                + SORTED_UUIDS[0] + "','"
//                + TIMESTAMP + "',"
//                + MEASUREMENT_VALUE_1 + ","
//                + MEASUREMENT_BATCH_ID + ","
//                + MEASUREMENT_GROUP_ID + ",'"
//                + MEASUREMENT_MODE + "'"
//                + "),('"
//                + SORTED_UUIDS[1] + "','"
//                + MEASUREMENT_TYPE_ID + "','"
//                + DEVICE_ID + "','"
//                + SORTED_UUIDS[0] + "','"
//                + TIMESTAMP + "',"
//                + MEASUREMENT_VALUE_2 + ","
//                + MEASUREMENT_BATCH_ID + ","
//                + MEASUREMENT_GROUP_ID + ",'"
//                + MEASUREMENT_MODE + "'"
//                + "),('"
//                + SORTED_UUIDS[2] + "','"
//                + MEASUREMENT_TYPE_ID + "','"
//                + DEVICE_ID + "','"
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
//    private static void insertDeviceUsers(Statement statement) throws SQLException {
//        String deviceUser = "INSERT INTO common.device_user (profile_user_id, device_id, api_key) VALUES "
//                + "('"
//                + SORTED_UUIDS[0] + "',"
//                + "'" + DEVICE_ID + "',"
//                + "'" + API_KEY_1
//                + "')";
//        statement.execute(deviceUser);
//    }
//
//    private Device getDevice() {
//        return deviceRepository.getReferenceById(DEVICE_ID);
//    }
//
//    private MeasurementType createMeasurementType() {
//        MeasurementType measurementType = new MeasurementType();
//        measurementType.setMeasurementType(MeasurementTypeEnum.BLOOD_PRESSURE);
//        measurementType.setMeasurementSubType(MeasurementSubTypeEnum.DIASTOLIC);
//        measurementType.setMinValue(10.99);
//        measurementType.setMaxValue(20.99);
//        measurementType.setUnit("unit");
//        measurementType.setMeasurementList(new ArrayList<>());
//        measurementType.setDeviceList(new ArrayList<>());
//
//        return measurementType;
//    }
//
//    private Measurement createMeasurement(MeasurementType measurementType, Device device, Profile profile) {
//        Measurement measurement = new Measurement();
//        measurement.setMeasurementType(measurementType);
//        measurement.setDevice(device);
//        measurement.setProfile(profile);
//        measurement.setMeasuredAt(OffsetDateTime.now());
//        measurement.setValue(10.0);
//        measurement.setMeasurementBatchId("measurementBatchId");
//        measurement.setMeasurementMode(MeasurementMode.SINGLE);
//        return measurement;
//    }
//
//    private DeviceUser createDeviceUser(Device device, Profile user) {
//        DeviceUserId deviceUserId = new DeviceUserId();
//        deviceUserId.setDevice(device);
//        deviceUserId.setProfile(user);
//
//        DeviceUser du = new DeviceUser();
//        du.setPrimaryKey(deviceUserId);
//        du.setApiKey("api_key");
//
//        return du;
//    }
//}

package si.result.eearly.mapper;

import com.google.protobuf.ByteString;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import si.result.eearly.domain.device.Device;
import si.result.eearly.domain.measurement.MeasurementType;
import si.result.eearly.genproto.GetSupportedDeviceResponse;
import si.result.eearly.genproto.GetSupportedDevicesForManufacturerResponse;
import si.result.eearly.genproto.ConnectionType;
import si.result.eearly.genproto.DeviceSummary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mapper(collectionMappingStrategy = CollectionMappingStrategy.TARGET_IMMUTABLE)
public interface DeviceMapper {

    MeasurementTypeMapper INSTANCE = Mappers.getMapper(MeasurementTypeMapper.class);

    default GetSupportedDeviceResponse deviceToSupportedDeviceResponse(Device device) {

        List<si.result.eearly.genproto.MeasurementType> measurementTypes = new ArrayList<>();
        for(MeasurementType measurementType : device.getMeasurementTypeList()) {
            measurementTypes.add(INSTANCE.measurementTypeToGenprotoMeasurementType(measurementType));
        }

        return GetSupportedDeviceResponse.newBuilder()
                .setDeviceId(device.getId().toString())
                .setDeviceName(device.getName())
                .setDescription(device.getDescription())
                .setConnectionType(maptoProtoConnectionType(device.getConnectionType()))
                .setPicture(ByteString.copyFrom(device.getPicture()))
                .addAllMeasurementTypes(measurementTypes)
                .addAllBluetoothDeviceNames(
                        device.getBluetoothDeviceNames() != null
                                ? device.getBluetoothDeviceNames()
                                : Collections.emptyList()
                )
                .build();
    }

    default GetSupportedDevicesForManufacturerResponse toSupportedDevicesForManufacturer(List<Device> devices) {
        List<DeviceSummary> deviceSummaries = new ArrayList<>();
        for(Device device : devices) {
            deviceSummaries.add(toDeviceSummary(device));
        }
        return GetSupportedDevicesForManufacturerResponse.newBuilder().addAllDevices(deviceSummaries).build();
    }

    private DeviceSummary toDeviceSummary(Device device) {
        return DeviceSummary.newBuilder()
                .setDeviceId(device.getId().toString())
                .setDeviceName(device.getName()).build();
    }

    private ConnectionType maptoProtoConnectionType(String connectionTypeString) {
        return switch (connectionTypeString) {
            case "API" -> ConnectionType.CONNECTION_API;
            case "Bluetooth" -> ConnectionType.CONNECTION_BLUETOOTH;
            default -> ConnectionType.CONNECTION_UNSPECIFIED;
        };
    }
}

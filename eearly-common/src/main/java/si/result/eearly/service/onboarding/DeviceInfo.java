package si.result.eearly.service.onboarding;

public record DeviceInfo(

        String os,

        String deviceType

) {
    @Override
    public String toString() {
        return os + " " + deviceType;
    }
}

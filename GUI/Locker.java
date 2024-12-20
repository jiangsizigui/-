public class Locker {
    private String id;
    private LockerSize size;
    private boolean isOccupied;
    private String pickupCode;
    private String phoneNumber;
    private long deliveryTime;

    public enum LockerSize {
        SMALL, MEDIUM, LARGE
    }

    public Locker(String id, LockerSize size) {
        this.id = id;
        this.size = size;
        this.isOccupied = false;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public LockerSize getSize() {
        return size;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }

    public String getPickupCode() {
        return pickupCode;
    }

    public void setPickupCode(String code) {
        this.pickupCode = code;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phone) {
        this.phoneNumber = phone;
    }

    public long getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(long time) {
        this.deliveryTime = time;
    }
}
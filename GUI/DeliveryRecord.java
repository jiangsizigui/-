public class DeliveryRecord {
    private String id;
    private String courierId;
    private String phoneNumber;
    private String lockerId;
    private String pickupCode;
    private long deliveryTime;
    private long pickupTime;
    private boolean pickedUp;
    private boolean overdue;
    private boolean redelivered;

    public DeliveryRecord(String id, String courierId, String phoneNumber, String lockerId, String pickupCode) {
        this.id = id;
        this.courierId = courierId;
        this.phoneNumber = phoneNumber;
        this.lockerId = lockerId;
        this.pickupCode = pickupCode;
        this.deliveryTime = System.currentTimeMillis();
        this.pickupTime = 0;
        this.pickedUp = false;
        this.overdue = false;
        this.redelivered = false;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getCourierId() {
        return courierId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getLockerId() {
        return lockerId;
    }

    public String getPickupCode() {
        return pickupCode;
    }

    public long getDeliveryTime() {
        return deliveryTime;
    }

    public long getPickupTime() {
        return pickupTime;
    }

    public boolean isPickedUp() {
        return pickedUp;
    }

    public boolean isOverdue() {
        return overdue;
    }

    public boolean isRedelivered() {
        return redelivered;
    }

    // Setters
    public void setDeliveryTime(long time) {
        this.deliveryTime = time;
    }

    public void setPickupTime(long time) {
        this.pickupTime = time;
    }

    public void setPickedUp(boolean pickedUp) {
        this.pickedUp = pickedUp;
    }

    public void setOverdue(boolean overdue) {
        this.overdue = overdue;
    }

    public void setRedelivered(boolean redelivered) {
        this.redelivered = redelivered;
    }
}
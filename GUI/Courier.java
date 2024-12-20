public class Courier {
    private String id;
    private String name;
    private String password;
    private String phone;
    private long registrationTime;
    private LinkedList<DeliveryRecord> deliveryRecords;

    public Courier(String id, String name, String password, String phone) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.phone = phone;
        this.registrationTime = System.currentTimeMillis();
        this.deliveryRecords = new LinkedList<>();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public LinkedList<DeliveryRecord> getDeliveryRecords() {
        return deliveryRecords;
    }

    public void addDeliveryRecord(DeliveryRecord record) {
        deliveryRecords.add(record);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // 添加获取密码的方法（仅用于保存账号信息）
    public String getPassword() {
        return password;
    }

    public long getRegistrationTime() {
        return registrationTime;
    }
}
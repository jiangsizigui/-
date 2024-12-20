import java.util.HashMap;
import java.util.LinkedList;

public class LockerManager {
    private LinkedList<Locker> lockers;
    private HashMap<String, Courier> couriers;
    private HashMap<String, DeliveryRecord> deliveryRecords;
    private HashMap<String, LinkedList<DeliveryRecord>> phoneRecords;
    private HashMap<String, CourierRegistration> pendingRegistrations;
    private HashMap<String, FileManager.AccountInfo> adminAccounts;
    private HashMap<String, FileManager.AccountInfo> courierAccounts;

    public LockerManager() {
        lockers = new LinkedList<>();
        couriers = new HashMap<>();
        deliveryRecords = new HashMap<>();
        phoneRecords = new HashMap<>();
        pendingRegistrations = new HashMap<>();

        // 加载账号信息
        adminAccounts = FileManager.loadAccounts(FileManager.ADMIN_FILE);
        courierAccounts = FileManager.loadAccounts(FileManager.COURIER_FILE);

        // 加载快递柜状态
        LinkedList<Locker> loadedLockers = FileManager.loadLockerStatus();
        if (!loadedLockers.isEmpty()) {
            lockers = loadedLockers;
        } else {
            initializeLockers(5, 3, 2); // 默认配置
        }

        // 加载投递记录
        LinkedList<DeliveryRecord> records = FileManager.loadDeliveryLogs();
        for (DeliveryRecord record : records) {
            deliveryRecords.put(record.getPickupCode(), record);

            // 更新手机号关联的记录
            LinkedList<DeliveryRecord> phoneDeliveries = phoneRecords.get(record.getPhoneNumber());
            if (phoneDeliveries == null) {
                phoneDeliveries = new LinkedList<>();
                phoneRecords.put(record.getPhoneNumber(), phoneDeliveries);
            }
            phoneDeliveries.add(record);
        }
    }

    public void initializeLockers(int small, int medium, int large) {
        lockers = new LinkedList<>();
        int id = 1;
        for (int i = 0; i < small; i++) {
            lockers.add(new Locker(String.format("S%03d", id++), Locker.LockerSize.SMALL));
        }
        id = 1;
        for (int i = 0; i < medium; i++) {
            lockers.add(new Locker(String.format("M%03d", id++), Locker.LockerSize.MEDIUM));
        }
        id = 1;
        for (int i = 0; i < large; i++) {
            lockers.add(new Locker(String.format("L%03d", id++), Locker.LockerSize.LARGE));
        }

        // 保存快递柜配置
        FileManager.saveLockerConfig(small, medium, large);
    }

    // 检查指定尺寸的快递柜是否可用
    private boolean checkLockerAvailability(Locker.LockerSize size) {
        FileManager.LockerConfig config = FileManager.loadLockerConfig();
        int configCount = 0;
        int usedCount = 0;

        switch (size) {
            case SMALL:
                configCount = config.smallLockers;
                break;
            case MEDIUM:
                configCount = config.mediumLockers;
                break;
            case LARGE:
                configCount = config.largeLockers;
                break;
        }

        // 统计已使用的快递柜数量
        for (Locker locker : lockers) {
            if (locker.getSize() == size && locker.isOccupied()) {
                usedCount++;
            }
        }

        return usedCount < configCount;
    }

    public Locker findAvailableLocker(Locker.LockerSize size) {
        for (int i = 0; i < lockers.size(); i++) {
            Locker locker = lockers.get(i);
            if (locker.getSize() == size && !locker.isOccupied()) {
                return locker;
            }
        }
        return null;
    }

    public String generatePickupCode() {
        // 生成6位数字取件码
        StringBuilder code;
        do {
            code = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                code.append((int) (Math.random() * 10));
            }
        } while (deliveryRecords.get(code.toString()) != null);
        return code.toString();
    }

    // 获取未取件数量
    public int getUnpickedCount(String phoneNumber) {
        LinkedList<DeliveryRecord> records = phoneRecords.get(phoneNumber);
        if (records == null) {
            return 0;
        }

        int count = 0;
        for (DeliveryRecord record : records) {
            if (!record.isPickedUp()) {
                count++;
            }
        }
        return count;
    }

    // 获取所有投递记录
    public LinkedList<DeliveryRecord> getAllDeliveryRecords() {
        LinkedList<DeliveryRecord> allRecords = new LinkedList<>();
        for (DeliveryRecord record : deliveryRecords.values()) {
            if (record != null) {
                allRecords.add(record);
            }
        }
        return allRecords;
    }

    // 获取统计信息
    public DeliveryStats getDeliveryStats() {
        int total = 0;
        int picked = 0;
        int unpicked = 0;
        int overdue = 0;

        LinkedList<DeliveryRecord> records = getAllDeliveryRecords();
        for (int i = 0; i < records.size(); i++) {
            DeliveryRecord record = records.get(i);
            total++;
            if (record.isPickedUp()) {
                picked++;
            } else {
                unpicked++;
                if (record.isOverdue()) {
                    overdue++;
                }
            }
        }

        return new DeliveryStats(total, picked, unpicked, overdue);
    }

    // 统计信息类
    public static class DeliveryStats {
        public final int totalDeliveries;
        public final int pickedDeliveries;
        public final int unpickedDeliveries;
        public final int overdueDeliveries;

        public DeliveryStats(int total, int picked, int unpicked, int overdue) {
            this.totalDeliveries = total;
            this.pickedDeliveries = picked;
            this.unpickedDeliveries = unpicked;
            this.overdueDeliveries = overdue;
        }
    }

    // 获取当前快递柜配置
    public LockerConfig getCurrentConfig() {
        int small = 0, medium = 0, large = 0;
        for (int i = 0; i < lockers.size(); i++) {
            Locker locker = lockers.get(i);
            switch (locker.getSize()) {
                case SMALL:
                    small++;
                    break;
                case MEDIUM:
                    medium++;
                    break;
                case LARGE:
                    large++;
                    break;
            }
        }
        return new LockerConfig(small, medium, large);
    }

    // 快递柜配置类
    public static class LockerConfig {
        public final int smallLockers;
        public final int mediumLockers;
        public final int largeLockers;

        public LockerConfig(int small, int medium, int large) {
            this.smallLockers = small;
            this.mediumLockers = medium;
            this.largeLockers = large;
        }
    }

    // 重置所有数据
    public void reset() {
        LockerConfig config = getCurrentConfig();
        lockers = new LinkedList<>();
        deliveryRecords = new HashMap<>();
        phoneRecords = new HashMap<>();
        initializeLockers(config.smallLockers, config.mediumLockers, config.largeLockers);
    }

    // 添加错误处理
    public DeliveryRecord deliverPackage(String courierId, String phoneNumber, Locker.LockerSize size) {
        // 检查快递柜是否可用
        if (!checkLockerAvailability(size)) {
            throw new RuntimeException("暂无可用快递柜，投递失败");
        }

        // 查找可用储物柜
        Locker locker = findAvailableLocker(size);
        if (locker == null) {
            return null;
        }

        // 生成取件码和快递单号
        String pickupCode = generatePickupCode();
        String deliveryId = String.format("D%d", System.currentTimeMillis() % 1000000);

        // 创建投递记录
        DeliveryRecord record = new DeliveryRecord(deliveryId, courierId, phoneNumber, locker.getId(), pickupCode);

        // 更新储物柜状态
        locker.setOccupied(true);
        locker.setPickupCode(pickupCode);
        locker.setPhoneNumber(phoneNumber);
        locker.setDeliveryTime(System.currentTimeMillis());

        // 保存记录
        deliveryRecords.put(pickupCode, record);

        // 更新手机号关联的记录
        LinkedList<DeliveryRecord> phoneDeliveries = phoneRecords.get(phoneNumber);
        if (phoneDeliveries == null) {
            phoneDeliveries = new LinkedList<>();
            phoneRecords.put(phoneNumber, phoneDeliveries);
        }
        phoneDeliveries.add(record);

        // 更新快递员的投递记录
        Courier courier = couriers.get(courierId);
        if (courier != null) {
            courier.addDeliveryRecord(record);
        }

        // 保存��态
        FileManager.saveLockerStatus(lockers);
        FileManager.appendDeliveryLog(record);

        return record;
    }

    public boolean pickupPackage(String phoneNumber, String pickupCode) {
        try {
            if (phoneNumber == null || pickupCode == null) {
                return false;
            }
            if (!phoneNumber.matches("\\d{11}")) {
                return false;
            }

            DeliveryRecord record = deliveryRecords.get(pickupCode);
            if (record == null || !record.getPhoneNumber().equals(phoneNumber) || record.isPickedUp()) {
                return false;
            }

            // 更新记录状态
            record.setPickedUp(true);
            record.setPickupTime(System.currentTimeMillis());

            // 更新储物柜状态
            boolean success = false;
            for (Locker locker : lockers) {
                if (locker.getId().equals(record.getLockerId())) {
                    locker.setOccupied(false);
                    locker.setPickupCode(null);
                    locker.setPhoneNumber(null);
                    success = true;
                    break;
                }
            }

            if (success) {
                FileManager.saveLockerStatus(lockers);
            }
            return success;
        } catch (Exception e) {
            System.err.println("Pickup error: " + e.getMessage());
            return false;
        }
    }

    // 其他管理方法...
    // 添加快递员登录、投递、取件等业务逻辑方法

    // 快递员登录
    public Courier courierLogin(String id, String password) {
        try {
            FileManager.AccountInfo accountInfo = courierAccounts.get(id);
            if (accountInfo != null &&
                    PasswordUtils.verifyPassword(password, accountInfo.salt, accountInfo.hashedPassword)) {
                return couriers.get(id);
            }
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
        }
        return null;
    }

    // 添加更多测试数据
    public void addTestData() {
        // 添加测试快递员
        Courier courier1 = new Courier("C001", "张三", "123456", "13800138001");
        Courier courier2 = new Courier("C002", "李四", "123456", "13800138002");

        addCourier(courier1);
        addCourier(courier2);

        // 模拟一些投递记录
        try {
            deliverPackage("C001", "13800138000", Locker.LockerSize.SMALL);
            deliverPackage("C001", "13800138001", Locker.LockerSize.MEDIUM);
            deliverPackage("C002", "13800138002", Locker.LockerSize.LARGE);

            // 模拟一已取出的记录
            DeliveryRecord record = deliverPackage("C002", "13800138003", Locker.LockerSize.SMALL);
            if (record != null) {
                pickupPackage("13800138003", record.getPickupCode());
            }
        } catch (Exception e) {
            System.err.println("添加测试数据错误: " + e.getMessage());
        }
    }

    // 快递员注册申请
    public static class CourierRegistration {
        public final String name;
        public final String phone;
        public final String password;
        public final long submitTime;

        public CourierRegistration(String name, String phone, String password) {
            this.name = name;
            this.phone = phone;
            this.password = password;
            this.submitTime = System.currentTimeMillis();
        }
    }

    // 提交注册申请
    public boolean submitCourierRegistration(String name, String phone, String password) {
        // 添加参数验证
        if (name == null || phone == null || password == null ||
                name.trim().isEmpty() || phone.trim().isEmpty() || password.trim().isEmpty()) {
            throw new IllegalArgumentException("姓名、手机号和密码不能为空");
        }

        // 检查手���号格式
        if (!phone.matches("\\d{11}")) {
            throw new IllegalArgumentException("手机号格式不正确");
        }

        // 检查手机号是否已被注册
        for (Courier courier : couriers.values()) {
            if (phone.equals(courier.getPhone())) {
                return false;
            }
        }

        // 检查是否已有待审核的申请
        if (pendingRegistrations.containsKey(phone)) {
            return false;
        }

        pendingRegistrations.put(phone, new CourierRegistration(name, phone, password));
        return true;
    }

    // 获取所有待审核的注申请
    public LinkedList<CourierRegistration> getPendingRegistrations() {
        LinkedList<CourierRegistration> registrations = new LinkedList<>();
        for (CourierRegistration reg : pendingRegistrations.values()) {
            registrations.add(reg);
        }
        return registrations;
    }

    // 审核通过注册申请
    public boolean approveCourierRegistration(String phone, String courierId) {
        CourierRegistration reg = pendingRegistrations.remove(phone);
        if (reg != null) {
            Courier courier = new Courier(courierId, reg.name, reg.password, reg.phone);
            return addCourier(courier);
        }
        return false;
    }

    // 拒���注册申请
    public boolean rejectCourierRegistration(String phone) {
        return pendingRegistrations.remove(phone) != null;
    }

    // 生成快递员ID
    public String generateCourierId() {
        int id = couriers.size() + 1;
        return String.format("C%03d", id);
    }

    // 验证管理员登录
    public boolean validateAdminLogin(String username, String password) {
        FileManager.AccountInfo accountInfo = adminAccounts.get(username);
        if (accountInfo != null) {
            return PasswordUtils.verifyPassword(password, accountInfo.salt, accountInfo.hashedPassword);
        }
        return false;
    }

    // 添加新的快递员
    public boolean addCourier(Courier courier) {
        if (courier == null || couriers.containsKey(courier.getId()) ||
                courierAccounts.containsKey(courier.getId())) {
            return false;
        }

        // 生成盐值和哈希密码
        String salt = PasswordUtils.generateSalt();
        String hashedPassword = PasswordUtils.hashPassword(courier.getPassword(), salt);

        // 保存快递员账号
        courierAccounts.put(courier.getId(), new FileManager.AccountInfo(hashedPassword, salt));
        FileManager.saveAccounts(courierAccounts, FileManager.COURIER_FILE);

        // 添加到内存中
        couriers.put(courier.getId(), courier);
        return true;
    }

    // 获取所有快递员
    public LinkedList<Courier> getAllCouriers() {
        LinkedList<Courier> courierList = new LinkedList<>();
        for (Courier courier : couriers.values()) {
            courierList.add(courier);
        }
        return courierList;
    }
}
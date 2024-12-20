import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class FileManager {
    public static final String DATA_DIR = "data/";
    public static final String ADMIN_FILE = DATA_DIR + "admin.txt";
    public static final String COURIER_FILE = DATA_DIR + "courier.txt";
    public static final String LOCKER_STATUS_FILE = DATA_DIR + "locker_status.txt";
    public static final String DELIVERY_LOG_FILE = DATA_DIR + "delivery_log.txt";
    public static final String LOCKER_CONFIG_FILE = DATA_DIR + "locker_config.txt";

    static {
        new File(DATA_DIR).mkdirs();
    }

    // 读取账号密码到HashMap
    public static HashMap<String, AccountInfo> loadAccounts(String filename) {
        HashMap<String, AccountInfo> accounts = new HashMap<>();
        try {
            File file = new File(filename);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
                if (filename.equals(ADMIN_FILE)) {
                    String salt = "admin_salt";
                    String hashedPassword = PasswordUtils.hashPassword("admin", salt);
                    accounts.put("admin", new AccountInfo(hashedPassword, salt));
                    saveAccounts(accounts, filename);
                }
            } else {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(",");
                        if (parts.length >= 3) {
                            accounts.put(parts[0], new AccountInfo(parts[1], parts[2]));
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("读取文件失败: " + filename);
            e.printStackTrace();
        }
        return accounts;
    }

    // 保存账号密码
    public static void saveAccounts(HashMap<String, AccountInfo> accounts, String filename) {
        try {
            File file = new File(filename);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                for (Map.Entry<String, AccountInfo> entry : accounts.entrySet()) {
                    writer.println(String.format("%s,%s,%s",
                            entry.getKey(),
                            entry.getValue().hashedPassword,
                            entry.getValue().salt));
                }
            }
        } catch (IOException e) {
            System.err.println("保存文件失败: " + filename);
            e.printStackTrace();
        }
    }

    // 添加投递记录
    public static void appendDeliveryLog(DeliveryRecord record) {
        try {
            File file = new File(DELIVERY_LOG_FILE);
            if (!file.exists()) {
                file.createNewFile();
            }
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
                writer.println(String.format("%s,%s,%s,%s,%s,%d,%d,%b,%b,%b",
                        record.getId(),
                        record.getCourierId(),
                        record.getPhoneNumber(),
                        record.getLockerId(),
                        record.getPickupCode(),
                        record.getDeliveryTime(),
                        record.getPickupTime(),
                        record.isPickedUp(),
                        record.isOverdue(),
                        record.isRedelivered()));
            }
        } catch (IOException e) {
            System.err.println("保存投递记录失败");
            e.printStackTrace();
        }
    }

    // 读取投递记录
    public static LinkedList<DeliveryRecord> loadDeliveryLogs() {
        LinkedList<DeliveryRecord> records = new LinkedList<>();
        File file = new File(DELIVERY_LOG_FILE);
        if (!file.exists()) {
            return records; // 返回空列表而不是报错
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 10) {
                    DeliveryRecord record = new DeliveryRecord(
                            parts[0], // id
                            parts[1], // courierId
                            parts[2], // phoneNumber
                            parts[3], // lockerId
                            parts[4] // pickupCode
                    );
                    record.setDeliveryTime(Long.parseLong(parts[5]));
                    record.setPickupTime(Long.parseLong(parts[6]));
                    record.setPickedUp(Boolean.parseBoolean(parts[7]));
                    record.setOverdue(Boolean.parseBoolean(parts[8]));
                    record.setRedelivered(Boolean.parseBoolean(parts[9]));
                    records.add(record);
                }
            }
        } catch (IOException e) {
            System.err.println("读取投递记录失败: " + e.getMessage());
        }
        return records;
    }

    // 保存快递柜状态
    public static void saveLockerStatus(LinkedList<Locker> lockers) {
        try {
            File file = new File(LOCKER_STATUS_FILE);
            if (!file.exists()) {
                file.createNewFile();
            }
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                for (Locker locker : lockers) {
                    writer.println(String.format("%s,%s,%b,%s,%s,%d",
                            locker.getId(),
                            locker.getSize(),
                            locker.isOccupied(),
                            locker.getPickupCode(),
                            locker.getPhoneNumber(),
                            locker.getDeliveryTime()));
                }
            }
        } catch (IOException e) {
            System.err.println("保存快递柜状态失败");
            e.printStackTrace();
        }
    }

    // 读取快递柜状态
    public static LinkedList<Locker> loadLockerStatus() {
        LinkedList<Locker> lockers = new LinkedList<>();
        File file = new File(LOCKER_STATUS_FILE);
        if (!file.exists()) {
            return lockers; // 返回空列表而不是报错
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    Locker locker = new Locker(
                            parts[0],
                            Locker.LockerSize.valueOf(parts[1]));
                    locker.setOccupied(Boolean.parseBoolean(parts[2]));
                    locker.setPickupCode(parts[3]);
                    locker.setPhoneNumber(parts[4]);
                    locker.setDeliveryTime(Long.parseLong(parts[5]));
                    lockers.add(locker);
                }
            }
        } catch (IOException e) {
            System.err.println("读取快递柜状态失败: " + e.getMessage());
        }
        return lockers;
    }

    // 保存快递柜配置
    public static void saveLockerConfig(int small, int medium, int large) {
        try {
            File file = new File(LOCKER_CONFIG_FILE);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println(String.format("%d,%d,%d", small, medium, large));
            }
        } catch (IOException e) {
            System.err.println("保存快递柜配置失败: " + e.getMessage());
        }
    }

    // 读取快递柜配置
    public static LockerConfig loadLockerConfig() {
        try {
            File file = new File(LOCKER_CONFIG_FILE);
            if (!file.exists()) {
                // 返回默认配置
                return new LockerConfig(5, 3, 2);
            }
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine();
                if (line != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 3) {
                        return new LockerConfig(
                                Integer.parseInt(parts[0]),
                                Integer.parseInt(parts[1]),
                                Integer.parseInt(parts[2]));
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("读取快递柜配置失败: " + e.getMessage());
        }
        return new LockerConfig(5, 3, 2); // 默认配置
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

    // 账号信息类
    public static class AccountInfo {
        public final String hashedPassword;
        public final String salt;

        public AccountInfo(String hashedPassword, String salt) {
            this.hashedPassword = hashedPassword;
            this.salt = salt;
        }
    }
}
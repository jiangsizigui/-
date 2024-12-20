import java.awt.CardLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class DeliveryLockerGUI extends JFrame {
    private LockerManager lockerManager;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    // 各个子界面
    private LoginGUI loginPanel;
    private CourierGUI courierPanel;
    private UserGUI userPanel;
    private AdminGUI adminPanel;

    public DeliveryLockerGUI() {
        // 初始化快递柜管理器
        lockerManager = new LockerManager();
        // 初始化一些测试数据
        initTestData();

        // 设置窗口属性
        setTitle("智能快递柜系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // 使用卡片布局
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 初始化各个界面
        loginPanel = new LoginGUI(this);
        courierPanel = new CourierGUI(this);
        userPanel = new UserGUI(this);
        adminPanel = new AdminGUI(this);

        // 添加各个界面到主面板
        mainPanel.add(loginPanel, "login");
        mainPanel.add(courierPanel, "courier");
        mainPanel.add(userPanel, "user");
        mainPanel.add(adminPanel, "admin");

        // 添加主面板到窗口
        add(mainPanel);

        // 显示登录界面
        showLogin();
    }

    // 切换到登录界面
    public void showLogin() {
        cardLayout.show(mainPanel, "login");
    }

    // 切换到快递员界面
    public void showCourier(Courier courier) {
        if (courierPanel == null) {
            courierPanel = new CourierGUI(this);
        }
        courierPanel.setCourier(courier);
        cardLayout.show(mainPanel, "courier");
        setVisible(true);
    }

    // 切换到用户界面
    public void showUser() {
        cardLayout.show(mainPanel, "user");
    }

    // 切换到管理员界面
    public void showAdmin() {
        adminPanel.onShow();
        cardLayout.show(mainPanel, "admin");
    }

    // 获取LockerManager实例
    public LockerManager getLockerManager() {
        return lockerManager;
    }

    // 初始化测试数据
    private void initTestData() {
        // 初始化储物柜
        lockerManager.initializeLockers(5, 3, 2);

        // 添加测试快递员（使用正确的构造函数）
        Courier testCourier = new Courier("C001", "测试快递员", "123456", "13800138000");
        lockerManager.addCourier(testCourier);
    }
}
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class AdminGUI extends JPanel {
    private DeliveryLockerGUI mainFrame;
    private JTable deliveryTable;
    private DefaultTableModel tableModel;
    private JSpinner smallLockerSpinner;
    private JSpinner mediumLockerSpinner;
    private JSpinner largeLockerSpinner;
    private JLabel statsLabel;
    private JTable courierTable;

    public AdminGUI(DeliveryLockerGUI mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // 顶部面板 - 标题和退出按钮
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("管理员控制面板", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        topPanel.add(titleLabel, BorderLayout.CENTER);

        JButton logoutButton = new JButton("退出登录");
        logoutButton.addActionListener(e -> mainFrame.showLogin());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(logoutButton);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // 中间面板 - 快递柜设置和记录显示
        JPanel centerPanel = new JPanel(new BorderLayout());

        // 快递柜设置面板
        JPanel lockerPanel = new JPanel(new GridBagLayout());
        lockerPanel.setBorder(BorderFactory.createTitledBorder("快递柜设置"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // 小型柜数量
        gbc.gridx = 0;
        gbc.gridy = 0;
        lockerPanel.add(new JLabel("小型柜数量："), gbc);
        smallLockerSpinner = new JSpinner(new SpinnerNumberModel(5, 0, 50, 1));
        gbc.gridx = 1;
        lockerPanel.add(smallLockerSpinner, gbc);

        // 中型柜数量
        gbc.gridx = 2;
        lockerPanel.add(new JLabel("中型柜数量："), gbc);
        mediumLockerSpinner = new JSpinner(new SpinnerNumberModel(3, 0, 30, 1));
        gbc.gridx = 3;
        lockerPanel.add(mediumLockerSpinner, gbc);

        // 大型柜数量
        gbc.gridx = 4;
        lockerPanel.add(new JLabel("大型柜数量："), gbc);
        largeLockerSpinner = new JSpinner(new SpinnerNumberModel(2, 0, 20, 1));
        gbc.gridx = 5;
        lockerPanel.add(largeLockerSpinner, gbc);

        // 更新按钮
        JButton updateButton = new JButton("更新快递柜配置");
        gbc.gridx = 6;
        lockerPanel.add(updateButton, gbc);

        // 添加快递员管理按钮
        JButton manageCourierButton = new JButton("快递员管理");
        gbc.gridx = 7;
        lockerPanel.add(manageCourierButton, gbc);

        centerPanel.add(lockerPanel, BorderLayout.NORTH);

        // 投递记录表格
        String[] columnNames = { "快递编号", "快递员ID", "收件人手机", "柜号", "取件码",
                "投递时间", "取件时间", "状态" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        deliveryTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(deliveryTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // 底部面板 - 统计信息
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("统计信息"));
        statsLabel = new JLabel("总投递量：0 | 已取件：0 | 未取件：0 | 滞留件：0");
        bottomPanel.add(statsLabel);
        add(bottomPanel, BorderLayout.SOUTH);

        // 加载当前配置
        FileManager.LockerConfig config = FileManager.loadLockerConfig();
        smallLockerSpinner.setValue(config.smallLockers);
        mediumLockerSpinner.setValue(config.mediumLockers);
        largeLockerSpinner.setValue(config.largeLockers);

        // 更新按钮事件
        updateButton.addActionListener(e -> {
            int small = (int) smallLockerSpinner.getValue();
            int medium = (int) mediumLockerSpinner.getValue();
            int large = (int) largeLockerSpinner.getValue();

            int option = JOptionPane.showConfirmDialog(this,
                    "更新快递柜配置将清空所有现有快递记录，是否继续？",
                    "确定更新",
                    JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.YES_OPTION) {
                mainFrame.getLockerManager().initializeLockers(small, medium, large);
                updateDeliveryTable();
                JOptionPane.showMessageDialog(this, "快递柜配置已更新");
            }
        });

        manageCourierButton.addActionListener(e -> showCourierManagementDialog());
        // 初始化快递员表格
        String[] courierColumnNames = { "账号", "姓名", "手机号", "注册时间", "投递数量" };
        DefaultTableModel courierTableModel = new DefaultTableModel(courierColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        courierTable = new JTable(courierTableModel);
    }

    private void updateDeliveryTable() {
        try {
            tableModel.setRowCount(0);
            LinkedList<DeliveryRecord> records = mainFrame.getLockerManager().getAllDeliveryRecords();

            for (int i = 0; i < records.size(); i++) {
                DeliveryRecord record = records.get(i);
                String status = record.isPickedUp() ? (record.isRedelivered() ? "已重新投递" : "已取出")
                        : (record.isOverdue() ? "滞留" : "待取件");

                String pickupTime = record.getPickupTime() > 0 ? new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(new java.util.Date(record.getPickupTime())) : "未取件";

                tableModel.addRow(new Object[] {
                        record.getId(),
                        record.getCourierId(),
                        record.getPhoneNumber(),
                        record.getLockerId(),
                        record.getPickupCode(),
                        new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                .format(new java.util.Date(record.getDeliveryTime())),
                        pickupTime,
                        status
                });
            }

            // 更新统计信息
            LockerManager.DeliveryStats stats = mainFrame.getLockerManager().getDeliveryStats();
            statsLabel.setText(String.format(
                    "总投递量：%d | 已取件：%d | 未取件：%d | 滞留件：%d",
                    stats.totalDeliveries,
                    stats.pickedDeliveries,
                    stats.unpickedDeliveries,
                    stats.overdueDeliveries));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "更新数据时发生错误：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showCourierManagementDialog() {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "快递员管理", true);
        dialog.setLayout(new BorderLayout());

        // 创建选项卡面板
        JTabbedPane tabbedPane = new JTabbedPane();

        // 快递员列表面板
        JPanel courierListPanel = new JPanel(new BorderLayout());
        String[] columnNames = { "账号", "姓名", "手机号", "注册时间", "投递数量" };
        DefaultTableModel courierTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable courierTable = new JTable(courierTableModel);
        courierListPanel.add(new JScrollPane(courierTable), BorderLayout.CENTER);

        // 待审核申请面板
        JPanel pendingPanel = new JPanel(new BorderLayout());
        String[] pendingColumns = { "姓名", "手机号", "申请时间", "状态" };
        DefaultTableModel pendingTableModel = new DefaultTableModel(pendingColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable pendingTable = new JTable(pendingTableModel);
        pendingPanel.add(new JScrollPane(pendingTable), BorderLayout.CENTER);

        // 添加审核按钮面板
        JPanel pendingButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton approveButton = new JButton("通过");
        JButton rejectButton = new JButton("拒绝");
        pendingButtonPanel.add(approveButton);
        pendingButtonPanel.add(rejectButton);
        pendingPanel.add(pendingButtonPanel, BorderLayout.SOUTH);

        // 添加选项卡
        tabbedPane.addTab("快递员列表", courierListPanel);
        tabbedPane.addTab("待审核申请", pendingPanel);
        dialog.add(tabbedPane, BorderLayout.CENTER);

        // 底部按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("添加快递员");
        JButton refreshButton = new JButton("刷新");
        JButton closeButton = new JButton("关闭");
        buttonPanel.add(addButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // 审核按钮事件
        approveButton.addActionListener(e -> {
            int selectedRow = pendingTable.getSelectedRow();
            if (selectedRow >= 0) {
                String phone = (String) pendingTable.getValueAt(selectedRow, 1);
                approveCourierRegistration(phone, pendingTableModel, courierTableModel);
            } else {
                JOptionPane.showMessageDialog(dialog, "请选择要审核的申请");
            }
        });

        rejectButton.addActionListener(e -> {
            int selectedRow = pendingTable.getSelectedRow();
            if (selectedRow >= 0) {
                String phone = (String) pendingTable.getValueAt(selectedRow, 1);
                if (mainFrame.getLockerManager().rejectCourierRegistration(phone)) {
                    JOptionPane.showMessageDialog(dialog, "已拒绝申请");
                    updatePendingApplicationsTable(pendingTableModel);
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "请选择要拒绝的申请");
            }
        });

        // 其他按钮事件
        addButton.addActionListener(e -> {
            showAddCourierDialog(dialog);
            updateCourierListTable(courierTableModel);
        });

        refreshButton.addActionListener(e -> {
            updateCourierListTable(courierTableModel);
            updatePendingApplicationsTable(pendingTableModel);
        });

        closeButton.addActionListener(e -> dialog.dispose());

        // 初始加载数据
        updateCourierListTable(courierTableModel);
        updatePendingApplicationsTable(pendingTableModel);

        dialog.setSize(800, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // 修改审核通过方法
    private void approveCourierRegistration(String phone, DefaultTableModel pendingTableModel,
            DefaultTableModel courierTableModel) {
        String courierId = mainFrame.getLockerManager().generateCourierId();
        if (mainFrame.getLockerManager().approveCourierRegistration(phone, courierId)) {
            JOptionPane.showMessageDialog(this, "审核通过");
            updatePendingApplicationsTable(pendingTableModel);
            updateCourierListTable(courierTableModel); // 更新快递员列表
        } else {
            JOptionPane.showMessageDialog(this, "审核失败");
        }
    }

    // 更新快递员列表表格
    private void updateCourierListTable(DefaultTableModel model) {
        model.setRowCount(0);
        LinkedList<Courier> couriers = mainFrame.getLockerManager().getAllCouriers();
        for (Courier courier : couriers) {
            model.addRow(new Object[] {
                    courier.getId(),
                    courier.getName(),
                    courier.getPhone(),
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            .format(new java.util.Date(courier.getRegistrationTime())),
                    courier.getDeliveryRecords().size()
            });
        }
    }

    // 当显示管理员界面时更新数据
    public void onShow() {
        updateDeliveryTable();
    }

    // 添加快递员表格更新方法
    private void updateCourierTable() {
        // 这里添加更新快递员列表的逻辑
        // 如果没有快递员表格，可以暂时留空或显示提示信息
        System.out.println("更新快递员列表");
    }

    // 添加缺失的更新待审核申请表格方法
    private void updatePendingApplicationsTable(DefaultTableModel model) {
        model.setRowCount(0);
        LinkedList<LockerManager.CourierRegistration> registrations = mainFrame.getLockerManager()
                .getPendingRegistrations();
        for (LockerManager.CourierRegistration reg : registrations) {
            model.addRow(new Object[] {
                    reg.name,
                    reg.phone,
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            .format(new java.util.Date(reg.submitTime)),
                    "待审核"
            });
        }
    }

    // 修改显示添加快递员对话框方法
    private void showAddCourierDialog(JDialog parentDialog) {
        JDialog dialog = new JDialog(parentDialog, "添加快递员", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // 添加输入字段
        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("账号："), gbc);
        JTextField idField = new JTextField(15);
        gbc.gridx = 1;
        dialog.add(idField, gbc);

        // 生成建议的账号
        String suggestedId = mainFrame.getLockerManager().generateCourierId();
        idField.setText(suggestedId);
        idField.setEditable(false); // 不允���修改账号

        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(new JLabel("姓名："), gbc);
        JTextField nameField = new JTextField(15);
        gbc.gridx = 1;
        dialog.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(new JLabel("手机号："), gbc);
        JTextField phoneField = new JTextField(15);
        gbc.gridx = 1;
        dialog.add(phoneField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        dialog.add(new JLabel("密码："), gbc);
        JPasswordField pwdField = new JPasswordField(15);
        gbc.gridx = 1;
        dialog.add(pwdField, gbc);

        // 按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton confirmButton = new JButton("确认");
        JButton cancelButton = new JButton("取消");
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        // 确认按钮事件
        confirmButton.addActionListener(e -> {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String password = new String(pwdField.getPassword());

            if (name.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请填写所有信息");
                return;
            }

            if (!phone.matches("\\d{11}")) {
                JOptionPane.showMessageDialog(dialog, "请输入正确的手机号");
                return;
            }

            try {
                Courier courier = new Courier(id, name, password, phone);
                if (mainFrame.getLockerManager().addCourier(courier)) {
                    JOptionPane.showMessageDialog(dialog, "添加快递员成功");
                    updateCourierListTable((DefaultTableModel) courierTable.getModel());
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "添加失败，账号或手机号已存在");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "添加失败：" + ex.getMessage());
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.pack();
        dialog.setLocationRelativeTo(parentDialog);
        dialog.setVisible(true);
    }
}
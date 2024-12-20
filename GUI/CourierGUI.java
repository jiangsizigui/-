import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class CourierGUI extends JPanel {
    private DeliveryLockerGUI mainFrame;
    private Courier currentCourier;
    private JTable deliveryTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> lockerSizeCombo;
    private JTextField phoneField;

    public CourierGUI(DeliveryLockerGUI mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // 顶部面板
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel welcomeLabel = new JLabel("欢迎, ");
        topPanel.add(welcomeLabel);
        JButton logoutButton = new JButton("退出登录");
        logoutButton.addActionListener(e -> mainFrame.showLogin());
        topPanel.add(logoutButton);
        add(topPanel, BorderLayout.NORTH);

        // 中间面板 - 投递操作
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 手机号输入
        gbc.gridx = 0;
        gbc.gridy = 0;
        centerPanel.add(new JLabel("收件人手机号："), gbc);

        phoneField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        centerPanel.add(phoneField, gbc);

        // 快递柜尺寸选择
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        centerPanel.add(new JLabel("快递柜尺寸："), gbc);

        String[] sizes = { "小型柜", "中型柜", "大型柜" };
        lockerSizeCombo = new JComboBox<>(sizes);
        gbc.gridx = 1;
        centerPanel.add(lockerSizeCombo, gbc);

        // 投递按钮
        JButton deliverButton = new JButton("投递");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        centerPanel.add(deliverButton, gbc);

        // 投递记录表格
        String[] columnNames = { "快递编号", "收件人手机", "柜号", "取件码", "投递时间", "状态" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        deliveryTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(deliveryTable);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        centerPanel.add(scrollPane, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // 投递按钮事件
        deliverButton.addActionListener(e -> handleDelivery());
    }

    private void handleDelivery() {
        String phone = phoneField.getText().trim();
        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入收件人手机号");
            return;
        }

        if (!phone.matches("\\d{11}")) {
            JOptionPane.showMessageDialog(this, "请输入正确的手机号");
            return;
        }

        Locker.LockerSize size;
        switch (lockerSizeCombo.getSelectedIndex()) {
            case 0:
                size = Locker.LockerSize.SMALL;
                break;
            case 1:
                size = Locker.LockerSize.MEDIUM;
                break;
            case 2:
                size = Locker.LockerSize.LARGE;
                break;
            default:
                return;
        }

        try {
            DeliveryRecord record = mainFrame.getLockerManager().deliverPackage(
                    currentCourier.getId(), phone, size);

            if (record != null) {
                updateDeliveryTable();
                JOptionPane.showMessageDialog(this,
                        String.format("投递成功！\n柜号：%s\n取件码：%s",
                                record.getLockerId(), record.getPickupCode()));
                phoneField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "没有可用的储物柜");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "投递失败：" + ex.getMessage());
        }
    }

    private void updateDeliveryTable() {
        tableModel.setRowCount(0);
        LinkedList<DeliveryRecord> records = currentCourier.getDeliveryRecords();
        for (int i = 0; i < records.size(); i++) {
            DeliveryRecord record = records.get(i);
            String status = record.isPickedUp() ? (record.isRedelivered() ? "已重新投递" : "已取出")
                    : (record.isOverdue() ? "已滞留" : "待取件");

            tableModel.addRow(new Object[] {
                    record.getId(),
                    record.getPhoneNumber(),
                    record.getLockerId(),
                    record.getPickupCode(),
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            .format(new java.util.Date(record.getDeliveryTime())),
                    status
            });
        }
    }

    public void setCourier(Courier courier) {
        this.currentCourier = courier;
        updateDeliveryTable();
    }
}
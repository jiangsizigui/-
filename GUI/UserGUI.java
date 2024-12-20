import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class UserGUI extends JPanel {
    private DeliveryLockerGUI mainFrame;
    private JTextField phoneField;
    private JTextField pickupCodeField;

    public UserGUI(DeliveryLockerGUI mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // 顶部面板
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("用户取件", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        topPanel.add(titleLabel, BorderLayout.CENTER);

        JButton backButton = new JButton("返回");
        backButton.addActionListener(e -> mainFrame.showLogin());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(backButton);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // 中间面板 - 取件信息输入
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // 手机号输入
        gbc.gridx = 0;
        gbc.gridy = 0;
        centerPanel.add(new JLabel("手机号："), gbc);

        phoneField = new JTextField(20);
        gbc.gridx = 1;
        centerPanel.add(phoneField, gbc);

        // 取件码输入
        gbc.gridx = 0;
        gbc.gridy = 1;
        centerPanel.add(new JLabel("取件码："), gbc);

        pickupCodeField = new JTextField(20);
        gbc.gridx = 1;
        centerPanel.add(pickupCodeField, gbc);

        // 取件按钮
        JButton pickupButton = new JButton("取件");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        centerPanel.add(pickupButton, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // 取件按钮事件
        pickupButton.addActionListener(e -> handlePickup());
    }

    private void handlePickup() {
        String phone = phoneField.getText().trim();
        String code = pickupCodeField.getText().trim();

        if (phone.isEmpty() || code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入手机号和取件码");
            return;
        }

        if (!phone.matches("\\d{11}")) {
            JOptionPane.showMessageDialog(this, "请输入正确的手机号");
            return;
        }

        try {
            if (mainFrame.getLockerManager().pickupPackage(phone, code)) {
                JOptionPane.showMessageDialog(this, "取件成功！");
                phoneField.setText("");
                pickupCodeField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "取件失败，请检查手机号和取件码是否正确");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "取件失败：" + ex.getMessage());
        }
    }
}
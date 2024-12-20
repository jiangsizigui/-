import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class LoginGUI extends JPanel {
    private DeliveryLockerGUI mainFrame;
    private JTextField phoneField;
    private JTextField pickupCodeField;

    public LoginGUI(DeliveryLockerGUI mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // 顶部标题
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("智能快递柜系统", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        topPanel.add(titleLabel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // 中间取件面板
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

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

        // 底部面板 - 快递员和管理员入口
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton courierButton = new JButton("快递员登录");
        bottomPanel.add(courierButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // 取件按钮事件
        pickupButton.addActionListener(e -> handlePickup());

        // 快递员登录按钮事件
        courierButton.addActionListener(e -> showCourierLogin());
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
            // 检查是否有多个快递
            int count = mainFrame.getLockerManager().getUnpickedCount(phone);
            if (count > 1) {
                JOptionPane.showMessageDialog(this,
                        String.format("您有%d个快递待取，请确认取件码", count));
            }

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

    private void showCourierLogin() {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "快递员登录", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // 账号输入
        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("账号："), gbc);
        JTextField idField = new JTextField(15);
        gbc.gridx = 1;
        dialog.add(idField, gbc);

        // 密码输入
        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(new JLabel("密码："), gbc);
        JPasswordField pwdField = new JPasswordField(15);
        gbc.gridx = 1;
        dialog.add(pwdField, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton loginButton = new JButton("登录");
        JButton registerButton = new JButton("注册");
        JButton adminButton = new JButton("管理员登录");
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        buttonPanel.add(adminButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        // 登录按钮事件
        loginButton.addActionListener(e -> {
            String id = idField.getText().trim();
            String password = new String(pwdField.getPassword());

            if (id.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请输入账号和密码");
                return;
            }

            Courier courier = mainFrame.getLockerManager().courierLogin(id, password);
            if (courier != null) {
                dialog.dispose();
                mainFrame.showCourier(courier);
            } else {
                JOptionPane.showMessageDialog(dialog, "账号或密码错误");
            }
        });

        // 注册按钮事件
        registerButton.addActionListener(e -> {
            dialog.dispose();
            showRegisterDialog();
        });

        // 管理员登录按钮事件
        adminButton.addActionListener(e -> {
            dialog.dispose();
            showAdminLogin();
        });

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showAdminLogin() {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "管理员登录", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // 账号输入
        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("账号："), gbc);
        JTextField usernameField = new JTextField(15);
        gbc.gridx = 1;
        dialog.add(usernameField, gbc);

        // 密码输入
        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(new JLabel("密码："), gbc);
        JPasswordField passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        dialog.add(passwordField, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton loginButton = new JButton("登录");
        JButton cancelButton = new JButton("取消");
        buttonPanel.add(loginButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        // 登录按钮事件
        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请输入账号和密码");
                return;
            }

            if (mainFrame.getLockerManager().validateAdminLogin(username, password)) {
                dialog.dispose();
                mainFrame.showAdmin();
            } else {
                JOptionPane.showMessageDialog(dialog, "账号或密码错误");
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showRegisterDialog() {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "快递员注册", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // 添加输入字段
        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(new JLabel("姓名："), gbc);
        JTextField nameField = new JTextField(15);
        gbc.gridx = 1;
        dialog.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(new JLabel("手机号："), gbc);
        JTextField phoneField = new JTextField(15);
        gbc.gridx = 1;
        dialog.add(phoneField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(new JLabel("密码："), gbc);
        JPasswordField pwdField = new JPasswordField(15);
        gbc.gridx = 1;
        dialog.add(pwdField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        dialog.add(new JLabel("确认密码："), gbc);
        JPasswordField confirmPwdField = new JPasswordField(15);
        gbc.gridx = 1;
        dialog.add(confirmPwdField, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        JButton submitButton = new JButton("提交申请");
        JButton cancelButton = new JButton("取消");
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        // 提交按钮事件
        submitButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String pwd = new String(pwdField.getPassword());
            String confirmPwd = new String(confirmPwdField.getPassword());

            if (name.isEmpty() || phone.isEmpty() || pwd.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请填写所有信息");
                return;
            }
            if (!phone.matches("\\d{11}")) {
                JOptionPane.showMessageDialog(dialog, "请输入正确的手机号");
                return;
            }
            if (!pwd.equals(confirmPwd)) {
                JOptionPane.showMessageDialog(dialog, "两次输入的密码不一致");
                return;
            }

            // 提交注册申请
            try {
                if (mainFrame.getLockerManager().submitCourierRegistration(name, phone, pwd)) {
                    JOptionPane.showMessageDialog(dialog, "注册申请已提交，请等待管理员审核");
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "注册申请提交失败，该手机号可能已被注册");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "注册申请提交失败：" + ex.getMessage());
            }
        });

        // 取消按钮事件
        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }
}
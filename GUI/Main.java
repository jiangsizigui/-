import javax.swing.SwingUtilities;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        // 确保数据目录存在
        new File("data").mkdirs();

        // 启动GUI
        SwingUtilities.invokeLater(() -> {
            DeliveryLockerGUI gui = new DeliveryLockerGUI();
            gui.setVisible(true);
        });
    }}



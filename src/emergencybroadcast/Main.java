package emergencybroadcast;

import emergencybroadcast.ui.UITheme;

import javax.swing.SwingUtilities;

/**
 * Main Application Launcher for Emergency Broadcast System.
 * System: Java Swing + UDP Multicast + MySQL (XAMPP)
 */
public class Main {

    public static void main(String[] args) {
        // Áp dụng giao diện dark theme toàn cục
        UITheme.applyGlobalDefaults();

        // Đẩy khởi tạo giao diện Swing LoginFrame lên Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            }
        });
    }
}

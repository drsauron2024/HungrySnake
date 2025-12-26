import javax.swing.*;

public class SnakeGameLauncher {
    public static void main(String[] args) {
        // 如果没有参数或第一个参数不是--console，启动GUI版本
        boolean useConsole = false;

        for (String arg : args) {
            if ("--console".equals(arg)) {
                useConsole = true;
                break;
            }
        }

        if (useConsole) {
            System.out.println("启动控制台版本...");
            try {
                // 直接调用控制台版本的main方法
                String[] mainArgs = {};
                Main.main(mainArgs);
            } catch (Exception e) {
                System.err.println("控制台版本启动失败: " + e.getMessage());
                e.printStackTrace();

                // 失败后尝试启动GUI版本
                System.out.println("尝试启动GUI版本...");
                startGUI();
            }
        } else {
            System.out.println("启动GUI版本...");
            startGUI();
        }
    }

    private static void startGUI() {
        // 在事件调度线程中启动GUI
        SwingUtilities.invokeLater(() -> {
            try {
                // 使用系统外观
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                // 创建并显示GUI
                SnakeGameGUI frame = new SnakeGameGUI();
                frame.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "启动GUI失败: " + e.getMessage() +
                                "\n请确保所有类都已正确编译。",
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}
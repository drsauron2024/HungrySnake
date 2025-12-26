import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BeautifulSnakeGame extends JFrame {
    // 颜色主题
    private static final Color DARK_BG = new Color(25, 25, 35);      // 深色背景
    private static final Color LIGHT_BG = new Color(40, 40, 50);     // 浅色背景
    private static final Color ACCENT_COLOR = new Color(0, 184, 148); // 主色调
    private static final Color TEXT_COLOR = new Color(220, 220, 220); // 文字颜色
    private static final Color PANEL_BG = new Color(35, 35, 45);     // 面板背景

    // 游戏设置
    private final int CELL_SIZE = 32;
    private final int WORLD_WIDTH = 20;
    private final int WORLD_HEIGHT = 15;

    // UI组件
    private GamePanel gamePanel;
    private JPanel statsPanel;
    private JPanel controlPanel;
    private JLabel scoreLabel;
    private JLabel lengthLabel;
    private JLabel timeLabel;
    private JLabel statusLabel;
    private JLabel foodLabel;
    private JLabel mapLabel;
    private JButton startButton;
    private JButton pauseButton;
    private JButton resetButton;
    private JButton helpButton;

    // 游戏逻辑
    private WorldManager worldManager;
    private ScoreManager scoreManager;
    private RuleEngine ruleEngine;
    private GameLoop gameLoop;
    private ScheduledExecutorService scheduler;

    private long startTime;
    private volatile Direction nextDirection = null;

    public BeautifulSnakeGame() {
        initUI();
        initGame();
        setupListeners();
        setupKeyboard();

        setVisible(true);
    }

    private void initUI() {
        setTitle("🐍 炫彩贪吃蛇 🎮");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 设置窗口图标（如果有的话）
        try {
            // ImageIcon icon = new ImageIcon("snake_icon.png");
            // setIconImage(icon.getImage());
        } catch (Exception e) {
            // 忽略图标加载错误
        }

        // 设置主布局
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(DARK_BG);

        // 创建游戏面板
        createGamePanel();

        // 创建右侧面板（统计信息）
        createStatsPanel();

        // 创建底部控制面板
        createControlPanel();

        // 创建顶部标题
        createTitlePanel();

        // 窗口设置
        pack();
        setLocationRelativeTo(null);
        setResizable(false);

        // 美化窗口边框
        ((JComponent) getContentPane()).setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );
    }

    private void createTitlePanel() {
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(DARK_BG);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // 创建渐变标题
        JLabel titleLabel = new JLabel("✨ 炫彩贪吃蛇 ✨");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 28));
        titleLabel.setForeground(new Color(255, 255, 255));

        // 添加发光效果
        titleLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 184, 148, 100), 2),
                BorderFactory.createEmptyBorder(5, 20, 5, 20)
        ));

        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);
    }

    private void createGamePanel() {
        gamePanel = new GamePanel();
        gamePanel.setPreferredSize(new Dimension(WORLD_WIDTH * CELL_SIZE, WORLD_HEIGHT * CELL_SIZE));
        gamePanel.setBackground(new Color(20, 20, 30));

        // 添加边框和阴影效果
        gamePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 80), 3),
                BorderFactory.createLineBorder(new Color(40, 40, 60), 1)
        ));

        // 添加鼠标点击获取焦点
        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                gamePanel.requestFocus();
                gamePanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_COLOR, 3),
                        BorderFactory.createLineBorder(new Color(40, 40, 60), 1)
                ));
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        SwingUtilities.invokeLater(() -> gamePanel.setBorder(
                                BorderFactory.createCompoundBorder(
                                        BorderFactory.createLineBorder(new Color(60, 60, 80), 3),
                                        BorderFactory.createLineBorder(new Color(40, 40, 60), 1)
                                )
                        ));
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
        });

        add(gamePanel, BorderLayout.CENTER);
    }

    private void createStatsPanel() {
        statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBackground(PANEL_BG);
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(60, 60, 80), 2, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        statsPanel.setPreferredSize(new Dimension(220, 0));

        // 标题
        JLabel statsTitle = new JLabel("📊 游戏统计");
        statsTitle.setFont(new Font("微软雅黑", Font.BOLD, 18));
        statsTitle.setForeground(ACCENT_COLOR);
        statsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsPanel.add(statsTitle);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // 分数显示
        scoreLabel = createStatItem("🎯 当前分数", "0");
        statsPanel.add(scoreLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // 蛇长度
        lengthLabel = createStatItem("🐍 蛇长度", "3");
        statsPanel.add(lengthLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // 时间
        timeLabel = createStatItem("⏰ 游戏时间", "00:00");
        statsPanel.add(timeLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // 状态
        statusLabel = createStatItem("📈 游戏状态", "准备开始");
        statsPanel.add(statusLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // 食物刷新
        foodLabel = createStatItem("🍎 食物刷新", "10秒");
        statsPanel.add(foodLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // 地图刷新
        mapLabel = createStatItem("🗺️ 地图刷新", "20秒");
        statsPanel.add(mapLabel);

        // 添加图例
        statsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        addLegend(statsPanel);

        add(statsPanel, BorderLayout.EAST);
    }

    private JLabel createStatItem(String title, String value) {
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setBackground(PANEL_BG);
        itemPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(180, 180, 200));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        itemPanel.add(titleLabel, BorderLayout.WEST);
        itemPanel.add(valueLabel, BorderLayout.EAST);

        return new JLabel() {
            @Override
            public Component add(Component comp) {
                return itemPanel.add(comp);
            }
        };
    }

    private void addLegend(JPanel panel) {
        JLabel legendTitle = new JLabel("🎨 图例说明");
        legendTitle.setFont(new Font("微软雅黑", Font.BOLD, 16));
        legendTitle.setForeground(ACCENT_COLOR);
        legendTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(legendTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        String[] legendItems = {
                "🐍 蛇头 - 红色带眼睛",
                "🟢 蛇身 - 绿色方块",
                "🍎 普通食物 - 绿色圆圈 (+1分)",
                "⭐ 特殊食物 - 黄色圆圈 (连击)",
                "💎 稀有食物 - 紫色圆圈 (高连击)",
                "🧱 障碍物 - 蓝色方块",
                "⬛ 空地 - 黑色格子"
        };

        for (String item : legendItems) {
            JLabel itemLabel = new JLabel(item);
            itemLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            itemLabel.setForeground(TEXT_COLOR);
            itemLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(itemLabel);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
    }

    private void createControlPanel() {
        controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(1, 4, 10, 0));
        controlPanel.setBackground(PANEL_BG);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 创建漂亮的按钮
        startButton = createStyledButton("🚀 开始游戏", ACCENT_COLOR);
        pauseButton = createStyledButton("⏸️ 暂停游戏", new Color(255, 193, 7));
        resetButton = createStyledButton("🔄 重新开始", new Color(233, 30, 99));
        helpButton = createStyledButton("❓ 游戏帮助", new Color(33, 150, 243));

        controlPanel.add(startButton);
        controlPanel.add(pauseButton);
        controlPanel.add(resetButton);
        controlPanel.add(helpButton);

        add(controlPanel, BorderLayout.SOUTH);
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 绘制渐变背景
                GradientPaint gradient = new GradientPaint(
                        0, 0, color,
                        0, getHeight(), color.darker()
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // 绘制边框
                g2.setColor(color.brighter());
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 15, 15);

                // 绘制文字
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("微软雅黑", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(120, 40);
            }
        };

        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);

        // 添加鼠标悬停效果
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.repaint();
            }
        });

        return button;
    }

    private void initGame() {
        worldManager = new WorldManager(WORLD_WIDTH, WORLD_HEIGHT);
        scoreManager = new ScoreManager();
        ruleEngine = new RuleEngine();

        worldManager.initializeGame();
        World world = worldManager.getWorld();
        FoodSpawner foodSpawner = worldManager.getFoodSpawner();

        gameLoop = new GameLoop(world, ruleEngine, scoreManager, foodSpawner);

        updateDisplay();
    }

    private void setupListeners() {
        startButton.addActionListener(e -> startGame());
        pauseButton.addActionListener(e -> togglePause());
        resetButton.addActionListener(e -> resetGame());
        helpButton.addActionListener(e -> showHelp());

        // 时间更新定时器
        Timer timer = new Timer(1000, e -> updateTime());
        timer.start();
    }

    private void setupKeyboard() {
        // 使用Key Bindings（Swing推荐的方式）
        JPanel contentPane = (JPanel) getContentPane();

        // 方向键控制
        bindKey("LEFT", KeyEvent.VK_LEFT, () -> setDirection(Direction.LEFT));
        bindKey("A", KeyEvent.VK_A, () -> setDirection(Direction.LEFT));

        bindKey("RIGHT", KeyEvent.VK_RIGHT, () -> setDirection(Direction.RIGHT));
        bindKey("D", KeyEvent.VK_D, () -> setDirection(Direction.RIGHT));

        bindKey("UP", KeyEvent.VK_UP, () -> setDirection(Direction.UP));
        bindKey("W", KeyEvent.VK_W, () -> setDirection(Direction.UP));

        bindKey("DOWN", KeyEvent.VK_DOWN, () -> setDirection(Direction.DOWN));
        bindKey("S", KeyEvent.VK_S, () -> setDirection(Direction.DOWN));

        // 空格键暂停
        bindKey("SPACE", KeyEvent.VK_SPACE, this::togglePause);

        // 确保游戏面板可以获得焦点
        gamePanel.setFocusable(true);
        gamePanel.requestFocus();
    }

    private void bindKey(String name, int keyCode, Runnable action) {
        JPanel contentPane = (JPanel) getContentPane();
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, 0);

        contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, name);
        contentPane.getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    // 游戏逻辑方法（保持原有逻辑）
    private void setDirection(Direction dir) {
        if (!gameLoop.isRunning() || gameLoop.isPaused()) {
            return;
        }

        Snake snake = worldManager.getWorld().getSnake();
        if (snake == null) return;

        Direction currentDir = snake.getCurrentDirection();
        if (!currentDir.isOpposite(dir)) {
            snake.changeDirection(dir);
            gamePanel.repaint();
        }
    }

    private void startGame() {
        if (!gameLoop.isRunning()) {
            startTime = System.currentTimeMillis();
            gameLoop.start();
            gameLoop.resume();

            // 确保焦点
            gamePanel.requestFocus();

            // 创建游戏循环
            scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(() -> {
                if (!gameLoop.isRunning() || gameLoop.isPaused()) {
                    return;
                }

                SwingUtilities.invokeLater(() -> {
                    boolean stillRunning = gameLoop.tick();
                    updateDisplay();

                    if (ruleEngine.isGameOver()) {
                        gameOver();
                    }
                });
            }, 0, 500, TimeUnit.MILLISECONDS);

            statusLabel.setText("游戏中");
            startButton.setEnabled(false);
            pauseButton.setText("⏸️ 暂停游戏");

            // 显示开始提示
            showMessage("游戏开始！", "使用方向键或WASD控制蛇的移动\n空格键：暂停/继续");
        }
    }

    private void togglePause() {
        if (gameLoop.isRunning()) {
            if (!gameLoop.isPaused()) {
                gameLoop.pause();
                statusLabel.setText("已暂停");
                pauseButton.setText("▶️ 继续游戏");
                showMessage("游戏暂停", "点击继续按钮或按空格键继续");
            } else {
                gameLoop.resume();
                statusLabel.setText("游戏中");
                pauseButton.setText("⏸️ 暂停游戏");
                gamePanel.requestFocus();
            }
        }
    }

    private void resetGame() {
        if (scheduler != null) {
            scheduler.shutdown();
        }

        initGame();
        scoreLabel.setText("0");
        lengthLabel.setText("3");
        timeLabel.setText("00:00");
        statusLabel.setText("准备开始");
        startButton.setEnabled(true);
        pauseButton.setText("⏸️ 暂停游戏");

        gamePanel.repaint();
        showMessage("游戏重置", "准备开始新游戏！");
    }

    private void showHelp() {
        String helpText = """
            🎮 游戏控制：
            • 方向键 或 WASD：控制蛇移动
            • 空格键：暂停/继续游戏
            
            🍎 食物类型：
            • 绿色食物：+1分，蛇长+1
            • 黄色食物：连击计分，蛇长+2
            • 紫色食物：高连击计分，蛇长+3
            
            ⚡ 游戏特性：
            • 每10秒刷新食物
            • 每20秒刷新地图
            • 连击吃同类型食物分数更高
            
            🎯 游戏目标：
            • 获得尽可能高的分数
            • 避免撞墙、障碍物和自己
            
            祝您游戏愉快！ 🐍✨
            """;

        showMessage("游戏帮助", helpText);
    }

    private void gameOver() {
        if (scheduler != null) {
            scheduler.shutdown();
        }

        String message = "🎮 游戏结束！\n\n" +
                "💥 原因：" + ruleEngine.getGameOverReason() + "\n" +
                "🏆 最终分数：" + scoreManager.getScore() + "\n" +
                "🐍 蛇长度：" + worldManager.getWorld().getSnake().getLength() + "\n\n" +
                "点击\"重新开始\"按钮再来一局！";

        showMessage("游戏结束", message);
        statusLabel.setText("游戏结束");
        startButton.setEnabled(false);
    }

    private void updateDisplay() {
        World world = worldManager.getWorld();

        scoreLabel.setText(String.valueOf(scoreManager.getScore()));
        lengthLabel.setText(String.valueOf(world.getSnake() != null ? world.getSnake().getLength() : 0));

        gamePanel.repaint();
    }

    private void updateTime() {
        if (gameLoop.isRunning() && !gameLoop.isPaused()) {
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            long minutes = elapsed / 60;
            long seconds = elapsed % 60;
            timeLabel.setText(String.format("%02d:%02d", minutes, seconds));

            long foodTime = gameLoop.getTimeUntilNextFoodRefresh() / 1000;
            long mapTime = gameLoop.getTimeUntilNextMapRefresh() / 1000;

            foodLabel.setText(foodTime + "秒");
            mapLabel.setText(mapTime + "秒");
        }
    }

    private void showMessage(String title, String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message, title,
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void showFocusHint() {
        gamePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 3),
                BorderFactory.createLineBorder(new Color(40, 40, 60), 1)
        ));

        new Thread(() -> {
            try {
                Thread.sleep(2000);
                SwingUtilities.invokeLater(() -> gamePanel.setBorder(
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(60, 60, 80), 3),
                                BorderFactory.createLineBorder(new Color(40, 40, 60), 1)
                        )
                ));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    // 内部类：游戏面板
    class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            World world = worldManager.getWorld();
            if (world == null) return;

            // 绘制渐变背景
            GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(15, 15, 25),
                    0, getHeight(), new Color(25, 25, 35)
            );
            g2.setPaint(gradient);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // 绘制网格线
            g2.setColor(new Color(40, 40, 50));
            for (int x = 0; x <= WORLD_WIDTH; x++) {
                g2.drawLine(x * CELL_SIZE, 0, x * CELL_SIZE, WORLD_HEIGHT * CELL_SIZE);
            }
            for (int y = 0; y <= WORLD_HEIGHT; y++) {
                g2.drawLine(0, y * CELL_SIZE, WORLD_WIDTH * CELL_SIZE, y * CELL_SIZE);
            }

            // 绘制障碍物（带阴影效果）
            Obstacles obstacles = world.getObstacles();
            if (obstacles != null) {
                g2.setColor(new Color(80, 120, 255));
                for (Point p : obstacles.getAllCells()) {
                    int x = p.x * CELL_SIZE;
                    int y = p.y * CELL_SIZE;

                    // 阴影
                    g2.setColor(new Color(60, 100, 235));
                    g2.fillRect(x + 2, y + 2, CELL_SIZE, CELL_SIZE);

                    // 主体
                    g2.setColor(new Color(100, 140, 255));
                    g2.fillRect(x, y, CELL_SIZE, CELL_SIZE);

                    // 高光
                    g2.setColor(new Color(140, 180, 255));
                    g2.fillRect(x, y, CELL_SIZE, 3);
                    g2.fillRect(x, y, 3, CELL_SIZE);
                }
            }

            // 绘制食物（带发光效果）
            for (Food food : world.getFoods()) {
                Point pos = food.getPosition();
                int x = pos.x * CELL_SIZE;
                int y = pos.y * CELL_SIZE;

                Color foodColor;
                switch (food.getType()) {
                    case NORMAL:
                        foodColor = new Color(100, 255, 100);
                        break;
                    case SPECIAL:
                        foodColor = new Color(255, 255, 100);
                        break;
                    case RARE:
                        foodColor = new Color(255, 100, 255);
                        break;
                    default:
                        foodColor = Color.GREEN;
                }

                // 发光效果
                g2.setColor(new Color(foodColor.getRed(), foodColor.getGreen(), foodColor.getBlue(), 100));
                g2.fillOval(x - 3, y - 3, CELL_SIZE + 6, CELL_SIZE + 6);

                // 食物主体
                g2.setColor(foodColor);
                g2.fillOval(x + 4, y + 4, CELL_SIZE - 8, CELL_SIZE - 8);

                // 高光
                g2.setColor(foodColor.brighter());
                g2.fillOval(x + 6, y + 6, CELL_SIZE / 4, CELL_SIZE / 4);
            }

            // 绘制蛇
            Snake snake = world.getSnake();
            if (snake != null) {
                boolean isHead = true;
                for (Point p : snake.getBody()) {
                    int x = p.x * CELL_SIZE;
                    int y = p.y * CELL_SIZE;

                    if (isHead) {
                        // 蛇头 - 带渐变效果
                        GradientPaint headGradient = new GradientPaint(
                                x, y, new Color(255, 100, 100),
                                x + CELL_SIZE, y + CELL_SIZE, new Color(200, 50, 50)
                        );
                        g2.setPaint(headGradient);
                        g2.fillRoundRect(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4, 10, 10);

                        // 蛇头边框
                        g2.setColor(new Color(255, 150, 150));
                        g2.setStroke(new BasicStroke(2));
                        g2.drawRoundRect(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4, 10, 10);

                        // 眼睛（根据方向）
                        g2.setColor(Color.WHITE);
                        Direction dir = snake.getCurrentDirection();
                        int eyeSize = CELL_SIZE / 5;

                        if (dir == Direction.UP || dir == Direction.DOWN) {
                            g2.fillOval(x + CELL_SIZE/3, y + CELL_SIZE/3, eyeSize, eyeSize);
                            g2.fillOval(x + 2*CELL_SIZE/3 - eyeSize, y + CELL_SIZE/3, eyeSize, eyeSize);
                        } else {
                            g2.fillOval(x + CELL_SIZE/3, y + CELL_SIZE/3, eyeSize, eyeSize);
                            g2.fillOval(x + CELL_SIZE/3, y + 2*CELL_SIZE/3 - eyeSize, eyeSize, eyeSize);
                        }

                        // 瞳孔
                        g2.setColor(Color.BLACK);
                        g2.fillOval(x + CELL_SIZE/3 + 1, y + CELL_SIZE/3 + 1, eyeSize - 2, eyeSize - 2);
                        g2.fillOval(x + 2*CELL_SIZE/3 - eyeSize + 1, y + CELL_SIZE/3 + 1, eyeSize - 2, eyeSize - 2);

                        isHead = false;
                    } else {
                        // 蛇身 - 带渐变效果
                        GradientPaint bodyGradient = new GradientPaint(
                                x, y, new Color(100, 255, 100),
                                x + CELL_SIZE, y + CELL_SIZE, new Color(50, 200, 50)
                        );
                        g2.setPaint(bodyGradient);
                        g2.fillRoundRect(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4, 8, 8);

                        // 蛇身纹理
                        g2.setColor(new Color(50, 150, 50, 100));
                        g2.fillRect(x + 4, y + 4, CELL_SIZE - 8, CELL_SIZE - 8);
                    }
                }
            }

            // 绘制边框
            g2.setColor(new Color(60, 60, 80));
            g2.setStroke(new BasicStroke(3));
            g2.drawRect(0, 0, WORLD_WIDTH * CELL_SIZE - 1, WORLD_HEIGHT * CELL_SIZE - 1);
        }
    }

    public static void main(String[] args) {
        // 使用系统外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 启动游戏
        SwingUtilities.invokeLater(() -> {
            BeautifulSnakeGame game = new BeautifulSnakeGame();
            game.setVisible(true);
        });
    }
}
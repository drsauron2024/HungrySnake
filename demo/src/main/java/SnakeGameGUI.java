import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SnakeGameGUI extends JFrame {
    private final int CELL_SIZE = 30;
    private final int WORLD_WIDTH = 20;
    private final int WORLD_HEIGHT = 15;

    private GamePanel gamePanel;
    private JPanel controlPanel;
    private JLabel scoreLabel;
    private JLabel lengthLabel;
    private JLabel timeLabel;
    private JLabel statusLabel;
    private JButton startButton;
    private JButton pauseButton;
    private JButton resetButton;

    private WorldManager worldManager;
    private ScoreManager scoreManager;
    private RuleEngine ruleEngine;
    private GameLoop gameLoop;
    private ScheduledExecutorService scheduler;

    private long startTime;
    private volatile Direction nextDirection;

    public SnakeGameGUI() {
        initComponents();
        initGame();
        setupListeners();

        // 确保游戏面板获得焦点
        gamePanel.requestFocusInWindow();
        System.out.println("GUI初始化完成，焦点设置...");
    }

    private void initComponents() {
        setTitle("贪吃蛇游戏");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 游戏面板
        gamePanel = new GamePanel();
        gamePanel.setPreferredSize(new Dimension(WORLD_WIDTH * CELL_SIZE, WORLD_HEIGHT * CELL_SIZE));
        gamePanel.setBackground(Color.BLACK);

        // 控制面板
        controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(1, 6, 10, 10));

        scoreLabel = new JLabel("分数: 0");
        lengthLabel = new JLabel("长度: 3");
        timeLabel = new JLabel("时间: 00:00");
        statusLabel = new JLabel("状态: 准备开始");

        startButton = new JButton("开始游戏");
        pauseButton = new JButton("暂停");
        resetButton = new JButton("重置");

        controlPanel.add(scoreLabel);
        controlPanel.add(lengthLabel);
        controlPanel.add(timeLabel);
        controlPanel.add(statusLabel);
        controlPanel.add(startButton);
        controlPanel.add(pauseButton);
        controlPanel.add(resetButton);

        add(gamePanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);

        // 键盘监听
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });
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

        // 时间更新定时器
        Timer timer = new Timer(1000, e -> updateTime());
        timer.start();
    }

    private void handleKeyPress(KeyEvent e) {
        if (!gameLoop.isRunning() || gameLoop.isPaused()) {
            return;
        }

        Direction currentDir = worldManager.getWorld().getSnake().getCurrentDirection();

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                if (currentDir != Direction.RIGHT) {
                    nextDirection = Direction.LEFT;
                }
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                if (currentDir != Direction.LEFT) {
                    nextDirection = Direction.RIGHT;
                }
                break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                if (currentDir != Direction.DOWN) {
                    nextDirection = Direction.UP;
                }
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                if (currentDir != Direction.UP) {
                    nextDirection = Direction.DOWN;
                }
                break;
            case KeyEvent.VK_SPACE:
                togglePause();
                break;
        }
    }

    private void startGame() {
        if (!gameLoop.isRunning()) {
            startTime = System.currentTimeMillis();
            gameLoop.start();
            gameLoop.resume();

            // 创建游戏循环
            scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(() -> {
                if (!gameLoop.isRunning() || gameLoop.isPaused()) {
                    return;
                }

                // 应用方向输入
                if (nextDirection != null) {
                    worldManager.getWorld().getSnake().changeDirection(nextDirection);
                    nextDirection = null;
                }

                // 执行游戏逻辑
                SwingUtilities.invokeLater(() -> {
                    gameLoop.tick();
                    updateDisplay();

                    if (ruleEngine.isGameOver()) {
                        gameOver();
                    }
                });
            }, 0, 500, TimeUnit.MILLISECONDS); // 每0.5秒更新一次

            statusLabel.setText("状态: 游戏中");
            startButton.setEnabled(false);
            pauseButton.setText("暂停");
        }
    }

    private void togglePause() {
        if (gameLoop.isRunning()) {
            if (!gameLoop.isPaused()) {
                gameLoop.pause();
                statusLabel.setText("状态: 已暂停");
                pauseButton.setText("继续");
            } else {
                gameLoop.resume();
                statusLabel.setText("状态: 游戏中");
                pauseButton.setText("暂停");
            }
        }
    }

    private void resetGame() {
        // 停止当前游戏
        if (scheduler != null) {
            scheduler.shutdown();
        }

        // 重置游戏状态
        initGame();
        scoreLabel.setText("分数: 0");
        lengthLabel.setText("长度: 3");
        timeLabel.setText("时间: 00:00");
        statusLabel.setText("状态: 准备开始");
        startButton.setEnabled(true);
        pauseButton.setText("暂停");

        gamePanel.repaint();
    }

    private void gameOver() {
        if (scheduler != null) {
            scheduler.shutdown();
        }

        String message = "游戏结束!\n" +
                "原因: " + ruleEngine.getGameOverReason() + "\n" +
                "最终分数: " + scoreManager.getScore() + "\n" +
                "蛇长度: " + worldManager.getWorld().getSnake().getLength();

        JOptionPane.showMessageDialog(this, message, "游戏结束", JOptionPane.INFORMATION_MESSAGE);
        statusLabel.setText("状态: 游戏结束");
        startButton.setEnabled(false);
    }

    private void updateDisplay() {
        World world = worldManager.getWorld();

        scoreLabel.setText("分数: " + scoreManager.getScore());
        lengthLabel.setText("长度: " + (world.getSnake() != null ? world.getSnake().getLength() : 0));

        gamePanel.repaint();
    }

    private void updateTime() {
        if (gameLoop.isRunning() && !gameLoop.isPaused()) {
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            long minutes = elapsed / 60;
            long seconds = elapsed % 60;
            timeLabel.setText(String.format("时间: %02d:%02d", minutes, seconds));

            // 显示刷新倒计时
            long foodTime = gameLoop.getTimeUntilNextFoodRefresh() / 1000;
            long mapTime = gameLoop.getTimeUntilNextMapRefresh() / 1000;
            statusLabel.setText(String.format("食物刷新: %ds 地图刷新: %ds", foodTime, mapTime));
        }
    }

    // 内部类：游戏绘制面板
    class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            World world = worldManager.getWorld();
            if (world == null) return;

            // 绘制网格背景
            g.setColor(new Color(30, 30, 30));
            g.fillRect(0, 0, getWidth(), getHeight());

            // 绘制网格线
            g.setColor(new Color(50, 50, 50));
            for (int x = 0; x <= WORLD_WIDTH; x++) {
                g.drawLine(x * CELL_SIZE, 0, x * CELL_SIZE, WORLD_HEIGHT * CELL_SIZE);
            }
            for (int y = 0; y <= WORLD_HEIGHT; y++) {
                g.drawLine(0, y * CELL_SIZE, WORLD_WIDTH * CELL_SIZE, y * CELL_SIZE);
            }

            // 绘制障碍物
            Obstacles obstacles = world.getObstacles();
            if (obstacles != null) {
                g.setColor(new Color(100, 100, 255));
                for (Point p : obstacles.getAllCells()) {
                    int x = p.x * CELL_SIZE;
                    int y = p.y * CELL_SIZE;
                    g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                    g.setColor(Color.WHITE);
                    g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
                    g.setColor(new Color(100, 100, 255));
                }
            }

            // 绘制食物
            for (Food food : world.getFoods()) {
                Point pos = food.getPosition();
                int x = pos.x * CELL_SIZE;
                int y = pos.y * CELL_SIZE;

                switch (food.getType()) {
                    case NORMAL:
                        g.setColor(Color.GREEN);
                        g.fillOval(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4);
                        break;
                    case SPECIAL:
                        g.setColor(Color.YELLOW);
                        g.fillOval(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4);
                        break;
                    case RARE:
                        g.setColor(Color.MAGENTA);
                        g.fillOval(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4);
                        break;
                }

                // 食物边框
                g.setColor(Color.WHITE);
                g.drawOval(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4);
            }

            // 绘制蛇
            Snake snake = world.getSnake();
            if (snake != null) {
                boolean isHead = true;
                for (Point p : snake.getBody()) {
                    int x = p.x * CELL_SIZE;
                    int y = p.y * CELL_SIZE;

                    if (isHead) {
                        // 蛇头
                        g.setColor(Color.RED);
                        g.fillOval(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);

                        // 绘制眼睛（根据方向）
                        g.setColor(Color.WHITE);
                        Direction dir = snake.getCurrentDirection();
                        int eyeSize = CELL_SIZE / 4;

                        if (dir == Direction.UP || dir == Direction.DOWN) {
                            g.fillOval(x + CELL_SIZE/4, y + CELL_SIZE/4, eyeSize, eyeSize);
                            g.fillOval(x + 3*CELL_SIZE/4 - eyeSize, y + CELL_SIZE/4, eyeSize, eyeSize);
                        } else {
                            g.fillOval(x + CELL_SIZE/4, y + CELL_SIZE/4, eyeSize, eyeSize);
                            g.fillOval(x + CELL_SIZE/4, y + 3*CELL_SIZE/4 - eyeSize, eyeSize, eyeSize);
                        }

                        isHead = false;
                    } else {
                        // 蛇身
                        g.setColor(Color.GREEN);
                        g.fillRect(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4);

                        // 蛇身细节
                        g.setColor(new Color(0, 150, 0));
                        g.fillRect(x + 4, y + 4, CELL_SIZE - 8, CELL_SIZE - 8);
                    }
                }
            }

            // 绘制边界
            g.setColor(Color.GRAY);
            g.drawRect(0, 0, WORLD_WIDTH * CELL_SIZE - 1, WORLD_HEIGHT * CELL_SIZE - 1);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            SnakeGameGUI game = new SnakeGameGUI();
            game.setVisible(true);
        });
    }
}
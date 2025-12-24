import java.awt.Point;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    private static volatile char nextDirectionInput = '\0'; // 用户输入的方向
    private static volatile boolean quit = false;
    
    public static void main(String[] args) throws Exception {
        System.out.println("========== 贪吃蛇游戏 ==========");
        
        // 1. 初始化游戏管理器
        int width = 20;
        int height = 15;
        WorldManager worldManager = new WorldManager(width, height);
        
        // 2. 初始化其他管理器
        ScoreManager scoreManager = new ScoreManager();
        RuleEngine ruleEngine = new RuleEngine();
        
        // 3. 初始化并配置游戏世界
        worldManager.initializeGame();
        World world = worldManager.getWorld();
        FoodSpawner foodSpawner = worldManager.getFoodSpawner();
        
        // 4. 创建游戏循环（同步版本）
        GameLoop gameLoop = new GameLoop(world, ruleEngine, scoreManager, foodSpawner);
        
        // 5. 显示游戏说明
        printInstructions();
        
        System.out.println("\n初始地图:");
        printGameMap(world);

        System.out.println("\n按回车键开始游戏...");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

        // 6. 启动游戏
        System.out.println("\n游戏开始！");
        System.out.println("- 蛇每秒自动前进一格");
        System.out.println("- 按A/D键可以让蛇左转/右转");
        System.out.println("- 每5秒刷新食物，每10秒刷新地图");
        gameLoop.start();
        gameLoop.resume();
        
        // 7. 创建定时器，每秒自动移动一次
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            if (!gameLoop.isRunning() || gameLoop.isPaused() || quit) {
                return;
            }
            
            // 处理方向输入
            processDirectionInput(world);
            
            // 执行游戏tick（蛇移动并检查）
            boolean stillRunning = gameLoop.tick();
            if (!stillRunning) {
                System.out.println("\n游戏结束: " + ruleEngine.getGameOverReason());
                scheduler.shutdown();
            }
        }, 0, 1, TimeUnit.SECONDS); // 每秒执行一次
        
        // 8. 主控制循环（只处理用户输入）
        boolean gameActive = true;
        
        while (gameActive && !quit) {
            // 显示当前状态（每秒更新一次）
            displayStatus(gameLoop, world, scoreManager, ruleEngine);
            
            // 获取用户输入（非阻塞）
            if (System.in.available() > 0) {
                String input = scanner.nextLine().trim().toLowerCase();
                
                if (input.isEmpty()) {
                    continue;
                }
                
                char command = input.charAt(0);
                switch (command) {
                    case 'a':
                    case 'd':
                        // 记录方向输入，在下一次tick时处理
                        nextDirectionInput = command;
                        System.out.println("指令已接收: " + (command == 'a' ? "左转" : "右转"));
                        break;
                    case 'p':
                        if (!gameLoop.isPaused()) {
                            gameLoop.pause();
                            System.out.println("游戏已暂停");
                        }
                        break;
                    case 'r':
                        if (gameLoop.isPaused()) {
                            gameLoop.resume();
                            System.out.println("游戏继续");
                        }
                        break;
                    case 'm':
                        printGameMap(world);
                        break;
                    case 'q':
                        quit = true;
                        System.out.println("退出游戏");
                        scheduler.shutdown();
                        break;
                    default:
                        System.out.println("未知命令，请重新输入");
                        break;
                }
            }
            
            // 短暂休眠，避免CPU占用过高
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            
            // 检查游戏是否结束
            if (ruleEngine.isGameOver()) {
                gameActive = false;
                scheduler.shutdown();
            }
        }
        
        // 9. 等待定时器关闭
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 10. 游戏结束，显示最终结果
        scanner.close();
        printFinalResults(scoreManager, world);
        
        System.out.println("\n感谢游玩！");
    }
    
    /**
     * 处理方向输入
     */
    private static void processDirectionInput(World world) {
        if (nextDirectionInput != '\0') {
            Snake snake = world.getSnake();
            Direction currentDir = snake.getCurrentDirection();
            Direction newDir = currentDir;
            
            switch (nextDirectionInput) {
                case 'a': // 左转
                    if (currentDir == Direction.UP) {
                        newDir = Direction.LEFT;
                    } else if (currentDir == Direction.DOWN) {
                        newDir = Direction.RIGHT;
                    } else if (currentDir == Direction.LEFT) {
                        newDir = Direction.DOWN;
                    } else if (currentDir == Direction.RIGHT) {
                        newDir = Direction.UP;
                    }
                    break;
                case 'd': // 右转
                    if (currentDir == Direction.UP) {
                        newDir = Direction.RIGHT;
                    } else if (currentDir == Direction.DOWN) {
                        newDir = Direction.LEFT;
                    } else if (currentDir == Direction.LEFT) {
                        newDir = Direction.UP;
                    } else if (currentDir == Direction.RIGHT) {
                        newDir = Direction.DOWN;
                    }
                    break;
            }
            
            // 检查是否与当前方向相反（不允许直接反向）
            if (!currentDir.isOpposite(newDir)) {
                snake.changeDirection(newDir);
            }
            
            // 清空输入
            nextDirectionInput = '\0';
        }
    }
    
    /**
     * 显示游戏状态
     */
    private static void displayStatus(GameLoop gameLoop, World world, ScoreManager scoreManager, RuleEngine ruleEngine) {
        System.out.println("\n==================================");
        System.out.println("当前状态: " + (gameLoop.isPaused() ? "暂停" : "运行中"));
        System.out.println("分数: " + scoreManager.getScore());
        System.out.println("蛇长度: " + world.getSnake().getLength());
        System.out.println("当前方向: " + world.getSnake().getCurrentDirection());
        System.out.println("场上食物: " + world.getFoods().size() + "个");
        
        // 显示食物刷新倒计时
        long timeUntilFoodRefresh = gameLoop.getTimeUntilNextFoodRefresh();
        System.out.println("距离下次食物刷新: " + (timeUntilFoodRefresh / 1000) + "秒");
        
        // 显示地图刷新倒计时
        long timeUntilMapRefresh = gameLoop.getTimeUntilNextMapRefresh();
        System.out.println("距离下次地图刷新: " + (timeUntilMapRefresh / 1000) + "秒");
        
        // 显示小型地图
        printSimpleMap(world);
    }
    
    /**
     * 打印游戏说明
     */
    private static void printInstructions() {
        System.out.println("游戏说明:");
        System.out.println("1. 蛇每秒自动前进一格");
        System.out.println("2. 按A键左转，按D键右转（蛇不能直接反向）");
        System.out.println("3. 游戏开始时随机生成5个食物");
        System.out.println("4. 每5秒清空未吃掉的食物，重新生成5个新食物");
        System.out.println("5. 每10秒地图重新生成障碍物（避开蛇和食物）");
        System.out.println("6. 食物类型和计分规则:");
        System.out.println("   - 普通食物 (*): 每个加1分");
        System.out.println("   - 特殊食物 ($): 连续吃n个，得分 = 1² + 2² + ... + n²");
        System.out.println("   - 稀有食物 (&): 连续吃n个，得分 = 1³ + 2³ + ... + n³");
        System.out.println("7. 避免撞墙、撞到自己或障碍物");
        System.out.println("\n控制命令:");
        System.out.println("  a - 左转     d - 右转");
        System.out.println("  p - 暂停游戏   r - 继续游戏");
        System.out.println("  m - 显示详细地图   q - 退出游戏");
        System.out.println("\n注意: 蛇每秒自动移动，你需要在移动前决定转向！");
    }
    
    /**
     * 打印简单地图（只显示蛇周围区域）
     */
    private static void printSimpleMap(World world) {
        Snake snake = world.getSnake();
        if (snake == null) return;
        
        Point head = snake.getHead();
        int viewRadius = 5; // 显示蛇头周围5格
        
        int minX = Math.max(0, head.x - viewRadius);
        int maxX = Math.min(world.getWidth() - 1, head.x + viewRadius);
        int minY = Math.max(0, head.y - viewRadius);
        int maxY = Math.min(world.getHeight() - 1, head.y + viewRadius);
        
        System.out.println("\n当前视角 (蛇头周围" + viewRadius + "格):");
        System.out.print("  ");
        for (int x = minX; x <= maxX; x++) {
            System.out.print(x % 10);
        }
        System.out.println();
        
        for (int y = minY; y <= maxY; y++) {
            System.out.print(y % 10 + " ");
            for (int x = minX; x <= maxX; x++) {
                Point current = new Point(x, y);
                char symbol = '.';
                
                if (world.getSnake() != null) {
                    if (world.getSnake().getHead().equals(current)) {
                        // 根据方向显示不同的蛇头符号
                        Direction dir = snake.getCurrentDirection();
                        switch (dir) {
                            case UP: symbol = '↑'; break;
                            case DOWN: symbol = '↓'; break;
                            case LEFT: symbol = '←'; break;
                            case RIGHT: symbol = '→'; break;
                        }
                    } else if (world.getSnake().contains(current)) {
                        symbol = 'o'; // 蛇身
                    } else {
                        // 检查是否有食物在这个位置
                        Food food = world.getFoodAt(current);
                        if (food != null) {
                            switch (food.getType()) {
                                case NORMAL:
                                    symbol = '*';
                                    break;
                                case SPECIAL:
                                    symbol = '$';
                                    break;
                                case RARE:
                                    symbol = '&';
                                    break;
                            }
                        } else if (world.getObstacles() != null && world.getObstacles().getAllCells().contains(current)) {
                            symbol = '#'; // 障碍物
                        }
                    }
                }
                
                System.out.print(symbol);
            }
            System.out.println();
        }
        
        System.out.println("图例: ↑↓←→-蛇头(方向) o-蛇身 *-普通食物 $-特殊食物 &-稀有食物 #-障碍物");
    }
    
    /**
     * 打印完整游戏地图
     */
    private static void printGameMap(World world) {
        System.out.println("\n完整地图 (" + world.getWidth() + "x" + world.getHeight() + "):");
        System.out.print("  ");
        for (int x = 0; x < world.getWidth(); x++) {
            System.out.print(x % 10);
        }
        System.out.println();
        
        for (int y = 0; y < world.getHeight(); y++) {
            System.out.print(y % 10 + " ");
            for (int x = 0; x < world.getWidth(); x++) {
                Point current = new Point(x, y);
                char symbol = '.';
                
                if (!world.inBounds(current)) {
                    symbol = '#';
                } else if (world.getSnake() != null) {
                    Snake snake = world.getSnake();
                    if (snake.getHead().equals(current)) {
                        // 根据方向显示不同的蛇头符号
                        Direction dir = snake.getCurrentDirection();
                        switch (dir) {
                            case UP: symbol = '↑'; break;
                            case DOWN: symbol = '↓'; break;
                            case LEFT: symbol = '←'; break;
                            case RIGHT: symbol = '→'; break;
                        }
                    } else if (snake.contains(current)) {
                        symbol = 'o'; // 蛇身
                    } else {
                        // 检查是否有食物在这个位置
                        Food food = world.getFoodAt(current);
                        if (food != null) {
                            switch (food.getType()) {
                                case NORMAL:
                                    symbol = '*';
                                    break;
                                case SPECIAL:
                                    symbol = '$';
                                    break;
                                case RARE:
                                    symbol = '&';
                                    break;
                            }
                        } else if (world.getObstacles() != null && world.getObstacles().getAllCells().contains(current)) {
                            symbol = '#'; // 障碍物
                        }
                    }
                }
                
                System.out.print(symbol);
            }
            System.out.println();
        }
        
        System.out.println("\n图例: ↑↓←→-蛇头(方向) o-蛇身 *-普通食物 $-特殊食物 &-稀有食物 #-障碍物");
    }
    
    /**
     * 打印最终结果
     */
    private static void printFinalResults(ScoreManager scoreManager, World world) {
        System.out.println("\n========== 游戏结束 ==========");
        System.out.println("最终分数: " + scoreManager.getScore());
        System.out.println("蛇最终长度: " + (world.getSnake() != null ? world.getSnake().getLength() : 0));
        System.out.println("=============================\n");
    }
}
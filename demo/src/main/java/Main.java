import java.awt.Point;
import java.util.Scanner;

public class Main {
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
        gameLoop.start();
        gameLoop.resume();
        
        // 7. 主游戏循环
        boolean quit = false;
        boolean gameActive = true;
        
        while (gameActive && !quit) {
            // 显示当前状态
            System.out.println("\n==================================");
            System.out.println("当前状态: " + (gameLoop.isPaused() ? "暂停" : "运行中"));
            System.out.println("分数: " + scoreManager.getScore());
            System.out.println("连击: " + scoreManager.getCombo());
            System.out.println("蛇长度: " + world.getSnake().getLength());
            
            // 显示小型地图
            printSimpleMap(world);
            
            // 检查游戏是否结束
            if (ruleEngine.isGameOver()) {
                System.out.println("\n游戏结束: " + ruleEngine.getGameOverReason());
                gameActive = false;
                break;
            }
            
            // 获取用户输入
            System.out.print("\n输入命令 (wasd-移动, p-暂停, r-继续, m-详细地图, q-退出): ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            if (input.isEmpty()) {
                continue;
            }
            
            switch (input.charAt(0)) {
                case 'w':
                    world.getSnake().changeDirection(Direction.UP);
                    System.out.println("方向：向上");
                    break;
                case 's':
                    world.getSnake().changeDirection(Direction.DOWN);
                    System.out.println("方向：向下");
                    break;
                case 'a':
                    world.getSnake().changeDirection(Direction.LEFT);
                    System.out.println("方向：向左");
                    break;
                case 'd':
                    world.getSnake().changeDirection(Direction.RIGHT);
                    System.out.println("方向：向右");
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
                    continue; // 显示地图后不执行tick
                case 'q':
                    quit = true;
                    System.out.println("退出游戏");
                    continue; // 退出不执行tick
                default:
                    System.out.println("未知命令，请重新输入");
                    continue; // 无效命令不执行tick
            }
            
            // 如果游戏没有暂停，执行一次游戏tick
            if (!gameLoop.isPaused()) {
                boolean stillRunning = gameLoop.tick();
                if (!stillRunning) {
                    gameActive = false;
                }
            }
        }
        
        // 8. 游戏结束，显示最终结果
        scanner.close();
        printFinalResults(scoreManager, world);
        
        System.out.println("\n感谢游玩！");
    }
    
    /**
     * 打印游戏说明
     */
    private static void printInstructions() {
        System.out.println("游戏说明:");
        System.out.println("1. 使用 WASD 键控制蛇的移动方向");
        System.out.println("2. 每次输入WASD后，蛇会移动一步");
        System.out.println("3. 吃食物增长身体并获得分数");
        System.out.println("4. 食物类型:");
        System.out.println("   - 普通食物 (*): 1分");
        System.out.println("   - 特殊食物 ($): 连击平方分");
        System.out.println("   - 稀有食物 (&): 连击立方分");
        System.out.println("5. 连续吃食物可以增加连击数");
        System.out.println("6. 避免撞墙、撞到自己或障碍物");
        System.out.println("\n控制命令:");
        System.out.println("  w - 向上     a - 向左     s - 向下     d - 向右");
        System.out.println("  p - 暂停游戏   r - 继续游戏");
        System.out.println("  m - 显示详细地图   q - 退出游戏");
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
                        symbol = '@'; // 蛇头
                    } else if (world.getSnake().contains(current)) {
                        symbol = 'o'; // 蛇身
                    } else if (world.getFood() != null && world.getFood().getPosition().equals(current)) {
                        switch (world.getFood().getType()) {
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
                        symbol = '#';
                    }
                }
                
                System.out.print(symbol);
            }
            System.out.println();
        }
        
        System.out.println("图例: @-蛇头 o-蛇身 *-普通食物 $-特殊食物 &-稀有食物 #-障碍物");
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
                    if (world.getSnake().getHead().equals(current)) {
                        symbol = '@';
                    } else if (world.getSnake().contains(current)) {
                        symbol = 'o';
                    } else if (world.getFood() != null && world.getFood().getPosition().equals(current)) {
                        switch (world.getFood().getType()) {
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
                        symbol = '#';
                    }
                }
                
                System.out.print(symbol);
            }
            System.out.println();
        }
        
        System.out.println("\n图例: @-蛇头 o-蛇身 *-普通食物 $-特殊食物 &-稀有食物 #-障碍物");
    }
    
    /**
     * 打印最终结果
     */
    private static void printFinalResults(ScoreManager scoreManager, World world) {
        System.out.println("\n========== 游戏结束 ==========");
        System.out.println("最终分数: " + scoreManager.getScore());
        System.out.println("最高连击: " + scoreManager.getHighestCombo());
        System.out.println("蛇最终长度: " + (world.getSnake() != null ? world.getSnake().getLength() : 0));
        System.out.println("=============================\n");
    }
}
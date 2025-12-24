import java.awt.Point;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameLoop {
    private final World world;
    private final RuleEngine ruleEngine;
    private final ScoreManager scoreManager;
    private final FoodSpawner foodSpawner;
    private final ObstacleGenerator obstacleGenerator;
    
    private volatile boolean running = false;
    private volatile boolean paused = false;
    
    private long lastFoodRefreshTime = 0;
    private long lastMapRefreshTime = 0;
    private final long FOOD_REFRESH_INTERVAL = 5000; // 5秒
    private final long MAP_REFRESH_INTERVAL = 10000; // 10秒
    
    public GameLoop(World world, RuleEngine ruleEngine, ScoreManager scoreManager, 
                    FoodSpawner foodSpawner) {
        this.world = world;
        this.ruleEngine = ruleEngine;
        this.scoreManager = scoreManager;
        this.foodSpawner = foodSpawner;
        this.obstacleGenerator = new ObstacleGenerator(world);
    }
    
    public void start() {
        running = true;
        lastFoodRefreshTime = System.currentTimeMillis();
        lastMapRefreshTime = System.currentTimeMillis();
    }
    
    public void stop() {
        running = false;
    }
    
    public void pause() {
        paused = true;
    }
    
    public void resume() {
        paused = false;
    }
    
    /**
     * 执行一次游戏循环（同步方法）
     */
    public boolean tick() {
        if (!running || paused) {
            return false;
        }
        
        // 1. 检查是否需要刷新食物（每5秒）
        checkFoodRefresh();
        
        // 2. 检查是否需要刷新地图（每10秒）
        checkMapRefresh();
        
        // 3. 蛇移动（方向已在外部设置）
        Snake snake = world.getSnake();
        snake.move();
        
        // 4. 检查碰撞和规则
        ruleEngine.checkCollision(this);
        ruleEngine.checkFood(this);
        
        return running; // 返回游戏是否还在运行
    }
    
    /**
     * 检查并刷新食物（每5秒）
     */
    private void checkFoodRefresh() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFoodRefreshTime >= FOOD_REFRESH_INTERVAL) {
            // 清空未吃掉的食物
            world.clearFoods();
            
            // 生成5个新食物
            List<Food> newFoods = foodSpawner.spawnMultiple(world, world.getSnake(), 5);
            for (Food food : newFoods) {
                world.addFood(food);
            }
            
            lastFoodRefreshTime = currentTime;
            
            System.out.println("食物已刷新！生成了" + newFoods.size() + "个新食物");
        }
    }
    
    /**
     * 检查并刷新地图（每10秒）
     */
    private void checkMapRefresh() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMapRefreshTime >= MAP_REFRESH_INTERVAL) {
            // 获取需要避开的位置：蛇身、蛇头、所有食物
            Set<Point> positionsToAvoid = new HashSet<>();
            
            // 避开蛇的所有身体部分
            Snake snake = world.getSnake();
            if (snake != null) {
                for (Point bodyPart : snake.getBody()) {
                    positionsToAvoid.add(new Point(bodyPart));
                }
            }
            
            // 避开所有食物
            List<Food> foods = world.getFoods();
            if (foods != null) {
                for (Food food : foods) {
                    positionsToAvoid.add(new Point(food.getPosition()));
                }
            }
            
            // 重新生成障碍物（不超过总格子的15%）
            int totalCells = world.getWidth() * world.getHeight();
            int maxObstacleCells = (int)(totalCells * 0.15);
            
            Obstacles newObstacles = obstacleGenerator.generate(maxObstacleCells, positionsToAvoid);
            
            // 检查连通性，如果不连通则重新生成（最多尝试5次）
            int attempts = 0;
            while (attempts < 5) {
                // 临时设置新障碍物检查连通性
                Obstacles oldObstacles = world.getObstacles();
                world.setObstacles(newObstacles);
                
                if (checkMapConnectivity()) {
                    // 连通性检查通过，保留新障碍物
                    System.out.println("地图已刷新！生成了新的障碍物布局");
                    break;
                } else {
                    // 不连通，重新生成
                    newObstacles = obstacleGenerator.generate(maxObstacleCells, positionsToAvoid);
                    world.setObstacles(oldObstacles); // 恢复旧障碍物
                    attempts++;
                }
            }
            
            if (attempts >= 5) {
                System.out.println("地图刷新失败：无法生成连通的地图，保持原有障碍物");
            }
            
            lastMapRefreshTime = currentTime;
        }
    }
    
    /**
     * 检查地图连通性
     */
    private boolean checkMapConnectivity() {
        Snake snake = world.getSnake();
        if (snake == null) return false;
        
        Point head = snake.getHead();
        boolean[][] visited = new boolean[world.getWidth()][world.getHeight()];
        java.util.List<Point> queue = new java.util.ArrayList<>();
        queue.add(head);
        visited[head.x][head.y] = true;
        
        int reachableCells = 0;
        
        while (!queue.isEmpty()) {
            Point current = queue.remove(0);
            reachableCells++;
            
            // 检查四个方向
            for (Direction dir : Direction.values()) {
                Point neighbor = new Point(current.x + dir.dx, current.y + dir.dy);
                
                if (world.inBounds(neighbor) && 
                    !visited[neighbor.x][neighbor.y] && 
                    !world.isOccupied(neighbor)) {
                    
                    visited[neighbor.x][neighbor.y] = true;
                    queue.add(neighbor);
                }
            }
        }
        
        // 计算空单元格总数
        int totalEmpty = 0;
        for (int x = 0; x < world.getWidth(); x++) {
            for (int y = 0; y < world.getHeight(); y++) {
                Point p = new Point(x, y);
                if (!world.isOccupied(p)) {
                    totalEmpty++;
                }
            }
        }
        
        // 如果能到达大部分空单元格，认为地图连通
        return reachableCells >= Math.min(10, totalEmpty);
    }
    
    public World getWorld() {
        return world;
    }
    
    public ScoreManager getScoreManager() {
        return scoreManager;
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public boolean isPaused() {
        return paused;
    }
    
    public long getTimeUntilNextFoodRefresh() {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastFoodRefreshTime;
        return Math.max(0, FOOD_REFRESH_INTERVAL - elapsed);
    }
    
    public long getTimeUntilNextMapRefresh() {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastMapRefreshTime;
        return Math.max(0, MAP_REFRESH_INTERVAL - elapsed);
    }
}
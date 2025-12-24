import java.util.concurrent.TimeUnit;

public class GameLoop {
    private final World world;
    private final RuleEngine ruleEngine;
    private final ScoreManager scoreManager;
    private final FoodSpawner foodSpawner;
    
    private volatile boolean running = false;
    private volatile boolean paused = false;
    
    private int tickIntervalMs = 200;
    
    public GameLoop(World world, RuleEngine ruleEngine, ScoreManager scoreManager, FoodSpawner foodSpawner) {
        this.world = world;
        this.ruleEngine = ruleEngine;
        this.scoreManager = scoreManager;
        this.foodSpawner = foodSpawner;
    }
    
    public void start() {
        running = true;
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
    
    public void setSpeed(int intervalMs) {
        this.tickIntervalMs = Math.max(50, Math.min(1000, intervalMs));
    }
    
    /**
     * 执行一次游戏循环（同步方法）
     */
    public boolean tick() {
        if (!running || paused) {
            return false;
        }
        
        // 1. 蛇移动
        Snake snake = world.getSnake();
        snake.move();
        
        // 2. 检查碰撞和规则
        ruleEngine.checkCollision(this);
        ruleEngine.checkFood(this);
        
        // 3. 如果食物被吃了，生成新食物
        if (world.getFood() == null) {
            Food newFood = foodSpawner.spawn(world, snake);
            if (newFood != null) {
                world.setFood(newFood);
            }
        }
        
        return running; // 返回游戏是否还在运行
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
    
    public int getTickIntervalMs() {
        return tickIntervalMs;
    }
}
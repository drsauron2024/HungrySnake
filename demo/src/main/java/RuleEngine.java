import java.awt.Point;

public class RuleEngine {
    private boolean gameOver = false;
    private String gameOverReason = "";
    
    public void checkCollision(GameLoop gameLoop) {
        if (gameOver) return;
        
        World world = gameLoop.getWorld();
        Snake snake = world.getSnake();
        
        Point head = snake.getHead();
        
        // 1. 检查边界碰撞
        if (!world.inBounds(head)) {
            gameOver = true;
            gameOverReason = "撞墙了!";
            return;
        }
        
        // 2. 检查自身碰撞（跳过头部）
        boolean selfCollision = false;
        boolean first = true;
        for (Point bodyPart : snake.getBody()) {
            if (first) {
                first = false;
                continue; // 跳过头部
            }
            if (bodyPart.equals(head)) {
                selfCollision = true;
                break;
            }
        }
        
        if (selfCollision) {
            gameOver = true;
            gameOverReason = "撞到自己了!";
            return;
        }
        
        // 3. 检查障碍物碰撞
        Obstacles obstacles = world.getObstacles();
        if (obstacles != null && obstacles.getAllCells().contains(head)) {
            gameOver = true;
            gameOverReason = "撞到障碍物了!";
            return;
        }
    }
    
    public void checkFood(GameLoop gameLoop) {
        if (gameOver) return;
        
        World world = gameLoop.getWorld();
        Snake snake = world.getSnake();
        ScoreManager scoreManager = gameLoop.getScoreManager();
        
        Point head = snake.getHead();
        
        // 检查是否吃到任何食物
        Food foodEaten = world.getFoodAt(head);
        if (foodEaten != null) {
            FoodType type = foodEaten.getType();
            
            // 计算分数（ScoreManager内部处理连续逻辑）
            scoreManager.eatFood(type);
            
            // 蛇增长
            snake.grow(type.getGrowth());
            
            // 移除被吃掉的这个食物（其他食物保留）
            world.removeFoodAt(head);
        } 
    }
    
    public boolean isGameOver() {
        return gameOver;
    }
    
    public String getGameOverReason() {
        return gameOverReason;
    }
    
    public void reset() {
        gameOver = false;
        gameOverReason = "";
    }
}
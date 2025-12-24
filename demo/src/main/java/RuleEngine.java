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
        Food food = world.getFood();
        ScoreManager scoreManager = gameLoop.getScoreManager();
        
        if (food == null) return;
        
        Point head = snake.getHead();
        
        // 检查是否吃到食物
        if (head.equals(food.getPosition())) {
            // 计算增长量
            int growthAmount = 1; // 默认
            FoodType type = food.getType();
            
            // 根据食物类型计算分数和增长
            scoreManager.eatFood(type);
            
            // 根据食物类型增加不同长度
            switch (type) {
                case SPECIAL:
                    growthAmount = 2;
                    break;
                case RARE:
                    growthAmount = 3;
                    break;
                case NORMAL:
                default:
                    growthAmount = 1;
                    break;
            }
            
            snake.grow(growthAmount);
            
            // 移除食物
            world.setFood(null);
            
            // 更新游戏速度（随着分数增加，游戏加速）
            int score = scoreManager.getScore();
            int newSpeed = Math.max(50, 200 - (score / 10) * 10);
            gameLoop.setSpeed(newSpeed);
        } else {
            // 没吃到食物，重置连击
            scoreManager.resetCombo();
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
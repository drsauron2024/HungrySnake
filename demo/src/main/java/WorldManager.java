import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorldManager {
    private final World world;
    private final FoodSpawner foodSpawner;
    private final ObstacleGenerator obstacleGenerator;
    
    public WorldManager(int width, int height) {
        this.world = new World(width, height);
        this.foodSpawner = new FoodSpawner(3); // 最小距离为3
        this.obstacleGenerator = new ObstacleGenerator(world);
    }
    
    public void initializeGame() {
        // 1. 创建蛇（初始在中间位置）
        Point startPoint = new Point(world.getWidth() / 2, world.getHeight() / 2);

        // 确保起始点在边界内
        if (!world.inBounds(startPoint)) {
            startPoint = new Point(5, 5); // 如果中心点不行，使用固定位置
        }

        Snake snake = new Snake(startPoint, Direction.RIGHT, 3);
        world.setSnake(snake);
        
        // 2. 初始生成障碍物（避开蛇的位置）
        Set<Point> positionsToAvoid = new HashSet<>();
        positionsToAvoid.add(snake.getHead());
        for (Point segment : snake.getBody()) {
            positionsToAvoid.add(segment);
        }
        
        // 3. 避开蛇头周围3x3区域
        Point head = snake.getHead();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                Point adjacent = new Point(head.x + dx, head.y + dy);
                if (world.inBounds(adjacent)) {
                    positionsToAvoid.add(adjacent);
                }
            }
        }
        
        int totalCells = world.getWidth() * world.getHeight();
        int maxObstacleCells = (int)(totalCells * 0.15);
        Obstacles obstacles = obstacleGenerator.generate(maxObstacleCells, positionsToAvoid);
        
        // 3. 移除蛇头朝向方向三个格子内的障碍物
        //clearObstaclesInFrontOfSnake(snake, obstacles);
        
        world.setObstacles(obstacles);
        
        // 4. 初始生成5个食物
        List<Food> initialFoods = foodSpawner.spawnMultiple(world, snake, 5);
        for (Food food : initialFoods) {
            world.addFood(food);
        }
        
        System.out.println("游戏初始化完成！");
        System.out.println("- 生成了" + obstacles.getAllCells().size() + "个障碍物");
        System.out.println("- 生成了" + initialFoods.size() + "个初始食物");
    }
    
    /**
     * 清除蛇头朝向方向连续三个格子内的障碍物
     */
    /*private void clearObstaclesInFrontOfSnake(Snake snake, Obstacles obstacles) {
        if (obstacles == null || snake == null) {
            return;
        }
        
        Point head = snake.getHead();
        Direction direction = snake.getCurrentDirection();
        
        List<Point> cellsToClear = new ArrayList<>();
        
        // 检查蛇头前方三个格子
        for (int i = 1; i <= 3; i++) {
            Point frontCell = new Point(
                head.x + direction.dx * i,
                head.y + direction.dy * i
            );
            
            // 如果格子超出边界，停止检查
            if (!world.inBounds(frontCell)) {
                break;
            }
            
            cellsToClear.add(frontCell);
        }
        
        // 从障碍物中移除这些格子
        if (!cellsToClear.isEmpty()) {
            for (Wall wall : obstacles.getWalls()) {
                List<Point> wallCells = wall.getCells();
                wallCells.removeAll(cellsToClear);
            }
            
            // 清理可能因此变空的墙
            obstacles.getWalls().removeIf(wall -> wall.getCells().isEmpty());
            
            System.out.println("已清除蛇头前方3格内的障碍物");
        }
    }*/
    
    public void respawnFoods(int count) {
        Snake snake = world.getSnake();
        List<Food> newFoods = foodSpawner.spawnMultiple(world, snake, count);
        world.setFoods(newFoods);
    }
    
    public boolean checkConnectivity() {
        Snake snake = world.getSnake();
        Point head = snake.getHead();
        
        // 使用BFS检查连通性
        boolean[][] visited = new boolean[world.getWidth()][world.getHeight()];
        List<Point> queue = new ArrayList<>();
        queue.add(head);
        visited[head.x][head.y] = true;
        
        int emptyCellsReached = 0;
        
        while (!queue.isEmpty()) {
            Point current = queue.remove(0);
            
            // 检查四个方向
            for (Direction dir : Direction.values()) {
                Point neighbor = new Point(current.x + dir.dx, current.y + dir.dy);
                
                if (world.inBounds(neighbor) && 
                    !visited[neighbor.x][neighbor.y] && 
                    !world.isOccupied(neighbor)) {
                    
                    visited[neighbor.x][neighbor.y] = true;
                    queue.add(neighbor);
                    emptyCellsReached++;
                }
            }
        }
        
        // 如果还能到达一定数量的空单元格，认为地图仍然连通
        int totalEmpty = countEmptyCells();
        return emptyCellsReached >= Math.min(5, totalEmpty);
    }
    
    private int countEmptyCells() {
        int count = 0;
        for (int x = 0; x < world.getWidth(); x++) {
            for (int y = 0; y < world.getHeight(); y++) {
                if (world.isEmpty(new Point(x, y))) {
                    count++;
                }
            }
        }
        return count;
    }
    
    public World getWorld() {
        return world;
    }
    
    public FoodSpawner getFoodSpawner() {
        return foodSpawner;
    }
    
    public ObstacleGenerator getObstacleGenerator() {
        return obstacleGenerator;
    }
}
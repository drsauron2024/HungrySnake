import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

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
        
        // 2. 生成障碍物（不超过总格子的15%）
        int totalCells = world.getWidth() * world.getHeight();
        int maxObstacleCells = (int)(totalCells * 0.15);
        Obstacles obstacles = obstacleGenerator.generate(maxObstacleCells);
        world.setObstacles(obstacles);
        
        // 3. 生成第一个食物
        Food firstFood = foodSpawner.spawn(world, snake);
        world.setFood(firstFood);
    }
    
    public void respawnFood() {
        Snake snake = world.getSnake();
        Food newFood = foodSpawner.spawn(world, snake);
        world.setFood(newFood);
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
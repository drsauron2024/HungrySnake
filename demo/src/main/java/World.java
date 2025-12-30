import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class World {

    private final int width;
    private final int height;

    private Snake snake;
    private List<Food> foods = new ArrayList<>();  // 改为食物列表
    private Obstacles obstacles;

    public World(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public boolean inBounds(Point p) {
        return p.x >= 0 && p.x < width &&
               p.y >= 0 && p.y < height;
    }

    public boolean isOccupied(Point p) {
        if (!inBounds(p)) return true;

        if (snake != null && snake.iscontains(p)) {
            return true;
        }
        
        // 检查是否被任何食物占据
        for (Food food : foods) {
            if (food.getPosition().equals(p)) {
                return true;
            }
        }
        
        if (obstacles != null && obstacles.getAllCells().contains(p)) {
            return true;
        }
        return false;
    }

    public boolean isEmpty(Point p) {
        return inBounds(p) && !isOccupied(p);
    }

    public List<Point> getEmptyPoints(
            int minX, int minY,
            int maxX, int maxY
    ) {
        List<Point> result = new ArrayList<>();

        minX = Math.max(0, minX);
        minY = Math.max(0, minY);
        maxX = Math.min(width - 1, maxX);
        maxY = Math.min(height - 1, maxY);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                Point p = new Point(x, y);
                if (isEmpty(p)) {
                    result.add(p);
                }
            }
        }
        return result;
    }

    public void setSnake(Snake snake) {
        this.snake = snake;
    }

    public void addFood(Food food) {
        if (food != null) {
            foods.add(food);
        }
    }
    
    public void setFoods(List<Food> foods) {
        this.foods = new ArrayList<>(foods);
    }
    
    public void clearFoods() {
        foods.clear();
    }
    
    public boolean removeFoodAt(Point position) {
        return foods.removeIf(food -> food.getPosition().equals(position));
    }

    public void setObstacles(Obstacles obstacles) {
        this.obstacles = obstacles;
    }

    public Snake getSnake() {
        return snake;
    }

    public List<Food> getFoods() {
        return foods;
    }
    
    /**
     * 获取指定位置的食物（如果存在）
     */
    public Food getFoodAt(Point position) {
        for (Food food : foods) {
            if (food.getPosition().equals(position)) {
                return food;
            }
        }
        return null;
    }

    public Obstacles getObstacles() {
        return obstacles;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
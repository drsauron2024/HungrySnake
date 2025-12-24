import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FoodSpawner {

    private final Random random = new Random();
    private final int minDistance;

    public FoodSpawner(int minDistance) {
        this.minDistance = minDistance;
    }

    public Food spawn(World world, Snake snake) {
        Point pos = choosePosition(world, snake);
        if (pos == null) {
            return null;
        }
        FoodType type = chooseFoodType();
        return new Food(pos, type);
    }
    
    /**
     * 批量生成多个食物
     */
    public List<Food> spawnMultiple(World world, Snake snake, int count) {
        List<Food> foods = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Food food = spawn(world, snake);
            if (food != null) {
                foods.add(food);
            }
        }
        return foods;
    }

    private Point choosePosition(World world, Snake snake) {
        Point head = snake.getHead();

        int minX = head.x - minDistance;
        int maxX = head.x + minDistance;
        int minY = head.y - minDistance;
        int maxY = head.y + minDistance;

        List<Point> candidates = world.getEmptyPoints(
            minX, minY, maxX, maxY
        );

        if (!candidates.isEmpty()) {
            return candidates.get(random.nextInt(candidates.size()));
        }

        List<Point> all = world.getEmptyPoints(
            0, 0,
            world.getWidth() - 1,
            world.getHeight() - 1
        );

        return all.isEmpty() ? null : all.get(random.nextInt(all.size()));
    }

    private FoodType chooseFoodType() {
        int r = random.nextInt(100);
        if (r < 30) return FoodType.RARE;
        if (r < 60) return FoodType.SPECIAL;
        return FoodType.NORMAL;
    }
}
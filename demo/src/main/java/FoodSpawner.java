import java.awt.Point;
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
        FoodType type = chooseFoodType();
        return new Food(pos, type);
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
        if (r < 65) return FoodType.NORMAL;
        if (r < 90) return FoodType.SPECIAL;
        return FoodType.RARE;
    }
}


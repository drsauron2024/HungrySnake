import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ObstacleGenerator {

    private final World world;
    private final Random random = new Random();

    private final int minWallLength = 3;
    private final int maxWallLength = 8;
    private final int maxTryPerWall = 10;

    public ObstacleGenerator(World world) {
        this.world = world;
    }

    public Obstacles generate(int maxTotalCells) {
        return generate(maxTotalCells, new HashSet<>());
    }
    
    /**
     * 生成障碍物，避开指定的位置
     */
    public Obstacles generate(int maxTotalCells, Set<Point> positionsToAvoid) {
        Obstacles obstacles = new Obstacles();
        int remaining = maxTotalCells;

        while (remaining >= minWallLength) {
            Wall wall = tryGenerateOneWall(remaining, positionsToAvoid);
            if (wall == null) {
                break;
            }
            obstacles.addWall(wall);
            remaining -= wall.getCells().size();
        }
        return obstacles;
    }

    private Wall tryGenerateOneWall(int remaining, Set<Point> positionsToAvoid) {
        for (int attempt = 0; attempt < maxTryPerWall; attempt++) {

            boolean horizontal = random.nextBoolean();
            Point start = randomPointInWorld();

            int maxLenByBoundary = computeMaxLength(start, horizontal);
            int maxLen = Math.min(maxLenByBoundary, remaining);
            maxLen = Math.min(maxLen, maxWallLength);

            if (maxLen < minWallLength) {
                continue;
            }

            int length = random.nextInt(maxLen - minWallLength + 1) + minWallLength;

            List<Point> cells = new ArrayList<>();
            boolean valid = true;

            for (int i = 0; i < length; i++) {
                int x = start.x + (horizontal ? i : 0);
                int y = start.y + (horizontal ? 0 : i);
                Point p = new Point(x, y);

                if (!world.inBounds(p) || !world.isEmpty(p) || positionsToAvoid.contains(p)) {
                    valid = false;
                    break;
                }
                cells.add(p);
            }

            if (valid) {
                return new Wall(cells);
            }
        }
        return null;
    }

    private int computeMaxLength(Point start, boolean horizontal) {
        int len = 0;
        int x = start.x;
        int y = start.y;

        while (true) {
            Point p = new Point(x, y);
            if (!world.inBounds(p)) {
                break;
            }
            len++;
            if (horizontal) {
                x++;
            } else {
                y++;
            }
        }
        return len;
    }

    private Point randomPointInWorld() {
        int x = random.nextInt(world.getWidth());
        int y = random.nextInt(world.getHeight());
        return new Point(x, y);
    }
}
import java.util.Set;
import java.awt.Point;
import java.util.Random;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class World {
    private final int width;
    private final int height;
    private final Set<Point> obstacles;
    private final Random random = new Random();

    public World(int width, int height) {
        this.width = width;
        this.height = height;
        this.obstacles = new HashSet<>();
    }

    //Foundational judger method to check if a point is within bounds
    public boolean inBounds(Point p){
        return p.x >= 0 && p.x < width && p.y >= 0 && p.y < height;
    }
    public boolean isObstacle(Point p) {
        return obstacles.contains(p);
    }
    public boolean isOccupied(Point p, Snake snake) {
        return isObstacle(p) || snake.iscontains(p);
    }

    public List<Point> getEmptyPoints(
        Snake snake,
        int minX, int minY,
        int maxX, int maxY
    ){
        List<Point> result = new ArrayList<>();

        minX = Math.max(0, minX);
        minY = Math.max(0, minY);
        maxX = Math.min(width - 1, maxX);
        maxY = Math.min(height - 1, maxY);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                Point p = new Point(x, y);
                if (!isOccupied(p, snake)) {
                    result.add(p);
                }
            }
        }
        return result;
    }

    //Adding obstacles
    public void addObstacle(Point p) {
        if (inBounds(p)) {
            obstacles.add(p);
        }
    }

    public Set<Point> getObstacles() {
        return obstacles;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}

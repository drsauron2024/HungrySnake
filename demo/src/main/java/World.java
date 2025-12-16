import java.util.Set;
import java.awt.Point;
import java.util.Random;
import java.util.HashSet;

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

    //Blank point generator
    public Point randomEmptyPoint(Snake snake){
        int freecount = width * height - obstacles.size() - snake.getLength();
        if(freecount <= 0) {
            return null; //You win!
        }

        int index = random.nextInt(freecount);

        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                Point p = new Point(x, y);
                if(!isOccupied(p, snake)) {
                    if(index == 0) {
                        return p;
                    }
                    index--;
                }
            }
        }
        return null; //You will never win
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

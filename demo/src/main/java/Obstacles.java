import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Obstacles {
    private final List<Wall> walls = new ArrayList<>();

    public void addWall(Wall wall) {
        walls.add(wall);
    }

    public List<Wall> getWalls() {
        return walls;
    }

    public List<Point> getAllCells() {
        List<Point> all = new ArrayList<>();
        for (Wall w : walls) {
            all.addAll(w.getCells());
        }
        return all;
    }
}

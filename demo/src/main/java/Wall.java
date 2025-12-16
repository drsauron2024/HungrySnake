import java.awt.Point;
import java.util.List;

public class Wall {
    private final List<Point> cells;

    public Wall(List<Point> cells) {
        this.cells = cells;
    }

    public List<Point> getCells() {
        return cells;
    }
}

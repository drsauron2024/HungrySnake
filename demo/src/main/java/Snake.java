import java.util.Deque;
import java.awt.Point;
import java.util.ArrayDeque;

public class Snake {
    private Deque<Point> body;
    private Direction currentDirection;
    private int growthPending;
    public Snake(Point start, Direction dir, int initialLength) {
        this.body = new ArrayDeque<>();
        this.currentDirection = dir;
        this.growthPending = 0;
        int dx, dy;
        switch(dir){
            case UP -> { dx = 0; dy = -1; }
            case DOWN -> { dx = 0; dy = 1; }
            case LEFT -> { dx = -1; dy = 0; }
            case RIGHT -> { dx = 1; dy = 0; }
            default -> throw new IllegalArgumentException("Invalid direction");
        }
        for (int i = 0; i < initialLength; i++) {
            body.addLast(new Point(start.x - i * dx, start.y - i * dy));
        }
    }
    private Point nextHead() {
        Point head = body.peekFirst();
        Point newHead = new Point(head);
        switch (currentDirection) {
            case UP -> newHead.y -= 1;
            case DOWN -> newHead.y += 1;
            case LEFT -> newHead.x -= 1;
            case RIGHT -> newHead.x += 1;
        }
        return newHead;
    }
    public void move() {
        Point newHead = nextHead();
        body.addFirst(newHead);
        if (growthPending > 0) {
            growthPending--;
        } else {
            body.removeLast();
        }
    }
    private boolean isOpposite(Direction dir1, Direction dir2) {
        return (dir1 == Direction.UP && dir2 == Direction.DOWN) ||
               (dir1 == Direction.DOWN && dir2 == Direction.UP) ||
               (dir1 == Direction.LEFT && dir2 == Direction.RIGHT) ||
               (dir1 == Direction.RIGHT && dir2 == Direction.LEFT);
    }
    public void changeDirection(Direction newDir) {
        if (!isOpposite(currentDirection, newDir)) {
            currentDirection = newDir;
        }
    }
    public void grow(int amount) {
        if(amount > 0) {
            growthPending += amount;
        }
    }
    public Iterable<Point> getBody() {
        return body;
    }
    public Direction getCurrentDirection() {
        return currentDirection;
    }
    public Point getHead() {
        return body.peekFirst();
    }
    public boolean iscontains(Point p) {
        return body.contains(p);
    }
    public int getLength() {
        return body.size();
    }
}


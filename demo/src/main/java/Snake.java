import java.awt.Point;
import java.util.ArrayDeque;
import java.util.Deque;

public class Snake {
    private Deque<Point> body;
    private Direction currentDirection;
    private int growthPending;
    
    public Snake(Point start, Direction dir, int initialLength) {
        this.body = new ArrayDeque<>();
        this.currentDirection = dir;
        this.growthPending = 0;
        for (int i = 0; i < initialLength; i++) {
            body.addLast(new Point(start.x - i * dir.dx, start.y - i * dir.dy));
        }
    }
    
    private Point nextHead() {
        Point head = body.peekFirst();
        Point newHead = new Point(head);
        
        newHead.x += currentDirection.dx;
        newHead.y += currentDirection.dy;
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
    
    public void changeDirection(Direction newDir) {
        // 确保新方向不是当前方向的相反方向
        if (newDir != null && !currentDirection.isOpposite(newDir)) {
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
    
    public boolean contains(Point p) {
        return body.contains(p);
    }
    
    public int getLength() {
        return body.size();
    }
}
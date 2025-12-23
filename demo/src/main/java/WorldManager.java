import java.awt.Point;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

/**
 * WorldManager - 管理整个世界观（地图、障碍、食物、连通性检查等）
 *
 * 设计要点：
 * - 维护地图边界、蛇引用、障碍集合、食物集合
 * - 提供判断格子可用 / 越界方法供其它模块调用
 * - 提供在蛇附近生成食物的策略
 * - 生成障碍并能进行连通性检查（BFS flood-fill）
 *
 * 注意：本类尽量降低对其它具体类的强耦合（把食物用内部记录 FoodRecord 表示）。
 */
public class WorldManager {
    private final int width;
    private final int height;
    private final Snake snake;

    // 障碍占位（不可行走格子）
    private final Set<Point> obstacles = new HashSet<>();

    // 食物记录（位置 + 生长量 / 类型占位）
    public static class FoodRecord {
        public final Point pos;
        public final int growAmount;
        public final String type; // 可替换为项目内 FoodType

        public FoodRecord(Point pos, int growAmount, String type) {
            this.pos = new Point(pos);
            this.growAmount = growAmount;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FoodRecord)) return false;
            FoodRecord that = (FoodRecord) o;
            return Objects.equals(pos, that.pos) && Objects.equals(type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos, type);
        }
    }

    private final List<FoodRecord> foods = new ArrayList<>();
    private final Random rnd = new Random();

    public WorldManager(int width, int height, Snake snake) {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("width/height must be > 0");
        this.width = width;
        this.height = height;
        this.snake = Objects.requireNonNull(snake, "snake");
    }

    // 地图信息接口
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    // 判断是否越界
    public boolean inBounds(Point p) {
        return p.x >= 0 && p.x < width && p.y >= 0 && p.y < height;
    }

    // 判断格子是否为空（未被蛇、障碍、食物占用）
    public boolean isEmpty(Point p) {
        if (!inBounds(p)) return false;
        if (snakeContains(p)) return false;
        if (obstacles.contains(p)) return false;
        for (FoodRecord f : foods) if (f.pos.equals(p)) return false;
        return true;
    }

    // 封装对蛇的查询（避免直接暴露实现）
    private boolean snakeContains(Point p) {
        try {
            return snake.contains(p);
        } catch (Throwable t) {
            // 如果 snake 不提供 contains，实现一个回退（遍历 body，如果可访问的话）
            try {
                // 假设 snake 提供 getBody() 返回 Iterable<Point>
                Iterable<Point> body = (Iterable<Point>) snake.getClass().getMethod("getBody").invoke(snake);
                for (Point bp : body) if (bp.equals(p)) return true;
            } catch (Throwable ignored) {}
            return false;
        }
    }

    // 在蛇附近生成一个食物位置（半径 radius），返回生成的位置或 null
    public Point generatePositionNearSnake(int radius) {
        Point head = getSnakeHead();
        if (head == null) return null;
        // 先尝试随机采样若干次，最后回退到全图扫描
        int attempts = Math.max(20, radius * 4);
        for (int i = 0; i < attempts; i++) {
            int dx = rnd.nextInt(2 * radius + 1) - radius;
            int dy = rnd.nextInt(2 * radius + 1) - radius;
            Point p = new Point(head.x + dx, head.y + dy);
            if (inBounds(p) && isEmpty(p)) return p;
        }
        // 回退扫描附近格子（从小到大半径）
        for (int r = 0; r <= radius; r++) {
            for (int dx = -r; dx <= r; dx++) {
                int dy = r - Math.abs(dx);
                Point p1 = new Point(head.x + dx, head.y + dy);
                if (inBounds(p1) && isEmpty(p1)) return p1;
                Point p2 = new Point(head.x + dx, head.y - dy);
                if (inBounds(p2) && isEmpty(p2)) return p2;
            }
        }
        return null;
    }

    // 在蛇附近生成食物记录（type 可选，growAmount 表示增长长度）
    public FoodRecord spawnFoodNearSnake(int radius, int growAmount, String type) {
        Point pos = generatePositionNearSnake(radius);
        if (pos == null) return null;
        FoodRecord fr = new FoodRecord(pos, growAmount, type);
        foods.add(fr);
        return fr;
    }

    // 直接在随机空格生成若干食物（全图）
    public List<FoodRecord> spawnRandomFoods(int count, int growAmount, String type) {
        List<FoodRecord> added = new ArrayList<>();
        int guard = 0;
        while (added.size() < count && guard++ < count * 50) {
            Point p = new Point(rnd.nextInt(width), rnd.nextInt(height));
            if (isEmpty(p)) {
                FoodRecord fr = new FoodRecord(p, growAmount, type);
                foods.add(fr);
                added.add(fr);
            }
        }
        return added;
    }

    // 获取并移除位于位置 p 的食物（如果存在）
    public FoodRecord consumeFoodAt(Point p) {
        for (int i = 0; i < foods.size(); i++) {
            FoodRecord f = foods.get(i);
            if (f.pos.equals(p)) {
                foods.remove(i);
                return f;
            }
        }
        return null;
    }

    // 障碍管理
    public boolean addObstacle(Point p) {
        if (!inBounds(p) || snakeContains(p)) return false;
        return obstacles.add(new Point(p));
    }

    public void clearObstacles() {
        obstacles.clear();
    }

    // 随机生成障碍（简单随机），若需要保证连通性可在生成后调用 ensureConnectivity()
    public void generateRandomObstacles(int count) {
        int guard = 0;
        while (obstacles.size() < count && guard++ < count * 50) {
            Point p = new Point(rnd.nextInt(width), rnd.nextInt(height));
            if (isEmpty(p)) obstacles.add(p);
        }
    }

    // 连通性检查：从蛇头出发，检查能到达的空格数（不越过障碍/蛇体）
    // 如果返回值 >= minReachable 则认为连通性合格
    public boolean ensureConnectivity(int minReachable) {
        Point head = getSnakeHead();
        if (head == null) return false;
        boolean[][] seen = new boolean[width][height];
        Queue<Point> q = new ArrayDeque<>();
        q.add(head);
        seen[head.x][head.y] = true;
        int reachable = 0;
        final int[] dx = {1,-1,0,0};
        final int[] dy = {0,0,1,-1};
        while (!q.isEmpty()) {
            Point p = q.poll();
            reachable++;
            if (reachable >= minReachable) return true;
            for (int k = 0; k < 4; k++) {
                int nx = p.x + dx[k];
                int ny = p.y + dy[k];
                if (nx < 0 || nx >= width || ny < 0 || ny >= height) continue;
                if (seen[nx][ny]) continue;
                Point np = new Point(nx, ny);
                if (!isWalkable(np)) continue;
                seen[nx][ny] = true;
                q.add(np);
            }
        }
        return reachable >= minReachable;
    }

    // 判断格子是否可行走（不在障碍、不在蛇体）
    private boolean isWalkable(Point p) {
        if (!inBounds(p)) return false;
        if (obstacles.contains(p)) return false;
        // 注意：不把食物当成阻挡（蛇要能走到食物处）
        return !snakeContains(p);
    }

    // 返回蛇头（尝试通过 snake.getHead 或反射回退）
    public Point getSnakeHead() {
        try {
            return (Point) snake.getClass().getMethod("getHead").invoke(snake);
        } catch (Throwable t) {
            // 回退：如果 snake 提供 getBody() 的 Iterable，则取第一个
            try {
                Iterable<Point> body = (Iterable<Point>) snake.getClass().getMethod("getBody").invoke(snake);
                for (Point p : body) return p;
            } catch (Throwable ignored) {}
            return null;
        }
    }

    // 供外部使用的只读视图
    public Set<Point> getObstacles() { return new HashSet<>(obstacles); }
    public List<FoodRecord> getFoods() { return new ArrayList<>(foods); }

    // 重置世界（保留蛇引用）
    public void reset() {
        obstacles.clear();
        foods.clear();
    }
}
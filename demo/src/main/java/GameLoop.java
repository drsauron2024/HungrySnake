import java.awt.Point;
import java.util.Set;

/**
 * 简单的游戏循环（Engine）。
 * - 构造签名：GameLoop(WorldManager, Snake)
 * - 提供 start()/pause()/resume()/stop()
 * - 每 tick: snake.move(); 检查越界/自撞/障碍；处理吃食物；更新内部 score
 */
public class GameLoop {
    public enum State { RUNNING, PAUSED, STOPPED }

    private final WorldManager world;
    private final Snake snake;
    private final int tickMillis;
    private volatile State state = State.STOPPED;
    private Thread loopThread;
    private int score = 0;

    public GameLoop(WorldManager world, Snake snake) {
        this(world, snake, 150);
    }

    public GameLoop(WorldManager world, Snake snake, int tickMillis) {
        if (world == null || snake == null) throw new IllegalArgumentException("world and snake required");
        this.world = world;
        this.snake = snake;
        this.tickMillis = Math.max(10, tickMillis);
    }

    public synchronized void start() {
        if (state == State.RUNNING) return;
        state = State.RUNNING;
        loopThread = new Thread(this::runLoop, "GameLoop-Thread");
        loopThread.start();
    }

    public synchronized void pause() {
        if (state == State.RUNNING) state = State.PAUSED;
    }

    public synchronized void resume() {
        if (state == State.PAUSED) state = State.RUNNING;
    }

    public synchronized void stop() {
        state = State.STOPPED;
        if (loopThread != null) {
            loopThread.interrupt();
        }
    }

    public State getState() { return state; }
    public int getScore() { return score; }

    private void runLoop() {
        while (state != State.STOPPED) {
            if (state == State.RUNNING) {
                tick();
            }
            try {
                Thread.sleep(tickMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void tick() {
        // 移动蛇
        snake.move();
        Point head = snake.getHead();

        // 越界检查（撞墙 -> 游戏结束）
        if (head == null || !world.inBounds(head)) {
            onGameOver("Hit wall / out of bounds");
            return;
        }

        // 障碍检查
        Set<Point> obs = world.getObstacles();
        if (obs != null && obs.contains(head)) {
            onGameOver("Hit obstacle");
            return;
        }

        // 自撞检查：遍历 body 看 head 出现次数 >1 即自撞
        int occ = 0;
        for (Point p : snake.getBody()) {
            if (head.equals(p)) occ++;
            if (occ > 1) {
                onGameOver("Self collision");
                return;
            }
        }

        // 吃食物检查
        WorldManager.FoodRecord eaten = world.consumeFoodAt(head);
        if (eaten != null) {
            snake.grow(eaten.growAmount);
            score += Math.max(1, eaten.growAmount);
            onFoodEaten(eaten);
        }

        // 可扩展：在此处调用 RuleEngine / Renderer / ScoreManager 等模块
    }

    private void onFoodEaten(WorldManager.FoodRecord f) {
        // 简单日志输出；真实项目可由 Renderer / ScoreManager 处理
        System.out.println("[GameLoop] Ate " + f.type + " at " + f.pos + " (+" + f.growAmount + ")  Score=" + score);
    }

    private void onGameOver(String reason) {
        System.out.println("[GameLoop] Game Over: " + reason + "  Final score=" + score);
        stop();
    }
}
import java.awt.Point;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class Main {
    public static void main(String[] args) throws Exception {
        final int width = 30;
        final int height = 20;

        // 初始化蛇（起点在地图中央，向右，初始长度 5）
        Snake snake = new Snake(new Point(width / 2, height / 2), Direction.RIGHT, 5);

        // 初始化世界管理器
        WorldManager world = new WorldManager(width, height, snake);

        // 生成初始内容
        world.spawnRandomFoods(3, 1, "NORMAL");
        world.generateRandomObstacles(10);
        Class<?> cls = Class.forName("GameLoop");
        Object engine = null;

        // 通过可见构造器寻找合适的签名（避免通过捕获异常来判断构造器）
        for (Constructor<?> c : cls.getConstructors()) {
            Class<?>[] params = c.getParameterTypes();
            if (params.length == 2 && params[0] == WorldManager.class && params[1] == Snake.class) {
                engine = c.newInstance(world, snake);
                break;
            }
            if (params.length == 2 && params[0] == Snake.class && params[1] == WorldManager.class) {
                engine = c.newInstance(snake, world);
                break;
            }
            if (params.length == 0) {
                engine = c.newInstance();
                break;
            }
        }
        if (engine != null) {
            String[] candidates = {"start", "run", "loop", "startLoop"};
            // 遍历公开方法，寻找可用的启动方法名
            for (Method m : cls.getMethods()) {
                for (String name : candidates) {
                    if (m.getName().equals(name) && m.getParameterCount() == 0) {
                        m.invoke(engine);
                        System.out.println("Launched GameLoop via " + name);
                        return;
                    }
                }
            }
            System.out.println("GameLoop instantiated but no start method found.");
        } else {
            System.out.println("No suitable GameLoop constructor found, fallback to demo loop.");
        }

        // 回退：简单的 headless 演示循环（ticks）
        for (int tick = 0; tick < 50; tick++) {
            snake.move();
            Point head = snake.getHead();
            WorldManager.FoodRecord eaten = world.consumeFoodAt(head);
            if (eaten != null) {
                snake.grow(eaten.growAmount);
                System.out.println("Tick " + tick + " - Ate " + eaten.type + " at " + head + " (+"
                        + eaten.growAmount + ")");
            }
            System.out.println("Tick " + tick + " - Head=" + head + " Length=" + snake.getLength());
            Thread.sleep(150);
        }
    }
}
public class ScoreManager {
    private int score = 0;
    
    // 记录连续吃相同类型食物的次数
    private FoodType lastFoodType = null;
    private int consecutiveCount = 0;

    public void eatFood(FoodType type) {
        // 如果吃的食物类型和上次不同，重置连续计数
        if (lastFoodType == null || lastFoodType != type) {
            lastFoodType = type;
            consecutiveCount = 1;
        } else {
            // 相同类型食物，连续计数+1
            consecutiveCount++;
        }
        
        // 根据食物类型和连续次数计算分数
        switch (type) {
            case NORMAL:
                // 普通食物：每个加1分，不考虑连续
                score += 1;
                break;
                
            case SPECIAL:
                // 二级食物：连续吃n个，分数加combo数的平方
                // 即：1² + 2² + 3² + ... + n²
                score += consecutiveCount * consecutiveCount;
                break;
                
            case RARE:
                // 三级食物：连续吃n个，分数加combo数的立方
                // 即：1³ + 2³ + 3³ + ... + n³
                score += consecutiveCount * consecutiveCount * consecutiveCount;
                break;
        }
        
        // 输出调试信息
        System.out.println("吃了" + type + "食物，连续" + consecutiveCount + "个，本次得分：" + 
            (type == FoodType.NORMAL ? 1 : 
             type == FoodType.SPECIAL ? consecutiveCount * consecutiveCount : 
             consecutiveCount * consecutiveCount * consecutiveCount));
    }

    public void resetCombo() {
        // 当吃到不同类型的食物或没吃到食物时，由外部调用
        lastFoodType = null;
        consecutiveCount = 0;
    }

    public int getScore() {
        return score;
    }
}
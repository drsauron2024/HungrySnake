public class ScoreManager {
    private int score = 0;
    private int combo = 0;
    private int highestCombo = 0;

    public void eatFood(FoodType type) {
        combo++;
        highestCombo = Math.max(highestCombo, combo);
        score += type.scoreForCombo(combo);
    }

    public void resetCombo() {
        combo = 0;
    }

    public int getScore() {
        return score;
    }

    public int getCombo() {
        return combo;
    }
    //ff
    public int getHighestCombo() {
        return highestCombo;
    }
}

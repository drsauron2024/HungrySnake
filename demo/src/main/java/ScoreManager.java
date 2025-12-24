public class ScoreManager {
    private int score = 0;
    private int combo = 0;
    private int highestCombo = 0;

    public int eatFood(FoodType type) {
        combo++;
        return switch (type) {
            case NORMAL -> 1;
            case SPECIAL -> (int) Math.pow(combo, 2);
            case RARE -> (int) Math.pow(combo, 3);
        };
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

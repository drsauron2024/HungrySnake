public enum FoodType {
    NORMAL{
        @Override
        public int getGrowth() {
            return 1;
        }
        @Override
        public int scoreForCombo(int combo) {
            return 1;
        }
    },
    SPECIAL{
        @Override
        public int getGrowth() {
            return 2;
        }
        @Override
        public int scoreForCombo(int combo) {
            return combo * combo;
        }
    },
    RARE{
        @Override
        public int getGrowth() {
            return 3;
        }
        @Override
        public int scoreForCombo(int combo) {
            return combo * combo * combo;
        }
    };

    public abstract int scoreForCombo(int combo);
    public abstract int getGrowth();
}

public enum FoodType {
    NORMAL{
        @Override
        public int scoreForCombo(int combo) {
            return 1;
        }
    },
    SPECIAL{
        public int getGrowth() {
            return 2;
        }
        @Override
        public int scoreForCombo(int combo) {
            return combo * combo;
        }
    },
    RARE{
        public int getGrowth() {
            return 3;
        }
        @Override
        public int scoreForCombo(int combo) {
            return combo * combo * combo;
        }
    };


    public abstract int scoreForCombo(int combo);
}

package two;

public class NutritionFacts {
    private final int servingSize; // required
    private final int servings; // required
    private final int calories; // optional
    private final int sodium; // optional
    private final int fat; // optional
    private final int carbohydrate; // optional

    public NutritionFacts(int servingSize, int servings) {
        this(servingSize, servings, 0);
    }

    public NutritionFacts(int servingSize, int servings, int calories) {
        this(servingSize, servings, calories, 0);
    }

    public NutritionFacts(int servingSize, int servings, int calories, int sodium) {
        this(servingSize, servings, calories, sodium, 0);
    }

    public NutritionFacts(int servingSize, int servings, int calories, int sodium, int fat) {
        this(servingSize, servings, calories, sodium, fat, 0);
    }


    public NutritionFacts(int servingSize, int servings, int calories, int sodium, int fat, int carbohydrate) {
        this.servingSize = servingSize;
        this.servings = servings;
        this.calories = calories;
        this.sodium = sodium;
        this.fat = fat;
        this.carbohydrate = carbohydrate;
    }

    public static void main(String[] args) {
        NutritionFacts cocaCola =
                new NutritionFacts(240, 8, 100, 0, 25, 27);
    }
}

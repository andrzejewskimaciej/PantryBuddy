import GUI.Frame;
import Style.Theme;
import basicDataTypes.Ingredient;


public class Main {
    public static void main(String[] args) {
        Theme.setTheme();
        Ingredient.removeUnnecessaryIngredients();
        new Frame();
    }



}
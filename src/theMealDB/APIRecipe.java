package theMealDB;

import java.util.Map;

record APIRecipe(
        APIMeal APIMeal,
        String category,
        String area,
        String instructions,
        String ytLink,
        Map<String, String> ingredientsToAmounts
) {

    @Override
    public String toString() {
        return "Recipe{" +
                "meal=" + APIMeal +
                ", category='" + category + '\'' +
                ", area='" + area + '\'' +
                ", ytLink='" + ytLink + '\'' +
                ", ingredientsToAmounts=" + ingredientsToAmounts +
                ", instructions='" + instructions + '\'' +
                '}';
    }
}


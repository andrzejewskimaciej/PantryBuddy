package theMealDB;

import basicDataTypes.Amount;
import basicDataTypes.Ingredient;
import basicDataTypes.Meal;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import database.DatabaseConnection;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

public class JsonToSQLiteLoader {
    private List<APIRecipe> APIRecipeList;
    private List<APIIngredient> APIIngredientList;
    private List<Meal> meals;
    private Map<String,Ingredient> ingredients;

    public static void main(String[] args) {
        JsonToSQLiteLoader jsonToSQLiteLoader = new JsonToSQLiteLoader();
        jsonToSQLiteLoader.loadJSON();
        jsonToSQLiteLoader.convert();
        jsonToSQLiteLoader.addToDatabase();
        jsonToSQLiteLoader.cleanDatabase();
        Fixes.manualFixCategories();
        Fixes.setEssentials();
        DatabaseConnection.backupBuiltins();
    }

    static void addLoaded(List<APIIngredient> apiIngredientList, List<APIRecipe> apiRecipeList) {
        JsonToSQLiteLoader jsonToSQLiteLoader = new JsonToSQLiteLoader();
        jsonToSQLiteLoader.APIIngredientList = apiIngredientList;
        jsonToSQLiteLoader.APIRecipeList = apiRecipeList;
        jsonToSQLiteLoader.convert();
        jsonToSQLiteLoader.addToDatabase();
    }

    private void loadJSON () {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            APIRecipeList = objectMapper.readValue(new File("data/recipes.json"),
                    new TypeReference<>() {
                    });
            APIIngredientList = objectMapper.readValue(new File("data/ingredients.json"),
                    new TypeReference<>() {
                    });
            APIIngredientList = APIIngredientList.stream()
                    .map(x -> {
                        if (Objects.equals(x.type(), "null")) {
                            x = new APIIngredient(
                                    x.name(),
                                    x.description(),
                                    null
                            );
                        }
                        return x;
                    })
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void convert() {
        ingredients = new HashMap<>();
        int i = 0;
        for (; i < APIIngredientList.size(); i++) {
            APIIngredient apiIngredient = APIIngredientList.get(i);
            Ingredient ingredient = new Ingredient(
                    i,
                    apiIngredient.name().toLowerCase(),
                    apiIngredient.description(),
                    apiIngredient.type()
            );
            ingredients.put(ingredient.name().toLowerCase(), ingredient);
        }

        meals = new ArrayList<>();
        for (APIRecipe apiRecipe:APIRecipeList) {
            List<Amount> amounts = new ArrayList<>();
            for (String APIIngredientName:apiRecipe.ingredientsToAmounts().keySet()) {
                if (APIIngredientName == null || APIIngredientName.equals("null")) continue;
                var properIngredientName =
                        Fixes.fixIngredientName(APIIngredientName.toLowerCase());
                if (ingredients.get(properIngredientName) == null) {
                    ingredients.put(properIngredientName, new  Ingredient(
                            i,
                            properIngredientName,
                            "",
                            null
                    ));
                            i++;
                }
                String amountText = apiRecipe.ingredientsToAmounts().get(APIIngredientName);
                if (!Objects.equals(properIngredientName, APIIngredientName.toLowerCase())) {
                    amountText = "(" + APIIngredientName.toLowerCase() + ") " + amountText;
                }
                Amount amount = new Amount(
                        ingredients.get(properIngredientName),
                        amountText,
                        0
                );
                amounts.add(amount);
            }
            Meal meal = new Meal(
                    Integer.parseInt(apiRecipe.APIMeal().id()),
                    apiRecipe.APIMeal().name(),
                    downloadImage(apiRecipe.APIMeal().thumbnail_url()),
                    apiRecipe.category(),
                    apiRecipe.area(),
                    false,
                    apiRecipe.ytLink(),
                    apiRecipe.instructions(),
                    amounts,
                    false);
            meals.add(meal);
        }
    }

    private static BufferedImage downloadImage(String url) {
        try {
            URL imgUrl;
            try {
                imgUrl = new URI(url).toURL();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            BufferedImage img = ImageIO.read(imgUrl);
            if (img == null) {
                System.out.println("failed download for " + url);
            }
            return img;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addToDatabase() {
        try {
            var oldCommit = DatabaseConnection.conn.getAutoCommit();
            DatabaseConnection.conn.setAutoCommit(false);
            try {
                for (Ingredient ingredient:ingredients.values()) {
                    ingredient.addToDatabase();
                }
                for (Meal meal:meals) {
                    meal.addToDatabase();
                }
            } catch (SQLException e) {
                DatabaseConnection.conn.rollback();
                throw new RuntimeException(e);
            }
            DatabaseConnection.conn.commit();
            DatabaseConnection.conn.setAutoCommit(oldCommit);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void cleanDatabase() {
        var sql = "DELETE FROM ingredients " +
                "WHERE ingredients.id IN (SELECT ingredients.id " +
                "FROM ingredients LEFT JOIN amounts ON ingredients.id = amounts.ingredient_id " +
                "WHERE amounts.ingredient_id IS NULL);";
        try (var stmt = DatabaseConnection.conn.createStatement()) {
            var oldCommit = DatabaseConnection.conn.getAutoCommit();
            DatabaseConnection.conn.setAutoCommit(false);
            stmt.execute(sql);
            DatabaseConnection.conn.commit();
            DatabaseConnection.conn.setAutoCommit(oldCommit);
        } catch (SQLException e) {
            try {
                DatabaseConnection.conn.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
    }

}

/*
SELECT *
FROM ingredients LEFT JOIN amounts ON ingredients.id = amounts.ingredient_id
WHERE amounts.ingredient_id IS NULL;

SELECT *
FROM ingredients LEFT JOIN amounts ON ingredients.id = amounts.ingredient_id
WHERE ingredients.name LIKE '%water' AND ingredients.name != 'water';
 */

/*
DELETE FROM ingredients 
WHERE ingredients.id IN (SELECT ingredients.id 
FROM ingredients LEFT JOIN amounts ON ingredients.id = amounts.ingredient_id 
WHERE amounts.ingredient_id IS NULL);

SELECT * FROM ingredients
WHERE ingredients.id IN (SELECT ingredients.id
                         FROM ingredients LEFT JOIN amounts ON ingredients.id = amounts.ingredient_id
                         WHERE amounts.ingredient_id IS NULL);
 */
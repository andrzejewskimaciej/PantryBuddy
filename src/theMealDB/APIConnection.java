package theMealDB;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


class APIConnection {
    /*
    NIE WYKORZYSTYWAĆ PROGRAMATYCZNIE W INNYCH MIEJSCACH
    Pakiet ten powinien być uruchamiany jedynie ręcznie poprzez funkcję "main"
    w celu pozyskania danych z API serwisu theMealDB.
    Uzyskane dane są umieszczane w odpowiednich plikach w katalogu "data".
    Pozyskanie wszystkich danych nam potrzebnych z serwisu jest bardzo
    zadaniem bardzo obciążającym API i regularnie powoduje błąd 429 (Too many requests),
    jako że wymagane zapytania HTTP są wykonywane w pętli dla 303 obiektów.
    Z szacunku dla API, pozyskawszy pożądane, dane nie należy go uruchamiać.
     */
    public static void main(String[] args) {
        List<APIIngredient> APIIngredientList = getIngredientList();
        List<String> areaList = getAreaList();
        List<String> categoryList = getCategoryList();
        List<APIMeal> APIMealList = categoryList.stream()
                .flatMap(s -> getMealListForCategory(s).stream())
                .toList();
        List<APIRecipe> APIRecipeList = APIMealList.stream()
                .filter(Objects::nonNull)
                .map(APIConnection::getRecipeForMeal)
                .toList();

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(new File("data/ingredients.json"), APIIngredientList);
            objectMapper.writeValue(new File("data/areas.json"), areaList);
            objectMapper.writeValue(new File("data/categories.json"), categoryList);
            objectMapper.writeValue(new File("data/meals.json"), APIMealList);
            objectMapper.writeValue(new File("data/recipes.json"), APIRecipeList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JsonToSQLiteLoader.addLoaded(APIIngredientList, APIRecipeList);
    }

    private static APIRecipe getRecipeForMeal(APIMeal APIMeal) {
        JsonNode response;
        try {
            response = jsonFromUrl("https://www.themealdb.com/api/json/v1/1/lookup.php?i=" + APIMeal.id());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        JsonNode recipe = response.get("meals").get(0);
        Map<String, String> ingredientsToAmounts = new HashMap<>();
        for (int i = 1; i <= 20; i++) {
            String ingredient = recipe.get("strIngredient" + i).asText();
            String amount = recipe.get("strMeasure" + i).asText();
            if (ingredient.isEmpty()) break;
            ingredientsToAmounts.put(ingredient, amount);
        }
        return new APIRecipe(APIMeal,
                recipe.get("strCategory").asText(),
                recipe.get("strArea").asText(),
                recipe.get("strInstructions").asText(),
                recipe.get("strYoutube").asText(),
                ingredientsToAmounts
                );
    }

    private static List<APIMeal> getMealListForCategory(String category) {
        JsonNode response;
        try {
            response = jsonFromUrl("https://www.themealdb.com/api/json/v1/1/filter.php?c=" + category);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        JsonNode meals = response.get("meals");
        List<APIMeal> categoriesList = new ArrayList<>();
        for (JsonNode arrayItem : meals) {
            categoriesList.add(
                    new APIMeal(
                            arrayItem.get("strMeal").asText(),
                            arrayItem.get("idMeal").asText(),
                            arrayItem.get("strMealThumb").asText()
                    )
            );
        }

        return categoriesList;
    }

    private static List<String> getAreaList() {
        JsonNode response;
        try {
            response = jsonFromUrl("https://www.themealdb.com/api/json/v1/1/list.php?a=list");
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        JsonNode areas = response.get("meals");
        List<String> areaList = new ArrayList<>();
        for (JsonNode arrayItem : areas) {
            areaList.add(
                    arrayItem.get("strArea").asText()
            );
        }

        return areaList;
    }

    private static List<String> getCategoryList() {
        JsonNode response;
        try {
            response = jsonFromUrl("https://www.themealdb.com/api/json/v1/1/list.php?c=list");
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        JsonNode categories = response.get("meals");
        List<String> categoryList = new ArrayList<>();
        for (JsonNode arrayItem : categories) {
            categoryList.add(
                            arrayItem.get("strCategory").asText()
            );
        }

        return categoryList;
    }

    private static List<APIIngredient> getIngredientList() {
        JsonNode response;
        try {
            response = jsonFromUrl("https://www.themealdb.com/api/json/v1/1/list.php?i=list");
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        JsonNode ingredients = response.get("meals");
        List<APIIngredient> APIIngredientList = new ArrayList<>();
        for (JsonNode arrayItem : ingredients) {
            APIIngredientList.add(
                    new APIIngredient(
                            arrayItem.get("strIngredient").asText(),
                            arrayItem.get("strDescription").asText(),
                            arrayItem.get("strType").asText()
                    )
            );
        }

        return APIIngredientList;
    }

    private static JsonNode jsonFromUrl(String url) throws IOException, URISyntaxException {
        HttpURLConnection connection = getHTTPConnection(url);
        return jsonFromHTTPConnection(connection);
    }

    private static JsonNode jsonFromHTTPConnection(HttpURLConnection connection) throws IOException {
        InputStream inStream;
        inStream = connection.getInputStream();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(inStream);
    }

    private static HttpURLConnection getHTTPConnection(String url)
            throws URISyntaxException, IOException {
        HttpURLConnection connection =
                (HttpURLConnection) new URI(url)
                        .toURL()
                        .openConnection();
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        if (connection.getResponseCode() != 200) {
            if (connection.getResponseCode() == 429) {
                System.err.println("Encountered error 429(Too Many Requests), waiting 10s for cooldown to end");
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return getHTTPConnection(url);
            }
            System.err.println("Response code: "
                    + connection.getResponseCode()
                    + "\n" + connection.getResponseMessage()
            );
        }
        return connection;
    }
}



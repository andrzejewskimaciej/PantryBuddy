package theMealDB;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import database.DatabaseConnection;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

class Fixes {
    private static final Map<String, String> replacements = loadReplacements();

    private static Map<String, String> loadReplacements() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(new File("data/fixups/replacements.json"),
                    new TypeReference <>() {});
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    protected static String fixIngredientName(String toBeFixed) {
        var fix= replacements.get(toBeFixed);
        return fix==null?toBeFixed:fix;
    }

    public static void main(String[] args) {
        manualFixCategories();
        setEssentials();
    }

    protected static void manualFixCategories() {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map;
        try {
            map = mapper.readValue(new File("data/fixups/manualCategory.json"),
                    new TypeReference <>() {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (String mealName : map.keySet()) {
            var sql = "UPDATE meals " +
                    "SET category = \"" + map.get(mealName) + "\" " +
                    " WHERE name = \"" + mealName + "\";";
            try (var stmt = DatabaseConnection.conn.createStatement()){
                var oldAutoCommit = DatabaseConnection.conn.getAutoCommit();
                DatabaseConnection.conn.setAutoCommit(false);
                stmt.execute(sql);
                DatabaseConnection.conn.commit();
                DatabaseConnection.conn.setAutoCommit(oldAutoCommit);
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

    protected static void setEssentials() {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, List<String>> map;
        try {
            map = mapper.readValue(new File("data/fixups/essentials.json"),
                    new TypeReference <>() {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (String category : map.keySet()) {
            var ingredients = map.get(category).stream()
                    .map(s -> "'" + s + "'")
                    .collect(Collectors.joining(", "));
            var sql = "UPDATE amounts " +
                    "SET priority = 1 " +
                    "WHERE rowid IN " +
                    " (SELECT a.rowid " +
                    "FROM meals m JOIN amounts a ON m.id = a.meal_id JOIN main.ingredients i ON i.id = a.ingredient_id " +
                    "WHERE i.name IN (" +
                    ingredients + ")" +
                    (Objects.equals(category, "All")? "":
                    " AND category = \"" + category + "\"") +
                    ");";
            try (var stmt = DatabaseConnection.conn.createStatement()){
                stmt.execute(sql);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
/*
SELECT a.rowid
FROM meals m JOIN amounts a ON m.id = a.meal_id JOIN main.ingredients i ON i.id = a.ingredient_id
WHERE category = 'Beef' AND i.name IN ('beef', 'beef fillet')
 */

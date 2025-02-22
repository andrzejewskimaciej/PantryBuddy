package Features;

import database.DatabaseConnection;

import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class SearchByName extends JPanel {

    private static final ArrayList<Integer> ingredientsIDList = new ArrayList<>();

    public static void addIngredientToList(int ingredient_id){
        if (!ingredientsIDList.contains(ingredient_id)) {
            ingredientsIDList.add(ingredient_id);
        }
    }

    public static void clearList(){
        ingredientsIDList.clear();
    }

    public static ResultSet SearchingByNameIngredients(JTextField textfield, boolean isShuffled,
                                                       ArrayList<String> catList, ArrayList<String> areaList) {
        String insertedMealName = "'" + textfield.getText() + "'"; //quotes in case meal name has space inside
        return queryDatabase(insertedMealName, isShuffled, catList, areaList);
    }



    private static ResultSet queryDatabase(String insertedMealName, boolean isShuffled,
                                           ArrayList<String> catList, ArrayList<String> areaList) {
        var sqlPart1 = "SELECT m.id, m.name, m.category, m.area, m.is_favourite, i.image ";
        var sqlPart2 = "FROM meals m LEFT JOIN images i ON m.id = i.meal_id ";
        var sqlPart3 = "WHERE m.name LIKE '%'||" + insertedMealName + "||'%' ";
        if (!catList.isEmpty()){
            sqlPart3 += "AND m.category IN (" + "?,".repeat(catList.size() - 1) + "?) ";
        }
        if (!areaList.isEmpty()){
            sqlPart3 += "AND m.area IN (" + "?,".repeat(areaList.size() - 1) + "?) ";
        }

        if (isShuffled) {
            sqlPart3 += " ORDER BY RANDOM() ";
        } else {

            sqlPart3 += " ORDER BY m.is_favourite DESC";
        }
        if (ingredientsIDList.isEmpty()) {
            var sql = sqlPart1 + sqlPart2 + sqlPart3;
                    //"SELECT name, category, area, is_favourite, thumbnail_img_id FROM meals WHERE name LIKE '%'||" + insertedMealName + "||'%' ";
            try {
                var pstmt = DatabaseConnection.conn.prepareStatement(sql);
                prepareCategoryAreaForSQL(pstmt, catList, areaList);
                return pstmt.executeQuery();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            sqlPart3 = "WHERE m.name LIKE '%'||" + insertedMealName + "||'%' ";
            if (!catList.isEmpty()){
                sqlPart3 += "AND m.category IN (" + "?,".repeat(catList.size() - 1) + "?) ";
            }
            if (!areaList.isEmpty()){
                sqlPart3 += "AND m.area IN (" + "?,".repeat(areaList.size() - 1) + "?) ";
            }
            var sql = sqlPart1 +
                    ", COUNT(*) AS cnt " +
                    sqlPart2 +
                    "INNER JOIN amounts a ON m.id = a.meal_id " +
                    "INNER JOIN ingredients ing ON ing.id = a.ingredient_id " +
                    sqlPart3 +
                    "AND ing.id IN (" + "?,".repeat(ingredientsIDList.size() - 1) + "?) " +
                    "GROUP BY m.id HAVING cnt >= " + ingredientsIDList.size() + " ORDER BY RANDOM()";

            try {
                var pstmt = DatabaseConnection.conn.prepareStatement(sql);
                int counter  = prepareCategoryAreaForSQL(pstmt, catList, areaList);
                for (int i = 0; i < ingredientsIDList.size(); i++) {
                    pstmt.setInt(counter + i + 1, ingredientsIDList.get(i));
                }
                return pstmt.executeQuery();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static ResultSet GetOnlyFavourites(JTextField textfield) {
        String insertedMealName = "'" + textfield.getText() + "'";
        var sqlPart1 = "SELECT m.id, m.name, m.category, m.area, m.is_favourite, i.image ";
        var sqlPart2 = "FROM meals m LEFT JOIN images i ON m.id = i.meal_id ";
        var sqlPart3 = "WHERE m.name LIKE '%'||" + insertedMealName + "||'%' AND m.is_favourite = 1 ";


            var sql = sqlPart1 + sqlPart2 + sqlPart3;
            //"SELECT name, category, area, is_favourite, thumbnail_img_id FROM meals WHERE name LIKE '%'||" + insertedMealName + "||'%' ";
            try {
                var stmt = DatabaseConnection.conn.createStatement();
                return stmt.executeQuery(sql);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

    }

    private static int prepareCategoryAreaForSQL(java.sql.PreparedStatement pstmt,
                                                                   ArrayList<String> catList, ArrayList<String> areaList){
        for (int i = 0; i < catList.size(); i++) {
            try {
                pstmt.setString(i + 1, catList.get(i));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        int counter = catList.size();

        for (int i = 0; i < areaList.size(); i++) {
            try {
                pstmt.setString(counter + i + 1, areaList.get(i));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        counter += areaList.size();
        return counter;
    }



}

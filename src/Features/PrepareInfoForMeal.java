package Features;

import database.DatabaseConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class PrepareInfoForMeal {

    /**
     * @return whole recipe for chosen meal in form of arraylists inside arraylist.
     * The first element of list are instructions, the second is link if available and
     * then every element is (if exists) a list with info about unique ingredient and its amount
     */
    public static ArrayList<ArrayList<String>> prepareRecipeForChosenMeal(int meal_id) {
        var sql =
                "SELECT m.instructions, m.link, ing.name, ing.description, ing.type, " +
                        "a.amount, a.priority " +
                        "FROM meals m " +
                        "INNER JOIN amounts a ON m.id = a.meal_id " +
                        "INNER JOIN ingredients ing ON ing.id = a.ingredient_id " +
                        "WHERE m.id = " + meal_id;

        try {
            var stmt = DatabaseConnection.conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            boolean whether_first = true;
            ArrayList<ArrayList<String>> recipeInfoList = new ArrayList<>();
            while (rs.next()) {
                if (whether_first){
                    recipeInfoList.add(new ArrayList<>(Collections.singleton(rs.getString("instructions"))));
                    recipeInfoList.add(new ArrayList<>(Collections.singleton(rs.getString("link"))));
                    whether_first = false;
                }
                recipeInfoList.add(new ArrayList<>(Arrays.asList(rs.getString("name"),
                        rs.getString("description"), rs.getString("type"),
                        rs.getString("amount"), String.valueOf(rs.getInt("priority")))));
            }
            return recipeInfoList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }


    //var sql1 =
    //                "SELECT m.instructions, m.link, ing.name, ing.description, ing.type, " +
    //                        "a.amount, a.priority " +
    //                        "FROM meals m " +
    //                        "LEFT JOIN amounts a ON m.id = a.meal_id " +
    //                        "LEFT JOIN ingredients ing ON ing.id = a.ingredient_id " +
    //                        "WHERE m.id = " + meal_id;
//        var sql1 =
//                "SELECT m.instructions, m.link " +
//                        "FROM meals m " +
//                        "WHERE m.id = " + meal_id;
//        var sql2 =
//                "SELECT ing.name, ing.description, ing.type, " +
//                        "a.amount, a.priority " +
//                        "FROM amounts a " +
//                        "INNER JOIN ingredients ing ON ing.id = a.ingredient_id " +
//                        "WHERE a.meal_id = " + meal_id;
//        try {
//            var stmt1 = DatabaseConnection.conn.createStatement();
//            var stmt2 = DatabaseConnection.conn.createStatement();
//            ResultSet rs1 = stmt1.executeQuery(sql1);
//            ResultSet rs2 = stmt2.executeQuery(sql2);
//            //boolean whether_first = true;
//            ArrayList<ArrayList<String>> recipeInfoList = new ArrayList<>();
////            while (rs1.next()) {
////                if (whether_first){
////                    recipeInfoList.add(new ArrayList<>(Collections.singleton(rs1.getString("instructions"))));
////                    recipeInfoList.add(new ArrayList<>(Collections.singleton(rs1.getString("link"))));
////                    whether_first = false;
////                }
////                recipeInfoList.add(new ArrayList<>(Arrays.asList(rs1.getString("name"),
////                        rs1.getString("description"), rs1.getString("type"),
////                        rs1.getString("amount"), String.valueOf(rs1.getInt("priority")))));
////
////            }
//            while (rs1.next()) {
//                recipeInfoList.add(new ArrayList<>(Collections.singleton(rs1.getString("instructions"))));
//                recipeInfoList.add(new ArrayList<>(Collections.singleton(rs1.getString("link"))));
//            }
//            while (rs2.next()) {
//                recipeInfoList.add(new ArrayList<>(Arrays.asList(rs2.getString("name"),
//                    rs2.getString("description"), rs2.getString("type"),
//                    rs2.getString("amount"), String.valueOf(rs2.getInt("priority")))));
//            }
//            return recipeInfoList;
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }

}

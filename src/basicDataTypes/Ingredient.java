package basicDataTypes;

import database.DatabaseConnection;

import java.sql.SQLException;

public record Ingredient(int id,
                         String name,
                         String description,
                         String type) {

    public void addToDatabase() throws SQLException {
        var sqlIngredients =
                "INSERT INTO ingredients(id, name, description, type)" +
                        "   VALUES(?,?,?,?)";
            var pstmt = DatabaseConnection.conn.prepareStatement(sqlIngredients);
            int ing_id = id;
            if (id == -1) {ing_id = findFreeIngredientId();}
            pstmt.setInt(1, ing_id);
            pstmt.setString(2, name);
            pstmt.setString(3, description);
            pstmt.setString(4, type);
            pstmt.execute();
    }

private static int findFreeIngredientId() {
    var sql =
            "SELECT coalesce(min(t1.id) + 1, 0) as free_id " +
                    "FROM ingredients t1 LEFT OUTER JOIN" +
                    "   ingredients t2 " +
                    "   ON t1.id = t2.id - 1 " +
                    "WHERE t2.id IS NULL;";
    try (var stmt = DatabaseConnection.conn.createStatement();
         var rs = stmt.executeQuery(sql)) {
        return rs.getInt("free_id");
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
}

    public static String[] getIngredientsTypesList() {
        var sql = "SELECT DISTINCT type FROM ingredients";
        try (var pstmt = DatabaseConnection.conn.prepareStatement(sql);
             var rs = pstmt.executeQuery()) {
            var types = new java.util.ArrayList<String>();
            while (rs.next()) {
                types.add(rs.getString(1));
            }
            return types.toArray(new String[0]);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static String[] getAllIngredientsNames() {
        var sql = "SELECT name FROM ingredients";
        try (var pstmt = DatabaseConnection.conn.prepareStatement(sql);
             var rs = pstmt.executeQuery()) {
            var ingredients = new java.util.ArrayList<String>();
            while (rs.next()) {
                ingredients.add(rs.getString(1));
            }
            return ingredients.toArray(new String[0]);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Ingredient findIngredientByName(String name) throws SQLException {
        var sql = "SELECT * FROM ingredients WHERE name = ?";
        var pstmt = DatabaseConnection.conn.prepareStatement(sql);
        pstmt.setString(1, name);
        var rs = pstmt.executeQuery();
        rs.next();
        return new Ingredient(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4));
    }

    public static Ingredient findIngredientById(int ingredientId) throws SQLException {
        var sql = "SELECT * FROM ingredients WHERE id = ?";
        var pstmt = DatabaseConnection.conn.prepareStatement(sql);
        pstmt.setInt(1, ingredientId);
        var rs = pstmt.executeQuery();
        rs.next();
        return new Ingredient(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4));
    }

    @Override
    public String name() {
        return name;
    }

    public static void removeUnnecessaryIngredients(){
        var sql = "SELECT ingredients.id FROM ingredients " +
                "LEFT JOIN amounts ON ingredients.id = amounts.ingredient_id " +
                "WHERE amounts.ingredient_id IS NULL";
        try (var stmt = DatabaseConnection.conn.createStatement();
             var rs = stmt.executeQuery(sql)){
            while (rs.next()) {
                var id = rs.getInt("id");
                var sql2 = "DELETE FROM ingredients WHERE id = " + id;
                stmt.execute(sql2);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

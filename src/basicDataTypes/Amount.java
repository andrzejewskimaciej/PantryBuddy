package basicDataTypes;

import database.DatabaseConnection;

import java.sql.SQLException;

public record Amount(Ingredient ingredient,
                     String amount,
                     int priority) {
    public Amount {
        if (ingredient == null) throw new IllegalArgumentException();
        if (amount == null) throw new IllegalArgumentException();
    }

    public void addToDatabase(int mealId) throws SQLException {
        var sqlAmounts =
                "INSERT INTO amounts(meal_id, ingredient_id, amount, priority)" +
                        "   VALUES(?,?,?,?)";
        var pstmt = DatabaseConnection.conn.prepareStatement(sqlAmounts);
        pstmt.setInt(1, mealId);
        pstmt.setInt(2, ingredient.id());
        pstmt.setString(3, amount);
        pstmt.setInt(4, priority);
        pstmt.execute();

    }
    public static void deleteAmount(int mealId, int ingredientId) throws SQLException {
        var sqlDeleteAmount =
                "DELETE FROM amounts " +
                        "WHERE meal_id = ? AND ingredient_id = ?";
        var pstmt = DatabaseConnection.conn.prepareStatement(sqlDeleteAmount);
        pstmt.setInt(1, mealId);
        pstmt.setInt(2, ingredientId);
        pstmt.execute();
    }
    public String getIgredientName() {
        return ingredient.name();
    }

    @Override
    public String amount() {
        return amount;
    }

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public Ingredient ingredient() {
        return ingredient;
    }
}

package basicDataTypes;

import database.DatabaseConnection;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public record Meal(int id,
                   String name,
                   BufferedImage thumbnail,
                   String category,
                   String area,
                   boolean is_favourite,
                   String link,
                   String instructions,
                   List<Amount> amounts,
                   boolean is_custom
) {

    public Meal(int id,
                String name,
                BufferedImage thumbnail,
                String category,
                String area,
                boolean is_favourite,
                String link,
                String instructions,
                List<Amount> amounts) {
        this(id,
                name,
                thumbnail,
                category,
                area,
                is_favourite,
                link,
                instructions,
                amounts,
                true);
    }

    private static int findFreeImageId() {

        var sql =
                "SELECT coalesce(min(t1.id) + 1, 0) as free_id " +
                        "FROM images t1 LEFT OUTER JOIN" +
                        "   images t2 " +
                        "   ON t1.id = t2.id - 1 " +
                        "WHERE t2.id IS NULL;";
        try (var stmt = DatabaseConnection.conn.createStatement();
             var rs = stmt.executeQuery(sql)){
            return rs.getInt("free_id");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private static int findFreeMealId () {

        var sql =
                "SELECT coalesce(min(t1.id) + 1, 0) as free_id " +
                        "FROM meals t1 LEFT OUTER JOIN" +
                        "   meals t2 " +
                        "   ON t1.id = t2.id - 1 " +
                        "WHERE t2.id IS NULL;";
        try (var stmt = DatabaseConnection.conn.createStatement();
             var rs = stmt.executeQuery(sql)){
            return rs.getInt("free_id");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String[] getCategoriesList() {

        var sql = "SELECT DISTINCT category FROM meals";
        try (var stmt = DatabaseConnection.conn.createStatement();
             var rs = stmt.executeQuery(sql)) {
            var categories = new java.util.ArrayList<String>();
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
            return categories.toArray(new String[0]);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String[] getAreasList() {

        var sql = "SELECT DISTINCT area FROM meals";
        try (var stmt = DatabaseConnection.conn.createStatement();
             var rs = stmt.executeQuery(sql)) {
            var areas = new java.util.ArrayList<String>();
            while (rs.next()) {
                areas.add(rs.getString("area"));
            }
            return areas.toArray(new String[0]);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addToDatabase() throws SQLException {
        var sqlMeals =
                "INSERT INTO meals(id, name, thumbnail_img_id, category," +
                        " area, is_favourite, link, instructions, is_custom)" +
                        "   VALUES(?,?,?,?,?,?,?,?,?)";
        var sqlImages =
                "INSERT INTO images(id, meal_id, image)" +
                        "   VALUES(?,?,?)";
        int thumbnail_id = findFreeImageId();
        var pstmt = DatabaseConnection.conn.prepareStatement(sqlMeals);
        int meal_id = id;
        if (id == -1) {meal_id = findFreeMealId();}
        pstmt.setInt(1, meal_id);
        pstmt.setString(2, name);
        pstmt.setString(4, category);
        pstmt.setString(5, area);
        pstmt.setBoolean(6, is_favourite);
        pstmt.setString(7, link);
        pstmt.setString(8, instructions);
        pstmt.setBoolean(9, is_custom);
        pstmt.execute();
        if (thumbnail != null) {
            pstmt = DatabaseConnection.conn.prepareStatement(sqlImages);
            pstmt.setInt(1, thumbnail_id);
            pstmt.setInt(2, meal_id);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ImageIO.write(thumbnail, "jpg", baos);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            pstmt.setBytes(3, baos.toByteArray());
            pstmt.execute();
            var updateThumbnailSQL =    "UPDATE meals " +
                                        "SET thumbnail_img_id = ? " +
                                        "WHERE id = ?";
            try (var stmt = DatabaseConnection.conn.prepareStatement(updateThumbnailSQL)) {
                stmt.setInt(1, thumbnail_id);
                stmt.setInt(2, meal_id);
                stmt.execute();
            }
        }

        if (amounts == null) return;
        for (Amount amount:amounts) {
            amount.addToDatabase(meal_id);
        }
    }


    public static boolean deleteMeal(int id) {
        var verifySql = "SELECT is_custom FROM meals WHERE id = ?";
        var deleteSql = "DELETE FROM meals WHERE id = ?";
        try (
                var pstmtVerify = DatabaseConnection.conn.prepareStatement(verifySql);
                var pstmtDelete = DatabaseConnection.conn.prepareStatement(deleteSql)
        ) {
            var oldCommit = DatabaseConnection.conn.getAutoCommit();
            DatabaseConnection.conn.setAutoCommit(false);
            pstmtVerify.setInt(1, id);
            var rs = pstmtVerify.executeQuery();
            if (rs.next() && !rs.getBoolean("is_custom")) {
                JOptionPane.showMessageDialog(null, "Could not delete builtin meal", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            pstmtDelete.setInt(1, id);
            int affectedRows = pstmtDelete.executeUpdate();
            boolean ifSuccessful;
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(null, "Meal deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                ifSuccessful = true;
            } else {
                JOptionPane.showMessageDialog(null, "Meal not found.", "Error", JOptionPane.ERROR_MESSAGE);
                ifSuccessful = false;
            }
            DatabaseConnection.conn.commit();
            DatabaseConnection.conn.setAutoCommit(oldCommit);
            return ifSuccessful;
        } catch (SQLException e) {
            try {
                DatabaseConnection.conn.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
    }

    public void updateToDatabase() {

    var updateMealSql = "UPDATE meals SET name = ?, category = ?, area = ?, is_favourite = ?, link = ?, instructions = ? WHERE id = ?";
    var updateImageSql = "UPDATE images SET image = ? WHERE meal_id = ?";
    var insertImageSql = "INSERT INTO images(id, meal_id, image) VALUES(?,?,?)";

    try (
        var pstmtMeal = DatabaseConnection.conn.prepareStatement(updateMealSql);
        var pstmtImageUpdate = DatabaseConnection.conn.prepareStatement(updateImageSql);
        var pstmtImageInsert = DatabaseConnection.conn.prepareStatement(insertImageSql)
    ) {
        pstmtMeal.setString(1, name);
        pstmtMeal.setString(2, category);
        pstmtMeal.setString(3, area);
        pstmtMeal.setBoolean(4, is_favourite);
        pstmtMeal.setString(5, link);
        pstmtMeal.setString(6, instructions);
        pstmtMeal.setInt(7, id);
        pstmtMeal.executeUpdate();

        if (thumbnail != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(thumbnail, "jpg", baos);
            var thumbnailBytes = baos.toByteArray();

            pstmtImageUpdate.setBytes(1, thumbnailBytes);
            pstmtImageUpdate.setInt(2, id);
            int affectedRows = pstmtImageUpdate.executeUpdate();
            if (affectedRows == 0) {
                int thumbnail_id = findFreeImageId();
                pstmtImageInsert.setInt(1, thumbnail_id);
                pstmtImageInsert.setInt(2, id);
                pstmtImageInsert.setBytes(3, thumbnailBytes);
                pstmtImageInsert.executeUpdate();
            }
        } else {
            var deleteImageSql = "DELETE FROM images WHERE meal_id = ?";
            var pstmtDeleteImage = DatabaseConnection.conn.prepareStatement(deleteImageSql);
            pstmtDeleteImage.setInt(1, id);
            pstmtDeleteImage.executeUpdate();
        }

        if (amounts != null) {
            for (Amount amount : amounts) {
                var amountInDbSql = "SELECT EXISTS (SELECT * FROM amounts WHERE meal_id = ? AND ingredient_id = ? AND amount = ? AND priority = ?)";
                try (var pstmt = DatabaseConnection.conn.prepareStatement(amountInDbSql)) {
                    pstmt.setInt(1, id);
                    pstmt.setInt(2, amount.ingredient().id());
                    pstmt.setString(3, amount.amount());
                    pstmt.setInt(4, amount.priority());
                    var rs = pstmt.executeQuery();
                    if (rs.next() && rs.getBoolean(1)) {
                        continue;
                    }
                }
                amount.addToDatabase(id);
            }
        }
    } catch (IOException | SQLException e) {
        throw new RuntimeException(e);
    }
}
    }


/*
UPDATE meals
    SET is_custom=TRUE
 */
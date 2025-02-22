package database;

import org.sqlite.SQLiteConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;

public class DatabaseConnection {
    private static String url = "jdbc:sqlite:data/database.db";

    public static Connection conn = establishConnection();

    static {
        createTables();
    }

    private static Connection establishConnection() {
        try {
            SQLiteConfig sqLiteConfig = new SQLiteConfig();
            sqLiteConfig.enforceForeignKeys(true);
            Connection conn = DriverManager.getConnection(url, sqLiteConfig.toProperties());
            System.out.println("Connection to SQLite has been established.");
            return conn;
        } catch (SQLException e) {
//            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void createTables() {
        String sqlMeals =
                "CREATE TABLE IF NOT EXISTS meals (" +
                        "id INTEGER PRIMARY KEY," +
                        "name TEXT NOT NULL," +
                        "category TEXT," +
                        "area TEXT," +
                        "is_favourite INTEGER NOT NULL DEFAULT FALSE," +
                        "instructions TEXT," +
                        "link TEXT," +
                        "thumbnail_img_id INTEGER DEFAULT NULL," +
                        "is_custom INTEGER NOT NULL DEFAULT TRUE, " +
                        "FOREIGN KEY (thumbnail_img_id) " +
                        "REFERENCES images (id)" +
                        "   ON DELETE SET DEFAULT " +
                        "   ON UPDATE CASCADE " +
                        ")";

        String sqlIngredients =
                "CREATE TABLE IF NOT EXISTS ingredients (" +
                        "id INTEGER PRIMARY KEY," +
                        "name TEXT UNIQUE NOT NULL," +
                        "description TEXT," +
                        "type TEXT," +
                        "is_in_pantry INTEGER NOT NULL DEFAULT FALSE" +
                        ")";

        String sqlAmounts =
                "CREATE TABLE IF NOT EXISTS amounts (" +
                        "meal_id INTEGER NOT NULL," +
                        "ingredient_id INTEGER NOT NULL," +
                        "amount TEXT NOT NULL," +
                        "priority INTEGER," +
                        "FOREIGN KEY (meal_id) " +
                        "REFERENCES meals (id)" +
                        "   ON DELETE CASCADE " +
                        "   ON UPDATE CASCADE," +
                        "FOREIGN KEY (ingredient_id) " +
                        "REFERENCES ingredients (id) " +
                        "   ON DELETE CASCADE " +
                        "   ON UPDATE CASCADE " +
                        ")";

        String sqlImages =
                "CREATE TABLE IF NOT EXISTS images (" +
                        "id INTEGER PRIMARY KEY," +
                        "meal_id INTEGER NOT NULL," +
                        "image BLOB NOT NULL," +
                        "FOREIGN KEY (meal_id) " +
                        "REFERENCES meals (id)" +
                        "   ON DELETE CASCADE " +
                        "   ON UPDATE CASCADE " +
                        ")";

        try (Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sqlMeals);
            stmt.execute(sqlIngredients);
            stmt.execute(sqlAmounts);
            stmt.execute(sqlImages);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    protected static void initTestDatabase () throws IOException {
        Path source = Paths.get("data/database.db");
        Path dest = Paths.get("data/testDatabase.db");
        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);

//        assertEquals(Files.readAllBytes(source), Files.readAllBytes(dest));

        url = "jdbc:sqlite:data/testDatabase.db";
        conn = establishConnection();
    }

    public static void closeConnection() {
        try {
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

//    public static void main(String[] args) {
////        backupBuiltins();
//        resetBuiltinsFromBackup();
//    }

    public static void backupBuiltins() {
        var sqlMeals = "CREATE TABLE IF NOT EXISTS builtin_meals " +
                "AS SELECT id, name, category, area, instructions, link, thumbnail_img_id " +
                "FROM meals " +
                "WHERE is_custom = FALSE; ";
        var sqlIngredients = "CREATE TABLE IF NOT EXISTS builtin_ingredients " +
                "AS SELECT *" +
                " FROM ingredients; ";
        var sqlAmounts = "CREATE TABLE IF NOT EXISTS builtin_amounts " +
                "AS SELECT * " +
                "FROM amounts";
        var sqlImages = "CREATE TABLE IF NOT EXISTS builtin_images " +
                "AS SELECT * " +
                "FROM images; ";
        try (var stmt = conn.createStatement()){
            var oldAutocommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            stmt.execute(sqlMeals);
            stmt.execute(sqlIngredients);
            stmt.execute(sqlAmounts);
            stmt.execute(sqlImages);
            conn.commit();
            conn.setAutoCommit(oldAutocommit);
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
    }

    public static void resetBuiltinsFromBackup() {
        var sqlMeals = "UPDATE meals " +
                "SET name = b.name," +
                " category = b.category," +
                " area = b.area," +
                " instructions = b.instructions," +
                " link = b.link," +
                " thumbnail_img_id = b.thumbnail_img_id " +
                " FROM builtin_meals AS b" +
                " WHERE meals.id = b.id;";

        var sqlImages1 = "INSERT INTO images (id, meal_id, image)  " +
                "SELECT b.id, b.meal_id, b.image " +
                "FROM builtin_images b LEFT JOIN images i ON b.id = i.id " +
                "WHERE i.id IS NULL ;";

        var sqlImages2 = "UPDATE images " +
                "SET meal_id = b.meal_id," +
                " image = b.image" +
                " FROM builtin_images AS b" +
                " WHERE images.id = b.id;";

        var sqlIngredients = "INSERT INTO ingredients (id, name, description, type, is_in_pantry)  " +
                "SELECT b.*" +
                " FROM builtin_ingredients AS b LEFT JOIN ingredients i ON b.id = i.id " +
                " WHERE i.id IS NULL ;";

        var sqlAmounts1 = "DELETE FROM amounts " +
                "WHERE NOT (SELECT is_custom FROM meals WHERE meals.id = amounts.meal_id);";

        var sqlAmounts2 = "INSERT INTO amounts " +
                "SELECT b.*" +
                "FROM builtin_amounts AS b LEFT JOIN amounts a " +
                "ON b.meal_id = a.meal_id " +
                "AND b.ingredient_id = a.ingredient_id" +
                " WHERE a.meal_id IS NULL ;";


        try (var stmt = conn.createStatement()){
            var oldAutocommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            stmt.execute(sqlImages1);
            stmt.execute(sqlImages2);
            stmt.execute(sqlIngredients);
            stmt.execute(sqlMeals);
            stmt.execute(sqlAmounts1);
            stmt.execute(sqlAmounts2);
            conn.commit();
            conn.setAutoCommit(oldAutocommit);
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
    }
}

/*
UPDATE meals
SET name = 'test'
 */

/*
PRAGMA FOREIGN_KEYS = ON;
UPDATE images
    SET image = (SELECT image FROM images LIMIT 1);
DELETE FROM images
    WHERE mod(id, 2) = 0
 */

/*
SELECT *
FROM meals
WHERE mod(thumbnail_img_id, 2) = 0;
    SELECT *
FROM images
    WHERE mod(id, 2) =0
 */

/*
DELETE FROM amounts
    WHERE meal_id = 52958
 */
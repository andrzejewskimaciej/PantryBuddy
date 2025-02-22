package Tests;
import GUI.MealOnList;
import Style.Theme;
import basicDataTypes.Meal;
import database.DatabaseConnection;
import org.junit.jupiter.api.*;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class BasicTests {
    @BeforeAll
    public static void initAll() {
        try {
            Method method = DatabaseConnection.class.getDeclaredMethod("initTestDatabase");
            method.setAccessible(true);
            method.invoke(null);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        Theme.setTheme();
    }
    @AfterAll
    public static void cleanupAll() {
        try {
            Method method = DatabaseConnection.class.getDeclaredMethod("closeConnection");
            method.setAccessible(true);
            method.invoke(null);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        File file = new File("data/testDatabase.db");
        if (!file.delete()) {
            throw new RuntimeException("Could not delete mock database under path " + file.getPath());
        }
    }

    @Test
    void mealAddToDatabase () throws SQLException {
        Meal meal = new Meal(-1,
                "testMeal",
                null,
                "testCategory",
                "testArea",
                false,
                "link",
                "make a test meal",
                null
                );
        BufferedImage image = new BufferedImage(1, 2, BufferedImage.TYPE_3BYTE_BGR);
        Meal meal2 = new Meal(-2,
                "testMeal2",
                image,
                "testCategory",
                "testArea",
                true,
                "link",
                "make a test meal",
                null
        );
        meal.addToDatabase();
        meal2.addToDatabase();
        assertThrows(SQLException.class, meal2::addToDatabase);
        var sqlMeal = "SELECT * FROM meals WHERE id < 0;";

        var stmt = DatabaseConnection.conn.createStatement();
        var rs = stmt.executeQuery(sqlMeal);
        Meal meal3 = null;
        while (rs.next()){
            System.out.println("Test mealAddToDatabase: " +
                    "found the following meal for the query " + sqlMeal);
            Meal mealTemp = new Meal(rs.getInt("id"),
                    rs.getString("name"),
                    null,
                    rs.getString("category"),
                    rs.getString("area"),
                    rs.getBoolean("is_favourite"),
                    rs.getString("link"),
                    rs.getString("instructions"),
                    null);
            System.out.println(mealTemp);
            if (mealTemp.id() == -1) meal3 = mealTemp;
        }
        assertEquals(meal, meal3);
        var sqlImage = "SELECT * FROM images WHERE meal_id = -2;";
        rs = stmt.executeQuery(sqlImage);
        assertTrue(rs.next());
        System.out.println("Test mealAddToDatabase: " +
                "found the following image for the query " + sqlImage);
        System.out.println("id: " + rs.getInt("id") +
                ", meal_id: " + rs.getInt("meal_id") +
                ", image blob no. of bytes: " + rs.getBytes("image").length);
    }

    @Test
    void testDisplaySearchedMeals() throws InterruptedException {
        JPanel mealsPanel;
        JScrollPane scrollPane = new JScrollPane();
        JPanel mainPanel = new JPanel();
        mainPanel.add(scrollPane);
        var sqlEven = "SELECT * " +
                "FROM meals " +
                "LEFT JOIN images ON meals.id = images.meal_id " +
                "WHERE mod(meals.id, 2) = 0;";
        var sqlOdd = "SELECT * " +
                "FROM meals " +
                "LEFT JOIN images ON meals.id = images.meal_id " +
                "WHERE mod(meals.id, 2) = 1;";
        try {
            var stmt = DatabaseConnection.conn.createStatement();
            var rs = stmt.executeQuery(sqlEven);
            mealsPanel = MealOnList.displaySearchedMeals(mainPanel, rs, scrollPane);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Thread.sleep(3000);
        var evens = Arrays.stream(mealsPanel.getComponents()).toList();
        try {
            var stmt = DatabaseConnection.conn.createStatement();
            var rs = stmt.executeQuery(sqlOdd);
            mealsPanel = MealOnList.displaySearchedMeals(mainPanel, rs, scrollPane);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Thread.sleep(3000);
        var odds = Arrays.stream(mealsPanel.getComponents()).toList();
        evens.forEach(System.out::println);
        System.out.println("-----------------------------------------------------------");
        odds.forEach(System.out::println);
        System.out.println("-----------------------------------------------------------");
        try {
            var stmt = DatabaseConnection.conn.createStatement();
            var rs = stmt.executeQuery(sqlEven);
            mealsPanel = MealOnList.displaySearchedMeals(mainPanel, rs, scrollPane);
            Thread.sleep(1000);
            Arrays.stream(mealsPanel.getComponents()).forEach(System.out::println);
            var rs2 = stmt.executeQuery(sqlOdd);
            mealsPanel = MealOnList.displaySearchedMeals(mainPanel, rs2, scrollPane);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("-----------------------------------------------------------");
        var problematic = Arrays.stream(mealsPanel.getComponents()).toList();
        assertTrue(
                problematic.stream()
                        .filter(evens::contains)
                        .findAny()
                        .isEmpty()
        );
    }

}

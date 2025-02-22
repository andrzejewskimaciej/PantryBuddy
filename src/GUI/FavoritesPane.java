package GUI;

import database.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;

import static Features.SearchByName.*;

public class FavoritesPane extends JPanel implements ActionListener, Refreshable<FavoritesPane>{

    private static final JButton searchByNameButton = new JButton("Search");
    private static final JTextField searchByNameTextF = new JTextField(20);
    JScrollPane scrollMealsListPanel;

    public FavoritesPane() {

        this.setLayout(new BorderLayout(0, 0));

        JPanel searchPanel = new JPanel(new BorderLayout(20, 20));
        this.add(searchPanel, BorderLayout.NORTH);
        scrollMealsListPanel = new JScrollPane();
        this.add(scrollMealsListPanel, BorderLayout.CENTER);
        scrollMealsListPanel.getVerticalScrollBar().setUnitIncrement(20);


        JPanel searchByNamePanel = new JPanel();
        searchPanel.add(searchByNamePanel, BorderLayout.CENTER);

        searchByNamePanel.add(searchByNameTextF, BorderLayout.CENTER);
        JPanel searchButtonsPanel = new JPanel(new FlowLayout());
        searchButtonsPanel.add(searchByNameButton);
        searchByNamePanel.add(searchButtonsPanel);
        searchByNameTextF.addActionListener(this);
        searchByNameButton.addActionListener(this);
        searchMeals();

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (searchByNameButton.equals(e.getSource()) || searchByNameTextF.equals(e.getSource())) {
            searchMeals();
        }
    }

    private void searchMeals() {
        if (!areThereFavourites()) {
            JPanel panel = new JPanel();
            JLabel infoLabel = new JLabel();
            infoLabel.setFont(infoLabel.getFont().deriveFont(Font.PLAIN + Font.ITALIC,
                    infoLabel.getFont().getSize() * 2));
            infoLabel.setText("There are currently no favourite meals");
            panel.add(infoLabel);
            scrollMealsListPanel.setViewportView(panel);
            return;
        }
        clearList();
        ResultSet rs = GetOnlyFavourites(searchByNameTextF);
        try {
            MealOnList.displaySearchedMeals(this, rs, scrollMealsListPanel);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public FavoritesPane getRefreshed() {
        searchMeals();
        return this;
    }

    private boolean areThereFavourites() {
        var sql = "SELECT COUNT(*) FROM meals WHERE is_favourite = TRUE";
        try (var stmt = DatabaseConnection.conn.createStatement();
            var rs = stmt.executeQuery(sql)){
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
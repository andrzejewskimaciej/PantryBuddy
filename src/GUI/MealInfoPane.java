package GUI;

import GUI.AddMealPane.EditingMealPane;
import Style.Theme;
import basicDataTypes.Meal;
import database.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MealInfoPane extends JPanel implements Refreshable<MealInfoPane> {

    private final JButton deleteButton;
    private final JButton editButton;
    public final MealOnList mealOnList;
    private final JTextField linkTextField;

    public MealInfoPane(MealOnList mealOnList) {
        this.mealOnList = mealOnList;
        //setLayout(new BorderLayout());
        setLayout(new BorderLayout(8,8));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel("Name: " + mealOnList.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD,
                nameLabel.getFont().getSize()*2));
        infoPanel.add(nameLabel);

        JLabel categoryAreaLabel = new JLabel("Category: " + mealOnList.getCategory() + ", Area: " + mealOnList.getArea());
        categoryAreaLabel.setFont(new Font("Arial", Font.ITALIC,
                categoryAreaLabel.getFont().getSize()));
        infoPanel.add(categoryAreaLabel);

        JLabel instructionsTitleLabel = new JLabel("Instructions:");
        instructionsTitleLabel.setFont(new Font("Arial", Font.BOLD,
                instructionsTitleLabel.getFont().getSize()*3/2));


        JTextArea instructionsTextArea = new JTextArea();
        instructionsTextArea.setEditable(false);
        instructionsTextArea.setLineWrap(true);
        instructionsTextArea.setColumns(20);

        JScrollPane amountTextAreaScroll = new JScrollPane();

        try {
            int mealId = mealOnList.getId();
            String sql = "SELECT m.instructions, m.name AS meal_name, ing.name AS ingredient_name, ing.description, ing.type, a.amount, a.priority " +
                    "FROM meals m " +
                    "INNER JOIN amounts a ON m.id = a.meal_id " +
                    "INNER JOIN ingredients ing ON ing.id = a.ingredient_id " +
                    "WHERE m.id = ?";
            PreparedStatement pstmt = DatabaseConnection.conn.prepareStatement(sql);
            pstmt.setInt(1, mealId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                instructionsTextArea.setText(rs.getString("instructions"));

                JLabel amountTitleLabel = new JLabel("Ingredients with their amounts:");
//                amountTitleLabel.setFont(new Font("Arial", Font.BOLD, 18));
                amountTitleLabel.setFont(new Font("Arial", Font.BOLD,
                        amountTitleLabel.getFont().getSize()*3/2));
                infoPanel.add(amountTitleLabel);

                JTextArea amountTextArea = new JTextArea();
//                amountTextArea.setFont(new Font("Arial", Font.PLAIN, 13));
                amountTextArea.setEditable(false);
                amountTextArea.setLineWrap(true);
                amountTextArea.setColumns(20);
                amountTextAreaScroll.setViewportView(amountTextArea);
                infoPanel.add(amountTextAreaScroll);

                do {
                    amountTextArea.append(String.format("- %s: %s\n", rs.getString("ingredient_name"), rs.getString("amount")));
                } while (rs.next());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        infoPanel.add(instructionsTitleLabel);
        JScrollPane instructionScrollPane = new JScrollPane(instructionsTextArea);
        infoPanel.add(instructionScrollPane);
        SwingUtilities.invokeLater(() -> {instructionScrollPane.getVerticalScrollBar().setValue(0);
            amountTextAreaScroll.getVerticalScrollBar().setValue(0);});


        Listener listener = new Listener();

        linkTextField = new JTextField(20);
        linkTextField.setEditable(false);
        linkTextField.setBorder(BorderFactory.createTitledBorder("Link"));
        linkTextField.setToolTipText("Click to open in browser");
        linkTextField.addMouseListener(listener);

        linkTextField.setForeground(Color.CYAN);
        linkTextField.setFont(linkTextField.getFont().deriveFont(Font.ITALIC));
        linkTextField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));



        infoPanel.add(linkTextField);
        try {
            PreparedStatement stmt = DatabaseConnection.conn.prepareStatement(
                    "SELECT link FROM meals WHERE id = ?");
            stmt.setInt(1, mealOnList.getId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                linkTextField.setText(rs.getString("link"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        linkTextField.setMaximumSize(new Dimension(3 * Theme.screenDimensions.width / 5, Theme.screenDimensions.height / 15));


        JPanel imageAndButtonsPanel = new JPanel(new BorderLayout());

        BufferedImage image = mealOnList.getImageBytes();
        if (image != null) {
            JLabel imageLabel = new JLabel(new ImageIcon(image.getScaledInstance(
                    Theme.screenDimensions.width/5*2,
                    Theme.screenDimensions.height/7*2,
                    Image.SCALE_DEFAULT)));
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
            imageAndButtonsPanel.add(imageLabel, BorderLayout.CENTER);
        }

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridBagLayout());

        deleteButton = new JButton("Delete recipe");
        //deleteButton.setBackground(Color.RED);
        deleteButton.setBackground(new Color(204, 0, 0));
        deleteButton.addActionListener(listener);


        editButton = new JButton("Edit recipe");
        editButton.setBackground(Color.BLUE);
        editButton.addActionListener(_ ->
                new GoBackPanel(
                        getParent(),
                        new EditingMealPane(mealOnList),
                        () -> {
                            int result = JOptionPane.showConfirmDialog(this, "Do you want to discard changes?", "Discard changes", JOptionPane.YES_NO_OPTION);
                            return result == JOptionPane.YES_OPTION;
                        }
                ));

        JButton favoriteButton = getFavoriteButton(mealOnList);

        //buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        //buttonsPanel.add(editButton);
        buttonsPanel.add(favoriteButton, adjustButtonAlignment(0));
        buttonsPanel.add(editButton, adjustButtonAlignment(1));
        //buttonsPanel.add(Box.createVerticalStrut(Theme.screenDimensions.height / 5));
        buttonsPanel.add(deleteButton, adjustButtonAlignment( 2));
//        buttonsPanel.add(deleteButton);
//        buttonsPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        //imageAndButtonsPanel.add(buttonsPanel, BorderLayout.SOUTH);
        imageAndButtonsPanel.add(buttonsPanel, BorderLayout.EAST);

        //add(imageAndButtonsPanel, BorderLayout.EAST);
        add(imageAndButtonsPanel, BorderLayout.NORTH);
        add(infoPanel, BorderLayout.CENTER);


    }

    private static JButton getFavoriteButton(MealOnList mealOnList) {
        JButton favoriteButton = new JButton();
        favoriteButton.setFont(
                favoriteButton.getFont().deriveFont(Font.BOLD,
                        favoriteButton.getFont().getSize()*2)
        );
        favoriteButton.setForeground(Theme.favouriteYellow);
        favoriteButton.setText(
                mealOnList.getIsFavourite()?"★":"☆"
        );
        favoriteButton.addActionListener(_ -> {
            var sql = "UPDATE meals " +
                    "SET is_favourite = ?" +
                    "WHERE id = ?";
            try (var pstmt = DatabaseConnection.conn.prepareStatement(sql)){
                var oldAutocommit = DatabaseConnection.conn.getAutoCommit();
                DatabaseConnection.conn.setAutoCommit(false);
                pstmt.setBoolean(1, !mealOnList.getIsFavourite());
                pstmt.setInt(2, mealOnList.getId());
                pstmt.executeUpdate();
                DatabaseConnection.conn.commit();
                DatabaseConnection.conn.setAutoCommit(oldAutocommit);
                mealOnList.setIsFavourite(!mealOnList.getIsFavourite());
                favoriteButton.setText(
                        mealOnList.getIsFavourite()?"★":"☆"
                );
            } catch (SQLException ex) {
                try {
                    DatabaseConnection.conn.rollback();
                } catch (SQLException exc) {
                    throw new RuntimeException(exc);
                }
                throw new RuntimeException(ex);
            }
        });


        favoriteButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                if (mealOnList.getIsFavourite()){
                    favoriteButton.setToolTipText("Click to remove meal from Favorites");
                } else {
                    favoriteButton.setToolTipText("Click to add meal to Favorites");
                }
            }
        });

        return favoriteButton;
    }

    @Override
    public MealInfoPane getRefreshed() {
        return new MealInfoPane(mealOnList.getRefreshed());
    }

    private class Listener extends MouseAdapter implements ActionListener {


        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == deleteButton) {
                int n = JOptionPane.showConfirmDialog(
                        null,
                        "Do you really want to delete recipe from database?",
                        "Delete recipe?",
                        JOptionPane.YES_NO_OPTION);
                if (n == JOptionPane.YES_OPTION) {
                    boolean ifDeleted = Meal.deleteMeal(mealOnList.getId());
                    if (ifDeleted) {
                        Container parent = mealOnList.getParent();
                        parent.remove(mealOnList);
                    }

                }
            }
            if (e.getSource() == editButton){
                new GoBackPanel(MealInfoPane.this, new EditingMealPane(mealOnList));


            }
        }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
                    String link = linkTextField.getText();
                    if (Desktop.isDesktopSupported() && link != null && !link.isEmpty()) {
                        try {
                            Desktop.getDesktop().browse(new URI(link));
                        } catch (IOException | URISyntaxException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }



    }

    private GridBagConstraints adjustButtonAlignment(int rowNumber){
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipadx = Theme.screenDimensions.width / 200;
        c.ipady = Theme.screenDimensions.height / 200;
        //c.weightx = 10.0;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(0,0,Theme.screenDimensions.height / 200,Theme.screenDimensions.width / 30);
        c.gridx = 0;
        c.gridy = rowNumber;
        return c;
    }
}
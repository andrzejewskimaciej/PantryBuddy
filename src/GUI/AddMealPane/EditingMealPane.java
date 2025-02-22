package GUI.AddMealPane;

import GUI.GoBackPanel;
import GUI.MealOnList;
import Style.Theme;
import basicDataTypes.Amount;
import basicDataTypes.Meal;
import database.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

public class EditingMealPane extends AddNewMealRecipePane{
private final MealOnList mealOnList;

    public EditingMealPane(MealOnList mealOnList){
        super();
        this.mealOnList = mealOnList;
        this.isImageLoaded = mealOnList.isImageLoaded();
        setVisible(false);
        AddNewMealRecipePane.sizeAdapter sizeAdapter = new AddNewMealRecipePane.sizeAdapter();
        sizeAdapter.componentResized(null);

        headerLabel.setText("You are editing existing recipe");
        nameField.setText(mealOnList.getName());
        categoryComboBox.setSelectedItem(mealOnList.getCategory());
        areaComboBox.setSelectedItem(mealOnList.getArea());
        isFavouriteField.setSelected(mealOnList.getIsFavourite());

        amountsPanel.addAmount(mealOnList.getAmountsList());

        thumbnail = mealOnList.getImageBytes();
        repaintImageStatus();


        try {
            PreparedStatement stmt = DatabaseConnection.conn.prepareStatement(
                    "SELECT link, instructions " +
                    "FROM meals " +
                    "WHERE id = ?");

            stmt.setInt(1, mealOnList.getId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                linkField.setText(rs.getString("link"));
                instructionsField.setText(rs.getString("instructions"));
            }
        } catch (SQLException e) {
            throw new RuntimeException();
        }

        listener listener = new listener();
        addRecipeButton.setText("Save changes");
        //addRecipeButton.setMaximumSize(new Dimension(2* addRecipeButton.getPreferredSize().width, addRecipeButton.getPreferredSize().height * 2));
        addRecipeButton.setPreferredSize(new Dimension(Theme.screenDimensions.width / 13,
                Theme.screenDimensions.height / 20));
        addRecipeButton.removeActionListener(addRecipeButton.getActionListeners()[0]);
        addRecipeButton.addActionListener(listener);
        removeImageButton.addActionListener(listener);

        setVisible(true);
    }

    @Override
    protected void repaintImageStatus() {
        isImageLoadedField.setText(!(isImageLoaded) ? "(No image loaded)" : "(Image already loaded)");
        removeImageButton.setVisible(isImageLoaded);
        repaint();
    }

    private class listener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == addRecipeButton) {
                String name = nameField.getText();
                String category = Objects.requireNonNull(categoryComboBox.getSelectedItem()).toString();
                String area = Objects.requireNonNull(areaComboBox.getSelectedItem()).toString();
                boolean isFavourite = isFavouriteField.isSelected();
                String link = linkField.getText();
                String instructions = instructionsField.getText();
                ArrayList<Amount> amountList = amountsPanel.getAmountList();
                ArrayList<Amount> removedAmounts = amountsPanel.getRemovedAmounts();

                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(EditingMealPane.this, "Please fill name field.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }

//                mealOnList.setImageLoaded(isImageLoaded);

                for (Amount amount : removedAmounts) {
                    boolean isIngredientInList = false;
                    for (Amount a : amountList) {
                        if (a.ingredient().id() == amount.ingredient().id()) {
                            isIngredientInList = true;
                            break;
                        }
                    }
                    if (!isIngredientInList) {
                        try {
                            Amount.deleteAmount(mealOnList.getId(), amount.ingredient().id());
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }


                Meal updatedMeal = new Meal(mealOnList.getId(), name, thumbnail, category, area, isFavourite, link, instructions, amountList);
                updatedMeal.updateToDatabase();
                JOptionPane.showMessageDialog(EditingMealPane.this, "Meal updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                SwingUtilities.invokeLater(() -> {
                    Container parent = getParent();
                    if (parent instanceof GoBackPanel) {
                        ((GoBackPanel) parent).goBackAndRefresh();
                    }
                });

            } else if (e.getSource() == removeImageButton){
                //System.out.println("click");
                thumbnail = null;
                isImageLoaded = false;
                repaintImageStatus();
            }
        }
    }


}

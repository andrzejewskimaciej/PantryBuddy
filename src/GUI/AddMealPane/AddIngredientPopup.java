package GUI.AddMealPane;

import basicDataTypes.Ingredient;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddIngredientPopup extends JDialog {
    private final JTextField nameField;
    private final JTextField descriptionField;
    private final JComboBox<String> typeComboBox;
    private final JButton addButton;
    private final Connection databaseConnection;
    private final JComboBox<String> amountsIngredientComboBox;
    public AddIngredientPopup(Frame owner, Connection databaseConnection, JComboBox<String> amountsIngredientComboBox) {
        super(owner, "Add new ingredient to database", true);
        this.databaseConnection = databaseConnection;
        this.amountsIngredientComboBox = amountsIngredientComboBox;
        setLayout(new GridLayout(4, 2, 10, 10));

        nameField = new JTextField(20);
        descriptionField = new JTextField(20);

        typeComboBox = new JComboBox<>(Ingredient.getIngredientsTypesList());
        typeComboBox.setEditable(true);
        AutoCompleteDecorator.decorate(typeComboBox);
        typeComboBox.setBorder(BorderFactory.createTitledBorder("Select or define new"));

        addButton = new JButton("Add");
        JButton cancelButton = new JButton("Cancel");

        add(new JLabel("Name:"));
        add(nameField);
        add(new JLabel("Description:"));
        add(descriptionField);
        add(new JLabel("Type:"));
        add(typeComboBox);
        add(addButton);
        add(cancelButton);

        addButton.addActionListener(new AddIngredientListener());
        cancelButton.addActionListener(_ -> dispose());

        pack();
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    private boolean checkIfIngredientAlreadyExists(String name){

        try (PreparedStatement pstmt = databaseConnection.prepareStatement(
                "SELECT COUNT(*) FROM ingredients WHERE name = ?")) {
            pstmt.setString(1, name);
            try (var rs = pstmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private class AddIngredientListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == addButton){
            String name = nameField.getText().toLowerCase();
            String description = descriptionField.getText();
            String type = (String) typeComboBox.getSelectedItem();
            if (!name.isEmpty()) {
                try {
                    if (!(checkIfIngredientAlreadyExists(name))) {
                        new Ingredient(-1, name, description, type).addToDatabase();
                        JOptionPane.showMessageDialog(AddIngredientPopup.this, "Ingredient added successfully.");
                        amountsIngredientComboBox.removeAllItems();
                        for (String IngredientName : Ingredient.getAllIngredientsNames()) {
                            amountsIngredientComboBox.addItem(IngredientName);
                        }
                    } else {
                        JOptionPane.showMessageDialog(AddIngredientPopup.this, "Ingredient already exists.");
                    }
                    dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AddIngredientPopup.this, "Error adding ingredient to database.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(AddIngredientPopup.this, "Please fill name field.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        }}
    }
}



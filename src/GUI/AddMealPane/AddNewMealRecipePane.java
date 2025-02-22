package GUI.AddMealPane;

import Style.Theme;
import basicDataTypes.Amount;
import basicDataTypes.Meal;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class AddNewMealRecipePane extends JPanel {
    protected final JTextField nameField;
    protected final JComboBox<String> categoryComboBox;
    protected final JComboBox<String> areaComboBox;
    protected final JCheckBox isFavouriteField;
    protected final JTextField linkField;
    protected final JTextArea instructionsField;
    protected final JButton addRecipeButton;
    protected final JButton loadImageButton;
    BufferedImage thumbnail;
    protected final MakeAmountListPanel amountsPanel;
    protected final JScrollPane amountsPanelScroll;
    protected final JScrollPane instructionsFieldScroll;
    protected final JPanel addingRecipeLabelPanel;
    protected final JLabel headerLabel;
    protected final JLabel isImageLoadedField;
    protected final JButton removeImageButton;
    protected boolean isImageLoaded;

    public AddNewMealRecipePane() {
        setLayout(new BorderLayout());

        sizeAdapter sizeAdapter = new sizeAdapter();
        addComponentListener(sizeAdapter);

        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));

        headerLabel = new JLabel("You are adding a new recipe");
        headerLabel.setFont(new Font("Arial", Font.BOLD, headerLabel.getFont().getSize()*3/2));
        addingRecipeLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addingRecipeLabelPanel.add(headerLabel);
        fieldsPanel.add(addingRecipeLabelPanel);

        nameField = new JTextField(20);
        nameField.setBorder(BorderFactory.createTitledBorder(null, "Name of meal" ,
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.ITALIC, Theme.screenDimensions.height / 80), new Color(0, 220, 0)));
        //nameField.setBackground(Theme.activeColor);
        fieldsPanel.add(nameField);
        fieldsPanel.add(Box.createVerticalGlue());

        amountsPanel = new MakeAmountListPanel();
        amountsPanelScroll = new JScrollPane(amountsPanel);
        amountsPanelScroll.setBorder(BorderFactory.createTitledBorder(null, "Ingredients and their amounts" ,
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.ITALIC, Theme.screenDimensions.height / 80), new Color(0, 220, 0)));
        amountsPanelScroll.getVerticalScrollBar().setUnitIncrement(16);
        fieldsPanel.add(amountsPanelScroll);
        fieldsPanel.add(Box.createVerticalGlue());

        instructionsField = new JTextArea();
        instructionsField.setColumns(20);
        instructionsField.setLineWrap(true);
        instructionsField.setWrapStyleWord(true);
        instructionsFieldScroll = new JScrollPane(instructionsField);
        instructionsFieldScroll.setBorder(BorderFactory.createTitledBorder(null, "Instructions",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.ITALIC, Theme.screenDimensions.height / 80), new Color(0, 220, 0)));
        fieldsPanel.add(instructionsFieldScroll);
        fieldsPanel.add(Box.createVerticalGlue());

        linkField = new JTextField(20);
        linkField.setBorder(BorderFactory.createTitledBorder(null, "Link (to external source)",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.ITALIC, Theme.screenDimensions.height / 80), new Color(0, 220, 0)));
        fieldsPanel.add(linkField);
        fieldsPanel.add(Box.createVerticalGlue());

        categoryComboBox = new JComboBox<>(Arrays.stream(Meal.getCategoriesList()).sorted().toArray(String[]::new));
        categoryComboBox.setEditable(true);
        AutoCompleteDecorator.decorate(categoryComboBox);
        categoryComboBox.setSelectedItem("");
        categoryComboBox.setBorder(BorderFactory.createTitledBorder(null, "Category (select existing or create new)",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.ITALIC, Theme.screenDimensions.height / 80), new Color(0, 220, 0)));
        fieldsPanel.add(categoryComboBox);
        fieldsPanel.add(Box.createVerticalGlue());

        areaComboBox = new JComboBox<>(Arrays.stream(Meal.getAreasList()).sorted().toArray(String[]::new));
        areaComboBox.setEditable(true);
        areaComboBox.setSelectedItem("");
        AutoCompleteDecorator.decorate(areaComboBox);
        areaComboBox.setBorder(BorderFactory.createTitledBorder(null, "Area (select existing or create new)",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.ITALIC, Theme.screenDimensions.height / 80), new Color(0, 220, 0)));
        fieldsPanel.add(areaComboBox);
        fieldsPanel.add(Box.createVerticalGlue());

        AddRecipeListener listener = new AddRecipeListener();

        loadImageButton = new JButton("Load Image");
        loadImageButton.addActionListener(_ -> {
            if ( chooseFile()) {
                isImageLoaded = true;
            }
            repaintImageStatus();
        });

        removeImageButton = new JButton("Remove Image");
        removeImageButton.setBackground(new Color(204, 0, 0));
        if (thumbnail == null) removeImageButton.setVisible(false);
        removeImageButton.addActionListener(listener);

        isImageLoadedField = new JLabel();
        repaintImageStatus();

        isFavouriteField = new JCheckBox("Is Favourite");

        JPanel leftAlignedPanel = new JPanel(new FlowLayout());
        leftAlignedPanel.add(removeImageButton);
        leftAlignedPanel.add(Box.createHorizontalStrut(Theme.screenDimensions.width / 60));
        leftAlignedPanel.add(loadImageButton);
        leftAlignedPanel.add(isImageLoadedField);
        leftAlignedPanel.add(Box.createHorizontalStrut(Theme.screenDimensions.width / 7));
        leftAlignedPanel.add(isFavouriteField);

        addRecipeButton = new JButton("Add Recipe");
        addRecipeButton.setFont(new Font("Arial", Font.BOLD, addRecipeButton.getFont().getSize()*8/7));
        addRecipeButton.addActionListener(listener);

        JPanel addRecipeButtonPanel = new JPanel(new FlowLayout());
        addRecipeButton.setPreferredSize(new Dimension(addRecipeButton.getPreferredSize().width, addRecipeButton.getPreferredSize().height * 2));
        addRecipeButtonPanel.add(addRecipeButton);


        fieldsPanel.add(leftAlignedPanel);
        fieldsPanel.add(addRecipeButtonPanel);

        add(fieldsPanel, BorderLayout.CENTER);


    }

    protected void repaintImageStatus(){
        isImageLoadedField.setText((thumbnail == null) ? "(No image loaded)" : "(Image already loaded)");
        removeImageButton.setVisible(thumbnail != null);
        repaint();
    }

    private class AddRecipeListener implements ActionListener {
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

                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(AddNewMealRecipePane.this, "Please fill name field.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Meal meal = new Meal(-1, name, thumbnail, category, area, isFavourite, link, instructions, amountList);
                try {
                    meal.addToDatabase();
                    JOptionPane.showMessageDialog(AddNewMealRecipePane.this, "Meal added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    SwingUtilities.invokeLater(() -> {
                        removeAll();
                        add(new AddNewMealRecipePane());
                        revalidate();
                        repaint();
                    });
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }

            else if (e.getSource() == removeImageButton){
                thumbnail = null;
                repaintImageStatus();
            }
        }

    }

    private boolean chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new ImageFilter());
        fileChooser.setAcceptAllFileFilterUsed(false);
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                thumbnail = ImageIO.read(selectedFile);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            return true;
        }
        return false;
    }

    private static class ImageFilter extends FileFilter {
        public final static String JPEG = "jpeg";
        public final static String JPG = "jpg";
        public final static String GIF = "gif";
        public final static String TIFF = "tiff";
        public final static String TIF = "tif";
        public final static String PNG = "png";

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String extension = getExtension(f);
            if (extension != null) {
                return extension.equals(TIFF) ||
                        extension.equals(TIF) ||
                        extension.equals(GIF) ||
                        extension.equals(JPEG) ||
                        extension.equals(JPG) ||
                        extension.equals(PNG);
            }
            return false;
        }

        @Override
        public String getDescription() {
            return "Image Only";
        }

        String getExtension(File f) {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');

            if (i > 0 &&  i < s.length() - 1) {
                ext = s.substring(i+1).toLowerCase();
            }
            return ext;
        }
    }

    protected class sizeAdapter extends ComponentAdapter {


        @Override
        public void componentResized(ComponentEvent e) {
            addingRecipeLabelPanel.setMaximumSize(new Dimension(4 *Theme.screenDimensions.width / 5, Theme.screenDimensions.height / 15));
            nameField.setMaximumSize(new Dimension(4 *Theme.screenDimensions.width / 5, Theme.screenDimensions.height / 15));
            amountsPanelScroll.setMaximumSize(new Dimension(4 *Theme.screenDimensions.width / 5, Theme.screenDimensions.height / 10));
            amountsPanel.setMaximumSize(new Dimension(4 *Theme.screenDimensions.width / 5, Theme.screenDimensions.height / 6));
            amountsPanel.updateSize();
            instructionsFieldScroll.setMaximumSize(new Dimension(4 *Theme.screenDimensions.width / 5, Theme.screenDimensions.height / 5));
            instructionsField.setMaximumSize(new Dimension(4 *Theme.screenDimensions.width / 5, Theme.screenDimensions.height / 5));
            instructionsField.setRows(Theme.screenDimensions.height / 85);
            linkField.setMaximumSize(new Dimension(4 *Theme.screenDimensions.width / 5, Theme.screenDimensions.height / 15));
            categoryComboBox.setMaximumSize(new Dimension(4 *Theme.screenDimensions.width / 5, Theme.screenDimensions.height / 15));
            areaComboBox.setMaximumSize(new Dimension(4 *Theme.screenDimensions.width / 5, Theme.screenDimensions.height / 15));
            addRecipeButton.setMaximumSize(new Dimension(2 *Theme.screenDimensions.width / 15, Theme.screenDimensions.height / 15));
            repaint();

        }
    }
}

package GUI.CustomCheckBox;

public class CheckItem {
    private final String text;
    private boolean selected;

    public CheckItem(String text, boolean selected) {
        this.text = text;
        this.selected = selected;
    }

    protected boolean isSelected() {
        return selected;
    }

    protected void setSelected(boolean isSelected) {
        selected = isSelected;
    }

    @Override
    public String toString() {
        return text;
    }
}

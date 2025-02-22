package theMealDB;

record APIIngredient(String name, String description, String type) {

    @Override
    public String toString() {
        return "Ingredient{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}

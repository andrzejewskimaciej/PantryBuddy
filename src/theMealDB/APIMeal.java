package theMealDB;

record APIMeal(String name, String id, String thumbnail_url) {

    @Override
    public String toString() {
        return "MealFromAPI{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", thumbnail_url='" + thumbnail_url + '\'' +
                '}';
    }

}

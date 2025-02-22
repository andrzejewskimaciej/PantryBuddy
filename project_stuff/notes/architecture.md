## Baza

### meals
1. id INTEGER PRIMARY KEY
2. name TEXT NOT NULL
3. thumbnail_img_id INTEGER
4. category TEXT
5. area TEXT
6. is_favourite INTEGER NOT NULL
7. instructions TEXT
8link TEXT

### ingredients
1. id INTEGER PRIMARY KEY
2. name TEXT NOT NULL
3. description TEXT
4. type TEXT

### amounts
1. meal_id INTEGER NOT NULL
2. ingredient_id INTEGER NOT NULL
3. amount TEXT NOT NULL
4. priority INTEGER

### images
1. id INTEGER PRIMARY KEY
2. meal_id INTEGER NOT NULL
3. image BLOB NOT NULL

## Opis

Powyżej znajdują się tabelki planowanej bazy wraz z kolejnymi kolumnami
w formie wyliczenia. Tabelki będą miały powiązania między sobą poprzez odpowiednie
pola z kluczami. Tak np. *meal_id* oznacza id z tabelki *meals*.
- Tabelka *meals* ma zwierać same opcje dań, które przechowuje aplikacja.
- W tabelce *ingredients* będą się znajdować składniki 
obsługiwane przez aplikację.
- W tabelce *amounts* będą pary indeksów z tabelki
*meals* i *ingredients* (wraz z dodatkowymi informacjami) oznaczające
użycie danego składnika do danego dania. 
- Tabelka images będzie przechowywała obrazki dla odpowiednich dań. 
Jeśli danie ma miniaturkę do wyświetlenia, to będzie ona oznaczona w tabelce *meals*
za pomocą id odpowiedniego obrazka z tabelki *images*

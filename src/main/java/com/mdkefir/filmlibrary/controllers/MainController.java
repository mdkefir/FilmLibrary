package com.mdkefir.filmlibrary.controllers;

import com.mdkefir.filmlibrary.models.Movie;
import com.mdkefir.filmlibrary.models.Series;
import com.mdkefir.filmlibrary.models.SportEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class MainController {

    @FXML private TextField searchField;

    @FXML
    private Label filmLabel;

    @FXML
    private TilePane moviesTilePane; // Это корневой элемент вашего интерфейса

    @FXML
    private void handleFilmsMenu() {
        // Обработка нажатия на кнопку "Фильмы"
    }

    @FXML
    private void handleSeriesMenu() {
        // Обработка нажатия на кнопку "Сериалы"
    }

    @FXML
    private void handleSearch() {
        // Обработка поиска
    }

    @FXML
    private ToggleButton moviesButton;

    @FXML
    private ToggleButton seriesButton;

    @FXML
    private ToggleButton sportsButton;

    @FXML
    private ToggleGroup categoryToggleGroup = new ToggleGroup();
    @FXML
    private List<Movie> getMovies() {
        return List.of(
                new Movie("Терминатор 2: СД", "/com/mdkefir/filmlibrary/images/movies/1.png"),
                new Movie("1+1", "/com/mdkefir/filmlibrary/images/movies/2.png"),
                new Movie("Ходячий замок", "/com/mdkefir/filmlibrary/images/movies/3.png"),
                new Movie("Список Шиндлера", "/com/mdkefir/filmlibrary/images/movies/4.png"),
                new Movie("Властелин колец", "/com/mdkefir/filmlibrary/images/movies/5.png"),
                new Movie("Бойцовский клуб", "/com/mdkefir/filmlibrary/images/movies/6.png"),
                new Movie("Интерстеллар", "/com/mdkefir/filmlibrary/images/movies/7.png"),
                new Movie("Форрест Гамп", "/com/mdkefir/filmlibrary/images/movies/8.png"),
                new Movie("Зеленая миля", "/com/mdkefir/filmlibrary/images/movies/9.png"),
                new Movie("Властелин колец 2", "/com/mdkefir/filmlibrary/images/movies/10.png")
                // Добавьте больше фильмов
        );
    }
    @FXML
    private List<Series> getSeries() {
        return List.of(
                new Series("Во все тяжкие", "/com/mdkefir/filmlibrary/images/series/1.png"),
                new Series("Игра престолов", "/com/mdkefir/filmlibrary/images/series/2.png"),
                new Series("Клан Сопрано", "/com/mdkefir/filmlibrary/images/series/3.png"),
                new Series("Офис", "/com/mdkefir/filmlibrary/images/series/4.png"),
                new Series("Друзья", "/com/mdkefir/filmlibrary/images/series/5.png"),
                new Series("Гравити Фолз", "/com/mdkefir/filmlibrary/images/series/6.png"),
                new Series("Тед Лассо", "/com/mdkefir/filmlibrary/images/series/7.png"),
                new Series("Атака Титанов", "/com/mdkefir/filmlibrary/images/series/8.png"),
                new Series("Шерлок", "/com/mdkefir/filmlibrary/images/series/9.png"),
                new Series("Чернобыль", "/com/mdkefir/filmlibrary/images/series/10.png"),
                new Series("Голяк", "/com/mdkefir/filmlibrary/images/series/11.png"),
                new Series("Крепость Бадабер", "/com/mdkefir/filmlibrary/images/series/12.png")
                // Добавьте больше сериалов
        );
    }
    @FXML
    private List<SportEvent> getSportEvents() {
        return List.of(
                new SportEvent("World Cup 2022", "/com/mdkefir/filmlibrary/images/sportevent/1.png"),
                new SportEvent("Олимпийские игры 2014", "/com/mdkefir/filmlibrary/images/sportevent/2.png")
                // Добавьте больше спортивных событий
        );
    }
    private Node getContentForMovies() {
        return createContentTilePane(getMovies());
    }

    private Node getContentForSeries() {
        return createContentTilePane(getSeries());
    }

    private Node getContentForSports() {
        return createContentTilePane(getSportEvents());
    }
    @FXML
    private VBox createVBoxForContent(String title, String imagePath) {
        VBox vbox = new VBox(5);
        InputStream is = getClass().getResourceAsStream(imagePath);
        if (is == null) {
            throw new IllegalArgumentException("ПОСОСИПОСОСИПОСОСИПОСОСИ " + imagePath);
        }
        Image image = new Image(is);
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(225);
        imageView.setFitWidth(150);
        Label label = new Label(title);
        vbox.getChildren().addAll(imageView, label);
        vbox.getStyleClass().add("filmListTile"); // Добавьте класс стилей, как в вашем FXML
        return vbox;
    }

    @FXML
    public void initialize() {
        // Назначаем все кнопки одной группе
        moviesButton.setToggleGroup(categoryToggleGroup);
        seriesButton.setToggleGroup(categoryToggleGroup);
        sportsButton.setToggleGroup(categoryToggleGroup);

        // настройка категорий для отображения
        moviesButton.setOnAction(event -> updateTilePaneContent(getContentForMovies()));
        seriesButton.setOnAction(event -> updateTilePaneContent(getContentForSeries()));
        sportsButton.setOnAction(event -> updateTilePaneContent(getContentForSports()));

        // По умолчанию показываем контент для фильмов
        updateTilePaneContent(getContentForMovies());

        // Найти все VBox'ы с классом "film-box"
        List<VBox> filmBoxes = moviesTilePane.getChildren().stream()
                .filter(node -> node instanceof VBox)
                .map(node -> (VBox) node)
                .filter(vbox -> vbox.getStyleClass().contains("filmListTile"))
                .collect(Collectors.toList());

        // Применить обработчики к найденным VBox'ам
        for (VBox vbox : filmBoxes) {
            vbox.setOnMouseEntered(event -> setLabelStyle(vbox, Color.WHITE));
            vbox.setOnMouseExited(event -> setLabelStyle(vbox, Color.web("#b1b3b4")));
        }

        // По умолчанию выбираем "Фильмы"
        moviesButton.setSelected(true);

    }


    public void handleTVMenu(ActionEvent actionEvent) {
    }

    public void applyFilters(ActionEvent actionEvent) {
    }

    private void setLabelStyle(VBox vbox, Color color) {
        vbox.getChildren().filtered(node -> node instanceof Label).forEach(node -> {
            ((Label) node).setTextFill(color);
        });
    }



    /* ChoiceBox ФИЛЬТРЫ*/
    @FXML
    private void onGenreSelected(ActionEvent event) {
        // Логика обработки выбора жанра
    }

    @FXML
    private void onYearSelected(ActionEvent event) {
        // Логика обработки выбора года
    }

    @FXML
    private void onCountrySelected(ActionEvent event) {
        // Логика обработки выбора страны
    }

    @FXML
    private void onRatingSelected(ActionEvent event) {
        // Логика обработки выбора рейтинга
    }
    @FXML
    private void updateTilePaneContent(Node content) {
        if (content instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) content;
            if (scrollPane.getContent() instanceof TilePane) {
                TilePane newTilePane = (TilePane) scrollPane.getContent();

                moviesTilePane.getChildren().clear(); // Очистка текущего содержимого TilePane
                moviesTilePane.getChildren().addAll(newTilePane.getChildren()); // Добавление нового содержимого
            }
        }
    }

    private Node createContentTilePane(List<?> contents) {
        TilePane tilePane = new TilePane();

        for (Object content : contents) {
            if (content instanceof Movie) {
                Movie movie = (Movie) content;
                VBox vbox = createVBoxForContent(movie.getTitle(), movie.getImagePath());
                tilePane.getChildren().add(vbox);
            } else if (content instanceof Series) {
                Series series = (Series) content;
                VBox vbox = createVBoxForContent(series.getTitle(), series.getImagePath());
                tilePane.getChildren().add(vbox);
            } else if (content instanceof SportEvent) {
                SportEvent sportEvent = (SportEvent) content;
                VBox vbox = createVBoxForContent(sportEvent.getTitle(), sportEvent.getImagePath());
                tilePane.getChildren().add(vbox);
            }
        }

        // Create ScrollPane and bind TilePane's preferred width/height to ScrollPane's viewport dimensions
        ScrollPane scrollPane = new ScrollPane(tilePane);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #1e252b;"); // Set the background color if not using CSS


        return scrollPane;
    }



}
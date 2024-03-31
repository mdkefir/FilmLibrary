package com.mdkefir.filmlibrary.controllers;

import com.mdkefir.filmlibrary.models.Movie;
import com.mdkefir.filmlibrary.models.Series;
import com.mdkefir.filmlibrary.models.SportEvent;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;
import java.io.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;

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

    public void getMovies() {
        new Thread(() -> {
            try {
                List<Movie> movies = parseKinopoisk();
                Platform.runLater(() -> {
                    // обновите ваш TilePane с помощью полученных данных
                    try {
                        updateTilePaneContent(movies);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public String downloadImage(String imageUrl, String s) throws IOException {
        // Create a URL object from the image URL string
        URL url = new URL(imageUrl);
        // Extract the file name from the URL
        String fileName = Paths.get(url.getPath()).getFileName().toString();
        // Define the path where the image will be saved
        Path targetPath = Paths.get(System.getProperty("user.dir")).resolve("images").resolve(fileName);

        // Open a stream to the image URL
        try (InputStream in = url.openStream()) {
            // Copy the image to the target path, replacing any existing file
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        // Return the file path as a string
        return targetPath.toString();
    }

    public String downloadAndConvertImage(String imageUrl, String destinationFolder) throws IOException, URISyntaxException {
        // Скачивание изображения
        String imageExtension = imageUrl.substring(imageUrl.lastIndexOf(".") + 1);
        String localImagePath = downloadImage(imageUrl, destinationFolder);

        // Конвертация в формат PNG, если это необходимо
        if (!"png".equals(imageExtension) && !"jpg".equals(imageExtension)) {
            String outputImagePath = localImagePath.substring(0, localImagePath.lastIndexOf(".")) + ".png";
            convertToPng(localImagePath, outputImagePath);
            return outputImagePath; // Вернуть новый путь к изображению
        }

        return localImagePath; // Вернуть исходный путь к изображению
    }

    public static void convertToPng(String inputImagePath, String outputImagePath) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new File(inputImagePath));
        ImageIO.write(bufferedImage, "png", new File(outputImagePath));
    }

    public List<Movie> parseKinopoisk() {
        List<Movie> movies = new ArrayList<>();
        try {
            String baseUrl = "https://lordfilm.ai/";
            Document doc = Jsoup.connect(baseUrl).get();
            Elements movieElements = doc.select(".th-item"); // Обновленный селектор

            for (Element movieElement : movieElements) {
                String title = movieElement.select(".th-title").text();
                String imageUrl = movieElement.select("img").first().absUrl("src");

                if (!imageUrl.isEmpty()) {
                    // Download image and get local path
                    String localImagePath = downloadAndConvertImage(imageUrl, "/com/mdkefir/filmlibrary/images/");
                    // Use local path instead of URL
                    movies.add(new Movie(title, localImagePath));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return movies;
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
        moviesButton.setOnAction(event -> loadMovies());
        seriesButton.setOnAction(event -> loadMovies()); // Обновите для работы с сериалами
        sportsButton.setOnAction(event -> loadMovies()); // Обновите для работы со спортивными событиями

        loadMovies(); // Загрузка фильмов при инициализации

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
    public void updateMovieList() {
        new Thread(() -> {
            List<Movie> movies = parseKinopoisk();
            Platform.runLater(() -> {
                try {
                    updateTilePaneContent(movies);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
        }).start();
    }
    // Этот метод теперь принимает List<Movie> и обновляет moviesTilePane
    public void updateTilePaneContent(List<Movie> movies) throws FileNotFoundException, URISyntaxException {
        moviesTilePane.getChildren().clear();
        for (Movie movie : movies) {
            Node movieNode = createMovieNode(movie);
            moviesTilePane.getChildren().add(movieNode);
        }
    }

    private Node createMovieNode(Movie movie) throws FileNotFoundException, URISyntaxException {
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        // Create an Image object using the file path
        File file = new File(movie.getImagePath());
        Image image = new Image(file.toURI().toString());
        ImageView imageView = new ImageView(image);
        Label label = new Label(movie.getTitle());
        vbox.getChildren().addAll(imageView, label);
        imageView.setFitHeight(225);
        imageView.setFitWidth(150);
        return vbox;
    }

    // Этот метод вызывается при инициализации или при смене категории
    public void loadMovies() {
        new Thread(() -> {
            List<Movie> movies = parseKinopoisk(); // Парсинг данных
            Platform.runLater(() -> {
                try {
                    updateTilePaneContent(movies); // Обновление UI
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
        }).start();
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
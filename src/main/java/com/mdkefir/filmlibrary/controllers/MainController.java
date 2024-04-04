package com.mdkefir.filmlibrary.controllers;

import com.mdkefir.filmlibrary.models.Movie;
import com.mdkefir.filmlibrary.models.Series;
import com.mdkefir.filmlibrary.models.Cartoon;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
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

import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
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
    private ToggleButton cartoonsButton;

    @FXML
    private ToggleGroup categoryToggleGroup = new ToggleGroup();

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

    public List<Movie> parseMedia(String baseUrl, int totalPages) {
        List<Movie> mediaList = new ArrayList<>();
        try {
            for (int i = 1; i <= totalPages; i++) {
                Document doc = Jsoup.connect(baseUrl + "page/" + i + "/").get();
                Elements mediaElements = doc.select(".sect-cont.sect-items.clearfix .th-item");

                for (Element mediaElement : mediaElements) {
                    String title = mediaElement.select(".th-title").text();
                    String year = mediaElement.select(".th-year").text();
                    String rating = mediaElement.select(".th-rates .th-rate.th-rate-imdb").text();
                    String imageUrl = mediaElement.select("img").first().absUrl("src");

                    if (!imageUrl.isEmpty()) {
                        String localImagePath = downloadAndConvertImage(imageUrl, "images/"); // Путь к папке images внутри проекта
                        mediaList.add(new Movie(title, year, rating, localImagePath));
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return mediaList;
    }

    /*
    public List<Movie> parseFilms() {
    List<Movie> movies = new ArrayList<>();
    String baseUrl = "https://lordfilm.ai/filmy/page/";

    try {
        for (int i = 1; i <= 5; i++) { // Loop through pages 1 to 5
            Document doc = Jsoup.connect(baseUrl + i + "/").get();
            Elements movieElements = doc.select(".sect-cont.sect-items.clearfix .th-item");

            for (Element movieElement : movieElements) {
                String title = movieElement.select(".th-title").text();
                String imageUrl = movieElement.select("img").first().absUrl("src");

                if (!imageUrl.isEmpty()) {
                    movies.add(new Movie(title, imageUrl));
                }
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }

    return movies;
}*/

    @FXML
    public void initialize() {
        // Назначаем все кнопки одной группе
        moviesButton.setToggleGroup(categoryToggleGroup);
        seriesButton.setToggleGroup(categoryToggleGroup);
        cartoonsButton.setToggleGroup(categoryToggleGroup);

        // настройка категорий для отображения
        moviesButton.setOnAction(event -> loadMovies());
        seriesButton.setOnAction(event -> loadSeries());
        cartoonsButton.setOnAction(event -> loadCartoons());

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
    // Этот метод теперь принимает List<Movie> и обновляет moviesTilePane
    public void updateTilePaneContent(List<Movie> movies) throws FileNotFoundException, URISyntaxException {
        moviesTilePane.getChildren().clear();
        for (Movie movie : movies) {
            Node movieNode = createMovieNode(movie);
            moviesTilePane.getChildren().add(movieNode);
        }
    }

    private Node createMovieNode(Movie movie) throws FileNotFoundException {
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-background-color: #151A1EFF;");

        File file = new File(movie.getImagePath());
        Image image = new Image(file.toURI().toString());
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(225);
        imageView.setFitWidth(150);

        Label titleLabel = new Label(movie.getTitle());
        titleLabel.setMaxWidth(150);
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-weight: bold;");
        titleLabel.setAlignment(Pos.CENTER);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);

        Label yearLabel = new Label(movie.getYear() + ", ");
        yearLabel.setAlignment(Pos.CENTER);

        Label ratingLabel = new Label("✫" + movie.getRating());
        ratingLabel.setStyle("-fx-text-fill: yellow;"); // Желтый цвет только для рейтинга
        ratingLabel.setAlignment(Pos.CENTER);

        hBox.getChildren().addAll(yearLabel, ratingLabel);
        vbox.getChildren().addAll(imageView, titleLabel, hBox);

        return vbox;
    }

    public void clearContent() {
        if (!moviesTilePane.getChildren().isEmpty()) {
            moviesTilePane.getChildren().clear();
        }
    }

    // Отдельные методы для парсинга фильмов, сериалов и мультфильмов
    public void loadMovies() {
        clearContent(); // очистка перед загрузкой
        new Thread(() -> {
            List<Movie> movies = parseMedia("https://lordfilm.ai/filmy/", 5);
            Platform.runLater(() -> {
                try {
                    updateTilePaneContent(movies);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }).start();
    }

    public void loadSeries() {
        clearContent(); // очистка перед загрузкой
        new Thread(() -> {
            List<Movie> series = parseMedia("https://lordfilm.ai/serialy/", 5);
            Platform.runLater(() -> {
                try {
                    updateTilePaneContent(series);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }).start();
    }

    public void loadCartoons() {
        clearContent(); // очистка перед загрузкой
        new Thread(() -> {
            List<Movie> cartoons = parseMedia("https://lordfilm.ai/multserialy/", 3);
            Platform.runLater(() -> {
                try {
                    updateTilePaneContent(cartoons);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }).start();
    }

}
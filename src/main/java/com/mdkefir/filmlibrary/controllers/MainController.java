package com.mdkefir.filmlibrary.controllers;
import com.mdkefir.filmlibrary.Database;
import com.mdkefir.filmlibrary.models.Movie;
import com.mdkefir.filmlibrary.models.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javafx.stage.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;

public class MainController implements AuthCallback{

    @Override
    public void onLoginSuccess(User user) {
        this.currentUser = user;
        setCurrentUserId(user.getId());
        System.out.println("Пользователь авторизован: " + user.getUsername());
        showAccountScreen(); // Показ новой формы после авторизации
        updateFriendList(); // Обновляем список друзей
    }

    @FXML
    private ScrollPane scrollPaneMovie; // Добавьте ScrollPane в FXML и свяжите его здесь

    private AuthController authController;
    private String currentCategory;
    private Stage loadingStage;
    private Stage authorizeStage;

    private Stage accountStage;


    @FXML
    private Label warningFavoriteLabel;


    private Database db;

    @FXML
    private TilePane moviesTilePane; // Это корневой элемент вашего интерфейса

    @FXML
    private ToggleButton moviesButton;

    @FXML
    private ToggleButton seriesButton;

    @FXML
    private ToggleButton cartoonsButton;

    @FXML ToggleButton myAccountButton;

    @FXML
    private ToggleButton favoritesButton;

    @FXML
    private AnchorPane friendsPane;

    @FXML
    private AnchorPane filtersPane;


    @FXML
    private ComboBox<String> favoriteFriendComboBox;


    @FXML
    private Button acceptFavoriteFriendButton;


    @FXML
    private ToggleButton friendsButton;


    @FXML
    private Label friendStatusLabel;

    @FXML
    private TextField nameFriend;
    @FXML
    private TextField secretCodeFriend;
    @FXML
    private ComboBox<String> friendChoose;

    @FXML
    private Button friendAdd;
    @FXML
    private Button friendDelete;
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private Button resetButton;

    private boolean viewingFriendFavorites = false;
    private String currentFriend = null;

    private int currentUserId;

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        loadFriends();
    }


    private AtomicInteger currentPage = new AtomicInteger(1);
    private volatile boolean isLoading = false;
    private final int totalPages = 30;

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

    public List<Movie> parseMedia(String baseUrl, int page) {
        List<Movie> mediaList = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(baseUrl + "page/" + (page + 1) + "/").get();
            Elements mediaElements = doc.select(".sect-cont.sect-items.clearfix .th-item");

            for (Element mediaElement : mediaElements) {
                String title = mediaElement.select(".th-title").text();
                String year = mediaElement.select(".th-year").text();
                String rating = mediaElement.select(".th-rates .th-rate.th-rate-imdb").text();
                String imageUrl = mediaElement.select("img").first().absUrl("src");

                if (!imageUrl.isEmpty()) {
                    String localImagePath = downloadAndConvertImage(imageUrl, "images/");
                    mediaList.add(new Movie (title, year, rating, localImagePath));
                }
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return mediaList;
    }

    // Переменные для хранения исходных размеров filtersPane
    private double originalFiltersPaneWidth;
    private double originalFiltersPaneHeight;


    @FXML
    public void initialize() {
        authController = new AuthController(); // Добавьте эту строку для инициализации authController
        authController.setAuthCallback(this); // Инициализируем authController и устанавливаем обратный вызов

        // Сохранить исходные размеры filtersPane
        originalFiltersPaneWidth = filtersPane.getPrefWidth();
        originalFiltersPaneHeight = filtersPane.getPrefHeight();
        // Назначаем все кнопки одной группе
        hideAllExtraPanes();

        friendsButton.setOnAction(event -> {
            if (currentUser == null) {

                System.out.println("Вы не авторизированы");
                showAuthorizeScreen();
            }
            else if (friendsButton.isSelected()) {
                hideAllExtraPanes();
                showFriendsPane();
            } else {
                hideAllExtraPanes();
            }
        });

        favoritesButton.setOnAction(event -> {
            if (currentUser == null) {

                System.out.println("Вы не авторизированы");
                showAuthorizeScreen();
            }
            else {
                if (favoritesButton.isSelected()) {
                    currentCategory = "favorites";
                    showFavoritePane();
                    loadFavorites();
                } else {
                    hideAllExtraPanes();
                }
            }
        });

        moviesButton.setOnAction(event -> {
            currentCategory = "movies";
            showLoadingScreen();
            currentPage.set(1); // Устанавливаем номер текущей страницы на 1
            clearContent(); // Очищаем содержимое перед загрузкой нового
            loadMovies();
            hideAllExtraPanes();
        });

        seriesButton.setOnAction(event -> {
            currentCategory = "series";
            showLoadingScreen();
            currentPage.set(1); // Устанавливаем номер текущей страницы на 1
            clearContent(); // Очищаем содержимое перед загрузкой нового
            loadSeries();
            hideAllExtraPanes();
        });

        cartoonsButton.setOnAction(event -> {
            currentCategory = "cartoons";
            showLoadingScreen();
            currentPage.set(1); // Устанавливаем номер текущей страницы на 1
            clearContent(); // Очищаем содержимое перед загрузкой нового
            loadCartoons();
            hideAllExtraPanes();
        });

        myAccountButton.setOnAction(event -> {
            if (currentUser == null) {
                showAuthorizeScreen();
            } else {
                showAccountScreen();
            }
        });


        friendAdd.setOnAction(event -> {
            handleAddFriend();
        });

        friendDelete.setOnAction(event -> {
            handleDeleteFriend();
        });

        searchButton.setOnAction(event -> handleSearch());
        searchField.setOnAction(event -> handleSearch());
        resetButton.setOnAction(event -> handleReset());

        acceptFavoriteFriendButton.setOnAction(event -> handleLoadFriendFavorites());

        scrollPaneMovie.vvalueProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue.doubleValue() >= scrollPaneMovie.getVmax() - 0.5) { // Threshold can be adjusted
                if (!isLoading && currentPage.get() <= totalPages) {
                    switch (currentCategory) {
                        case "movies":
                            loadMovies();
                            break;
                        case "series":
                            loadSeries();
                            break;
                        case "cartoons":
                            loadCartoons();
                            break;
                    }
                }
            }
        });

        loadMovies();


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
        /*createLoginPopup();*/
    }

    private void hideAllExtraPanes() {
        // Скрыть и установить размеры filtersPane в 0
        filtersPane.setVisible(false);
        filtersPane.setManaged(false);
        filtersPane.setPrefWidth(0);
        filtersPane.setPrefHeight(0);

        friendsPane.setVisible(false);
        friendsPane.setManaged(false);

        // Привязать ScrollPane к правому краю
        AnchorPane.clearConstraints(scrollPaneMovie);
        AnchorPane.setTopAnchor(scrollPaneMovie, 0.0);
        AnchorPane.setBottomAnchor(scrollPaneMovie, 0.0);
        AnchorPane.setLeftAnchor(scrollPaneMovie, 0.0);
        AnchorPane.setRightAnchor(scrollPaneMovie, 0.0);

        // Принудительно обновить компоновку
        scrollPaneMovie.layout();
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        clearContent();
        if (keyword.isEmpty()) {
            if ("favorites".equals(currentCategory)) {
                if (viewingFriendFavorites) {
                    loadFriendFavorites(currentFriend);
                } else {
                    loadAllFavorites();
                }
            } else {
                showLoadingScreen();
                loadMovies();
            }
        } else {
            if ("favorites".equals(currentCategory)) {
                if (viewingFriendFavorites) {
                    searchFriendFavorites(currentFriend, keyword);
                } else {
                    searchFavorites(keyword);
                }
            } else {
                loadSearchResults(keyword);
            }
        }
    }


    public void loadSearchResults(String keyword) {
        // Удаляем первый символ из строки поиска
        String modifiedKeyword = keyword.length() > 1 ? keyword.substring(1) : "";
        String searchUrl = "https://lordfilm.ai/?do=search&subaction=search&search_start=0&full_search=0&story=" + URLEncoder.encode(modifiedKeyword, StandardCharsets.UTF_8);

        new Thread(() -> {
            try {
                Document doc = Jsoup.connect(searchUrl).get();
                Elements movieElements = doc.select(".th-item");
                List<Movie> searchResults = new ArrayList<>();
                for (Element movieElement : movieElements) {
                    String title = movieElement.select(".th-title").text();
                    String year = movieElement.select(".th-year").text();
                    String rating = movieElement.select(".th-rates .th-rate-imdb span").text();
                    String imageUrl = movieElement.select("img").first().absUrl("src");

                    if (!imageUrl.isEmpty()) {
                        String localImagePath = downloadAndConvertImage(imageUrl, "images/");
                        searchResults.add(new Movie(title, year, rating, localImagePath));
                    }
                }
                Platform.runLater(() -> {
                    try {
                        if (searchResults.isEmpty()) {
                            warningFavoriteLabel.setText("Нет результатов по вашему запросу");
                        } else {
                            updateTilePaneContent(searchResults);
                            warningFavoriteLabel.setText("Список фильмов получен");
                        }
                        closeLoadingScreen();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    closeLoadingScreen();
                    warningFavoriteLabel.setText("Ошибка загрузки результатов поиска");
                });
            }
        }).start();
    }


    public void searchFavorites(String keyword) {
        // Удаляем первый символ из строки поиска
        String modifiedKeyword = keyword.length() > 1 ? keyword.substring(1) : "";

        String sql = "SELECT title, year, rating, image_path " +
                "FROM favorites " +
                "WHERE user_id = ? AND LOWER(title) LIKE ?";
        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUser.getId());
            pstmt.setString(2, "%" + modifiedKeyword.toLowerCase() + "%");
            ResultSet rs = pstmt.executeQuery();
            List<Movie> favoriteMovies = new ArrayList<>();
            while (rs.next()) {
                String title = rs.getString("title");
                String year = rs.getString("year");
                String rating = rs.getString("rating");
                String imagePath = rs.getString("image_path");
                favoriteMovies.add(new Movie(title, year, rating, imagePath));
            }
            Platform.runLater(() -> {
                try {
                    if (favoriteMovies.isEmpty()) {
                        warningFavoriteLabel.setText("Нет результатов по вашему запросу");
                    } else {
                        updateTilePaneContent(favoriteMovies);
                        warningFavoriteLabel.setText("Список фильмов получен");
                    }
                    closeLoadingScreen();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                closeLoadingScreen();
                warningFavoriteLabel.setText("Ошибка загрузки результатов поиска");
            });
        }
    }

    private void loadAllFavorites() {
        if (currentUser == null) {
            System.out.println("Вы не авторизированы");
            return;
        }
        String sql = "SELECT title, year, rating, image_path FROM favorites WHERE user_id = ?";
        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUser.getId());
            ResultSet rs = pstmt.executeQuery();
            List<Movie> favoriteMovies = new ArrayList<>();
            while (rs.next()) {
                String title = rs.getString("title");
                String year = rs.getString("year");
                String rating = rs.getString("rating");
                String imagePath = rs.getString("image_path");
                favoriteMovies.add(new Movie(title, year, rating, imagePath));
            }
            Platform.runLater(() -> {
                try {
                    clearContent();
                    updateTilePaneContent(favoriteMovies);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }




    public void searchFriendFavorites(String friendName, String keyword) {
        // Удаляем первый символ из строки поиска
        String modifiedKeyword = keyword.length() > 1 ? keyword.substring(1) : "";

        String sql = "SELECT f.title, f.year, f.rating, f.image_path " +
                "FROM favorites f " +
                "JOIN users u ON f.user_id = u.id " +
                "WHERE u.username = ? AND u.allow_favorites_access = TRUE AND LOWER(f.title) LIKE ?";
        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, friendName);
            pstmt.setString(2, "%" + modifiedKeyword.toLowerCase() + "%");
            ResultSet rs = pstmt.executeQuery();
            List<Movie> favoriteMovies = new ArrayList<>();
            while (rs.next()) {
                String title = rs.getString("title");
                String year = rs.getString("year");
                String rating = rs.getString("rating");
                String imagePath = rs.getString("image_path");
                favoriteMovies.add(new Movie(title, year, rating, imagePath));
            }
            Platform.runLater(() -> {
                try {
                    if (favoriteMovies.isEmpty()) {
                        warningFavoriteLabel.setText("Нет результатов по вашему запросу");
                    } else {
                        updateTilePaneContent(favoriteMovies);
                        warningFavoriteLabel.setText("Список фильмов получен");
                    }
                    closeLoadingScreen();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                closeLoadingScreen();
                warningFavoriteLabel.setText("Ошибка загрузки результатов поиска");
            });
        }
    }


    @FXML
    private void handleReset() {
        // Сбрасываем выбор друга в ComboBox
        friendChoose.getSelectionModel().clearSelection();
        favoriteFriendComboBox.getSelectionModel().clearSelection();

        // Сбрасываем состояние просмотра избранных друга
        viewingFriendFavorites = false;
        currentFriend = null;

        // Загружаем избранные фильмы текущего пользователя
        loadAllFavorites();

        // Очистить сообщение об ошибке
        warningFavoriteLabel.setText("");
    }

    private void showFriendsPane() {
        friendsPane.setVisible(true);
        friendsPane.setManaged(true);
    }

    private void showFavoritePane() {
        filtersPane.setVisible(true);
        filtersPane.setManaged(true);

        // Восстановить исходные размеры filtersPane
        filtersPane.setPrefWidth(originalFiltersPaneWidth);
        filtersPane.setPrefHeight(originalFiltersPaneHeight);

        // Привязать ScrollPane к правому краю
        AnchorPane.clearConstraints(scrollPaneMovie);
        AnchorPane.setTopAnchor(scrollPaneMovie, 0.0);
        AnchorPane.setBottomAnchor(scrollPaneMovie, 0.0);
        AnchorPane.setLeftAnchor(scrollPaneMovie, 0.0);
        AnchorPane.setRightAnchor(scrollPaneMovie, originalFiltersPaneWidth); // Учитывать ширину filtersPane

        // Принудительно обновить компоновку
        scrollPaneMovie.layout();
    }

    private void setLabelStyle(VBox vbox, Color color) {
        vbox.getChildren().filtered(node -> node instanceof Label).forEach(node -> {
            ((Label) node).setTextFill(color);
        });
    }

    // Этот метод теперь принимает List<Movie> и обновляет moviesTilePane
    public void updateTilePaneContent(List<Movie> movies) throws FileNotFoundException, URISyntaxException {
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

        CheckBox favoriteCheckBox = new CheckBox("В избранное");
        favoriteCheckBox.setSelected(currentUser != null && isFavorite(movie.getTitle()));
        favoriteCheckBox.setOnAction(event -> {
            if (currentUser == null) {
                showAuthorizeScreen();
                System.out.println("Вы не авторизированы");
                favoriteCheckBox.setSelected(false);
                return;
            }
            toggleFavorite(movie);
        });

        hBox.getChildren().addAll(yearLabel, ratingLabel);
        vbox.getChildren().addAll(imageView, titleLabel, hBox, favoriteCheckBox);

        return vbox;
    }

    public void toggleFavorite(Movie movie) {
        if (isFavorite(movie.getTitle())) {
            removeFavorite(movie.getTitle());
        } else {
            addFavorite(movie);
        }
    }


    public void addFavorite(Movie movie) {
        if (currentUser == null) {
            System.out.println("Вы не авторизированы");
            return;
        }
        String sql = "INSERT INTO favorites (user_id, title, year, rating, image_path) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUser.getId());
            pstmt.setString(2, movie.getTitle());
            pstmt.setString(3, movie.getYear());
            pstmt.setString(4, movie.getRating());
            pstmt.setString(5, movie.getImagePath());
            pstmt.executeUpdate();
            System.out.println("Фильм добавлен в избранное");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    public void removeFavorite(String movieTitle) {
        if (currentUser == null) {
            System.out.println("Вы не авторизированы");
            return;
        }
        String sql = "DELETE FROM favorites WHERE user_id = ? AND title = ?";
        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUser.getId());
            pstmt.setString(2, movieTitle);
            pstmt.executeUpdate();
            System.out.println("Фильм удален из избранного");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void loadFavorites() {
        if (currentUser == null) {
            System.out.println("Вы не авторизированы");
            return;
        }
        String sql = "SELECT title, year, rating, image_path FROM favorites WHERE user_id = ?";
        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUser.getId());
            ResultSet rs = pstmt.executeQuery();
            List<Movie> favoriteMovies = new ArrayList<>();
            while (rs.next()) {
                String title = rs.getString("title");
                String year = rs.getString("year");
                String rating = rs.getString("rating");
                String imagePath = rs.getString("image_path");
                favoriteMovies.add(new Movie(title, year, rating, imagePath));
            }
            Platform.runLater(() -> {
                try {
                    clearContent(); // Очистить предыдущий контент
                    updateTilePaneContent(favoriteMovies); // Отобразить избранные фильмы
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }



    User currentUser;

    public boolean isFavorite(String movieTitle) {
        if (currentUser == null) {
            System.out.println("Вы не авторизированы");
            return false;
        }
        String sql = "SELECT 1 FROM favorites WHERE user_id = ? AND title = ?";
        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUser.getId());
            pstmt.setString(2, movieTitle);
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // Возвращает true, если запись найдена
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }


    public void clearContent() {
        if (!moviesTilePane.getChildren().isEmpty()) {
            moviesTilePane.getChildren().clear();
        }
    }

    // Отдельные методы для парсинга фильмов, сериалов и мультфильмов
    public void loadMovies() {
        if ("favorites".equals(currentCategory)) return; //

        currentCategory = "movies";
        if (isLoading || currentPage.get() > totalPages) return; // Проверка, идет ли загрузка и есть ли еще страницы
        isLoading = true;
        int pageToLoad = currentPage.getAndIncrement(); // Получаем текущую страницу для загрузки и увеличиваем счетчик

        new Thread(() -> {
            List<Movie> movies = parseMedia("https://lordfilm.ai/filmy/", pageToLoad);
            System.out.println("Страница фильма : " + pageToLoad);
            Platform.runLater(() -> {
                try {
                    updateTilePaneContent(movies);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    isLoading = false; // Сбрасываем состояние загрузки
                    closeLoadingScreen();
                }
            });
        }).start();
    }

    public void loadSeries() {
        if ("favorites".equals(currentCategory)) return; //

        currentCategory = "series";
        if (isLoading || currentPage.get() > totalPages) return; // Проверка, идет ли загрузка и есть ли еще страницы
        isLoading = true;
        int pageToLoad = currentPage.getAndIncrement();

        new Thread(() -> {
            List<Movie> series = parseMedia("https://lordfilm.ai/serialy/", pageToLoad);
            System.out.println("Страница сериала : " + pageToLoad);
            Platform.runLater(() -> {
                try {
                    updateTilePaneContent(series);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    isLoading = false;
                    closeLoadingScreen();
                }
            });
        }).start();
    }

    public void loadCartoons() {
        if ("favorites".equals(currentCategory)) return; //

        currentCategory = "cartoons";
        if (isLoading || currentPage.get() > 3) return; // Проверка, идет ли загрузка и есть ли еще страницы
        isLoading = true;
        int pageToLoad = currentPage.getAndIncrement();

        new Thread(() -> {
            List<Movie> cartoons = parseMedia("https://lordfilm.ai/multserialy/", pageToLoad);
            System.out.println("Страница мультика : " + pageToLoad);
            Platform.runLater(() -> {
                try {
                    updateTilePaneContent(cartoons);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    isLoading = false;
                    closeLoadingScreen();
                }
            });
        }).start();
    }

    private void showLoadingScreen(){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mdkefir/filmlibrary/fxml/main_download.fxml"));
            Parent root = loader.load();

            //Создание новой формы
            loadingStage = new Stage();
            loadingStage.initStyle(StageStyle.UNDECORATED);
            loadingStage.setScene(new Scene(root));
            loadingStage.initModality(Modality.APPLICATION_MODAL); // делаем окно модальным
            loadingStage.setTitle("Загрузка");
            loadingStage.show();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAuthorizeScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mdkefir/filmlibrary/fxml/main_enter_account.fxml"));
            Parent root = loader.load();

            AuthController authController = loader.getController();
            authController.setAuthCallback(this); // Устанавливаем обратный вызов

            // Создание новой формы
            authorizeStage = new Stage();
            Scene scene = new Scene(root);
            authorizeStage.setScene(scene);
            authorizeStage.setTitle("Авторизация/Регистрация");
            //Прозрачность фона

            scene.setFill(Color.TRANSPARENT);
            authorizeStage.initStyle(StageStyle.TRANSPARENT);


            // Получаем расположение кнопки
            Node source = myAccountButton;
            Window theStage = source.getScene().getWindow();
            double x = theStage.getX() + source.localToScene(source.getBoundsInLocal()).getMinX();
            double y = theStage.getY() + source.localToScene(source.getBoundsInLocal()).getMinY();

            // Устанавливаем позицию окна рядом с кнопкой
            authorizeStage.setX(x+205);
            authorizeStage.setY(y + source.getBoundsInLocal().getHeight()-270);
            authorizeStage.show();


            // Обработка потери фокуса окна
            authorizeStage.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    authorizeStage.close();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showAccountScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mdkefir/filmlibrary/fxml/main_account.fxml"));
            Parent root = loader.load();

            // Получаем контроллер формы аккаунта
            AccountController accountController = loader.getController();
            accountController.setMainController(this); // Передаем MainController в AccountController
            accountController.setCurrentUser(currentUser); // Передаем текущего пользователя

            // Создание новой формы
            accountStage = new Stage();
            Scene scene = new Scene(root);
            accountStage.setScene(scene);
            accountStage.setTitle("Мой аккаунт");

            // Прозрачность фона
            scene.setFill(Color.TRANSPARENT);
            accountStage.initStyle(StageStyle.TRANSPARENT);

            // Получаем расположение кнопки
            Node source = myAccountButton;
            Window theStage = source.getScene().getWindow();
            double x = theStage.getX() + source.localToScene(source.getBoundsInLocal()).getMinX();
            double y = theStage.getY() + source.localToScene(source.getBoundsInLocal()).getMinY();

            // Устанавливаем позицию окна рядом с кнопкой
            accountStage.setX(x + 205);
            accountStage.setY(y + source.getBoundsInLocal().getHeight() - 270);
            accountStage.show();

            // Обработка потери фокуса окна
            accountStage.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    accountStage.close();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void closeAccountScreen() {
        if (accountStage != null) {
            accountStage.close();
        }
    }

    private void closeLoadingScreen(){
        if (loadingStage != null){
            loadingStage.close();
        }
    }


    @FXML
    private void handleAddFriend() {
        String friendName = nameFriend.getText();
        String secretCode = secretCodeFriend.getText();
        if (currentUser != null && friendName != null && !friendName.isEmpty() && secretCode != null && !secretCode.isEmpty()) {
            if (authController.addFriend(currentUser.getId(), friendName, secretCode)) {
                friendStatusLabel.setText("Друг добавлен");
                updateFriendList();
            } else {
                friendStatusLabel.setText("Ошибка добавления");
            }
        } else {
            friendStatusLabel.setText("Заполни поля");
        }
    }

    @FXML
    private void handleDeleteFriend() {
        String selectedFriend = friendChoose.getValue();
        if (currentUser != null && selectedFriend != null) {
            if (authController.deleteFriend(currentUser.getId(), selectedFriend)) {
                friendStatusLabel.setText("Друг удален");
                updateFriendList();
            } else {
                friendStatusLabel.setText("Ошибка удаления");
            }
        } else {
            friendStatusLabel.setText("Выберите друга");
        }
    }

    private void updateFriendList() {
        if (currentUser != null) {
            List<String> friends = authController.getFriends(currentUser.getId());
            friendChoose.getItems().setAll(friends);
            favoriteFriendComboBox.getItems().setAll(friends);
        }
    }

    private void handleLoadFriendFavorites() {
        String selectedFriend = favoriteFriendComboBox.getValue();
        if (selectedFriend != null) {
            currentFriend = selectedFriend;
            viewingFriendFavorites = true;
            loadFriendFavorites(selectedFriend);
        } else {
            System.out.println("Выберите друга из списка.");
        }
    }




    public boolean updateAllowFavoritesAccess(int userId, boolean allowAccess) {
        String sql = "UPDATE users SET allow_favorites_access = ? WHERE id = ?";
        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, allowAccess);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Ошибка обновления доступа к избранному: " + e.getMessage());
            return false;
        }
    }

    private void loadFriendFavorites(String friendName) {
        String sql = "SELECT f.title, f.year, f.rating, f.image_path " +
                "FROM favorites f " +
                "JOIN users u ON f.user_id = u.id " +
                "WHERE u.username = ? AND u.allow_favorites_access = TRUE";
        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, friendName);
            ResultSet rs = pstmt.executeQuery();
            List<Movie> favoriteMovies = new ArrayList<>();
            if (rs.isBeforeFirst()) { // Проверка, есть ли результаты
                while (rs.next()) {
                    String title = rs.getString("title");
                    String year = rs.getString("year");
                    String rating = rs.getString("rating");
                    String imagePath = rs.getString("image_path");
                    favoriteMovies.add(new Movie(title, year, rating, imagePath));
                }
                Platform.runLater(() -> {
                    try {
                        clearContent();
                        updateTilePaneContent(favoriteMovies);
                        warningFavoriteLabel.setText("Список фильмов получен");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                Platform.runLater(() -> {
                    warningFavoriteLabel.setText("Доступ к избранному другу ограничен");
                    closeLoadingScreen();
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                closeLoadingScreen();
                warningFavoriteLabel.setText("Ошибка загрузки избранных фильмов друга");
            });
        }
    }



    private void loadFriends() {
        friendChoose.getItems().clear();

        try (Connection conn = Database.connect()) {
            // Получить список друзей текущего пользователя
            String loadFriendsSQL = "SELECT u.username FROM friends f JOIN users u ON f.friend_id = u.id WHERE f.user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(loadFriendsSQL);
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                friendChoose.getItems().add(rs.getString("username"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            friendStatusLabel.setText("Ошибка загрузки");
        }
    }

    public boolean updateSecretCode(int userId, String newSecretCode) {
        String sql = "UPDATE users SET secret_code = ? WHERE id = ?";
        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newSecretCode);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Ошибка обновления секретного кода: " + e.getMessage());
            return false;
        }
    }


    public void logout() {
        // Очистка текущего пользователя
        currentUser = null;
        currentUserId = -1;

        // Закрытие формы "Мой аккаунт"
        closeAccountScreen();

        // Показ формы авторизации
        showAuthorizeScreen();
    }
}
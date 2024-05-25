package com.mdkefir.filmlibrary.controllers;

import com.mdkefir.filmlibrary.Database;
import com.mdkefir.filmlibrary.models.Movie;
import com.mdkefir.filmlibrary.models.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
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

import javafx.scene.shape.Rectangle;
import javafx.stage.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;

public class MainController {

    @FXML private TextField searchField;

    private AuthController authController;
    private String currentCategory;
    private Stage loadingStage;

    // Добавляем поля класса для использования в POPUP
    private TextField emailField = new TextField();
    private PasswordField passwordField = new PasswordField();
    private Label statusLabel = new Label(""); // Для сообщений о состоянии

    private Database db;

    @FXML
    private HBox searchHBox;


    @FXML
    private AnchorPane authPane; // Панель авторизации/регистрации

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
    private ScrollPane scrollPaneMovie; // Добавьте ScrollPane в FXML и свяжите его здесь

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
    private ToggleGroup categoryToggleGroup = new ToggleGroup();

    @FXML
    private AnchorPane friendsPane;

    @FXML
    private AnchorPane filtersPane;

    @FXML
    private CheckBox showAccessCheckBox;

    @FXML
    private ComboBox<String> favoriteFriendComboBox;

    @FXML
    private Button acceptFavoriteFriendButton;

    @FXML
    private Label favoriteText1;

    @FXML
    private Label favoriteText2;

    @FXML
    private ToggleButton friendsButton;

    @FXML
    private Rectangle friendRectangle;

    @FXML
    private Label friendStatusLabel;

    @FXML
    private TextField nameFriend;
    @FXML
    private TextField secretCodeFriend;
    @FXML
    private ComboBox<String> friendChoose;
    @FXML
    private TextField fieldSecretWord;

    @FXML
    private Button friendAdd;
    @FXML
    private Button friendDelete;
    @FXML
    private Button applySecretWordButton;

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
        authController = new AuthController();
        // Назначаем все кнопки одной группе
        hideAllExtraPanes();

        friendsButton.setOnAction(event -> {
            if (friendsButton.isSelected()) {
                hideAllExtraPanes();
                showFriendsPane();
            } else {
                hideAllExtraPanes();
            }
        });

        favoritesButton.setOnAction(event -> {
            if (favoritesButton.isSelected()) {
                showFavoritePane();
                loadFavorites();
            } else {
                hideAllExtraPanes();
            }
        });

        moviesButton.setOnAction(event -> {

            /*scrollPaneMovie.setVisible(false);
            scrollPaneMovie.setManaged(false);
            filtersPane.setVisible(false);
            filtersPane.setManaged(false);
            searchHBox.setVisible(false);*/
            showLoadingScreen();
            currentPage.set(1); // Устанавливаем номер текущей страницы на 1
            clearContent(); // Очищаем содержимое перед загрузкой нового
            loadMovies();
            hideAllExtraPanes();
        });

        seriesButton.setOnAction(event -> {

            showLoadingScreen();
            currentPage.set(1); // Устанавливаем номер текущей страницы на 1
            clearContent(); // Очищаем содержимое перед загрузкой нового
            loadSeries();
            hideAllExtraPanes();
        });

        cartoonsButton.setOnAction(event -> {

            showLoadingScreen();
            currentPage.set(1); // Устанавливаем номер текущей страницы на 1
            clearContent(); // Очищаем содержимое перед загрузкой нового
            loadCartoons();
            hideAllExtraPanes();
        });

        myAccountButton.setOnAction(event -> {

        });

        friendAdd.setOnAction(event -> {
            handleAddFriend();
        });

        friendDelete.setOnAction(event -> {
            handleDeleteFriend();
        });
        applySecretWordButton.setOnAction(event -> {
            handleApplySecretWord();
        });

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
        createLoginPopup();
    }

    private void hideAllExtraPanes() {
        friendsPane.setVisible(false);
        friendsPane.setManaged(false);
        filtersPane.setVisible(false);
        filtersPane.setManaged(false);
    }

    private void showFriendsPane() {
        friendsPane.setVisible(true);
        friendsPane.setManaged(true);
    }

    private void showFavoritePane() {
        filtersPane.setVisible(true);
        filtersPane.setManaged(true);
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

    private void createLoginPopup() {
        Popup popup = new Popup();
        VBox popupContent = new VBox(10);
        popupContent.setStyle("-fx-background-color: #283035; -fx-padding: 10;");

        Label titleLabel = new Label("Авторизация / Регистрация");
        titleLabel.setStyle("-fx-text-fill: white;");

        // Используйте поля класса
        emailField.setPromptText("Эл. почта");
        passwordField.setPromptText("Пароль");

        // Создание HBox для кнопок
        HBox buttonBar = new HBox(10);  // Расстояние между кнопками
        Button loginButton = new Button("Войти");
        Button registerButton = new Button("Регистрация");
        buttonBar.getChildren().addAll(loginButton, registerButton);
        buttonBar.setAlignment(Pos.CENTER);  // Центрирование кнопок в HBox

        // Настройка statusLabel
        statusLabel.setStyle("-fx-text-fill: WHITE; -fx-font-size: 14px;");
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        statusLabel.setAlignment(Pos.CENTER);  // Центрирование текста внутри Label

        // Настройка statusLabel
        friendStatusLabel.setStyle("-fx-text-fill: WHITE; -fx-font-size: 14px;");
        friendStatusLabel.setMaxWidth(Double.MAX_VALUE);
        friendStatusLabel.setAlignment(Pos.CENTER);  // Центрирование текста внутри Label

        // Установка обработчиков
        loginButton.setOnAction(e -> handleLogin());
        registerButton.setOnAction(e -> handleRegistration());

        popupContent.getChildren().addAll(titleLabel, emailField, passwordField, buttonBar, statusLabel);
        popup.getContent().add(popupContent);

        myAccountButton.setOnAction(event -> {
            if (!popup.isShowing()) {
                Node source = (Node) event.getSource();
                Window theStage = source.getScene().getWindow();
                double x = 6 + theStage.getX() + source.localToScene(source.getBoundsInLocal()).getMinX();
                double y = theStage.getY() + source.localToScene(source.getBoundsInLocal()).getMinY();
                popup.show(theStage, x, y + source.getBoundsInLocal().getHeight() + 40);
            } else {
                popup.hide();
            }
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



    private User currentUser;
    public void removeFavorite(int movieId) {
        String sql = "DELETE FROM favorites WHERE user_id = ? AND movie_id = ?";
        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUser.getId());
            pstmt.setInt(2, movieId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

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




    // Метод для установки текущего пользователя после успешной авторизации
    public void setCurrentUser(int id, String username) {
        this.currentUser = new User(id, username);
    }


    public void clearContent() {
        if (!moviesTilePane.getChildren().isEmpty()) {
            moviesTilePane.getChildren().clear();
        }
    }

    // Отдельные методы для парсинга фильмов, сериалов и мультфильмов
    public void loadMovies() {
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

    private void closeLoadingScreen(){
        if (loadingStage != null){
            loadingStage.close();
        }
    }


    // Пример метода, который вызывается при нажатии кнопки "Регистрация"

    private void handleRegistration() {
        String username = emailField.getText();  // Используйте email в качестве логина
        String password = passwordField.getText();
        if (authController.registerUser(username, password)) {
            statusLabel.setText("Регистрация успешна");
        } else {
            statusLabel.setText("Ошибка регистрации");
        }
    }


    @FXML
    private void handleLogin() {
        String username = emailField.getText();
        String password = passwordField.getText();
        if (authController.loginUser(username, password)) {
            int userId = authController.getUserId(username);
            currentUser = new User(userId, username);  // Создание текущего пользователя
            statusLabel.setText("Авторизация успешна");
        } else {
            statusLabel.setText("Ошибка авторизации");
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
                friendStatusLabel.setText("Ошибка добавления друга");
            }
        } else {
            friendStatusLabel.setText("Необходимо заполнить все поля");
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
                friendStatusLabel.setText("Ошибка удаления друга");
            }
        } else {
            friendStatusLabel.setText("Выберите друга для удаления");
        }
    }

    private void updateFriendList() {
        if (currentUser != null) {
            List<String> friends = authController.getFriends(currentUser.getId());
            friendChoose.getItems().setAll(friends);
        }
    }


    @FXML
    private void handleApplySecretWord() {
        String secretWord = fieldSecretWord.getText();

        if (secretWord.isEmpty()) {
            friendStatusLabel.setText("Введите секретное слово");
            return;
        }

        try (Connection conn = Database.connect()) {
            // Обновить секретное слово текущего пользователя
            String updateSecretWordSQL = "UPDATE users SET secret_code = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(updateSecretWordSQL);
            pstmt.setString(1, secretWord);
            pstmt.setInt(2, currentUser.getId());
            pstmt.executeUpdate();

            friendStatusLabel.setText("Секретное слово обновлено");
        } catch (SQLException e) {
            e.printStackTrace();
            friendStatusLabel.setText("Ошибка обновления секретного слова");
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
            friendStatusLabel.setText("Ошибка загрузки друзей");
        }
    }


}
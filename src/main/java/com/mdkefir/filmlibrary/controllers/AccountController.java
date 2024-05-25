package com.mdkefir.filmlibrary.controllers;

import com.mdkefir.filmlibrary.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class AccountController {

    @FXML
    private Label nicknameLabel;

    @FXML
    private Label secretcodeLabel;

    @FXML
    private TextField secretcodeField;

    @FXML
    private Label statusLabel;

    @FXML
    private Button changeSecretButton;

    @FXML
    private Button exitButton;

    @FXML
    private CheckBox showAccessCheckBox;

    private MainController mainController;
    private User currentUser;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        if (currentUser != null) {
            updateAccountInfo();
        }
    }


    @FXML
    private void initialize() {
        changeSecretButton.setOnAction(event -> handleChangeSecretCode());
        exitButton.setOnAction(event -> {handleExitAccount();});
        showAccessCheckBox.setOnAction(event -> handleShowAccessCheckBox());
    }

    private void updateAccountInfo() {
        nicknameLabel.setText(currentUser.getUsername());
        secretcodeLabel.setText(currentUser.getSecretCode());
    }

    private void handleChangeSecretCode() {
        String newSecretCode = secretcodeField.getText();
        if (newSecretCode.isEmpty()) {
            statusLabel.setText("Ошибка: Введите новый секретный код");
            return;
        }

        if (mainController.updateSecretCode(currentUser.getId(), newSecretCode)) {
            currentUser.setSecretCode(newSecretCode);
            secretcodeLabel.setText(newSecretCode);
            statusLabel.setText("Секретный код успешно обновлен");
        } else {
            statusLabel.setText("Ошибка: Не удалось обновить секретный код");
        }
    }

    private void handleShowAccessCheckBox() {
        boolean allowAccess = showAccessCheckBox.isSelected();
        if (mainController.updateAllowFavoritesAccess(currentUser.getId(), allowAccess)) {
            currentUser.setAllowFavoritesAccess(allowAccess);
            statusLabel.setText("Доступ к избранному обновлен");
        } else {
            statusLabel.setText("Ошибка: Не удалось обновить доступ к избранному");
        }
    }

    private void handleExitAccount() {
        if (mainController != null) {
            mainController.logout();
        }
    }
}

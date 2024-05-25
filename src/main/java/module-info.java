module com.mdkefir.filmlibrary {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires org.jsoup;
    requires java.desktop;
    requires java.sql;

    opens com.mdkefir.filmlibrary.controllers to javafx.fxml;
    exports com.mdkefir.filmlibrary;
    exports com.mdkefir.filmlibrary.controllers;

}
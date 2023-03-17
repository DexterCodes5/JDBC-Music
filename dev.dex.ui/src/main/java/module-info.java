module dev.dex.ui {
    requires dev.dex.db;

    requires javafx.controls;
    requires javafx.fxml;

    opens dev.dex.ui to javafx.fxml;
    exports dev.dex.ui;
}
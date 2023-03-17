package dev.dex.ui;

import dev.dex.common.*;
import dev.dex.db.*;
import javafx.collections.*;
import javafx.concurrent.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.control.*;

public class Controller {
    @FXML
    private TableView artistTable;
    @FXML
    private ProgressBar progressBar;


    @FXML
    public void listArtists() {
        Task<ObservableList<Artist>> task = new GetAllArtistsTask();
        artistTable.itemsProperty().bind(task.valueProperty());
        progressBar.progressProperty().bind(task.progressProperty());
        progressBar.setVisible(true);
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                progressBar.setVisible(false);
            }
        });
        task.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                progressBar.setVisible(false);
            }
        });
        new Thread(task).start();
    }

    @FXML
    public void showAlbums() {
        // Select the id of the artist, query the database, populate the table view
        Artist artist = (Artist) artistTable.getSelectionModel().getSelectedItem();
        if (artist == null) {
            System.out.println("No artists selected");
            return;
        }
        Task<ObservableList<Album>> task = new Task<ObservableList<Album>>() {
            @Override
            protected ObservableList<Album> call() throws Exception {
                return FXCollections.observableArrayList(
                        Datasource.getInstance().queryAlbumsByArtistId(artist.get_id()));
            }
        };
        artistTable.itemsProperty().bind(task.valueProperty());
        progressBar.progressProperty().bind(task.progressProperty());
        progressBar.setVisible(true);
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                progressBar.setVisible(false);
            }
        });
        task.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                progressBar.setVisible(false);
            }
        });
        new Thread(task).start();
    }

    @FXML
    public void updateArtist() {
        final Artist artist = (Artist) artistTable.getItems().get(2);

        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return Datasource.getInstance().updateArtist(artist.get_id(), "AC/DC");
            }
        };
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                if (task.valueProperty().get()) {
                    artistTable.refresh();
                }
            }
        });
        new Thread(task).start();
    }

}

class GetAllArtistsTask extends Task {
    @Override
    protected ObservableList<Artist> call() throws Exception {
        return FXCollections.observableArrayList(Datasource.getInstance().queryArtists(Datasource.ORDER_BY_ASC));
    }
}

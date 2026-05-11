package theknife.client.controller;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import theknife.client.ClientTK;
import theknife.client.ServerConnection;
import theknife.client.SessioneCorrente;
import theknife.model.Prenotazione;
import theknife.shared.Request;
import theknife.shared.Response;

public class GestorePrenotazioniController {

    @FXML private Label lblTitolo;
    @FXML private Label lblSottotitolo;
    @FXML private Label lblEmpty;
    @FXML private Label lblError;
    @FXML private VBox vboxPrenotazioni;
    @FXML private MenuButton accountMenuButton;
    @FXML private Button btnNavHome;
    @FXML private Button btnNavRestaurants;
    @FXML private Button btnNavReviews;
    @FXML private Button btnNavBookings;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM uuuu HH:mm", Locale.ITALIAN);

    @FXML
    public void initialize() {
        if (!SessioneCorrente.getInstance().isUserLogged() || !SessioneCorrente.getInstance().isGestore()) {
            ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome");
            return;
        }

        configureNavbar();
        updateAccountMenu();

        if (lblTitolo != null) {
            lblTitolo.setText("Prenotazioni");
        }
        if (lblSottotitolo != null) {
            lblSottotitolo.setText("Visualizza e gestisci tutte le prenotazioni dei tuoi ristoranti.");
        }

        caricaPrenotazioni();
        wrapInScrollPane(vboxPrenotazioni);
    }

    private void wrapInScrollPane(javafx.scene.Node node) {
        Platform.runLater(() -> {
            if (!(node instanceof javafx.scene.control.ScrollPane) && node != null && node.getParent() instanceof javafx.scene.layout.Pane parent) {
                int index = parent.getChildren().indexOf(node);
                if (index < 0) return;

                parent.getChildren().remove(node);
                javafx.scene.control.ScrollPane sp = new javafx.scene.control.ScrollPane();
                sp.setFitToWidth(true);
                sp.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 0;");
                sp.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);

                if (parent instanceof javafx.scene.layout.VBox) {
                    javafx.scene.layout.Priority vgrow = javafx.scene.layout.VBox.getVgrow(node);
                    javafx.scene.layout.VBox.setVgrow(sp, vgrow != null ? vgrow : javafx.scene.layout.Priority.ALWAYS);
                } else if (parent instanceof javafx.scene.layout.AnchorPane) {
                    Double top = javafx.scene.layout.AnchorPane.getTopAnchor(node);
                    if (top != null) javafx.scene.layout.AnchorPane.setTopAnchor(sp, top);
                    Double bottom = javafx.scene.layout.AnchorPane.getBottomAnchor(node);
                    if (bottom != null) javafx.scene.layout.AnchorPane.setBottomAnchor(sp, bottom);
                    Double left = javafx.scene.layout.AnchorPane.getLeftAnchor(node);
                    if (left != null) javafx.scene.layout.AnchorPane.setLeftAnchor(sp, left);
                    Double right = javafx.scene.layout.AnchorPane.getRightAnchor(node);
                    if (right != null) javafx.scene.layout.AnchorPane.setRightAnchor(sp, right);
                }

                sp.setContent(node);
                parent.getChildren().add(index, sp);
            }
        });
    }

    private void caricaPrenotazioni() {
        if (lblError != null) {
            lblError.setVisible(false);
            lblError.setManaged(false);
        }

        new Thread(() -> {
            try {
                Request req = new Request("PRENOTAZIONI_GESTORE");
                req.addParametro("idGestore", SessioneCorrente.getInstance().getUtenteLoggato().getId());
                Response res = ServerConnection.getInstance().send(req);
                if (res.isSuccesso() && vboxPrenotazioni != null) {
                    @SuppressWarnings("unchecked")
                    List<Prenotazione> prenotazioni = (List<Prenotazione>) res.getPayload();
                    Platform.runLater(() -> popolaUI(prenotazioni));
                } else if (lblError != null) {
                    Platform.runLater(() -> {
                        lblError.setText(res.getMessaggio());
                        lblError.setVisible(true);
                        lblError.setManaged(true);
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (lblError != null) {
                        lblError.setText("Errore di connessione");
                        lblError.setVisible(true);
                        lblError.setManaged(true);
                    }
                });
            }
        }).start();
    }

    private void popolaUI(List<Prenotazione> prenotazioni) {
        vboxPrenotazioni.getChildren().clear();
        if (prenotazioni == null) {
            prenotazioni = List.of();
        }
        boolean empty = prenotazioni.isEmpty();

        if (lblEmpty != null) {
            lblEmpty.setVisible(empty);
            lblEmpty.setManaged(empty);
        }

        if (empty) {
            return;
        }

        for (Prenotazione prenotazione : prenotazioni) {
            vboxPrenotazioni.getChildren().add(createBookingCard(prenotazione));
        }
    }

    private VBox createBookingCard(Prenotazione prenotazione) {
        VBox card = new VBox(12);
        card.getStyleClass().add("tk-booking-card");
        card.getStyleClass().add("tk-booking-card-owner");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = new StackPane(new Label(initials(prenotazione.getNomeUtente())));
        avatar.getStyleClass().add("tk-result-thumb");
        avatar.setPrefSize(42, 42);
        avatar.setMinSize(42, 42);
        avatar.setMaxSize(42, 42);

        VBox meta = new VBox(2);
        Label restaurant = new Label(prenotazione.getNomeRistorante());
        restaurant.getStyleClass().add("tk-card-title");
        Label user = new Label(prenotazione.getNomeUtente());
        user.getStyleClass().add("tk-text-secondary");
        meta.getChildren().addAll(restaurant, user);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label status = new Label(getStatusLabel(prenotazione.getStato()));
        status.getStyleClass().add(getStatusStyle(prenotazione.getStato()));

        header.getChildren().addAll(avatar, meta, spacer, status);

        // Date and time
        Label dateTime = new Label(prenotazione.getDataPrenotazione() != null ?
            prenotazione.getDataPrenotazione().format(formatter) : "");
        dateTime.getStyleClass().add("tk-text-secondary");

        // Party size
        Label partySize = new Label("Persone: " + prenotazione.getNumeroPersone());
        partySize.getStyleClass().add("tk-text-secondary");

        // Notes
        Label notes = new Label();
        if (prenotazione.getNoteSpeciali() != null && !prenotazione.getNoteSpeciali().isBlank()) {
            notes.setText("Note: " + prenotazione.getNoteSpeciali());
            notes.setWrapText(true);
            notes.getStyleClass().add("tk-text-secondary");
        }

        card.getChildren().addAll(header, dateTime, partySize);
        if (prenotazione.getNoteSpeciali() != null && !prenotazione.getNoteSpeciali().isBlank()) {
            card.getChildren().add(notes);
        }

        Region vSpacer = new Region();
        vSpacer.setPickOnBounds(false);
        VBox.setVgrow(vSpacer, Priority.ALWAYS);
        card.getChildren().add(vSpacer);

        // Action buttons based on status
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.BOTTOM_RIGHT);

        if ("Pending".equalsIgnoreCase(prenotazione.getStato()) || "IN_ATTESA".equalsIgnoreCase(prenotazione.getStato())) {
            Button btnAccetta = new Button("Accetta");
            btnAccetta.getStyleClass().add("tk-btn-primary");
            btnAccetta.setOnAction(e -> updateBookingStatus(prenotazione, "CONFERMATA"));

            Button btnRifiuta = new Button("Rifiuta");
            btnRifiuta.getStyleClass().add("tk-btn-secondary");
            btnRifiuta.setOnAction(e -> updateBookingStatus(prenotazione, "ANNULLATA"));

            actions.getChildren().addAll(btnAccetta, btnRifiuta);
        } else if ("Confirmed".equalsIgnoreCase(prenotazione.getStato()) || "CONFERMATA".equalsIgnoreCase(prenotazione.getStato())) {
            Button btnAnnulla = new Button("Annulla");
            btnAnnulla.getStyleClass().add("tk-btn-secondary");
            btnAnnulla.setOnAction(e -> updateBookingStatus(prenotazione, "ANNULLATA"));

            actions.getChildren().add(btnAnnulla);
        }

        card.getChildren().add(actions);
        return card;
    }

    private String getStatusLabel(String stato) {
        if (stato == null) return "Non disponibile";
        switch (stato.toLowerCase()) {
            case "pending":
            case "in_attesa":
                return "In attesa";
            case "confirmed":
            case "confermata":
                return "Confermata";
            case "cancelled":
            case "annullata":
                return "Annullata";
            case "completed":
            case "completata":
                return "Completata";
            default:
                return stato;
        }
    }

    private String getStatusStyle(String stato) {
        if (stato == null) return "tk-badge";
        switch (stato.toLowerCase()) {
            case "pending":
            case "in_attesa":
                return "tk-badge-warning";
            case "confirmed":
            case "confermata":
                return "tk-badge-success";
            case "cancelled":
            case "annullata":
                return "tk-badge-error";
            case "completed":
            case "completata":
                return "tk-badge-info";
            default:
                return "tk-badge";
        }
    }

    private void updateBookingStatus(Prenotazione prenotazione, String newStatus) {
        new Thread(() -> {
            try {
                Request req = new Request("MODIFICA_PRENOTAZIONE");
                req.addParametro("idUtente", prenotazione.getIdUtente());
                req.addParametro("idPrenotazione", prenotazione.getId());
                req.addParametro("dataPrenotazione", prenotazione.getDataPrenotazione());
                req.addParametro("posti", prenotazione.getPosti());
                req.addParametro("stato", newStatus);

                Response res = ServerConnection.getInstance().send(req);
                if (res.isSuccesso()) {
                    Platform.runLater(() -> caricaPrenotazioni());
                } else {
                    Platform.runLater(() -> {
                        if (lblError != null) {
                            lblError.setText(res.getMessaggio());
                            lblError.setVisible(true);
                            lblError.setManaged(true);
                        }
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (lblError != null) {
                        lblError.setText("Errore di connessione");
                        lblError.setVisible(true);
                        lblError.setManaged(true);
                    }
                });
            }
        }).start();
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) {
            return "U";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private void configureNavbar() {
        if (btnNavHome != null) {
            btnNavHome.setText("Home");
            btnNavHome.getStyleClass().setAll("tk-nav-item");
        }
        if (btnNavRestaurants != null) {
            btnNavRestaurants.setText("Restaurants");
            btnNavRestaurants.getStyleClass().setAll("tk-nav-item");
        }
        if (btnNavReviews != null) {
            btnNavReviews.setText("Review");
            btnNavReviews.getStyleClass().setAll("tk-nav-item");
        }
        if (btnNavBookings != null) {
            btnNavBookings.setText("Bookings");
            btnNavBookings.getStyleClass().setAll("tk-nav-active");
        }
    }

    private void updateAccountMenu() {
        if (accountMenuButton == null) {
            return;
        }

        accountMenuButton.setText("");
        accountMenuButton.getItems().clear();
        accountMenuButton.setGraphic(createAccountGraphic());

        MenuItem settings = new MenuItem("Settings");
        settings.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Settings");
            alert.setHeaderText("Settings");
            alert.setContentText("Settings are not available yet.");
            alert.showAndWait();
        });
        MenuItem logout = new MenuItem("Logout");
        logout.setOnAction(e -> handleLogout());

        accountMenuButton.getItems().addAll(settings, logout);
    }

    private StackPane createAccountGraphic() {
        SVGPath svgPath = new SVGPath();
        svgPath.setContent("M12.12 12.78C12.05 12.77 11.96 12.77 11.88 12.78C10.12 12.72 8.71997 11.28 8.71997 9.50998C8.71997 7.69998 10.18 6.22998 12 6.22998C13.81 6.22998 15.28 7.69998 15.28 9.50998C15.27 11.28 13.88 12.72 12.12 12.78Z M18.74 19.3801C16.96 21.0101 14.6 22.0001 12 22.0001C9.40001 22.0001 7.04001 21.0101 5.26001 19.3801C5.36001 18.4401 5.96001 17.5201 7.03001 16.8001C9.77001 14.9801 14.25 14.9801 16.97 16.8001C18.04 17.5201 18.64 18.4401 18.74 19.3801Z M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z");
        svgPath.getStyleClass().add("tk-account-menu-icon");
        StackPane icon = new StackPane(svgPath);
        icon.setPrefSize(18, 18);
        return icon;
    }

    @FXML private void handleHome() { ClientTK.loadScene("home.fxml", "TheKnife - Home"); }
    @FXML private void handleRestaurants() { ClientTK.loadScene("dashboard_gestore.fxml", "TheKnife - Dashboard"); }
    @FXML private void handleReviews() { ClientTK.loadScene("gestione_recensioni.fxml", "TheKnife - Review Management"); }
    @FXML private void handleBookings() { ClientTK.loadScene("prenotazioni_gestore.fxml", "TheKnife - Bookings"); }
    @FXML private void handleLogout() {
        try { ServerConnection.getInstance().send(new Request("DISCONNECT")); ServerConnection.getInstance().disconnect(); } catch (Exception e) {}
        SessioneCorrente.getInstance().logout();
        ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome");
    }
}

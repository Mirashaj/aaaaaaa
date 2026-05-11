package theknife.client.controller;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import theknife.client.ClientTK;
import theknife.client.ServerConnection;
import theknife.client.SessioneCorrente;
import theknife.model.Prenotazione;
import theknife.shared.Request;
import theknife.shared.Response;

public class PrenotazioniController {

    @FXML private AnchorPane loginGatePanel;
    @FXML private AnchorPane contentPanel;
    @FXML private Button btnAccedi;
    @FXML private Button btnRegistrati;
    @FXML private TilePane tilePrenotazioni;
    @FXML private VBox emptyStateBox;
    @FXML private Label lblError;
    @FXML private MenuButton accountMenuButton;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM uuuu HH:mm", Locale.ITALIAN);

    @FXML
    public void initialize() {
        updateAccountMenu();

        if (btnAccedi != null) {
            btnAccedi.setOnAction(e -> ClientTK.loadScene("login.fxml", "TheKnife - Login"));
        }
        if (btnRegistrati != null) {
            btnRegistrati.setOnAction(e -> ClientTK.loadScene("registrazione.fxml", "TheKnife - Registration"));
        }

        if (!SessioneCorrente.getInstance().isUserLogged()) {
            if (loginGatePanel != null) {
                loginGatePanel.setVisible(true);
                loginGatePanel.setManaged(true);
            }
            if (contentPanel != null) {
                contentPanel.setVisible(false);
                contentPanel.setManaged(false);
            }
            return;
        }

        if (loginGatePanel != null) {
            loginGatePanel.setVisible(false);
            loginGatePanel.setManaged(false);
        }
        if (contentPanel != null) {
            contentPanel.setVisible(true);
            contentPanel.setManaged(true);
        }

        if (SessioneCorrente.getInstance().isGestore()) {
            ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome");
            return;
        }

        loadPrenotazioni();
        wrapInScrollPane(tilePrenotazioni);
    }

    private void loadPrenotazioni() {
        if (lblError != null) lblError.setVisible(false);
        new Thread(() -> {
            try {
                Request req = new Request("VISUALIZZA_PRENOTAZIONI");
                req.addParametro("idUtente", SessioneCorrente.getInstance().getUtenteLoggato().getId());
                Response res = ServerConnection.getInstance().send(req);
                if (res.isSuccesso()) {
                    @SuppressWarnings("unchecked")
                    List<Prenotazione> list = (List<Prenotazione>) res.getPayload();
                    Platform.runLater(() -> {
                        tilePrenotazioni.getChildren().clear();
                        if (list == null || list.isEmpty()) {
                            if (emptyStateBox != null) {
                                emptyStateBox.setVisible(true);
                                emptyStateBox.setManaged(true);
                            }
                        } else {
                            if (emptyStateBox != null) {
                                emptyStateBox.setVisible(false);
                                emptyStateBox.setManaged(false);
                            }
                            list.forEach(p -> tilePrenotazioni.getChildren().add(createBookingCard(p)));
                        }
                    });
                } else {
                    Platform.runLater(() -> { lblError.setText(res.getMessaggio()); lblError.setVisible(true); lblError.setManaged(true); });
                }
            } catch (IOException | ClassNotFoundException e) {
                Platform.runLater(() -> { if (lblError != null) { lblError.setText("Errore di connessione"); lblError.setVisible(true); lblError.setManaged(true); } });
            }
        }).start();
    }

    private VBox createBookingCard(Prenotazione p) {
        VBox card = new VBox(10);
        card.getStyleClass().add("tk-booking-card");
        card.setPrefWidth(380);
        card.setPrefHeight(160);


        HBox header = new HBox(10);
        Label name = new Label(p.getNomeRistorante());
        name.getStyleClass().add("tk-card-title");
        name.setOnMouseClicked(e -> {
            theknife.model.Ristorante r = new theknife.model.Ristorante();
            r.setId(p.getIdRistorante());
            r.setNome(p.getNomeRistorante());
            SessioneCorrente.getInstance().setSelectedRistorante(r);
            ClientTK.loadScene("dettaglio_ristorante.fxml", "TheKnife - Dettaglio Ristorante");
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label date = new Label(p.getDataPrenotazione() != null ? p.getDataPrenotazione().format(formatter) : "");
        date.getStyleClass().add("tk-text-secondary");
        header.getChildren().addAll(name, spacer, date);

        Label seats = new Label("Posti: " + p.getPosti());
        seats.getStyleClass().add("tk-text-secondary");

        HBox actions = new HBox(8);
        actions.setAlignment(javafx.geometry.Pos.BOTTOM_RIGHT);
        Button btnEdit = new Button("Edit");
        btnEdit.getStyleClass().add("tk-btn-secondary");

        Button btnCancel = new Button("Delete");
        btnCancel.getStyleClass().add("tk-btn-primary");
        btnCancel.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white;");

        actions.getChildren().addAll(btnEdit, btnCancel);

        btnCancel.setOnAction(e -> cancelBooking(p.getId(), card));

        btnEdit.setOnAction(e -> showEditBooking(card, p, seats, date));

        Region vSpacer = new Region();
        vSpacer.setPickOnBounds(false);
        VBox.setVgrow(vSpacer, Priority.ALWAYS);

        card.getChildren().addAll(header, seats, vSpacer, actions);
        return card;
    }


    private void showEditBooking(VBox card, Prenotazione p, Label seatsLabel, Label dateLabel) {
        card.getChildren().removeIf(n -> n instanceof HBox && ((HBox) n).getUserData() != null && "edit-actions".equals(((HBox) n).getUserData()));

        HBox editor = new HBox(10);
        editor.setUserData("edit-actions");

        javafx.scene.control.DatePicker datePicker = new javafx.scene.control.DatePicker();
        if (p.getDataPrenotazione() != null) {
            datePicker.setValue(p.getDataPrenotazione().toLocalDate());
        }

        javafx.scene.control.TextField timeField = new javafx.scene.control.TextField();
        timeField.setPromptText("HH:mm");
        if (p.getDataPrenotazione() != null) {
            timeField.setText(String.format("%02d:%02d", p.getDataPrenotazione().getHour(), p.getDataPrenotazione().getMinute()));
        }

        javafx.scene.control.TextField seatsField = new javafx.scene.control.TextField(String.valueOf(p.getPosti()));
        seatsField.setPrefWidth(80);
        seatsField.setPromptText("Posti");

        Button btnSave = new Button("Salva");
        btnSave.getStyleClass().add("tk-btn-primary");

        Button btnCancelEdit = new Button("Cancel");
        btnCancelEdit.getStyleClass().add("tk-btn-secondary");

        editor.getChildren().addAll(new Label("Data:"), datePicker, new Label("Ora:"), timeField, new Label("Posti:"), seatsField, btnSave, btnCancelEdit);

        btnCancelEdit.setOnAction(ev -> {
            card.getChildren().remove(editor);
        });

        btnSave.setOnAction(ev -> {
            if (datePicker.getValue() == null) {
                lblError.setText("Seleziona una data.");
                lblError.setVisible(true);
                lblError.setManaged(true);
                return;
            }

            String time = timeField.getText() != null ? timeField.getText().trim() : "";
            java.time.LocalTime localTime;
            try {
                localTime = java.time.LocalTime.parse(time);
            } catch (Exception ex) {
                lblError.setText("Formato ora non valido (HH:mm).");
                lblError.setVisible(true);
                lblError.setManaged(true);
                return;
            }

            int newSeats;
            try {
                newSeats = Integer.parseInt(seatsField.getText().trim());
            } catch (Exception ex) {
                lblError.setText("Posti non validi.");
                lblError.setVisible(true);
                lblError.setManaged(true);
                return;
            }
            if (newSeats < 1) {
                lblError.setText("Posti devono essere >= 1.");
                lblError.setVisible(true);
                lblError.setManaged(true);
                return;
            }

            java.time.LocalDateTime newDate = java.time.LocalDateTime.of(datePicker.getValue(), localTime);
            if (newDate.isBefore(java.time.LocalDateTime.now())) {
                lblError.setText("La data e l'ora devono essere future.");
                lblError.setVisible(true);
                lblError.setManaged(true);
                return;
            }

            Request req = new Request("MODIFICA_PRENOTAZIONE");
            req.addParametro("idUtente", SessioneCorrente.getInstance().getUtenteLoggato().getId());
            req.addParametro("idPrenotazione", p.getId());
            req.addParametro("dataPrenotazione", newDate);
            req.addParametro("posti", newSeats);
            req.addParametro("stato", p.getStato() != null ? p.getStato() : "CONFERMATA");

            new Thread(() -> {
                try {
                    Response res = ServerConnection.getInstance().send(req);
                    if (res.isSuccesso()) {
                        Platform.runLater(() -> {
                            card.getChildren().remove(editor);
                            seatsLabel.setText("Posti: " + newSeats);
                            dateLabel.setText(newDate.format(formatter));
                        });
                    } else {
                        Platform.runLater(() -> {
                            lblError.setText(res.getMessaggio());
                            lblError.setVisible(true);
                            lblError.setManaged(true);
                        });
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    Platform.runLater(() -> {
                        lblError.setText("Errore di connessione");
                        lblError.setVisible(true);
                        lblError.setManaged(true);
                    });
                }
            }).start();
        });

        card.getChildren().add(editor);
    }

    private void cancelBooking(int idPrenotazione, VBox card) {
        new Thread(() -> {
            try {
                Request req = new Request("ELIMINA_PRENOTAZIONE");
                req.addParametro("idPrenotazione", idPrenotazione);
                Response res = ServerConnection.getInstance().send(req);
                if (res.isSuccesso()) {
                    Platform.runLater(() -> tilePrenotazioni.getChildren().remove(card));
                } else {
                    Platform.runLater(() -> { lblError.setText(res.getMessaggio()); lblError.setVisible(true); lblError.setManaged(true); });
                }
            } catch (IOException | ClassNotFoundException e) {
                Platform.runLater(() -> { lblError.setText("Errore di connessione"); lblError.setVisible(true); lblError.setManaged(true); });
            }
        }).start();
    }


    private void updateAccountMenu() {
        if (accountMenuButton == null) return;
        accountMenuButton.setText("");
        accountMenuButton.getItems().clear();
        accountMenuButton.setGraphic(createAccountGraphic());

        if (SessioneCorrente.getInstance().isUserLogged()) {
            MenuItem settings = new MenuItem("Settings");
            settings.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Settings");
                alert.setHeaderText("Settings");
                alert.setContentText("Settings are not available yet.");
                alert.showAndWait();
            });
            MenuItem logout = new MenuItem("Logout");
            logout.setOnAction(e -> { SessioneCorrente.getInstance().logout(); ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome"); });
            accountMenuButton.getItems().addAll(settings, logout);
        } else {
            MenuItem register = new MenuItem("Register");
            register.setOnAction(e -> ClientTK.loadScene("registrazione.fxml", "TheKnife - Registration"));
            MenuItem login = new MenuItem("Login");
            login.setOnAction(e -> ClientTK.loadScene("login.fxml", "TheKnife - Login"));
            MenuItem back = new MenuItem("Back");
            back.setOnAction(e -> ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome"));
            accountMenuButton.getItems().addAll(register, login, back);
        }
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
    @FXML private void handlePreferiti() { ClientTK.loadScene("preferiti.fxml", "TheKnife - Favorites"); }
    @FXML private void handleMieRecensioni() { ClientTK.loadScene("mie_recensioni.fxml", "TheKnife - My Reviews"); }
    @FXML private void handlePrenotazioni() { ClientTK.loadScene("prenotazioni.fxml", "TheKnife - Bookings"); }
    @FXML private void handleIndietro() { ClientTK.loadScene("home.fxml", "TheKnife - Home"); }

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
}

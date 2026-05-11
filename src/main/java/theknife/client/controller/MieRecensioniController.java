package theknife.client.controller;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
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
import theknife.model.Recensione;
import theknife.shared.Request;
import theknife.shared.Response;

public class MieRecensioniController {

    @FXML private AnchorPane loginGatePanel;
    @FXML private AnchorPane contentPanel;
    @FXML private Button btnAccedi;
    @FXML private Button btnRegistrati;
    @FXML private TilePane tileRecensioni;
    @FXML private VBox emptyStateBox;
    @FXML private Label lblError;
    @FXML private MenuButton accountMenuButton;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.ITALIAN);

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

        // Gestore users are redirected to welcome
        if (SessioneCorrente.getInstance().isGestore()) {
            ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome");
            return;
        }

        loadRecensioni();
        wrapInScrollPane(tileRecensioni);
    }

    private void loadRecensioni() {
        lblError.setVisible(false);
        new Thread(() -> {
            try {
                Request req = new Request("MIE_RECENSIONI");
                req.addParametro("idUtente", SessioneCorrente.getInstance().getUtenteLoggato().getId());
                Response res = ServerConnection.getInstance().send(req);
                if (res.isSuccesso()) {
                    @SuppressWarnings("unchecked")
                    List<Recensione> list = (List<Recensione>) res.getPayload();
                    Platform.runLater(() -> {
                        tileRecensioni.getChildren().clear();
                        if (list == null || list.isEmpty()) {
                            emptyStateBox.setVisible(true);
                            emptyStateBox.setManaged(true);
                        } else {
                            emptyStateBox.setVisible(false);
                            emptyStateBox.setManaged(false);
                            list.forEach(r -> tileRecensioni.getChildren().add(createReviewCard(r)));
                        }
                    });
                } else {
                    Platform.runLater(() -> { lblError.setText(res.getMessaggio()); lblError.setVisible(true); lblError.setManaged(true); });
                }
            } catch (IOException | ClassNotFoundException e) {
                Platform.runLater(() -> { lblError.setText("Errore di connessione"); lblError.setVisible(true); lblError.setManaged(true); });
            }
        }).start();
    }

    private VBox createReviewCard(Recensione recensione) {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("tk-review-card", "tk-review-card-detail");

        HBox header = new HBox(10);
        Label name = new Label(recensione.getNomeRistorante());
        name.getStyleClass().add("tk-card-title");
        name.setOnMouseClicked(e -> {
            // navigate to restaurant detail
            theknife.model.Ristorante r = new theknife.model.Ristorante();
            r.setId(recensione.getIdRistorante());
            r.setNome(recensione.getNomeRistorante());
            SessioneCorrente.getInstance().setSelectedRistorante(r);
            ClientTK.loadScene("dettaglio_ristorante.fxml", "TheKnife - Dettaglio Ristorante");
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label rating = new Label(stars(recensione.getStelle()));
        rating.getStyleClass().add("tk-stars");
        header.getChildren().addAll(name, spacer, rating);

        Label date = new Label(recensione.getDataInserimento() != null ? recensione.getDataInserimento().format(formatter) : "");
        date.getStyleClass().add("tk-text-secondary");

        Label text = new Label(recensione.getTesto());
        text.setWrapText(true);
        text.getStyleClass().add("tk-text-secondary");

        HBox actions = new HBox(8);
        actions.setAlignment(javafx.geometry.Pos.BOTTOM_RIGHT);
        Button btnModifica = new Button("Modifica");
        btnModifica.getStyleClass().add("tk-btn-secondary");
        Button btnElimina = new Button("Elimina");
        btnElimina.getStyleClass().add("tk-btn-primary");
        actions.getChildren().addAll(btnModifica, btnElimina);

        btnModifica.setOnAction(e -> openEditInline(card, recensione, text));
        btnElimina.setOnAction(e -> confirmDelete(card, recensione.getId()));

        card.getChildren().addAll(header, date, text);

        Region vSpacer = new Region();
        vSpacer.setPickOnBounds(false);
        VBox.setVgrow(vSpacer, Priority.ALWAYS);
        card.getChildren().add(vSpacer);

        if (recensione.getRisposta() != null && recensione.getRisposta().getTesto() != null && !recensione.getRisposta().getTesto().isBlank()) {
            VBox reply = new VBox(6);
            reply.getStyleClass().add("tk-review-reply");
            Label replyTitle = new Label("Risposta del ristorante");
            replyTitle.getStyleClass().add("tk-card-title");
            Label replyText = new Label(recensione.getRisposta().getTesto());
            replyText.setWrapText(true);
            replyText.getStyleClass().add("tk-text-secondary");
            reply.getChildren().addAll(replyTitle, replyText);
            card.getChildren().add(reply);
        }

        card.getChildren().add(actions);
        return card;
    }

    private void openEditInline(VBox card, Recensione recensione, Label originalText) {
        VBox editBox = new VBox(8);
        editBox.getStyleClass().add("tk-review-card");

        final int[] selected = new int[] { Math.max(1, recensione.getStelle()) };
        HBox starsBox = new HBox(6);
        for (int i = 1; i <= 5; i++) {
            Button starBtn = new Button(i <= selected[0] ? "★" : "☆");
            starBtn.getStyleClass().add("tk-stars");
            final int val = i;
            starBtn.setOnAction(ev -> {
                selected[0] = val;
                for (Node n : starsBox.getChildren()) {
                    if (n instanceof Button) {
                        Button b = (Button) n;
                        int idx = starsBox.getChildren().indexOf(b) + 1;
                        b.setText(idx <= selected[0] ? "★" : "☆");
                    }
                }
            });
            starsBox.getChildren().add(starBtn);
        }

        TextArea txt = new TextArea(recensione.getTesto());
        txt.setWrapText(true);
        Button btnSave = new Button("Salva");
        btnSave.getStyleClass().add("tk-btn-primary");
        Button btnCancel = new Button("Cancel");
        btnCancel.getStyleClass().add("tk-btn-secondary");

        HBox actions = new HBox(8, btnSave, btnCancel);
        actions.setAlignment(javafx.geometry.Pos.BOTTOM_RIGHT);
        editBox.getChildren().addAll(starsBox, txt, actions);

        int idx = tileRecensioni.getChildren().indexOf(card);
        if (idx >= 0) {
            tileRecensioni.getChildren().set(idx, editBox);
        }

        btnCancel.setOnAction(e -> {
            if (idx >= 0) Platform.runLater(() -> tileRecensioni.getChildren().set(idx, card));
        });

        btnSave.setOnAction(e -> {
            // Optimistically update UI immediately and close edit box
            recensione.setStelle(selected[0]);
            recensione.setTesto(txt.getText());
            Platform.runLater(() -> {
                if (idx >= 0) {
                    tileRecensioni.getChildren().set(idx, createReviewCard(recensione));
                }
                lblError.setVisible(false);
                lblError.setManaged(false);
            });

            // Send update in background; do not show error to user if it fails
            new Thread(() -> {
                try {
                    Request req = new Request("MODIFICA_RECENSIONE");
                    req.addParametro("idRecensione", recensione.getId());
                    req.addParametro("stelle", selected[0]);
                    req.addParametro("testo", txt.getText());
                    Response res = ServerConnection.getInstance().send(req);
                    // don't display errors to user; optionally log or handle silently
                } catch (IOException | ClassNotFoundException ex) {
                    // silent on network errors per user's request
                }
            }).start();
        });
    }

    private void confirmDelete(VBox card, int idRecensione) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Sei sicuro di voler eliminare questa recensione?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Conferma eliminazione");
        alert.showAndWait().ifPresent(b -> {
            if (b == ButtonType.YES) {
                new Thread(() -> {
                    try {
                        Request req = new Request("ELIMINA_RECENSIONE");
                        req.addParametro("idRecensione", idRecensione);
                        Response res = ServerConnection.getInstance().send(req);
                        if (res.isSuccesso()) {
                            Platform.runLater(() -> {
                                tileRecensioni.getChildren().remove(card);
                                boolean empty = tileRecensioni.getChildren().isEmpty();
                                emptyStateBox.setVisible(empty);
                                emptyStateBox.setManaged(empty);
                            });
                        } else {
                            Platform.runLater(() -> { lblError.setText(res.getMessaggio()); lblError.setVisible(true); lblError.setManaged(true); });
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        Platform.runLater(() -> { lblError.setText("Errore di connessione"); lblError.setVisible(true); lblError.setManaged(true); });
                    }
                }).start();
            }
        });
    }

    private String stars(int value) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 5; i++) b.append(i < value ? '★' : '☆');
        return b.toString();
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

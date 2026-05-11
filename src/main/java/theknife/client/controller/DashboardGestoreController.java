package theknife.client.controller;

import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import theknife.client.ClientTK;
import theknife.client.ServerConnection;
import theknife.client.SessioneCorrente;
import theknife.model.Ristorante;
import theknife.shared.Request;
import theknife.shared.Response;

public class DashboardGestoreController {
    @FXML private Label lblTitolo;
    @FXML private Label lblSottotitolo;
    @FXML private Label lblEmpty;
    @FXML private Label lblError;
    @FXML private TilePane tileRistoranti;
    @FXML private MenuButton accountMenuButton;
    @FXML private Button btnNavHome;
    @FXML private Button btnNavRestaurants;
    @FXML private Button btnNavReviews;
    @FXML private Button btnNavBookings;
    @FXML private Button btnNuovoRistorante;

    @FXML
    public void initialize() {
        if (!SessioneCorrente.getInstance().isUserLogged() || !SessioneCorrente.getInstance().isGestore()) {
            ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome");
            return;
        }

        configureNavbar();
        updateAccountMenu();

        if (lblTitolo != null) {
            lblTitolo.setText("I miei ristoranti");
        }
        if (lblSottotitolo != null) {
            lblSottotitolo.setText("Gestisci i tuoi locali, aggiungine di nuovi e apri i dettagli con un click.");
        }
        if (btnNuovoRistorante != null) {
            btnNuovoRistorante.setOnAction(e -> handleAggiungi());
        }

        caricaRistoranti();
        wrapInScrollPane(tileRistoranti);
    }

    private void caricaRistoranti() {
        if (lblError != null) {
            lblError.setVisible(false);
            lblError.setManaged(false);
        }

        new Thread(() -> {
            try {
                Request req = new Request("RISTORANTI_GESTORE");
                req.addParametro("idGestore", SessioneCorrente.getInstance().getUtenteLoggato().getId());
                Response res = ServerConnection.getInstance().send(req);
                if (res.isSuccesso() && tileRistoranti != null) {
                    @SuppressWarnings("unchecked")
                    List<Ristorante> ristoranti = (List<Ristorante>) res.getPayload();
                    Platform.runLater(() -> {
                        if (ristoranti == null || ristoranti.isEmpty()) {
                            tileRistoranti.getChildren().clear();
                            if (lblEmpty != null) {
                                lblEmpty.setVisible(true);
                                lblEmpty.setManaged(true);
                            }
                            return;
                        }
                        tileRistoranti.getChildren().setAll(ristoranti.stream().map(this::createRestaurantCard).toList());
                        if (lblEmpty != null) {
                            lblEmpty.setVisible(false);
                            lblEmpty.setManaged(false);
                        }
                    });
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
                        lblError.setText(e.getMessage());
                        lblError.setVisible(true);
                        lblError.setManaged(true);
                    }
                });
            }
        }).start();
    }

    private VBox createRestaurantCard(Ristorante ristorante) {
        VBox card = new VBox(12);
        card.getStyleClass().add("tk-card");
        card.setPrefWidth(350);
        card.setMinHeight(200);
        card.setOnMouseClicked(e -> openRestaurant(ristorante));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = new StackPane(new Label(iconForCuisine(ristorante.getTipoCucina())));
        avatar.getStyleClass().add("tk-result-thumb");
        avatar.setPrefSize(44, 44);
        avatar.setMinSize(44, 44);
        avatar.setMaxSize(44, 44);

        VBox meta = new VBox(2);
        Label name = new Label(ristorante.getNome());
        name.getStyleClass().add("tk-card-title");
        Label place = new Label(safeText(ristorante.getCitta()) + (safeText(ristorante.getNazione()).isEmpty() ? "" : ", " + safeText(ristorante.getNazione())));
        place.getStyleClass().add("tk-text-secondary");
        meta.getChildren().addAll(name, place);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label score = new Label(String.format("%.1f ★", ristorante.getMediaStelle()));
        score.getStyleClass().add("tk-stars");

        header.getChildren().addAll(avatar, meta, spacer, score);

        Label address = new Label(safeText(ristorante.getIndirizzo()));
        address.setWrapText(true);
        address.getStyleClass().add("tk-text-secondary");

        HBox badges = new HBox(8);
        Label cuisine = new Label(safeText(ristorante.getTipoCucina()).isEmpty() ? "Cuisine" : safeText(ristorante.getTipoCucina()));
        cuisine.getStyleClass().add("tk-chip");
        Label booking = new Label(ristorante.isPrenotazione() ? "Booking" : "No booking");
        booking.getStyleClass().add(ristorante.isPrenotazione() ? "tk-chip" : "tk-badge");
        Label delivery = new Label(ristorante.isDelivery() ? "Delivery" : "No delivery");
        delivery.getStyleClass().add(ristorante.isDelivery() ? "tk-chip" : "tk-badge");
        badges.getChildren().addAll(cuisine, booking, delivery);

        // edit button (prevent card click from firing)
        Button btnEdit = new Button("Edit");
        btnEdit.setFocusTraversable(false);
        btnEdit.getStyleClass().add("tk-btn-secondary");
        btnEdit.setOnAction(ev -> {
            ev.consume();
            // avoid triggering the card click handler
            SessioneCorrente.getInstance().setSelectedRistorante(ristorante);
            ClientTK.loadScene("aggiungi_ristorante.fxml", "TheKnife - Edit Restaurant");
        });

        HBox footerActions = new HBox(btnEdit);
        footerActions.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(header, address, badges, footerActions);
        return card;
    }

    private void openRestaurant(Ristorante ristorante) {
        if (ristorante == null) {
            return;
        }
        SessioneCorrente.getInstance().setSelectedRistorante(ristorante);
        ClientTK.loadScene("dettaglio_ristorante.fxml", "TheKnife - " + ristorante.getNome());
    }

    private void configureNavbar() {
        if (btnNavHome != null) {
            btnNavHome.setText("Home");
            btnNavHome.getStyleClass().setAll("tk-nav-item");
        }
        if (btnNavRestaurants != null) {
            btnNavRestaurants.setText("Restaurants");
            btnNavRestaurants.getStyleClass().setAll("tk-nav-active");
        }
        if (btnNavReviews != null) {
            btnNavReviews.setText("Review");
            btnNavReviews.getStyleClass().setAll("tk-nav-item");
        }
        if (btnNavBookings != null) {
            btnNavBookings.setVisible(false);
            btnNavBookings.setManaged(false);
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

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String iconForCuisine(String cuisine) {
        if (cuisine == null) return "🍽";
        String normalized = cuisine.toLowerCase();
        if (normalized.contains("ital")) return "🍝";
        if (normalized.contains("japan") || normalized.contains("sushi")) return "🍣";
        if (normalized.contains("pizza")) return "🍕";
        if (normalized.contains("sea")) return "🐟";
        if (normalized.contains("meat") || normalized.contains("grill")) return "🥩";
        return "🍽";
    }

    @FXML private void handleHome() { ClientTK.loadScene("home.fxml", "TheKnife - Home"); }
    @FXML private void handleRestaurants() { ClientTK.loadScene("dashboard_gestore.fxml", "TheKnife - Dashboard"); }
    @FXML private void handleReviews() { ClientTK.loadScene("gestione_recensioni.fxml", "TheKnife - Review Management"); }
    @FXML private void handleLogout() {
        try { ServerConnection.getInstance().send(new Request("DISCONNECT")); ServerConnection.getInstance().disconnect(); } catch (Exception e) {}
        SessioneCorrente.getInstance().logout();
        ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome");
    }

    @FXML
    private void handleAggiungi() { ClientTK.loadScene("aggiungi_ristorante.fxml", "TheKnife - Add Restaurant"); }

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

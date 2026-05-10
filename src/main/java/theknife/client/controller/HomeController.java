package theknife.client.controller;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
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

/*
 * 
 * Mirashaj Erik 760453 VA
 * GorchynskYi Igor 757184 VA
 * Kabuka Dan Mumanga 757708 VA
 * Mujeci Lorenzo 757597 VA
 *
 */

public class HomeController {

    private static final int RESULTS_PER_PAGE = 40;

    @FXML
    private TextField txtRicerca;

    @FXML
    private ComboBox<String> comboCucina;

    @FXML
    private ComboBox<String> comboPrezzo;

    @FXML
    private TextField txtCucina;

    @FXML
    private TextField txtPrezzoMin;

    @FXML
    private TextField txtPrezzoMax;

    @FXML
    private CheckBox chkDelivery;

    @FXML
    private CheckBox chkPrenotazione;

    @FXML
    private Label lblMessaggio;

    @FXML
    private TilePane flowRistoranti;

    @FXML
    private Button btnCaricaAltro;

    @FXML
    private VBox filterMenu;

    @FXML
    private TextField txtLatitudine;

    @FXML
    private TextField txtLongitudine;

    @FXML
    private TextField txtRaggio;

    @FXML
    private Slider sliderStelle;

    @FXML
    private Label lblStelleMin;

    @FXML
    private MenuButton accountMenuButton;

    private final List<Ristorante> risultatiCorrenti = new ArrayList<>();
    private int risultatiMostrati = 0;

    @FXML
    public void initialize() {
        txtRicerca.setOnAction(e -> handleCerca());


        if (sliderStelle != null && lblStelleMin != null) {
            sliderStelle.valueProperty().addListener((obs, oldValue, newValue) ->
                    lblStelleMin.setText(String.format("Valutazione min: %.1f", newValue.doubleValue()))
            );
        }

        if (SessioneCorrente.getInstance().isUserLogged()) {
            String dom = SessioneCorrente.getInstance().getUtenteLoggato().getDomicilio();
            if (dom != null && !dom.isEmpty()) {
                txtRicerca.setText(dom);
            }
        }

        updateAccountMenu();
        
        handleCerca();

        if (filterMenu != null) {
            filterMenu.setVisible(false);
            filterMenu.setManaged(false);
        }

        refreshLoadMoreButton();
    }

    private void updateAccountMenu() {
        if (accountMenuButton == null) {
            return;
        }

        accountMenuButton.setText("");
        accountMenuButton.getItems().clear();
        accountMenuButton.setGraphic(createAccountGraphic());

        if (SessioneCorrente.getInstance().isUserLogged()) {
            MenuItem myAccount = new MenuItem("My Account");
            myAccount.setOnAction(e -> handleMyAccount());

            MenuItem settings = new MenuItem("Settings");
            settings.setOnAction(e -> handleSettings());

            MenuItem logout = new MenuItem("Logout");
            logout.setOnAction(e -> handleLogout());

            accountMenuButton.getItems().addAll(myAccount, settings, new SeparatorMenuItem(), logout);
        } else {
            MenuItem login = new MenuItem("Login");
            login.setOnAction(e -> handleLogin());

            MenuItem register = new MenuItem("Create Account");
            register.setOnAction(e -> handleRegister());

            MenuItem settings = new MenuItem("Settings");
            settings.setOnAction(e -> handleSettings());

            accountMenuButton.getItems().addAll(login, register, new SeparatorMenuItem(), settings);
        }
    }

    private HBox createAccountGraphic() {
        SVGPath svgPath = new SVGPath();
        // Icona account
        svgPath.setContent("M12.12 12.78C12.05 12.77 11.96 12.77 11.88 12.78C10.12 12.72 8.71997 11.28 8.71997 9.50998C8.71997 7.69998 10.18 6.22998 12 6.22998C13.81 6.22998 15.28 7.69998 15.28 9.50998C15.27 11.28 13.88 12.72 12.12 12.78Z M18.74 19.3801C16.96 21.0101 14.6 22.0001 12 22.0001C9.40001 22.0001 7.04001 21.0101 5.26001 19.3801C5.36001 18.4401 5.96001 17.5201 7.03001 16.8001C9.77001 14.9801 14.25 14.9801 16.97 16.8001C18.04 17.5201 18.64 18.4401 18.74 19.3801Z M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z");
        svgPath.setScaleX(1);
        svgPath.setScaleY(1);
        svgPath.getStyleClass().add("tk-account-menu-icon");
        
        StackPane icon = new StackPane(svgPath);
        icon.setPrefSize(18, 18);
        icon.setMinSize(18, 18);
        icon.setMaxSize(18, 18);
        
        HBox box = new HBox(icon);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    @FXML
    private void handleLogin() {
        ClientTK.loadScene("login.fxml", "TheKnife - Login");
    }

    @FXML
    private void handleRegister() {
        ClientTK.loadScene("registrazione.fxml", "TheKnife - Registration");
    }

    @FXML
    private void handleLogout() {
        SessioneCorrente.getInstance().logout();
        ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome");
    }

    @FXML
    private void handleMyAccount() {
        if (SessioneCorrente.getInstance().isUserLogged() && SessioneCorrente.getInstance().isGestore()) {
            ClientTK.loadScene("dashboard_gestore.fxml", "TheKnife - Dashboard");
            return;
        }
        if (SessioneCorrente.getInstance().isUserLogged()) {
            ClientTK.loadScene("mie_recensioni.fxml", "TheKnife - My Reviews");
            return;
        }
        ClientTK.loadScene("login.fxml", "TheKnife - Login");
    }

    @FXML
    private void handleSettings() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Settings");
        alert.setHeaderText("Settings are coming soon");
        alert.setContentText("You will be able to manage account preferences here.");
        alert.showAndWait();
    }

    @FXML
    private void handleToggleFilters() {
        if (filterMenu == null) return;
        boolean show = !filterMenu.isVisible();
        filterMenu.setVisible(show);
        filterMenu.setManaged(show);
    }

    @FXML
    private void handleCerca() {
        String luogo = txtRicerca.getText().trim();
            String cucinaVal = (txtCucina != null) ? txtCucina.getText().trim() : "";
            String prezzoMinStr = (txtPrezzoMin != null) ? txtPrezzoMin.getText().trim() : "";
            String prezzoMaxStr = (txtPrezzoMax != null) ? txtPrezzoMax.getText().trim() : "";
        boolean delivery = chkDelivery.isSelected();
        boolean prenotazione = chkPrenotazione != null && chkPrenotazione.isSelected();

            double stelleMin = sliderStelle != null ? sliderStelle.getValue() : 0.0;

            lblMessaggio.setText("Ricerca in corso...");
        if (flowRistoranti != null) {
            flowRistoranti.getChildren().clear();
        }

        try {
            Request request = new Request("CERCA_RISTORANTE");
            if (!luogo.isEmpty()) {
                request.addParametro("luogo", luogo);
            }
                if (!cucinaVal.isEmpty()) {
                request.addParametro("tipoCucina", cucinaVal);
            }
            if (delivery) {
                request.addParametro("delivery", true);
            }
            if (prenotazione) {
                request.addParametro("prenotazione", true);
            }
            if (stelleMin > 0) {
                request.addParametro("stelleMin", stelleMin);
            }
            if (!prezzoMinStr.isEmpty()) {
                try {
                    double prezzoMin = Double.parseDouble(prezzoMinStr);
                    request.addParametro("prezzoMin", prezzoMin);
                } catch (NumberFormatException ex) {
                    lblMessaggio.setText("Il prezzo minimo deve essere un numero.");
                    return;
                }
            }
            if (!prezzoMaxStr.isEmpty()) {
                try {
                    double prezzoMax = Double.parseDouble(prezzoMaxStr);
                    request.addParametro("prezzoMax", prezzoMax);
                } catch (NumberFormatException ex) {
                    lblMessaggio.setText("Il prezzo massimo deve essere un numero.");
                    return;
                }
            }

            if (txtLatitudine != null && !txtLatitudine.getText().trim().isEmpty() &&
                txtLongitudine != null && !txtLongitudine.getText().trim().isEmpty() &&
                txtRaggio != null && !txtRaggio.getText().trim().isEmpty()) {
                try {
                    request.addParametro("latitudine", Double.parseDouble(txtLatitudine.getText().trim()));
                    request.addParametro("longitudine", Double.parseDouble(txtLongitudine.getText().trim()));
                    request.addParametro("raggioKm", Double.parseDouble(txtRaggio.getText().trim()));
                } catch (NumberFormatException ex) {
                    lblMessaggio.setText("Latitudine, longitudine e raggio devono essere numeri.");
                    return;
                }
            }

            Response response = ServerConnection.getInstance().send(request);

            if (response.isSuccesso()) {
                List<Ristorante> risultati = (List<Ristorante>) response.getPayload();
                risultatiCorrenti.clear();
                risultatiCorrenti.addAll(risultati);
                risultatiMostrati = 0;
                renderVisibleResults();
                lblMessaggio.setText(risultati.size() + " restaurants found.");
            } else {
                lblMessaggio.setText("Error: " + response.getMessaggio());
                if (flowRistoranti != null) {
                    flowRistoranti.getChildren().add(createEmptyState("Search failed", response.getMessaggio()));
                }
            }

        } catch (Exception e) {
            lblMessaggio.setText("Connection error.");
            if (flowRistoranti != null) {
                flowRistoranti.getChildren().add(createEmptyState("Connection error", "The app could not reach the server."));
            }
        }
    }

    @FXML
    private void handleLoadMore() {
        if (risultatiCorrenti.isEmpty()) {
            return;
        }
        renderMoreResults();
    }

    private void renderVisibleResults() {
        if (flowRistoranti == null) {
            return;
        }

        flowRistoranti.getChildren().clear();
        if (risultatiCorrenti.isEmpty()) {
            flowRistoranti.getChildren().add(createEmptyState("No restaurants found", "Try widening your filters or clearing the price range."));
            refreshLoadMoreButton();
            return;
        }

        renderMoreResults();
    }

    private void renderMoreResults() {
        if (flowRistoranti == null) {
            return;
        }

        int nextLimit = Math.min(risultatiCorrenti.size(), risultatiMostrati + RESULTS_PER_PAGE);
        for (int i = risultatiMostrati; i < nextLimit; i++) {
            flowRistoranti.getChildren().add(createRestaurantCard(risultatiCorrenti.get(i)));
        }
        risultatiMostrati = nextLimit;
        refreshLoadMoreButton();
    }

    private void refreshLoadMoreButton() {
        if (btnCaricaAltro == null) {
            return;
        }
        boolean hasMore = !risultatiCorrenti.isEmpty() && risultatiMostrati < risultatiCorrenti.size();
        btnCaricaAltro.setVisible(hasMore);
        btnCaricaAltro.setManaged(hasMore);
    }

    private VBox createRestaurantCard(Ristorante r) {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("tk-card", "tk-result-card");

        HBox header = new HBox(10);
        Label icon = new Label(getCuisineIcon(r.getTipoCucina()));
        icon.getStyleClass().add("tk-result-thumb");
        icon.setMinSize(44, 44);
        icon.setPrefSize(44, 44);
        icon.setAlignment(javafx.geometry.Pos.CENTER);

        VBox titleBox = new VBox(2);
        Label lblNome = new Label(r.getNome());
        lblNome.getStyleClass().add("tk-card-title");
        Label lblMeta = new Label((safeText(r.getCitta()) + ", " + safeText(r.getNazione())).replaceAll("^, |, $", ""));
        lblMeta.getStyleClass().add("tk-text-secondary");
        titleBox.getChildren().addAll(lblNome, lblMeta);

        header.getChildren().addAll(icon, titleBox);

        Label lblCucina = new Label("Cuisine: " + safeText(r.getTipoCucina()));
        lblCucina.getStyleClass().add("tk-text-secondary");

        Label lblIndirizzo = new Label(safeText(r.getIndirizzo()));
        lblIndirizzo.getStyleClass().add("tk-text-secondary");
        lblIndirizzo.setWrapText(true);

        HBox tagsRow = new HBox(8);
        Label lblPrezzo = new Label(formatPrice(r.getPrezzoMedio()));
        lblPrezzo.getStyleClass().addAll("tk-badge", getPriceStyleClass(r.getPrezzoMedio()));
        tagsRow.getChildren().add(lblPrezzo);

        if (r.isDelivery()) {
            Label lblDelivery = new Label("Delivery");
            lblDelivery.getStyleClass().add("tk-chip");
            tagsRow.getChildren().add(lblDelivery);
        }

        if (r.isPrenotazione()) {
            Label lblBooking = new Label("Booking");
            lblBooking.getStyleClass().add("tk-chip");
            tagsRow.getChildren().add(lblBooking);
        }

        HBox ratingRow = createRatingRow(r);

        card.setOnMouseClicked(event -> {
            SessioneCorrente.getInstance().setSelectedRistorante(r);
            ClientTK.loadScene("dettaglio_ristorante.fxml", "TheKnife - " + r.getNome());
        });

        card.getChildren().addAll(header, lblCucina, lblIndirizzo, tagsRow, ratingRow);
        return card;
    }

    private VBox createEmptyState(String title, String subtitle) {
        VBox empty = new VBox(6);
        empty.getStyleClass().add("tk-empty-state");
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("tk-section-title");
        Label lblSubtitle = new Label(subtitle);
        lblSubtitle.getStyleClass().add("tk-text-secondary");
        lblSubtitle.setWrapText(true);
        empty.getChildren().addAll(lblTitle, lblSubtitle);
        empty.setPrefWidth(320);
        return empty;
    }

    private String formatPrice(double price) {
        if (price <= 0) {
            return "Price on request";
        }
        if (price < 50) {
            return "€ Low";
        }
        if (price < 100) {
            return "€€ Medium";
        }
        return "€€€ High";
    }

    private String getPriceStyleClass(double price) {
        if (price <= 0) {
            return "tk-price-request";
        }
        if (price < 50) {
            return "tk-price-low";
        }
        if (price < 100) {
            return "tk-price-medium";
        }
        return "tk-price-high";
    }

    private String getCuisineIcon(String cuisine) {
        if (cuisine == null) {
            return "🍽";
        }
        String normalized = cuisine.toLowerCase();
        if (normalized.contains("japan")) return "🍣";
        if (normalized.contains("sea")) return "🦐";
        if (normalized.contains("grill") || normalized.contains("meat")) return "🥩";
        if (normalized.contains("creative")) return "✨";
        if (normalized.contains("ital")) return "🍝";
        if (normalized.contains("span")) return "🥘";
        if (normalized.contains("indian")) return "🍛";
        if (normalized.contains("burger") || normalized.contains("american")) return "🍔";
        if (normalized.contains("asian") || normalized.contains("chinese")) return "🍥";
        if (normalized.contains("vegetarian") || normalized.contains("vegan")) return "🥦";
        if (normalized.contains("pizza")) return "🍕";
        if (normalized.contains("mexican")) return "🌮";
        if (normalized.contains("mediterranean")) return "🥗";
        if (normalized.contains("thai")) return "🍜";
        if (normalized.contains("french")) return "🥖";
        if (normalized.contains("hot")) return "♨️";
        if (normalized.contains("dessert") || normalized.contains("sweet")) return "🍰";
        if (normalized.contains("sushi")) return "🍣";
        return "🍽";
    }

    private HBox createRatingRow(Ristorante r) {
        double value = r != null ? r.getMediaStelle() : 0.0;
        int recensioni = r != null ? r.getNumRecensioni() : 0;

        HBox ratingRow = new HBox(2);
        ratingRow.setAlignment(Pos.CENTER_LEFT);

        // Mostra voto se disponibile, altrimenti "Nessun voto"
        if (recensioni <= 0 && value <= 0.0001) {
            Label noRating = new Label("Nessun voto");
            noRating.getStyleClass().add("tk-stars");
            ratingRow.getChildren().add(noRating);
            return ratingRow;
        }

        // Genera stelle visive
        int fullStars = (int) value;
        boolean hasHalfStar = (value % 1) >= 0.5;
        int emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);

        for (int i = 0; i < fullStars; i++) {
            ratingRow.getChildren().add(createStarLabel("★", false));
        }
        if (hasHalfStar) {
            ratingRow.getChildren().add(createStarLabel("⯪", true));
        }
        for (int i = 0; i < emptyStars; i++) {
            ratingRow.getChildren().add(createStarLabel("☆", false));
        }
        Label valueLabel = new Label(String.format(" %.1f", value));
        valueLabel.getStyleClass().add("tk-stars");
        ratingRow.getChildren().add(valueLabel);

        return ratingRow;

    }

    private Label createStarLabel(String symbol, boolean isHalfStar) {
        Label star = new Label(symbol);
        star.getStyleClass().add("tk-stars");
        if (isHalfStar) {
            star.getStyleClass().add("tk-star-half");
        }
        return star;
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    @FXML
    private void handlePreferiti() {
        ClientTK.loadScene("preferiti.fxml", "TheKnife - Favorites");
    }

    @FXML
    private void handleMieRecensioni() {
        ClientTK.loadScene("mie_recensioni.fxml", "TheKnife - My Reviews");
    }

    @FXML
    private void handleHome() {
        ClientTK.loadScene("home.fxml", "TheKnife - Home");
    }

    @FXML
    private void handleIndietro() {
        if (SessioneCorrente.getInstance().isUserLogged() && 
            "gestore".equals(SessioneCorrente.getInstance().getUtenteLoggato().getRuolo())) {
            ClientTK.loadScene("dashboard_gestore.fxml", "TheKnife - Dashboard");
        } else {
            ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome");
        }
    }
}
package theknife.client.controller;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import theknife.client.ClientTK;
import theknife.client.ServerConnection;
import theknife.client.SessioneCorrente;
import theknife.model.Recensione;
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

@SuppressWarnings("unused")
public class DettaglioRistoranteController {
    
    @FXML private Label lblNome, lblCitta, lblIndirizzo, lblPrezzo, lblCucina, lblDelivery, lblPrenotazione, lblAvgRating, lblReviewCount, lblCoordinate, lblMessaggio, lblVotoSelezionato;
    @FXML private Label lblCucinaInfo, lblPrezzoInfo, lblDeliveryInfo, lblDescrizioneBrief;
    @FXML private Label lblIndirizzoInfo, lblCittaInfo, lblCucinaInfo2, lblPrezzoInfo2, lblLoginPrompt;
    @FXML private VBox vboxRecensioni;
    @FXML private VBox vboxFormRecensione;
    @FXML private Button btnPreferito;
    @FXML private Button btnPrenota;
    @FXML private Slider sliderStelle;
    @FXML private TextArea txtTestoRecensione;
    @FXML private MenuButton accountMenuButton;

    private boolean isPreferito = false;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @FXML
    public void initialize() {
        if (sliderStelle != null && lblVotoSelezionato != null) {
            lblVotoSelezionato.setText(String.valueOf((int) sliderStelle.getValue()));
            sliderStelle.valueProperty().addListener((obs, oldValue, newValue) -> lblVotoSelezionato.setText(String.valueOf(newValue.intValue())));
        }

        
        Ristorante r = SessioneCorrente.getInstance().getSelectedRistorante();
        if (r != null) {
            applyRestaurantToUI(r);

            loadFullRestaurantDetails(r.getId());
            loadReviews(r.getId());
            if (SessioneCorrente.getInstance().isUserLogged() && !SessioneCorrente.getInstance().isGestore()) {
                checkPreferito(r.getId());
            }
        }
        // Mostra form solo se loggato
        if (vboxFormRecensione != null && lblLoginPrompt != null) {
            boolean showForm = SessioneCorrente.getInstance().isUserLogged() && !SessioneCorrente.getInstance().isGestore();
            vboxFormRecensione.setVisible(showForm);
            vboxFormRecensione.setManaged(showForm);
            lblLoginPrompt.setVisible(!showForm);
            lblLoginPrompt.setManaged(!showForm);
            if (!showForm) {
                lblLoginPrompt.setText("Accedi per lasciare una recensione");
            }
        }
        
        // Mostra pulsante preferiti sempre
        if (btnPreferito != null) {
            btnPreferito.setDisable(false);
            btnPreferito.setText("Add to favorites");
            btnPreferito.setOnAction(e -> handlePreferito());
        }
        updateAccountMenu();
    }

    private void updateAccountMenu() {
        if (accountMenuButton == null) return;

        accountMenuButton.setText("");
        accountMenuButton.getItems().clear();
        accountMenuButton.setGraphic(createAccountGraphic());

        if (SessioneCorrente.getInstance().isUserLogged()) {
            MenuItem myAccount = new MenuItem("My Account");
            myAccount.setOnAction(e -> {
                if (SessioneCorrente.getInstance().isGestore()) {
                    ClientTK.loadScene("dashboard_gestore.fxml", "TheKnife - Dashboard");
                } else {
                    ClientTK.loadScene("mie_recensioni.fxml", "TheKnife - My Reviews");
                }
            });

            MenuItem settings = new MenuItem("Settings");
            settings.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Settings");
                alert.setHeaderText("Settings");
                alert.setContentText("Settings are not available yet.");
                alert.showAndWait();
            });

            MenuItem logout = new MenuItem("Logout");
            logout.setOnAction(e -> {
                SessioneCorrente.getInstance().logout();
                ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome");
            });

            accountMenuButton.getItems().addAll(myAccount, settings, new SeparatorMenuItem(), logout);
        } else {
            MenuItem login = new MenuItem("Login");
            login.setOnAction(e -> ClientTK.loadScene("login.fxml", "TheKnife - Login"));

            MenuItem register = new MenuItem("Create Account");
            register.setOnAction(e -> ClientTK.loadScene("registrazione.fxml", "TheKnife - Registration"));

            MenuItem settings = new MenuItem("Settings");
            settings.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Settings");
                alert.setHeaderText("Settings");
                alert.setContentText("Settings are not available yet.");
                alert.showAndWait();
            });

            accountMenuButton.getItems().addAll(login, register, new SeparatorMenuItem(), settings);
        }
    }

    private HBox createAccountGraphic() {
        SVGPath svgPath = new SVGPath();
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

    private void checkPreferito(int idRistorante) {
        if (!SessioneCorrente.getInstance().isUserLogged() || SessioneCorrente.getInstance().isGestore()) return;
        new Thread(() -> {
            try {
                Request req = new Request("VERIFICA_PREFERITO");
                req.addParametro("idUtente", SessioneCorrente.getInstance().getUtenteLoggato().getId());
                req.addParametro("idRistorante", idRistorante);
                Response res = ServerConnection.getInstance().send(req);
                if (res.isSuccesso()) {
                    isPreferito = (Boolean) res.getPayload();
                    Platform.runLater(this::updatePreferitoBtn);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Errore nel controllo dei preferiti: " + e.getMessage());
            }
        }).start();
    }

    private void updatePreferitoBtn() {
        if (btnPreferito != null) {
            btnPreferito.setText(isPreferito ? "Remove from favorites" : "Add to favorites");
            btnPreferito.setOnAction(e -> togglePreferito());
        }
    }

    private void loadFullRestaurantDetails(int idRistorante) {
        new Thread(() -> {
            try {
                Request req = new Request("DETTAGLIO_RISTORANTE");
                req.addParametro("idRistorante", idRistorante);
                Response res = ServerConnection.getInstance().send(req);
                if (res.isSuccesso()) {
                    Ristorante dettagli = (Ristorante) res.getPayload();
                    if (dettagli != null) {
                        SessioneCorrente.getInstance().setSelectedRistorante(dettagli);
                        Platform.runLater(() -> applyRestaurantToUI(dettagli));
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Errore nel caricamento dettagli ristorante: " + e.getMessage());
            }
        }).start();
    }

    private void applyRestaurantToUI(Ristorante r) {
        if (r == null) return;
        if (lblNome != null) lblNome.setText(safeText(r.getNome()));
        if (lblCitta != null) lblCitta.setText(formatLocation(r));
        if (lblIndirizzo != null) lblIndirizzo.setText(safeText(r.getIndirizzo()));

        // Prepara indirizzi per visualizzazione
        String indirizzoFull = safeText(r.getIndirizzo());
        String citta = safeText(r.getCitta());
        String nazione = safeText(r.getNazione());

        String streetOnly = indirizzoFull;
        if (!indirizzoFull.isEmpty() && !citta.isEmpty()) {
            String lowerAddr = indirizzoFull.toLowerCase();
            String lowerCity = citta.toLowerCase();
            int idx = lowerAddr.indexOf(lowerCity);
            if (idx > 0) {
                streetOnly = indirizzoFull.substring(0, idx).trim();
                // Rimuovi virgole finali
                while (streetOnly.endsWith(",") || streetOnly.endsWith(" ")) {
                    streetOnly = streetOnly.substring(0, streetOnly.length() - 1).trim();
                }
            }
        }

        if (lblIndirizzoInfo != null) lblIndirizzoInfo.setText(streetOnly.isEmpty() ? indirizzoFull : streetOnly);
        if (lblCittaInfo != null) lblCittaInfo.setText(citta.isEmpty() ? formatLocation(r) : (citta + (nazione.isEmpty() ? "" : ", " + nazione)));

        if (lblCucina != null) lblCucina.setText(safeText(r.getTipoCucina()));
        if (lblCucinaInfo != null) lblCucinaInfo.setText(safeText(r.getTipoCucina()));
        if (lblCucinaInfo2 != null) lblCucinaInfo2.setText(safeText(r.getTipoCucina()));

        if (lblPrezzo != null) lblPrezzo.setText(formatPrice(r.getPrezzoMedio()));
        if (lblPrezzoInfo != null) lblPrezzoInfo.setText(formatPrice(r.getPrezzoMedio()));
        if (lblPrezzoInfo2 != null) lblPrezzoInfo2.setText(String.format("%.0f€", r.getPrezzoMedio()));

        if (lblCoordinate != null) lblCoordinate.setText(String.format("%.4f, %.4f", r.getLatitudine(), r.getLongitudine()));

        if (lblDescrizioneBrief != null) lblDescrizioneBrief.setText(safeText(r.getDescrizione()));

        // Mostra badge se disponibili
        if (lblDelivery != null) {
            if (r.isDelivery()) {
                lblDelivery.setVisible(true);
                lblDelivery.setManaged(true);
                lblDelivery.setText("Consegna disponibile");
                lblDelivery.getStyleClass().setAll("tk-badge", "tk-positive");
            } else {
                lblDelivery.setVisible(false);
                lblDelivery.setManaged(false);
            }
        }
        if (lblPrenotazione != null) {
            if (r.isPrenotazione()) {
                lblPrenotazione.setVisible(true);
                lblPrenotazione.setManaged(true);
                lblPrenotazione.setText("Prenotazione disponibile");
                lblPrenotazione.getStyleClass().setAll("tk-badge", "tk-positive");
            } else {
                lblPrenotazione.setVisible(false);
                lblPrenotazione.setManaged(false);
            }
        }

        // Stile pulsante prenotazione
        if (btnPrenota != null) {
            if (r.isPrenotazione()) {
                btnPrenota.setText("Prenota un tavolo");
                btnPrenota.setDisable(false);
                btnPrenota.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white;");
            } else {
                btnPrenota.setText("Prenotazione non disponibile");
                btnPrenota.setDisable(true);
                btnPrenota.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: #555555;");
            }
        }

        // Info consegna
        if (lblDeliveryInfo != null) {
            lblDeliveryInfo.setText(r.isDelivery() ? "Disponibile" : "Non disponibile");
        }

        // Voto e numero recensioni
        if (lblAvgRating != null) {
            double avg = r.getMediaStelle();
            if (avg > 0) lblAvgRating.setText(String.format("%.1f / 5.0", avg));
        }
        if (lblReviewCount != null) {
            int num = r.getNumRecensioni();
            if (num > 0) lblReviewCount.setText(String.valueOf(num));
        }
    }
    
    private void loadReviews(int ristoranteId) {
        new Thread(() -> {
            try {
                Request req = new Request("VISUALIZZA_RECENSIONI");
                req.addParametro("idRistorante", ristoranteId);
                Response res = ServerConnection.getInstance().send(req);
                if (res.isSuccesso()) {
                    @SuppressWarnings("unchecked")
                    List<Recensione> recensioni = (List<Recensione>) res.getPayload();
                    Platform.runLater(() -> {
                        double avg = recensioni.stream().mapToInt(Recensione::getStelle).average().orElse(0.0);
                        lblAvgRating.setText(String.format("%.1f / 5.0", avg));
                        lblReviewCount.setText(String.valueOf(recensioni.size()));
                        if (vboxRecensioni != null) {
                            vboxRecensioni.getChildren().setAll(recensioni.stream().map(this::createReviewCard).toList());
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        lblAvgRating.setText("No reviews");
                        lblReviewCount.setText("0");
                        if (vboxRecensioni != null) {
                            vboxRecensioni.getChildren().setAll(createEmptyReviewsState("No reviews yet", "Be the first to share your experience."));
                        }
                    });
                }
            } catch (IOException | ClassNotFoundException e) {
                Platform.runLater(() -> {
                    lblAvgRating.setText("Error");
                    if (vboxRecensioni != null) {
                        vboxRecensioni.getChildren().setAll(createEmptyReviewsState("Could not load reviews", "Check the connection and try again."));
                    }
                });
            }
        }).start();
    }
    
    @FXML private void handleAnnullaRecensione() {
        if (vboxFormRecensione != null) {
            vboxFormRecensione.setVisible(false);
            vboxFormRecensione.setManaged(false);
        }
        if (lblMessaggio != null) {
            lblMessaggio.setText("Review cancelled.");
        }
    }

    @FXML private void handleInviaRecensione() {
        try {
            if (!SessioneCorrente.getInstance().isUserLogged()) {
                if (lblMessaggio != null) lblMessaggio.setText("Please login to submit a review.");
                return;
            }
            Ristorante r = SessioneCorrente.getInstance().getSelectedRistorante();
            int stelle = sliderStelle != null ? (int) sliderStelle.getValue() : 5;
            String testo = txtTestoRecensione != null ? txtTestoRecensione.getText() : "";
            
            Request req = new Request("AGGIUNGI_RECENSIONE");
            req.addParametro("idUtente", SessioneCorrente.getInstance().getUtenteLoggato().getId());
            req.addParametro("idRistorante", r.getId());
            req.addParametro("stelle", stelle);
            req.addParametro("testo", testo);
            
            Response res = ServerConnection.getInstance().send(req);
            if (res.isSuccesso()) {
                if (vboxFormRecensione != null) {
                    vboxFormRecensione.setVisible(false);
                    vboxFormRecensione.setManaged(false);
                }
                if (txtTestoRecensione != null) txtTestoRecensione.clear();
                loadReviews(r.getId());
                if (lblMessaggio != null) lblMessaggio.setText("Review submitted.");
            }



        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Errore nell'invio della recensione: " + e.getMessage());
        }
    }

    @FXML private void handlePreferito() {
        if (!SessioneCorrente.getInstance().isUserLogged() || SessioneCorrente.getInstance().isGestore()) {
            if (lblMessaggio != null) {
                lblMessaggio.setText("Login as customer to use favorites.");
            }
            return;
        }
        togglePreferito();
    }

    @FXML
    private void handleHome() {
        ClientTK.loadScene("home.fxml", "TheKnife - Home");
    }
    
    @FXML
    private void handlePreferiti() {
        ClientTK.loadScene("preferiti.fxml", "TheKnife - Favorites");
    }

    @FXML
    private void handleMieRecensioni() {
        ClientTK.loadScene("mie_recensioni.fxml", "TheKnife - My Reviews");
    }

    @FXML private void handlePrenota() {
        Ristorante r = SessioneCorrente.getInstance().getSelectedRistorante();
        if (r == null) {
            return;
        }
        if (!r.isPrenotazione()) {
            if (lblMessaggio != null) {
                lblMessaggio.setText("Booking is not available for this restaurant.");
            }
            return;
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Booking");
        alert.setHeaderText("Booking available");
        alert.setContentText("This restaurant accepts bookings. Booking flow will be added here.");
        alert.showAndWait();
    }

    private void togglePreferito() {
        try {
            Ristorante r = SessioneCorrente.getInstance().getSelectedRistorante();
            String command = isPreferito ? "RIMUOVI_PREFERITO" : "AGGIUNGI_PREFERITO";
            Request req = new Request(command);
            req.addParametro("idUtente", SessioneCorrente.getInstance().getUtenteLoggato().getId());
            req.addParametro("idRistorante", r.getId());
            
            Response res = ServerConnection.getInstance().send(req);
            if (res.isSuccesso()) {
                isPreferito = !isPreferito;
                updatePreferitoBtn();
                if (lblMessaggio != null) {
                    lblMessaggio.setText(isPreferito ? "Added to favorites." : "Removed from favorites.");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Errore nella modifica dei preferiti: " + e.getMessage());
        }
    }
    
    @FXML private void handleIndietro() {
        ClientTK.loadScene("home.fxml", "TheKnife - Home");
    }

    private VBox createReviewCard(Recensione recensione) {
        VBox card = new VBox(8);
        card.getStyleClass().add("tk-review-card");

        HBox header = new HBox(10);
        Label avatar = new Label(initials(recensione.getNomeUtente()));
        avatar.getStyleClass().add("tk-badge");
        avatar.setMinWidth(34);
        avatar.setAlignment(javafx.geometry.Pos.CENTER);

        VBox meta = new VBox(2);
        Label user = new Label(recensione.getNomeUtente());
        user.getStyleClass().add("tk-card-title");
        Label date = new Label(recensione.getDataInserimento() != null ? recensione.getDataInserimento().format(formatter) : "");
        date.getStyleClass().add("tk-text-secondary");
        meta.getChildren().addAll(user, date);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Label rating = new Label(stars(recensione.getStelle()));
        rating.getStyleClass().add("tk-stars");
        header.getChildren().addAll(avatar, meta, spacer, rating);

        Label text = new Label(recensione.getTesto());
        text.setWrapText(true);
        text.getStyleClass().add("tk-text-secondary");

        card.getChildren().addAll(header, text);

        if (recensione.getRisposta() != null && recensione.getRisposta().getTesto() != null && !recensione.getRisposta().getTesto().isBlank()) {
            VBox reply = new VBox(4);
            reply.getStyleClass().add("tk-review-reply");
            Label replyTitle = new Label("Manager reply");
            replyTitle.getStyleClass().add("tk-card-title");
            Label replyText = new Label(recensione.getRisposta().getTesto());
            replyText.setWrapText(true);
            replyText.getStyleClass().add("tk-text-secondary");
            reply.getChildren().addAll(replyTitle, replyText);
            card.getChildren().add(reply);
        }

        return card;
    }

    private VBox createEmptyReviewsState(String title, String subtitle) {
        VBox empty = new VBox(4);
        empty.getStyleClass().add("tk-empty-state");
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("tk-section-title");
        Label lblSubtitle = new Label(subtitle);
        lblSubtitle.getStyleClass().add("tk-text-secondary");
        lblSubtitle.setWrapText(true);
        empty.getChildren().addAll(lblTitle, lblSubtitle);
        return empty;
    }

    private String formatLocation(Ristorante r) {
        String city = safeText(r.getCitta());
        String country = safeText(r.getNazione());
        if (city.isEmpty()) {
            return country;
        }
        return country.isEmpty() ? city : city + ", " + country;
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

    private String stars(int value) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            builder.append(i < value ? '★' : '☆');
        }
        return builder.toString();
    }

    private String initials(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "?";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }
}

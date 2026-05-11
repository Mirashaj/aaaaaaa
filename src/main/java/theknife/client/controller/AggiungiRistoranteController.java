package theknife.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import theknife.client.ClientTK;
import theknife.client.ServerConnection;
import theknife.client.SessioneCorrente;
import theknife.shared.Request;
import theknife.shared.Response;

public class AggiungiRistoranteController {

    @FXML private TextField txtNome, txtIndirizzo, txtCitta, txtNazione, txtLatitudine, txtLongitudine, txtPrezzo, txtCucina;
    @FXML private CheckBox chkDelivery, chkPrenotazione;
    @FXML private Label lblErrore;
    @FXML private Button btnNavHome;
    @FXML private Button btnNavRestaurants;
    @FXML private Button btnNavReviews;

    @FXML
    public void initialize() {
        if (!SessioneCorrente.getInstance().isUserLogged() || !SessioneCorrente.getInstance().isGestore()) {
            ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome");
            return;
        }

        // Se proviene dal pulsante di modifica nella dashboard del gestore, pre-compila il modulo
        if (isEditMode()) {
            fillFormFromSelected();
        }


        if (btnNavHome != null) {
            btnNavHome.setOnAction(e -> ClientTK.loadScene("home.fxml", "TheKnife - Home"));
        }
        if (btnNavRestaurants != null) {
            btnNavRestaurants.setText("Restaurants");
            btnNavRestaurants.getStyleClass().setAll("tk-nav-active");
        }
        if (btnNavReviews != null) {
            btnNavReviews.setText("Review");
            btnNavReviews.getStyleClass().setAll("tk-nav-item");
            btnNavReviews.setOnAction(e -> ClientTK.loadScene("gestione_recensioni.fxml", "TheKnife - Review Management"));
        }
    }

    private boolean isEditMode() {
        return SessioneCorrente.getInstance() != null
                && SessioneCorrente.getInstance().getSelectedRistorante() != null
                && SessioneCorrente.getInstance().getSelectedRistorante().getId() > 0;
    }

    private void fillFormFromSelected() {
        var r = SessioneCorrente.getInstance().getSelectedRistorante();
        if (r == null) return;
        if (txtNome != null) txtNome.setText(r.getNome());
        if (txtIndirizzo != null) txtIndirizzo.setText(r.getIndirizzo());
        if (txtCitta != null) txtCitta.setText(r.getCitta());
        if (txtNazione != null) txtNazione.setText(r.getNazione());
        if (txtLatitudine != null) txtLatitudine.setText(String.valueOf(r.getLatitudine()));
        if (txtLongitudine != null) txtLongitudine.setText(String.valueOf(r.getLongitudine()));
        if (txtPrezzo != null) txtPrezzo.setText(String.valueOf(r.getPrezzoMedio()));
        if (txtCucina != null) txtCucina.setText(r.getTipoCucina());
        if (chkDelivery != null) chkDelivery.setSelected(r.isDelivery());
        if (chkPrenotazione != null) chkPrenotazione.setSelected(r.isPrenotazione());
    }

    @FXML
    private void handleSalva() {
        try {
            String nome = txtNome.getText().trim();
            String indirizzo = txtIndirizzo.getText().trim();
            String citta = txtCitta.getText().trim();
            String nazione = txtNazione.getText().trim();
            String cucina = txtCucina != null ? txtCucina.getText().trim() : "";

            if (nome.isEmpty() || indirizzo.isEmpty() || citta.isEmpty() || nazione.isEmpty()) {
                if (lblErrore != null) lblErrore.setText("Fill in all required fields.");
                return;
            }

            double lat = Double.parseDouble(txtLatitudine.getText().trim());
            double lon = Double.parseDouble(txtLongitudine.getText().trim());
            double prezzo = Double.parseDouble(txtPrezzo.getText().trim());

            String requestType = isEditMode() ? "MODIFICA_RISTORANTE" : "AGGIUNGI_RISTORANTE";
            Request req = new Request(requestType);

            req.addParametro("nome", nome);
            req.addParametro("indirizzo", indirizzo);
            req.addParametro("citta", citta);
            req.addParametro("nazione", nazione);
            req.addParametro("latitudine", lat);
            req.addParametro("longitudine", lon);
            req.addParametro("prezzoMedio", prezzo);
            req.addParametro("tipoCucina", cucina);
            if (chkDelivery != null) req.addParametro("delivery", chkDelivery.isSelected());
            if (chkPrenotazione != null) req.addParametro("prenotazione", chkPrenotazione.isSelected());
            req.addParametro("idGestore", SessioneCorrente.getInstance().getUtenteLoggato().getId());
            if (isEditMode()) {
                req.addParametro("idRistorante", SessioneCorrente.getInstance().getSelectedRistorante().getId());
            }

            Response res = ServerConnection.getInstance().send(req);

            if (res.isSuccesso()) {
                // Cancella la selezione per evitare di re-editare accidentalmente
                if (SessioneCorrente.getInstance().getSelectedRistorante() != null) {
                    SessioneCorrente.getInstance().setSelectedRistorante(null);
                }
                ClientTK.loadScene("dashboard_gestore.fxml", "TheKnife - Dashboard");

            } else if (lblErrore != null) {
                lblErrore.setText("Error: " + res.getMessaggio());
            }
        } catch (NumberFormatException e) {
            if (lblErrore != null) lblErrore.setText("Invalid coordinates or price.");
        } catch (Exception e) {
            if (lblErrore != null) lblErrore.setText("Connection error.");
        }
    }

    @FXML
    private void handleAnnulla() {
        ClientTK.loadScene("dashboard_gestore.fxml", "TheKnife - Dashboard");
    }
}

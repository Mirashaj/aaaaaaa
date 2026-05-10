package theknife.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import theknife.client.ClientTK;
import theknife.client.ServerConnection;
import theknife.client.SessioneCorrente;
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

public class AggiungiRistoranteController {

    @FXML private TextField txtNome, txtIndirizzo, txtCitta, txtNazione, txtLatitudine, txtLongitudine, txtPrezzo, txtCucina;
    @FXML private CheckBox chkDelivery, chkPrenotazione;
    @FXML private Label lblErrore;

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

            Request req = new Request("AGGIUNGI_RISTORANTE");
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

            Response res = ServerConnection.getInstance().send(req);
            if (res.isSuccesso()) ClientTK.loadScene("dashboard_gestore.fxml", "TheKnife - Dashboard");
                        else if (lblErrore != null) lblErrore.setText("Error: " + res.getMessaggio());

                } catch (NumberFormatException e) { if (lblErrore != null) lblErrore.setText("Invalid coordinates or price."); } 
                    catch (Exception e) { if (lblErrore != null) lblErrore.setText("Connection error."); }
    }

    @FXML private void handleAnnulla() { ClientTK.loadScene("dashboard_gestore.fxml", "TheKnife - Dashboard"); }
}

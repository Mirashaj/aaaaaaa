package theknife.client.controller;

import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
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

public class GestioneRecensioniController {

    @FXML private Label lblNomeRistorante;
    @FXML private Label lblRiepilogo;
    @FXML private VBox vboxRecensioni;

    @FXML
    public void initialize() {
        Ristorante r = SessioneCorrente.getInstance().getSelectedRistorante();
        if (r != null && lblNomeRistorante != null) {
            lblNomeRistorante.setText(r.getNome());
            caricaRecensioni(r.getId());
        }
    }

    private void caricaRecensioni(int idRistorante) {
        new Thread(() -> {
            try {
                Request req = new Request("VISUALIZZA_RECENSIONI");
                req.addParametro("idRistorante", idRistorante);
                Response res = ServerConnection.getInstance().send(req);
                if (res.isSuccesso() && vboxRecensioni != null) {
                    List<Recensione> recensioni = (List<Recensione>) res.getPayload();
                    Platform.runLater(() -> popolaUI(recensioni));
                }
            } catch (Exception e) { System.err.println("Errore nel caricamento delle recensioni: " + e.getMessage()); }
        }).start();
    }

    private void popolaUI(List<Recensione> recensioni) {
        vboxRecensioni.getChildren().clear();
        double somma = 0;
        for (Recensione r : recensioni) {
            somma += r.getStelle();
            VBox card = new VBox(5);
            card.getStyleClass().add("tk-card");
            Label lblUtente = new Label(r.getNomeUtente() + " - Rating: " + r.getStelle());
            Label lblTesto = new Label(r.getTesto());
            card.getChildren().addAll(lblUtente, lblTesto);

            if (r.getRisposta() != null) {
                Label lblRisposta = new Label("Your reply: " + r.getRisposta().getTesto());
                card.getChildren().add(lblRisposta);
            } else {
                TextArea txtRisposta = new TextArea();
                txtRisposta.setPromptText("Write a reply...");
                Button btnRispondi = new Button("Reply");
                btnRispondi.setOnAction(e -> inviaRisposta(r.getId(), txtRisposta.getText()));
                card.getChildren().addAll(txtRisposta, btnRispondi);
            }
            vboxRecensioni.getChildren().add(card);
        }
        if (lblRiepilogo != null) lblRiepilogo.setText(!recensioni.isEmpty() ? String.format("Average rating: %.1f - %d reviews", somma / recensioni.size(), recensioni.size()) : "No reviews yet.");
    }

    private void inviaRisposta(int idRecensione, String testo) {
        if (testo == null || testo.trim().isEmpty()) return;
        try {
            Request req = new Request("RISPONDI_RECENSIONE");
            req.addParametro("idRecensione", idRecensione); req.addParametro("idGestore", SessioneCorrente.getInstance().getUtenteLoggato().getId()); req.addParametro("testo", testo);
            if (ServerConnection.getInstance().send(req).isSuccesso()) caricaRecensioni(SessioneCorrente.getInstance().getSelectedRistorante().getId());
        } catch (Exception e) { System.err.println("Errore nell'invio della risposta: " + e.getMessage()); }
    }
    @FXML private void handleIndietro() { ClientTK.loadScene("dashboard_gestore.fxml", "TheKnife - Dashboard"); }
}

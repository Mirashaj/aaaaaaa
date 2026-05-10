package theknife.client.controller;

import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import theknife.client.ClientTK;
import theknife.client.ServerConnection;
import theknife.client.SessioneCorrente;
import theknife.model.Recensione;
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

public class MieRecensioniController {
    @FXML private VBox vboxRecensioni;
    @FXML private Label lblEmpty;

    @FXML
    public void initialize() {
        if (!SessioneCorrente.getInstance().isUserLogged() || SessioneCorrente.getInstance().isGestore()) {
            ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome");
            return;
        }
        caricaRecensioni();
    }

    private void caricaRecensioni() {
        new Thread(() -> {
            try {
                Request req = new Request("MIE_RECENSIONI");
                req.addParametro("idUtente", SessioneCorrente.getInstance().getUtenteLoggato().getId());
                Response res = ServerConnection.getInstance().send(req);

                if (res.isSuccesso()) {
                    List<Recensione> recensioni = (List<Recensione>) res.getPayload();
                    Platform.runLater(() -> popolaUI(recensioni));
                }
            } catch (Exception e) {
                System.err.println("Errore nel caricamento delle recensioni: " + e.getMessage());
            }
        }).start();
    }

    private void popolaUI(List<Recensione> recensioni) {
        if (vboxRecensioni == null) return;
        vboxRecensioni.getChildren().clear();
        if (recensioni == null || recensioni.isEmpty()) {
            if (lblEmpty != null) { lblEmpty.setVisible(true); lblEmpty.setManaged(true); }
        } else {
            if (lblEmpty != null) { lblEmpty.setVisible(false); lblEmpty.setManaged(false); }
            for (Recensione r : recensioni) {
                VBox card = new VBox(5);
                card.getStyleClass().add("tk-card");
                Label lblRest = new Label(r.getNomeRistorante());
                Label lblStelle = new Label("Rating: " + r.getStelle());
                Label lblTesto = new Label(r.getTesto());
                Button btnElimina = new Button("Delete");
                btnElimina.setOnAction(e -> eliminaRecensione(r.getId()));
                card.getChildren().addAll(lblRest, lblStelle, lblTesto, btnElimina);
                
                if (r.getRisposta() != null) {
                    Label lblRisposta = new Label("Manager reply: " + r.getRisposta().getTesto());
                    card.getChildren().add(lblRisposta);
                }
                vboxRecensioni.getChildren().add(card);
            }
        }
    }

    private void eliminaRecensione(int idRecensione) {
        try {
            Request req = new Request("ELIMINA_RECENSIONE");
            req.addParametro("idRecensione", idRecensione);
            if (ServerConnection.getInstance().send(req).isSuccesso()) caricaRecensioni();
        } catch (Exception e) { System.err.println("Errore nell'eliminazione della recensione: " + e.getMessage()); }
    }

    @FXML
    private void handleIndietro() {
        ClientTK.loadScene("home.fxml", "TheKnife - Home");
    }
}

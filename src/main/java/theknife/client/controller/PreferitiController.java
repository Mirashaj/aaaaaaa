package theknife.client.controller;

import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
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

public class PreferitiController {
    @FXML private FlowPane fpPreferiti;
    @FXML private Label lblEmpty;

    @FXML
    public void initialize() {
        if (!SessioneCorrente.getInstance().isUserLogged() || SessioneCorrente.getInstance().isGestore()) {
            ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome");
            return;
        }
        caricaPreferiti();
    }

    private void caricaPreferiti() {
        new Thread(() -> {
            try {
                Request req = new Request("VISUALIZZA_PREFERITI");
                req.addParametro("idUtente", SessioneCorrente.getInstance().getUtenteLoggato().getId());
                Response res = ServerConnection.getInstance().send(req);

                if (res.isSuccesso()) {
                    List<Ristorante> preferiti = (List<Ristorante>) res.getPayload();
                    Platform.runLater(() -> popolaUI(preferiti));
                }
            } catch (Exception e) {
                System.err.println("Errore nel caricamento dei preferiti: " + e.getMessage());
            }
        }).start();
    }

    private void popolaUI(List<Ristorante> preferiti) {
        if (fpPreferiti == null) return;
        fpPreferiti.getChildren().clear();
        if (preferiti == null || preferiti.isEmpty()) {
            if (lblEmpty != null) { lblEmpty.setVisible(true); lblEmpty.setManaged(true); }
        } else {
            if (lblEmpty != null) { lblEmpty.setVisible(false); lblEmpty.setManaged(false); }
            for (Ristorante r : preferiti) {
                VBox card = new VBox(5);
                card.getStyleClass().add("tk-card");
                Label lblNome = new Label(r.getNome());
                Button btnRimuovi = new Button("Remove from favorites");
                btnRimuovi.setOnAction(e -> rimuoviPreferito(r.getId()));
                card.setOnMouseClicked(e -> {
                    SessioneCorrente.getInstance().setSelectedRistorante(r);
                    ClientTK.loadScene("dettaglio_ristorante.fxml", "TheKnife - " + r.getNome());
                });
                card.getChildren().addAll(lblNome, btnRimuovi);
                fpPreferiti.getChildren().add(card);
            }
        }
    }

    private void rimuoviPreferito(int idRistorante) {
        try {
            Request req = new Request("RIMUOVI_PREFERITO");
            req.addParametro("idUtente", SessioneCorrente.getInstance().getUtenteLoggato().getId());
            req.addParametro("idRistorante", idRistorante);
            Response res = ServerConnection.getInstance().send(req);
            if (res.isSuccesso()) caricaPreferiti();
        } catch (Exception e) {
            System.err.println("Errore nella rimozione dai preferiti: " + e.getMessage());
        }
    }

    @FXML
    private void handleIndietro() {
        ClientTK.loadScene("home.fxml", "TheKnife - Home");
    }
}

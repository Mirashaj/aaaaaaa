package theknife.client.controller;

import java.util.List;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import theknife.client.ClientTK;
import theknife.client.ServerConnection;
import theknife.client.SessioneCorrente;
import theknife.model.RiepilogoRistorante;
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

public class DashboardGestoreController {
    @FXML private Label lblBenvenuto;
    @FXML private TableView<RiepilogoRistorante> tableRistoranti;
    @FXML private TableColumn<RiepilogoRistorante, String> colNome;
    @FXML private TableColumn<RiepilogoRistorante, Double> colMediaStelle;
    @FXML private TableColumn<RiepilogoRistorante, Integer> colNumRecensioni;

    @FXML
    public void initialize() {
        if (!SessioneCorrente.getInstance().isUserLogged() || !SessioneCorrente.getInstance().isGestore()) {
            ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome");
            return;
        }

        if (lblBenvenuto != null) lblBenvenuto.setText("Welcome, " + SessioneCorrente.getInstance().getUtenteLoggato().getNome());

        if (colNome != null) colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        if (colMediaStelle != null) colMediaStelle.setCellValueFactory(new PropertyValueFactory<>("mediaStelle"));
        if (colNumRecensioni != null) colNumRecensioni.setCellValueFactory(new PropertyValueFactory<>("numRecensioni"));

        if (tableRistoranti != null) {
            tableRistoranti.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    RiepilogoRistorante riepilogo = tableRistoranti.getSelectionModel().getSelectedItem();
                    if (riepilogo != null) {
                        Ristorante r = new Ristorante();
                        r.setId(riepilogo.getId());
                        r.setNome(riepilogo.getNome());
                        SessioneCorrente.getInstance().setSelectedRistorante(r);
                        ClientTK.loadScene("gestione_recensioni.fxml", "Review Management - " + r.getNome());
                    }
                }
            });
        }
        caricaRistoranti();
    }

    private void caricaRistoranti() {
        new Thread(() -> {
            try {
                Request req = new Request("RIEPILOGO_GESTORE");
                req.addParametro("idGestore", SessioneCorrente.getInstance().getUtenteLoggato().getId());
                Response res = ServerConnection.getInstance().send(req);
                if (res.isSuccesso() && tableRistoranti != null) {
                    List<RiepilogoRistorante> ristoranti = (List<RiepilogoRistorante>) res.getPayload();
                    Platform.runLater(() -> tableRistoranti.setItems(FXCollections.observableArrayList(ristoranti)));
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    @FXML
    private void handleAggiungi() { ClientTK.loadScene("aggiungi_ristorante.fxml", "TheKnife - Add Restaurant"); }

    @FXML
    private void handleLogout() {
        try { ServerConnection.getInstance().send(new Request("DISCONNECT")); ServerConnection.getInstance().disconnect(); } catch (Exception e) {}
        SessioneCorrente.getInstance().logout();
        ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome");
    }
}

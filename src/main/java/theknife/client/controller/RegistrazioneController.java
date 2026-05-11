package theknife.client.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import theknife.client.ClientTK;
import theknife.client.ServerConnection;
import theknife.client.SessioneCorrente;
import theknife.model.Utente;
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

public class RegistrazioneController {

    @FXML
    private TextField tfNome;

    @FXML
    private TextField tfCognome;

    @FXML
    private TextField tfEmail;

    @FXML
    private PasswordField pfPassword;

    @FXML
    private RadioButton rbCliente;

    @FXML
    private RadioButton rbGestore;

    @FXML
    private TextField tfDomicilio;

    @FXML
    private Label lblErrore;

    @FXML
    public void initialize() {
        String fieldStyle = "-fx-font-size: 13px; -fx-pref-height: 40px;";
        tfNome.setStyle(fieldStyle);
        tfCognome.setStyle(fieldStyle);
        tfEmail.setStyle(fieldStyle);
        pfPassword.setStyle(fieldStyle);
        tfDomicilio.setStyle(fieldStyle);
        tfNome.setAlignment(Pos.CENTER_LEFT);
        tfCognome.setAlignment(Pos.CENTER_LEFT);
        tfEmail.setAlignment(Pos.CENTER_LEFT);
        pfPassword.setAlignment(Pos.CENTER_LEFT);
        tfDomicilio.setAlignment(Pos.CENTER_LEFT);
    }

    @FXML
    private void handleRegistra() {
        String nome = tfNome.getText().trim();
        String cognome = tfCognome.getText().trim();
        String email = tfEmail.getText().trim();
        String password = pfPassword.getText();
        String ruolo = (rbGestore != null && rbGestore.isSelected()) ? "gestore" : "cliente";
        String domicilio = tfDomicilio.getText().trim();

        if (nome.isEmpty() || cognome.isEmpty() || email.isEmpty() || password.isEmpty() || domicilio.isEmpty()) {
            lblErrore.setText("Fill in all required fields, including location.");
            return;
        }

        try {
            Request request = new Request("REGISTRAZIONE");
            request.addParametro("nome", nome);
            request.addParametro("cognome", cognome);
            request.addParametro("email", email);
            request.addParametro("password", password);
            request.addParametro("ruolo", ruolo);
            request.addParametro("domicilio", domicilio);

            Response response = ServerConnection.getInstance().send(request);

            if (response.isSuccesso()) {
                Utente utente = (Utente) response.getPayload();
                System.out.println("Registration complete: " + utente);
                SessioneCorrente.getInstance().login(utente);
                ClientTK.loadScene("home.fxml", "TheKnife - Home");
            } else {
                lblErrore.setText(response.getMessaggio());
            }
        } catch (Exception e) {
            lblErrore.setText("Errore: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleIndietro() {
        ClientTK.loadScene("welcome.fxml", "TheKnife - Welcome");
    }

    @FXML
    private void handleAccedi() {
        ClientTK.loadScene("login.fxml", "TheKnife - Login");
    }
}

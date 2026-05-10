package theknife.client;

import theknife.model.Ristorante;
import theknife.model.Utente;

/*
 * 
 * Mirashaj Erik 760453 VA
 * GorchynskYi Igor 757184 VA
 * Kabuka Dan Mumanga 757708 VA
 * Mujeci Lorenzo 757597 VA
 *
 */

public class SessioneCorrente {
    
    private static SessioneCorrente instance;
    private Utente utenteLoggato;
    private String cittaGuest;
    private Ristorante selectedRistorante;
    private boolean openReviewFormOnLoad;
    
    private SessioneCorrente() {
    }
    
    public static synchronized SessioneCorrente getInstance() {
        if (instance == null) {
            instance = new SessioneCorrente();
        }
        return instance;
    }
    
    public void login(Utente u) {
        this.utenteLoggato = u;
    }
    
    public void logout() {
        this.utenteLoggato = null;
        this.cittaGuest = null;
        this.selectedRistorante = null;
    }
    
    public String getCittaGuest() {
        return cittaGuest;
    }
    
    public void setCittaGuest(String cittaGuest) {
        this.cittaGuest = cittaGuest;
    }
    
    public boolean isUserLogged() {
        return utenteLoggato != null;
    }
    
    public boolean isGestore() {
        return isUserLogged() && "gestore".equals(utenteLoggato.getRuolo());
    }
    
    public Utente getUtenteLoggato() {
        return utenteLoggato;
    }
    
    public void setSelectedRistorante(Ristorante r) {
        this.selectedRistorante = r;
    }
    
    public Ristorante getSelectedRistorante() {
        return selectedRistorante;
    }

    public void requestOpenReviewForm() {
        this.openReviewFormOnLoad = true;
    }

    public boolean consumeOpenReviewFormRequest() {
        boolean shouldOpen = openReviewFormOnLoad;
        openReviewFormOnLoad = false;
        return shouldOpen;
    }
}

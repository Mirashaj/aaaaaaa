package theknife.server.dao;

import theknife.model.Utente;

/*
 * 
 * Mirashaj Erik 760453 VA
 * GorchynskYi Igor 757184 VA
 * Kabuka Dan Mumanga 757708 VA
 * Mujeci Lorenzo 757597 VA
 *
 */

public interface UtenteDAO {
    Utente findByEmail(String email);
    Utente inserisci(Utente u);
    boolean esisteEmail(String email);
}

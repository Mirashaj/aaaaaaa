package theknife.server.dao;

import java.util.List;

import theknife.model.Prenotazione;

/*
 * PrenotazioneDAO
 */
public interface PrenotazioneDAO {
    List<Prenotazione> findByUtente(int idUtente);
    List<Prenotazione> findByGestore(int idGestore);
    Prenotazione inserisci(Prenotazione p);
    boolean elimina(int idPrenotazione);
    Prenotazione aggiorna(Prenotazione p);
}


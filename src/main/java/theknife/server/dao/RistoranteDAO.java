package theknife.server.dao;

import java.util.List;
import java.util.Map;

import theknife.model.Ristorante;

/*
 * 
 * Mirashaj Erik 760453 VA
 * GorchynskYi Igor 757184 VA
 * Kabuka Dan Mumanga 757708 VA
 * Mujeci Lorenzo 757597 VA
 *
 */

public interface RistoranteDAO {
    List<Ristorante> cerca(Map<String, Object> filtri);
    Ristorante findById(int id);
    List<Ristorante> findByGestore(int idGestore);
    List<theknife.model.RiepilogoRistorante> riepilogoByGestore(int idGestore);
    List<Ristorante> findVicini(double lat, double lon, double raggioKm);
    Ristorante inserisci(Ristorante r);
    void aggiungiPreferito(int idUtente, int idRistorante);
    void rimuoviPreferito(int idUtente, int idRistorante);
    List<Ristorante> findPreferiti(int idUtente);
    boolean isPreferito(int idUtente, int idRistorante);
}

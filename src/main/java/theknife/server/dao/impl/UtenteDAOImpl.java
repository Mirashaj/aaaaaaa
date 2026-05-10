package theknife.server.dao.impl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import theknife.model.Utente;
import theknife.server.DatabaseConnection;
import theknife.server.dao.UtenteDAO;

/*
 *
 * Mirashaj Erik 760453 VA
 * GorchynskYi Igor 757184 VA
 * Kabuka Dan Mumanga 757708 VA
 * Mujeci Lorenzo 757597 VA
 * 
 */

public class UtenteDAOImpl implements UtenteDAO {

    @Override
    public Utente findByEmail(String email) {
        String query = "SELECT * FROM Utenti WHERE email = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapUtente(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Utente inserisci(Utente u) {
        String query = "INSERT INTO Utenti " +
            "(nome, cognome, email, password_hash, data_nascita, domicilio, ruolo) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, u.getNome());
            stmt.setString(2, u.getCognome());
            stmt.setString(3, u.getEmail());
            stmt.setString(4, u.getPasswordHash());

            if (u.getDataNascita() != null) {
                stmt.setDate(5, Date.valueOf(u.getDataNascita()));
            } else {
                stmt.setNull(5, Types.DATE);
            }

            stmt.setString(6, u.getDomicilio());
            stmt.setString(7, u.getRuolo());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                u.setId(rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return u;
    }

    @Override
    public boolean esisteEmail(String email) {
        String query = "SELECT 1 FROM Utenti WHERE email = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Helper per mappare un ResultSet a un oggetto Utente.
     */
    private Utente mapUtente(ResultSet rs) throws SQLException {
        Utente u = new Utente();
        u.setId(rs.getInt("id"));
        u.setNome(rs.getString("nome"));
        u.setCognome(rs.getString("cognome"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));

        Date dataNascitaDb = rs.getDate("data_nascita");
        if (dataNascitaDb != null) {
            u.setDataNascita(dataNascitaDb.toLocalDate());
        }

        u.setDomicilio(rs.getString("domicilio"));
        u.setRuolo(rs.getString("ruolo"));

        return u;
    }
}

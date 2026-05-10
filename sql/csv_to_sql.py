import csv
import sys
import os

def extract_price(price_str):
    if not price_str:
        return "NULL"
    
    # Contiamo il numero di simboli di valuta
    symbols = ['€', '$', '£', '¥', '₩', '฿']
    count = 0
    for s in symbols:
        if s in price_str:
            count = price_str.count(s)
            break
            
    if count == 1:
        return "15.00"
    elif count == 2:
        return "35.00"
    elif count == 3:
        return "70.00"
    elif count >= 4:
        return "120.00"
    else:
        return "NULL"

def escape_sql(value):
    if not value:
        return "NULL"
    # Sostituisce il singolo apice con due singoli apici per l'escape in SQL
    return "'" + str(value).replace("'", "''") + "'"

def main():
    if len(sys.argv) < 2:
        print("Uso: python csv_to_sql.py <path_al_file.csv>")
        sys.exit(1)
        
    csv_file = sys.argv[1]
    
    # Crea il file nella stessa cartella dello script (sql/)
    script_dir = os.path.dirname(os.path.abspath(__file__))
    sql_file = os.path.join(script_dir, 'insert_data.sql')
    
    if not os.path.exists(csv_file):
        print(f"Errore: File non trovato al percorso: {csv_file}")
        sys.exit(1)
        
    imported = 0
    skipped = 0
    
    with open(csv_file, 'r', encoding='utf-8') as f_in, \
         open(sql_file, 'w', encoding='utf-8') as f_out:
        
        reader = csv.DictReader(f_in)
        
        f_out.write("-- File generato automaticamente da csv_to_sql.py\n")
        f_out.write("-- Contiene l'importazione dei dati ristoranti (michelin_my_maps.csv)\n\n")
        
        f_out.write("-- 1. Utenti mock per test (Gestori e Cliente)\n")
        f_out.write("INSERT INTO Utenti (nome, cognome, email, password_hash, ruolo) VALUES \n")
        f_out.write("('Mario', 'Rossi', 'mario.gestore@email.com', '$2a$10$wN09j4XfP.y48zR7ZcQJw.5d.e6K6XQ7.58wN09j4XfP.y48zR7ZcQJw', 'gestore'),\n")
        f_out.write("('Luigi', 'Verdi', 'luigi.gestore@email.com', '$2a$10$wN09j4XfP.y48zR7ZcQJw.5d.e6K6XQ7.58wN09j4XfP.y48zR7ZcQJw', 'gestore'),\n")
        f_out.write("('Giulia', 'Bianchi', 'giulia.cliente@email.com', '$2a$10$wN09j4XfP.y48zR7ZcQJw.5d.e6K6XQ7.58wN09j4XfP.y48zR7ZcQJw', 'cliente');\n\n")
        
        f_out.write("-- 2. Importazione RistorantiTheKnife\n")
        
        for row in reader:
            name = row.get('Name', '').strip()
            location = row.get('Location', '').strip()
            lat_str = row.get('Latitude', '').strip()
            lon_str = row.get('Longitude', '').strip()
            address = row.get('Address', '').strip()
            cuisine = row.get('Cuisine', '').strip()
            price = row.get('Price', '').strip()
            description = row.get('Description', '').strip()
            
            # 1) Salta le righe con campi obbligatori mancanti
            if not name or not location or not lat_str or not lon_str:
                skipped += 1
                continue
                
            # 2) Split della location in città e nazione
            parts = [p.strip() for p in location.split(',')]
            citta = parts[0]
            nazione = parts[1] if len(parts) > 1 else parts[0] # Fallback se manca la virgola
            
            # 3) Validazione coordinate
            try:
                lat = float(lat_str)
                lon = float(lon_str)
            except ValueError:
                skipped += 1
                continue
                
            # 4) Calcolo prezzo medio (da stringa simbolica)
            prezzo_medio = extract_price(price)
            
            # 5) SQL escaping
            sql_name = escape_sql(name)
            sql_citta = escape_sql(citta)
            sql_nazione = escape_sql(nazione)
            sql_address = escape_sql(address)
            sql_cuisine = escape_sql(cuisine)
            sql_description = escape_sql(description)
            
            query = (
                "INSERT INTO RistorantiTheKnife "
                "(nome, nazione, citta, indirizzo, latitudine, longitudine, prezzo_medio, delivery, prenotazione, tipo_cucina, descrizione, id_gestore) "
                f"VALUES ({sql_name}, {sql_nazione}, {sql_citta}, {sql_address}, {lat}, {lon}, {prezzo_medio}, FALSE, FALSE, {sql_cuisine}, {sql_description}, NULL);"
            )
            f_out.write(query + "\n")
            imported += 1
            
    print("=== RISULTATO IMPORTAZIONE ===")
    print(f" - Righe processate correttamente (Ristoranti importati): {imported}")
    print(f" - Righe scartate (Mancano campi obbligatori o coordinate non valide): {skipped}")
    print(f" - File SQL generato: {sql_file}")
    print("===============================")

if __name__ == '__main__':
    main()

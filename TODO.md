# TODO - Restaurant cards bigger + edit button

- [x] Step 1: Increase owner restaurant card size (DashboardGestore) via inline sizing.
- [x] Step 2: Add “Edit” button to each restaurant card in `DashboardGestoreController#createRestaurantCard`.
- [x] Step 3: Implement edit flow by reusing `aggiungi_ristorante.fxml` + `AggiungiRistoranteController` in “edit mode” (prefill fields from `SessioneCorrente.selectedRistorante`).
- [x] Step 4: Add backend support for updating a restaurant (server request + DAO update) if missing.
- [x] Step 5: Wire save button in `AggiungiRistoranteController` to call update when in edit mode.
- [ ] Step 6: Build/test (`mvn test` or `mvn package`) and sanity-check UI.

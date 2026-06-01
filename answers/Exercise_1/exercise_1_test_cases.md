# Test Case Design - Gift Sending Payment API

Created By: Mohanad Mohamed

Executed By: Mohanad Mohamed

## Notes and Assumptions

* I treated this endpoint as a wallet/payment-sensitive API, so I gave higher priority to wallet deduction, duplicate charging, authorization, concurrency, and idempotency.
* The live buggy admin app helps in understanding the business flow, but most API and database checks cannot be fully executed from the mock UI.
* For test cases that require backend response validation, database validation, wallet row locking, WebSocket events, or backend failure simulation, I mentioned that clearly in the Actual Result field.
* I assume wallet deduction, gift transaction creation, winner payout, and broadcast event should be handled as one safe operation. If one part fails, the full operation should be rolled back.
* I assume replaying the same idempotency key with the same request body should not charge the sender twice.
* I assume replaying the same idempotency key with a different request body should be rejected.
* I would confirm the exact backend error format and response codes with the backend team before automating these cases.

---

## 1. Happy Path

<table border="1" cellpadding="10" cellspacing="0" width="100%">
  <tr>
    <th width="8%">Test Case ID</th>
    <th width="7%">Requirement ID</th>
    <th width="13%">Title</th>
    <th width="12%">Description</th>
    <th width="12%">Preconditions</th>
    <th width="13%">Test Steps</th>
    <th width="15%">Test Data</th>
    <th width="13%">Expected Result</th>
    <th width="10%">Actual Result</th>
    <th width="4%">Priority</th>
    <th width="5%">Status</th>
  </tr>

  <tr>
    <td>PAY-GIFT-TC-001</td>
    <td>REQ-HP</td>
    <td>Gift API - Verify successful gift send deducts sender wallet correctly</td>
    <td>Verify that an authenticated sender can send a valid gift and the correct total cost is deducted from the sender wallet.</td>
    <td>Sender is authenticated.<br>Sender is joined to the target room.<br>Sender wallet has enough coins.<br>Gift exists and has a fixed coin price.<br>Room has enough eligible viewers.</td>
    <td>1. <b>Send POST request</b> using TD-001.<br>2. <b>Validate API response</b> contains success result and charged coins.<br>3. <b>Validate sender wallet</b> in database after request.<br>4. <b>Validate gift transaction</b> record in database.</td>
    <td><b>TD-001</b><br>Method: POST<br>Endpoint: /api/gifts/send<br>Header: Authorization = Bearer valid_sender_token<br>Header: Content-Type = application/json<br><br>Body:<br>room_id = 12345<br>gift_id = 7<br>quantity = 2<br>winners_count = 5<br>idempotency_key = valid UUID v4<br><br>Wallet balance before request = 10000 coins<br>Gift coin price = 500 coins<br>Expected charged coins = 1000 coins</td>
    <td>API returns success response.<br>coins_charged equals 1000.<br>Sender wallet is reduced from 10000 to 9000.<br>One gift transaction record is created with the correct sender, room, gift, quantity, and cost.</td>
    <td>Not executed - backend API and database access are required.</td>
    <td>P0</td>
    <td>Not Run</td>
  </tr>

  <tr>
    <td>PAY-GIFT-TC-002</td>
    <td>REQ-HP</td>
    <td>Winners - Verify successful gift send creates the correct number of winners</td>
    <td>Verify that the API selects the requested number of winners when enough eligible viewers are available in the room.</td>
    <td>Sender is authenticated.<br>Sender is joined to the room.<br>Sender has enough coins.<br>Room has at least 10 eligible viewers.</td>
    <td>1. <b>Send POST request</b> using TD-002.<br>2. <b>Validate API response</b> contains winners list.<br>3. <b>Validate winners count</b> in response.<br>4. <b>Validate winner payout records</b> in database.</td>
    <td><b>TD-002</b><br>Method: POST<br>Endpoint: /api/gifts/send<br>Header: Authorization = Bearer valid_sender_token<br>Header: Content-Type = application/json<br><br>Body:<br>room_id = 12345<br>gift_id = 7<br>quantity = 1<br>winners_count = 10<br>idempotency_key = valid UUID v4<br><br>Eligible viewers in room = 10 or more</td>
    <td>API returns success response.<br>Response contains exactly 10 winners.<br>Database contains exactly 10 winner payout records linked to the gift transaction.<br>No duplicate winner is selected in the same transaction.</td>
    <td>Not executed - backend API and database access are required.</td>
    <td>P1</td>
    <td>Not Run</td>
  </tr>

  <tr>
    <td>PAY-GIFT-TC-003</td>
    <td>REQ-HP</td>
    <td>Quantity - Verify maximum valid quantity is accepted</td>
    <td>Verify that the API accepts the maximum allowed quantity when the sender has enough balance.</td>
    <td>Sender is authenticated.<br>Sender is joined to the room.<br>Sender has enough coins for the maximum quantity.<br>Gift exists and room has enough viewers.</td>
    <td>1. <b>Send POST request</b> using TD-003.<br>2. <b>Validate API response</b> is successful.<br>3. <b>Validate charged coins</b> are calculated correctly.<br>4. <b>Validate sender wallet</b> is deducted once.</td>
    <td><b>TD-003</b><br>Method: POST<br>Endpoint: /api/gifts/send<br>Header: Authorization = Bearer valid_sender_token<br>Header: Content-Type = application/json<br><br>Body:<br>room_id = 12345<br>gift_id = 7<br>quantity = 100<br>winners_count = 5<br>idempotency_key = valid UUID v4<br><br>Gift coin price = 500 coins<br>Expected charged coins = 50000 coins</td>
    <td>API returns success response.<br>Total charged coins equal quantity multiplied by gift coin price.<br>Sender wallet is deducted once by the correct amount.<br>One transaction is created.</td>
    <td>Partially executable from the mock UI for input behavior only. Final API and DB checks require backend access.</td>
    <td>P1</td>
    <td>Not Run</td>
  </tr>
</table>

---

## 2. Field Validation

<table border="1" cellpadding="10" cellspacing="0" width="100%">
  <tr>
    <th width="8%">Test Case ID</th>
    <th width="7%">Requirement ID</th>
    <th width="13%">Title</th>
    <th width="12%">Description</th>
    <th width="12%">Preconditions</th>
    <th width="13%">Test Steps</th>
    <th width="15%">Test Data</th>
    <th width="13%">Expected Result</th>
    <th width="10%">Actual Result</th>
    <th width="4%">Priority</th>
    <th width="5%">Status</th>
  </tr>

  <tr>
    <td>PAY-GIFT-TC-004</td>
    <td>REQ-FV</td>
    <td>Validation - Verify missing required fields are rejected</td>
    <td>Verify that the API rejects the request when one required field is missing.</td>
    <td>Sender is authenticated.<br>Sender is joined to the room.<br>Sender has enough wallet balance.</td>
    <td>1. <b>Send POST request</b> using TD-004 without one required field.<br>2. <b>Repeat request</b> for each required field.<br>3. <b>Validate validation error</b> in API response.<br>4. <b>Validate no wallet change</b> in database.</td>
    <td><b>TD-004</b><br>Method: POST<br>Endpoint: /api/gifts/send<br>Header: Authorization = Bearer valid_sender_token<br>Header: Content-Type = application/json<br><br>Body variations:<br>Missing room_id<br>Missing gift_id<br>Missing quantity<br>Missing winners_count<br>Missing idempotency_key</td>
    <td>API returns validation error, expected 422.<br>Error response clearly mentions the missing field.<br>No wallet deduction happens.<br>No transaction record is created.</td>
    <td>Not executed - backend API access is required.</td>
    <td>P1</td>
    <td>Not Run</td>
  </tr>

  <tr>
    <td>PAY-GIFT-TC-005</td>
    <td>REQ-FV</td>
    <td>Quantity - Verify zero quantity is rejected</td>
    <td>Verify that the API rejects quantity = 0.</td>
    <td>Sender is authenticated.<br>Sender is joined to the room.<br>Gift exists.</td>
    <td>1. <b>Send POST request</b> using TD-005.<br>2. <b>Validate API response</b> contains quantity validation error.<br>3. <b>Validate no wallet deduction</b> in database.<br>4. <b>Validate no transaction</b> is created.</td>
    <td><b>TD-005</b><br>Method: POST<br>Endpoint: /api/gifts/send<br>Header: Authorization = Bearer valid_sender_token<br>Header: Content-Type = application/json<br><br>Body:<br>room_id = 12345<br>gift_id = 7<br>quantity = 0<br>winners_count = 5<br>idempotency_key = valid UUID v4</td>
    <td>API returns validation error for quantity.<br>Wallet balance remains unchanged.<br>No transaction record is created.<br>No winner payout record is created.</td>
    <td>Partially executable from the mock UI. Backend validation and DB checks require API access.</td>
    <td>P1</td>
    <td>Not Run</td>
  </tr>

  <tr>
    <td>PAY-GIFT-TC-006</td>
    <td>REQ-FV</td>
    <td>Quantity - Verify quantity above limit is rejected</td>
    <td>Verify that the API rejects quantity greater than the maximum allowed value.</td>
    <td>Sender is authenticated.<br>Sender is joined to the room.<br>Gift exists.</td>
    <td>1. <b>Send POST request</b> using TD-006.<br>2. <b>Validate API response</b> contains quantity validation error.<br>3. <b>Validate sender wallet</b> remains unchanged.<br>4. <b>Validate no event</b> is broadcast.</td>
    <td><b>TD-006</b><br>Method: POST<br>Endpoint: /api/gifts/send<br>Header: Authorization = Bearer valid_sender_token<br>Header: Content-Type = application/json<br><br>Body:<br>room_id = 12345<br>gift_id = 7<br>quantity = 101<br>winners_count = 5<br>idempotency_key = valid UUID v4</td>
    <td>API returns validation error for quantity.<br>No wallet deduction happens.<br>No transaction is created.<br>No gift.sent event is broadcast.</td>
    <td>Partially executable from the mock UI. Backend validation and DB checks require API access.</td>
    <td>P1</td>
    <td>Not Run</td>
  </tr>

  <tr>
    <td>PAY-GIFT-TC-007</td>
    <td>REQ-FV</td>
    <td>Winners - Verify winners_count greater than 50 is rejected</td>
    <td>Verify that the API rejects winners_count when it exceeds the maximum allowed value.</td>
    <td>Sender is authenticated.<br>Sender is joined to the room.<br>Room has viewers.</td>
    <td>1. <b>Send POST request</b> using TD-007.<br>2. <b>Validate API response</b> contains winners_count validation error.<br>3. <b>Validate no wallet deduction</b> happens.<br>4. <b>Validate no winner payout</b> is created.</td>
    <td><b>TD-007</b><br>Method: POST<br>Endpoint: /api/gifts/send<br>Header: Authorization = Bearer valid_sender_token<br>Header: Content-Type = application/json<br><br>Body:<br>room_id = 12345<br>gift_id = 7<br>quantity = 1<br>winners_count = 51<br>idempotency_key = valid UUID v4</td>
    <td>API returns validation error for winners_count.<br>Wallet remains unchanged.<br>No transaction record is created.<br>No winner payout records are created.</td>
    <td>Partially executable from the mock UI if winners input is available. Backend validation requires API access.</td>
    <td>P1</td>
    <td>Not Run</td>
  </tr>

  <tr>
    <td>PAY-GIFT-TC-008</td>
    <td>REQ-FV</td>
    <td>Idempotency - Verify invalid UUID format is rejected</td>
    <td>Verify that the API rejects an invalid idempotency_key format.</td>
    <td>Sender is authenticated.<br>Sender is joined to the room.<br>Other request fields are valid.</td>
    <td>1. <b>Send POST request</b> using TD-008.<br>2. <b>Validate API response</b> contains idempotency_key validation error.<br>3. <b>Validate no wallet deduction</b> happens.<br>4. <b>Validate no transaction</b> is created.</td>
    <td><b>TD-008</b><br>Method: POST<br>Endpoint: /api/gifts/send<br>Header: Authorization = Bearer valid_sender_token<br>Header: Content-Type = application/json<br><br>Body:<br>room_id = 12345<br>gift_id = 7<br>quantity = 1<br>winners_count = 5<br>idempotency_key = test123</td>
    <td>API returns validation error for idempotency_key.<br>Wallet remains unchanged.<br>No transaction record is created.</td>
    <td>Not executed - idempotency key is not exposed in the mock UI and backend API access is required.</td>
    <td>P1</td>
    <td>Not Run</td>
  </tr>

  <tr>
    <td>PAY-GIFT-TC-009</td>
    <td>REQ-FV</td>
    <td>Validation - Verify string values are rejected for integer fields</td>
    <td>Verify that the API rejects non-integer values for numeric fields.</td>
    <td>Sender is authenticated.<br>Sender is joined to the room.<br>Gift exists.</td>
    <td>1. <b>Send POST request</b> using TD-009-A.<br>2. <b>Repeat request</b> using TD-009-B and TD-009-C.<br>3. <b>Validate validation error</b> for each invalid numeric field.<br>4. <b>Validate no DB changes</b> happen.</td>
    <td><b>TD-009-A</b><br>quantity = ten<br><br><b>TD-009-B</b><br>room_id = abc<br><br><b>TD-009-C</b><br>winners_count = five<br><br>Method: POST<br>Endpoint: /api/gifts/send<br>Headers: valid auth and JSON content type</td>
    <td>API returns validation error for each invalid field.<br>No wallet update happens.<br>No transaction is created.</td>
    <td>Partially executable from the mock UI for quantity field only. Other API fields require backend access.</td>
    <td>P1</td>
    <td>Not Run</td>
  </tr>
</table>

---

## 3. Authentication and Authorization

<table border="1" cellpadding="10" cellspacing="0" width="100%">
  <tr>
    <th width="8%">Test Case ID</th>
    <th width="7%">Requirement ID</th>
    <th width="13%">Title</th>
    <th width="12%">Description</th>
    <th width="12%">Preconditions</th>
    <th width="13%">Test Steps</th>
    <th width="15%">Test Data</th>
    <th width="13%">Expected Result</th>
    <th width="10%">Actual Result</th>
    <th width="4%">Priority</th>
    <th width="5%">Status</th>
  </tr>

  <tr>
    <td>PAY-GIFT-TC-010</td>
    <td>REQ-AUTH</td>
    <td>Auth - Verify request without bearer token is rejected</td>
    <td>Verify that unauthenticated users cannot send gifts.</td>
    <td>No valid authentication token is provided.</td>
    <td>1. <b>Send POST request</b> using TD-010.<br>2. <b>Validate API response</b> returns unauthorized error.<br>3. <b>Validate wallet</b> remains unchanged.<br>4. <b>Validate no transaction</b> is created.</td>
    <td><b>TD-010</b><br>Method: POST<br>Endpoint: /api/gifts/send<br>Header: Content-Type = application/json<br>Authorization header: not sent<br><br>Body:<br>room_id = 12345<br>gift_id = 7<br>quantity = 1<br>winners_count = 5<br>idempotency_key = valid UUID v4</td>
    <td>API returns 401 Unauthorized.<br>No wallet deduction happens.<br>No transaction is created.<br>No broadcast event is sent.</td>
    <td>Not executed - real authenticated backend API is required.</td>
    <td>P0</td>
    <td>Not Run</td>
  </tr>

  <tr>
    <td>PAY-GIFT-TC-011</td>
    <td>REQ-AUTH</td>
    <td>Auth - Verify expired or invalid token is rejected</td>
    <td>Verify that the API rejects requests with expired, malformed, or invalid Sanctum token.</td>
    <td>Invalid or expired token is available.<br>Request body is otherwise valid.</td>
    <td>1. <b>Send POST request</b> using TD-011.<br>2. <b>Validate API response</b> returns unauthorized error.<br>3. <b>Validate no wallet update</b> happens.<br>4. <b>Validate no transaction</b> is created.</td>
    <td><b>TD-011</b><br>Method: POST<br>Endpoint: /api/gifts/send<br>Header: Authorization = Bearer invalid-token<br>Header: Content-Type = application/json<br><br>Body:<br>room_id = 12345<br>gift_id = 7<br>quantity = 1<br>winners_count = 5<br>idempotency_key = valid UUID v4</td>
    <td>API returns 401 Unauthorized.<br>No wallet or transaction changes happen.</td>
    <td>Not executed - real token-based backend API is required.</td>
    <td>P0</td>
    <td>Not Run</td>
  </tr>

  <tr>
    <td>PAY-GIFT-TC-012</td>
    <td>REQ-AUTH</td>
    <td>Room - Verify user cannot send gift to room they did not join</td>
    <td>Verify that a valid user cannot send a gift to a room where they are not a participant.</td>
    <td>Sender is authenticated.<br>Sender has enough coins.<br>Sender is not joined to the target room.</td>
    <td>1. <b>Send POST request</b> using TD-012.<br>2. <b>Validate API response</b> returns authorization or business error.<br>3. <b>Validate wallet</b> remains unchanged.<br>4. <b>Validate no transaction</b> is created.</td>
    <td><b>TD-012</b><br>Method: POST<br>Endpoint: /api/gifts/send<br>Header: Authorization = Bearer valid_sender_token<br>Header: Content-Type = application/json<br><br>Body:<br>room_id = unjoined_room_id<br>gift_id = 7<br>quantity = 1<br>winners_count = 5<br>idempotency_key = valid UUID v4</td>
    <td>API returns 403 or suitable business error.<br>Wallet remains unchanged.<br>No transaction is created.</td>
    <td>Not executed - backend room membership validation and DB access are required.</td>
    <td>P0</td>
    <td>Not Run</td>
  </tr>

  <tr>
    <td>PAY-GIFT-TC-013</td>
    <td>REQ-AUTH</td>
    <td>Gift - Verify user cannot send unavailable or unauthorized gift</td>
    <td>Verify that the API rejects a gift if the sender is not allowed to use or buy it.</td>
    <td>Sender is authenticated.<br>Sender is joined to room.<br>Gift exists but is not available to this sender.</td>
    <td>1. <b>Send POST request</b> using TD-013.<br>2. <b>Validate API response</b> returns authorization or business error.<br>3. <b>Validate wallet</b> remains unchanged.<br>4. <b>Validate no gift transaction</b> is created.</td>
    <td><b>TD-013</b><br>Method: POST<br>Endpoint: /api/gifts/send<br>Header: Authorization = Bearer valid_sender_token<br>Header: Content-Type = application/json<br><br>Body:<br>room_id = 12345<br>gift_id = restricted_gift_id<br>quantity = 1<br>winners_count = 5<br>idempotency_key = valid UUID v4</td>
    <td>API returns authorization or business error.<br>No wallet deduction happens.<br>No transaction record is created.</td>
    <td>Not executed - backend gift authorization rules are required.</td>
    <td>P1</td>
    <td>Not Run</td>
  </tr>
</table>

---

## 4. Concurrency and Race Conditions

<table border="1" cellpadding="10" cellspacing="0" width="100%">
  <tr>
    <th width="8%">Test Case ID</th>
    <th width="7%">Requirement ID</th>
    <th width="13%">Title</th>
    <th width="12%">Description</th>
    <th width="12%">Preconditions</th>
    <th width="13%">Test Steps</th>
    <th width="15%">Test Data</th>
    <th width="13%">Expected Result</th>
    <th width="10%">Actual Result</th>
    <th width="4%">Priority</th>
    <th width="5%">Status</th>
  </tr>

  <tr>
    <td>PAY-GIFT-TC-014</td>
    <td>REQ-CONC</td>
    <td>Wallet - Verify two simultaneous requests cannot double spend same balance</td>
    <td>Verify that concurrent gift requests cannot deduct more coins than the sender owns.</td>
    <td>Sender is authenticated.<br>Sender is joined to the room.<br>Sender has exactly enough coins for one request only.</td>
    <td>1. <b>Prepare two POST requests</b> using TD-014-A and TD-014-B.<br>2. <b>Send both requests at the same time</b> using a concurrency tool or script.<br>3. <b>Validate both API responses</b>.<br>4. <b>Validate final wallet balance</b> in database.<br>5. <b>Validate transaction count</b> in database.</td>
    <td><b>TD-014-A</b><br>Method: POST<br>Endpoint: /api/gifts/send<br>idempotency_key = UUID-A<br>Total cost = 1000 coins<br><br><b>TD-014-B</b><br>Method: POST<br>Endpoint: /api/gifts/send<br>idempotency_key = UUID-B<br>Total cost = 1000 coins<br><br>Starting wallet balance = 1000 coins</td>
    <td>Only one request succeeds.<br>The second request fails due to insufficient balance or conflict.<br>Final wallet balance must not be negative.<br>Only one successful transaction is stored.</td>
    <td>Not executed - requires real API concurrency test and DB verification.</td>
    <td>P0</td>
    <td>Not Run</td>
  </tr>

  <tr>
    <td>PAY-GIFT-TC-015</td>
    <td>REQ-CONC</td>
    <td>Wallet - Verify concurrent partial spends calculate final balance correctly</td>
    <td>Verify that multiple valid concurrent sends are handled safely when total cost is still within wallet balance.</td>
    <td>Sender is authenticated.<br>Sender is joined to the room.<br>Sender has enough balance for all concurrent requests.</td>
    <td>1. <b>Prepare three POST requests</b> using TD-015.<br>2. <b>Send requests concurrently</b> with different idempotency keys.<br>3. <b>Validate API responses</b>.<br>4. <b>Validate final wallet balance</b>.<br>5. <b>Validate transaction records</b>.</td>
    <td><b>TD-015</b><br>Method: POST<br>Endpoint: /api/gifts/send<br>Header: Authorization = Bearer valid_sender_token<br><br>Concurrent requests count = 3<br>Each request cost = 1000 coins<br>Starting wallet balance = 5000 coins<br>Expected final balance = 2000 coins<br>Each request uses different UUID key</td>
    <td>All three requests may succeed.<br>Final wallet balance equals 2000.<br>Three transaction records are created.<br>No duplicate or missing wallet deduction occurs.</td>
    <td>Not executed - requires real API concurrency test and DB verification.</td>
    <td>P0</td>
    <td>Not Run</td>
  </tr>

  <tr>
    <td>PAY-GIFT-TC-016</td>
    <td>REQ-CONC</td>
    <td>Idempotency - Verify same key sent concurrently is processed once only</td>
    <td>Verify that two concurrent requests using the same idempotency key do not create duplicate transactions.</td>
    <td>Sender is authenticated.<br>Sender is joined to room.<br>Sender has enough coins.</td>
    <td>1. <b>Prepare two identical POST requests</b> using TD-016.<br>2. <b>Send both requests at the same time</b>.<br>3. <b>Validate API responses</b>.<br>4. <b>Validate wallet deduction count</b>.<br>5. <b>Validate transaction count</b>.</td>
    <td><b>TD-016</b><br>Method: POST<br>Endpoint: /api/gifts/send<br>Same request body for both calls<br>Same idempotency_key for both calls<br><br>Body:<br>room_id = 12345<br>gift_id = 7<br>quantity = 1<br>winners_count = 5<br>idempotency_key = same UUID v4</td>
    <td>Only one wallet deduction occurs.<br>Only one transaction is created.<br>Both responses either return the same successful result or one returns a safe duplicate response.</td>
    <td>Not executed - idempotency behavior requires backend API and DB access.</td>
    <td>P0</td>
    <td>Not Run</td>
  </tr>

  <tr>
    <td>PAY-GIFT-TC-017</td>
    <td>REQ-CONC</td>
    <td>Winners - Verify audience changes during request do not create invalid winner count</td>
    <td>Verify that if room audience changes during processing, the API does not create more winners than eligible viewers.</td>
    <td>Room starts with enough viewers.<br>Some viewers may leave during request processing.</td>
    <td>1. <b>Start POST request</b> using TD-017.<br>2. <b>Simulate audience change</b> during request processing.<br>3. <b>Validate API response</b>.<br>4. <b>Validate winner records</b> in database.</td>
    <td><b>TD-017</b><br>Method: POST<br>Endpoint: /api/gifts/send<br>Header: Authorization = Bearer valid_sender_token<br><br>Body:<br>room_id = 12345<br>gift_id = 7<br>quantity = 1<br>winners_count = 10<br>idempotency_key = valid UUID v4<br><br>Audience changes from 10 viewers to lower value during processing</td>
    <td>API either uses a consistent snapshot or fails safely.<br>It must not create invalid winners.<br>It must not create duplicate winners.<br>It must not return unhandled server error.</td>
    <td>Not executed - requires backend test environment and audience simulation.</td>
    <td>P1</td>
    <td>Not Run</td>
  </tr>
</table>

---

## 5. Idempotency

<table border="1" cellpadding="10" cellspacing="0" width="100%">
  <tr>
    <th width="8%">Test Case ID</th>
    <th width="7%">Requirement ID</th>
    <th width="13%">Title</th>
    <th width="12%">Description</th>
    <th width="12%">Preconditions</th>
    <th width="13%">Test Steps</th>
    <th width="15%">Test Data</th>
    <th width="13%">Expected Result</th>
    <th width="10%">Actual Result</th>
    <th width="4%">Priority</th>
    <th width="5%">Status</th>
  </tr>

  <tr>
    <td>PAY-GIFT-TC-018</td>
    <td>REQ-IDEMP</td>
    <td>Idempotency - Verify retry with same key and same body does not double charge</td>
    <td>Verify that resending the same request with the same idempotency key returns the original result without charging the wallet again.</td>
    <td>Sender is authenticated.<br>Sender is joined to room.<br>Sender has enough coins.<br>First request succeeds.</td>
    <td>1. <b>Send first POST request</b> using TD-018.<br>2. <b>Send retry POST request</b> using the same test data and same idempotency key.<br>3. <b>Compare API responses</b>.<br>4. <b>Validate wallet deduction</b> happens once only.<br>5. <b>Validate transaction count</b> in database.</td>
    <td><b>TD-018</b><br>Method: POST<br>Endpoint: /api/gifts/send<br>Header: Authorization = Bearer valid_sender_token<br>Header: Content-Type = application/json<br><br>Body:<br>room_id = 12345<br>gift_id = 7<br>quantity = 1<br>winners_count = 5<br>idempotency_key = same UUID v4<br><br>Retry window = within 24 hours</td>
    <td>First request succeeds.<br>Second request does not create a new transaction.<br>Second request does not deduct wallet again.<br>Response should safely represent the original operation.</td>
    <td>Not executed - idempotency key and backend transaction records are not available from the mock UI.</td>
    <td>P0</td>
    <td>Not Run</td>
  </tr>

  <tr>
    <td>PAY-GIFT-TC-019</td>
    <td>REQ-IDEMP</td>
    <td>Idempotency - Verify same key with different body is rejected</td>
    <td>Verify that reusing an idempotency key with changed request data is not accepted.</td>
    <td>Sender is authenticated.<br>Sender has already used an idempotency key successfully.</td>
    <td>1. <b>Send first POST request</b> using TD-019-A.<br>2. <b>Send second POST request</b> using TD-019-B with the same key but different quantity.<br>3. <b>Validate API response</b> for second request.<br>4. <b>Validate wallet</b> is not charged for the changed body.</td>
    <td><b>TD-019-A</b><br>Method: POST<br>Endpoint: /api/gifts/send<br>quantity = 1<br>idempotency_key = UUID-K<br><br><b>TD-019-B</b><br>Method: POST<br>Endpoint: /api/gifts/send<br>quantity = 5<br>idempotency_key = UUID-K<br><br>Same user, same room, same gift</td>
    <td>Second request is rejected with conflict or validation error.<br>Wallet is not deducted for the second body.<br>No second transaction is created.</td>
    <td>Not executed - backend idempotency storage and DB access are required.</td>
    <td>P0</td>
    <td>Not Run</td>
  </tr>
</table>

---

## 6. Business Logic and Money

<table border="1" cellpadding="10" cellspacing="0" width="100%">
  <tr>
    <th width="8%">Test Case ID</th>
    <th width="7%">Requirement ID</th>
    <th width="13%">Title</th>
    <th width="12%">Description</th>
    <th width="12%">Preconditions</th>
    <th width="13%">Test Steps</th>
    <th width="15%">Test Data</th>
    <th width="13%">Expected Result</th>
    <th width="10%">Actual Result</th>
    <th width="4%">Priority</th>
    <th width="5%">Status</th>
  </tr>

  <tr>
    <td>PAY-GIFT-TC-020</td>
    <td>REQ-BM</td>
    <td>Wallet - Verify insufficient balance request is rejected</td>
    <td>Verify that the API rejects a gift send when sender wallet balance is lower than the required total cost.</td>
    <td>Sender is authenticated.<br>Sender is joined to room.<br>Sender wallet balance is lower than gift total cost.</td>
    <td>1. <b>Send POST request</b> using TD-020.<br>2. <b>Validate API response</b> returns insufficient balance error.<br>3. <b>Validate wallet</b> remains unchanged.<br>4. <b>Validate no transaction</b> is created.</td>
    <td><b>TD-020</b><br>Method: POST<br>Endpoint: /api/gifts/send<br>Header: Authorization = Bearer valid_sender_token<br>Header: Content-Type = application/json<br><br>Body:<br>room_id = 12345<br>gift_id = 7<br>quantity = 1<br>winners_count = 5<br>idempotency_key = valid UUID v4<br><br>Wallet balance = 400 coins<br>Gift price = 500 coins<br>Expected total cost = 500 coins</td>
    <td>API returns insufficient balance error.<br>Wallet remains 400 coins.<br>No transaction is created.<br>No winner payout is created.<br>No broadcast event is sent.</td>
    <td>Partially executable from mock UI only if UI allows creating insufficient balance state. Backend and DB checks require API access.</td>
    <td>P0</td>
    <td>Not Run</td>
  </tr>

  <tr>
    <td>PAY-GIFT-TC-021</td>
    <td>REQ-BM</td>
    <td>Wallet - Verify exact balance request leaves wallet at zero</td>
    <td>Verify that a sender can send a gift when balance exactly equals the required total cost.</td>
    <td>Sender is authenticated.<br>Sender is joined to room.<br>Sender balance equals the total gift cost exactly.</td>
    <td>1. <b>Send POST request</b> using TD-021.<br>2. <b>Validate API response</b> is successful.<br>3. <b>Validate final wallet balance</b> equals zero.<br>4. <b>Validate transaction amount</b> equals original balance.</td>
    <td><b>TD-021</b><br>Method: POST<br>Endpoint: /api/gifts/send<br>Header: Authorization = Bearer valid_sender_token<br><br>Body:<br>room_id = 12345<br>gift_id = 7<br>quantity = 2<br>winners_count = 5<br>idempotency_key = valid UUID v4<br><br>Wallet balance = 1000 coins<br>Gift price = 500 coins<br>Expected total cost = 1000 coins</td>
    <td>API succeeds.<br>Sender wallet becomes exactly 0.<br>No negative balance is created.<br>Transaction amount equals original wallet balance.</td>
    <td>Not executed - backend wallet setup and DB access are required.</td>
    <td>P0</td>
    <td>Not Run</td>
  </tr>

  <tr>
    <td>PAY-GIFT-TC-022</td>
    <td>REQ-BM</td>
    <td>Pricing - Verify total charged uses quantity multiplied by gift coin price</td>
    <td>Verify that the API calculates total cost using quantity multiplied by gift coin price, not only gift price or wrong arithmetic.</td>
    <td>Sender is authenticated.<br>Sender is joined to room.<br>Sender has enough balance.<br>Gift price is known.</td>
    <td>1. <b>Send POST request</b> using TD-022.<br>2. <b>Validate coins_charged</b> in API response.<br>3. <b>Validate transaction amount</b> in database.<br>4. <b>Validate wallet deduction</b> equals calculated total.</td>
    <td><b>TD-022</b><br>Method: POST<br>Endpoint: /api/gifts/send<br>Header: Authorization = Bearer valid_sender_token<br><br>Body:<br>room_id = 12345<br>gift_id = 7<br>quantity = 3<br>winners_count = 5<br>idempotency_key = valid UUID v4<br><br>Gift price = 500 coins<br>Expected total = 1500 coins</td>
    <td>Response shows coins_charged = 1500.<br>DB transaction amount equals 1500.<br>Wallet deduction equals 1500.<br>No mismatch exists between response and DB.</td>
    <td>Partially executable from mock UI by checking displayed total cost. Final API and DB checks require backend access.</td>
    <td>P0</td>
    <td>Not Run</td>
  </tr>

  <tr>
    <td>PAY-GIFT-TC-023</td>
    <td>REQ-BM</td>
    <td>Winners - Verify winners_count cannot exceed current audience size</td>
    <td>Verify that the API rejects a request when requested winners are greater than eligible room viewers.</td>
    <td>Sender is authenticated.<br>Sender is joined to room.<br>Room has only 3 eligible viewers.</td>
    <td>1. <b>Send POST request</b> using TD-023.<br>2. <b>Validate API response</b> returns business validation error.<br>3. <b>Validate wallet</b> remains unchanged.<br>4. <b>Validate no transaction</b> is created.</td>
    <td><b>TD-023</b><br>Method: POST<br>Endpoint: /api/gifts/send<br>Header: Authorization = Bearer valid_sender_token<br><br>Body:<br>room_id = 12345<br>gift_id = 7<br>quantity = 1<br>winners_count = 5<br>idempotency_key = valid UUID v4<br><br>Eligible audience size = 3</td>
    <td>API returns business validation error.<br>No wallet deduction happens.<br>No transaction is created.<br>No winner records are created.</td>
    <td>Partially executable from mock UI only if winners count and audience size can be controlled. Backend validation requires API access.</td>
    <td>P1</td>
    <td>Not Run</td>
  </tr>
</table>

---

## 7. Side Effects

<table border="1" cellpadding="10" cellspacing="0" width="100%">
  <tr>
    <th width="8%">Test Case ID</th>
    <th width="7%">Requirement ID</th>
    <th width="13%">Title</th>
    <th width="12%">Description</th>
    <th width="12%">Preconditions</th>
    <th width="13%">Test Steps</th>
    <th width="15%">Test Data</th>
    <th width="13%">Expected Result</th>
    <th width="10%">Actual Result</th>
    <th width="4%">Priority</th>
    <th width="5%">Status</th>
  </tr>

  <tr>
    <td>PAY-GIFT-TC-024</td>
    <td>REQ-SE</td>
    <td>Broadcast - Verify gift.sent event is sent after successful transaction</td>
    <td>Verify that a successful gift send broadcasts the expected gift.sent event to the room.</td>
    <td>Sender is authenticated.<br>Sender is joined to room.<br>Sender has enough balance.<br>WebSocket listener is connected to the room channel.</td>
    <td>1. <b>Connect WebSocket listener</b> using TD-024.<br>2. <b>Send POST request</b> using TD-024.<br>3. <b>Observe broadcast event</b>.<br>4. <b>Validate event payload</b>.</td>
    <td><b>TD-024</b><br>Method: POST<br>Endpoint: /api/gifts/send<br>WebSocket event name: gift.sent<br>Target room channel: room_12345<br><br>Body:<br>room_id = 12345<br>gift_id = 7<br>quantity = 1<br>winners_count = 5<br>idempotency_key = valid UUID v4</td>
    <td>API succeeds.<br>One gift.sent event is broadcast to the correct room.<br>Event payload contains transaction id, sender info, gift info, quantity, winners, and charged coins.</td>
    <td>Not executed - WebSocket backend/event channel is required.</td>
    <td>P1</td>
    <td>Not Run</td>
  </tr>

  <tr>
    <td>PAY-GIFT-TC-025</td>
    <td>REQ-SE</td>
    <td>Rollback - Verify failed payout does not leave partial wallet deduction</td>
    <td>Verify that if winner payout or event processing fails, the sender is not charged without a completed transaction.</td>
    <td>Backend test environment is available.<br>Failure can be simulated during winner payout or transaction processing.</td>
    <td>1. <b>Force payout failure</b> using TD-025 setup.<br>2. <b>Send POST request</b> using TD-025.<br>3. <b>Validate API response</b> returns safe error.<br>4. <b>Validate wallet rollback</b>.<br>5. <b>Validate no partial records</b> remain.</td>
    <td><b>TD-025</b><br>Method: POST<br>Endpoint: /api/gifts/send<br>Failure setup: simulate DB error during winner payout creation<br><br>Body:<br>room_id = 12345<br>gift_id = 7<br>quantity = 1<br>winners_count = 5<br>idempotency_key = valid UUID v4</td>
    <td>API returns safe error.<br>Sender wallet remains unchanged.<br>No partial transaction remains.<br>No partial winner payout records remain.<br>No gift.sent event is broadcast.</td>
    <td>Not executed - requires controlled backend failure simulation and DB access.</td>
    <td>P0</td>
    <td>Not Run</td>
  </tr>
</table>

---

## 8. Negative and Security

<table border="1" cellpadding="10" cellspacing="0" width="100%">
  <tr>
    <th width="8%">Test Case ID</th>
    <th width="7%">Requirement ID</th>
    <th width="13%">Title</th>
    <th width="12%">Description</th>
    <th width="12%">Preconditions</th>
    <th width="13%">Test Steps</th>
    <th width="15%">Test Data</th>
    <th width="13%">Expected Result</th>
    <th width="10%">Actual Result</th>
    <th width="4%">Priority</th>
    <th width="5%">Status</th>
  </tr>

  <tr>
    <td>PAY-GIFT-TC-026</td>
    <td>REQ-SEC</td>
    <td>Security - Verify client cannot override calculated coins_charged</td>
    <td>Verify that the backend ignores or rejects any client-sent field that tries to control charged coins.</td>
    <td>Sender is authenticated.<br>Sender is joined to room.<br>Sender has enough coins.</td>
    <td>1. <b>Send POST request</b> using TD-026.<br>2. <b>Validate API response</b> does not trust client amount.<br>3. <b>Validate transaction amount</b> in database.<br>4. <b>Validate wallet deduction</b> uses server calculation only.</td>
    <td><b>TD-026</b><br>Method: POST<br>Endpoint: /api/gifts/send<br>Header: Authorization = Bearer valid_sender_token<br><br>Body:<br>room_id = 12345<br>gift_id = 7<br>quantity = 3<br>winners_count = 5<br>idempotency_key = valid UUID v4<br>coins_charged = 1<br><br>Actual expected cost from server = 1500 coins</td>
    <td>API must not use client-provided coins_charged.<br>Either the request is rejected, or the backend calculates charge from server-side gift price and quantity only.<br>Wallet deduction must not equal 1 coin.</td>
    <td>Not executed - requires direct backend API request and DB check.</td>
    <td>P0</td>
    <td>Not Run</td>
  </tr>

  <tr>
    <td>PAY-GIFT-TC-027</td>
    <td>REQ-SEC</td>
    <td>Security - Verify malformed JSON request is handled safely</td>
    <td>Verify that malformed JSON does not cause an unhandled server error or partial transaction.</td>
    <td>Sender has valid token.<br>Request body is intentionally malformed.</td>
    <td>1. <b>Send malformed POST request</b> using TD-027.<br>2. <b>Validate API response</b> returns safe bad request error.<br>3. <b>Validate wallet</b> remains unchanged.<br>4. <b>Validate no stack trace</b> is exposed.</td>
    <td><b>TD-027</b><br>Method: POST<br>Endpoint: /api/gifts/send<br>Header: Authorization = Bearer valid_sender_token<br>Header: Content-Type = application/json<br><br>Body:<br>Malformed JSON, for example missing closing brace</td>
    <td>API returns clear bad request error, expected 400.<br>No wallet deduction happens.<br>No transaction is created.<br>No server stack trace or internal error details are exposed.</td>
    <td>Not executed - requires direct backend API request.</td>
    <td>P1</td>
    <td>Not Run</td>
  </tr>

  <tr>
    <td>PAY-GIFT-TC-028</td>
    <td>REQ-SEC</td>
    <td>Security - Verify injection-like values are rejected for numeric fields</td>
    <td>Verify that injection-style values in numeric fields are rejected by validation.</td>
    <td>Sender is authenticated.<br>Request contains injection-like input in numeric fields.</td>
    <td>1. <b>Send POST request</b> using TD-028-A.<br>2. <b>Send POST request</b> using TD-028-B.<br>3. <b>Validate API response</b> returns validation error.<br>4. <b>Validate no DB change</b> happens.</td>
    <td><b>TD-028-A</b><br>room_id = 1 OR 1=1<br><br><b>TD-028-B</b><br>gift_id = 7; DROP TABLE gifts<br><br>Method: POST<br>Endpoint: /api/gifts/send<br>Header: Authorization = Bearer valid_sender_token</td>
    <td>API returns validation error.<br>No DB changes occur.<br>No internal SQL error or stack trace is exposed.</td>
    <td>Not executed - requires direct backend API request and server response check.</td>
    <td>P1</td>
    <td>Not Run</td>
  </tr>
</table>

---

## 9. Rate Limiting

<table border="1" cellpadding="10" cellspacing="0" width="100%">
  <tr>
    <th width="8%">Test Case ID</th>
    <th width="7%">Requirement ID</th>
    <th width="13%">Title</th>
    <th width="12%">Description</th>
    <th width="12%">Preconditions</th>
    <th width="13%">Test Steps</th>
    <th width="15%">Test Data</th>
    <th width="13%">Expected Result</th>
    <th width="10%">Actual Result</th>
    <th width="4%">Priority</th>
    <th width="5%">Status</th>
  </tr>

  <tr>
    <td>PAY-GIFT-TC-029</td>
    <td>REQ-RATE</td>
    <td>Rate Limit - Verify more than 30 requests per minute are blocked</td>
    <td>Verify that the API enforces the limit of 30 requests per minute per user.</td>
    <td>Sender is authenticated.<br>Sender is joined to room.<br>Sender has enough balance or test wallet reset is available.</td>
    <td>1. <b>Send 30 POST requests</b> using TD-029 within one minute.<br>2. <b>Send 31st POST request</b> in the same minute.<br>3. <b>Validate API response</b> for the 31st request.<br>4. <b>Validate blocked request</b> does not change wallet or create transaction.</td>
    <td><b>TD-029</b><br>Method: POST<br>Endpoint: /api/gifts/send<br>Header: Authorization = Bearer valid_sender_token<br>Header: Content-Type = application/json<br><br>Total requests = 31<br>Time window = 1 minute<br>Same sender token<br>Unique idempotency key per request</td>
    <td>First allowed requests are processed according to wallet and idempotency rules.<br>The 31st request returns rate limit error, expected 429.<br>No wallet deduction happens for the blocked request.</td>
    <td>Not executed - requires real backend API rate limit behavior.</td>
    <td>P1</td>
    <td>Not Run</td>
  </tr>
</table>

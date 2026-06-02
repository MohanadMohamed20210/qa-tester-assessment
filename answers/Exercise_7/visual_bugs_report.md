# Live App Bug Hunt - Visual Bugs Report

Tester: Mohanad Mohamed
Application: Buggy Admin Panel
URL: https://mazen-salah.github.io/qa-tester-assessment/app/buggy-admin/
Browser: Chrome
Test Type: Manual exploratory testing

---

## Bug #1: Gift total cost is calculated using addition instead of multiplication

* **Where:** Send Gift tab - Total cost calculation

* **Steps to reproduce:**

  1. Open the live admin app.
  2. Go to the Send Gift tab.
  3. Select Box gift with price 500 coins.
  4. Enter quantity as 3.
  5. Observe the displayed Total cost.

* **Expected behavior:**
  Total cost should be calculated as gift price multiplied by quantity.
  For Box gift: 500 coins × 3 = 1500 coins.

* **Actual behavior:**
  Total cost is displayed as 503 coins.
  The displayed value looks like the app is calculating 500 + 3 instead of 500 × 3.

* **Screenshot:**
  screenshots/BUG-001-wrong-total-cost.png

* **Severity:** High

* **Severity reasoning:**
  This is High because it affects wallet/money calculation and may cause users to be charged the wrong amount.

* **Suspected root cause:**
  Frontend logic issue. The total cost calculation is likely adding the gift price and quantity instead of multiplying them.

* **Proposed fix:**
  Update the total cost calculation to use price × quantity. Convert input values to numbers safely and add tests for quantity values greater than 1.

---

## Bug #2: Negative gift quantity is accepted and creates a negative charge

- **Where:** Send Gift tab - Quantity input and Send Gift action

- **Steps to reproduce:**
  1. Open the live admin app.
  2. Go to the Send Gift tab.
  3. Enter Room ID as 12345.
  4. Select Box gift with price 500 coins.
  5. Enter quantity as -1.
  6. Enter Winners count as 2.
  7. Click Send Gift.
  8. Observe the Total cost and success toast message.

- **Expected behavior:**  
  The quantity field should reject negative values.  
  The Send Gift action should be blocked and a validation message should be shown.

- **Actual behavior:**  
  The form accepts quantity = -1.  
  The total cost is displayed as 499 coins.  
  After clicking Send Gift, the app shows a success toast: Gift sent! Charged -500 coins.

- **Screenshot:**  
  screenshots/BUG-002-negative-quantity-accepted.png

- **Severity:** Critical

- **Severity reasoning:**  
  This is Critical because accepting negative quantity can create a negative charge, which may increase wallet balance or corrupt wallet transactions.

- **Suspected root cause:**  
  Frontend validation and business logic issue. The quantity input is not restricted to positive integers, and the send gift calculation does not block negative values before processing.

- **Proposed fix:**  
  Validate quantity before sending the gift. Only allow positive integers within the accepted range, disable the Send Gift button for invalid values, and add a backend validation rule to reject quantity less than 1.

---

## Bug #3: Refresh Wallet button throws console error

- **Where:** Wallet tab - Refresh Wallet button

- **Steps to reproduce:**
  1. Open the live admin app.
  2. Go to the Wallet tab.
  3. Open browser DevTools.
  4. Go to the Console tab.
  5. Click Refresh Wallet.
  6. Observe the console output.

- **Expected behavior:**  
  Clicking Refresh Wallet should refresh the wallet balance without throwing any JavaScript errors.  
  If refresh fails, the user should see a clear error message and the app should handle it safely.

- **Actual behavior:**  
  The browser console shows a JavaScript error:  
  TypeError: Cannot read properties of undefined (reading 'coins')  
  The error points to buggy-admin/:384:40.

- **Screenshot:**  
  screenshots/BUG-003-refresh-wallet-console-error.png

- **Severity:** Medium

- **Severity reasoning:**  
  This is Medium because the bug does not fully block using the page, but it breaks the wallet refresh action and exposes an unhandled JavaScript error.

- **Suspected root cause:**  
  Frontend state/data handling issue. The refresh logic is probably trying to read a coins property from an undefined wallet or user object.

- **Proposed fix:**  
  Add a null/undefined check before reading the coins value. Make sure the refresh function uses the correct wallet state object and shows a user-friendly error if the data is unavailable.

---

## Bug #4: Header wallet balance does not sync with Wallet tab balance

- **Where:** Header wallet badge and Wallet tab

- **Steps to reproduce:**
  1. Open the live admin app.
  2. Go to the Send Gift tab.
  3. Send a gift using quantity = -1.
  4. Go to the Wallet tab.
  5. Compare the Wallet tab current balance with the top-right header wallet badge.

- **Expected behavior:**  
  The wallet balance should be consistent everywhere in the UI.  
  If the Wallet tab shows 10500 coins, the header wallet badge should also show 10500 coins.

- **Actual behavior:**  
  The Wallet tab shows Current balance: 10500 coins.  
  The top-right header badge still shows 10000 coins.

- **Screenshot:**  
  screenshots/BUG-004-header-wallet-balance-not-synced.png

- **Severity:** Medium

- **Severity reasoning:**  
  This is Medium because it creates inconsistent wallet information in the UI and can confuse the admin about the real balance.

- **Suspected root cause:**  
  Frontend state management issue. The Wallet tab balance is updated, but the shared header balance state is not refreshed after wallet changes.

- **Proposed fix:**  
  Store wallet balance in one shared state source and update all wallet UI components after any wallet-affecting action.

---

## Bug #5: Banned user remains visible as active user after successful ban action

- **Where:** Users tab - Ban button and users table

- **Steps to reproduce:**
  1. Open the live admin app.
  2. Go to the Users tab.
  3. Click Ban for user ID 101.
  4. Observe the toast message.
  5. Check whether the user row is removed or updated in the Users table.

- **Expected behavior:**  
  After a successful ban action, the user should no longer appear as an active user in the Users table, or the row should clearly show that the user is banned.  
  The Ban button should also be disabled, removed, or changed to another action such as Unban.

- **Actual behavior:**  
  A success toast appears saying: User banned (id 101).  
  However, the user still appears in the Users table and the Ban button is still available.

- **Screenshot:**  
  screenshots/BUG-005-banned-user-still-visible.png

- **Severity:** Medium

- **Severity reasoning:**  
  This is Medium because the action appears successful, but the UI state does not reflect the result. This can confuse admins and may lead them to repeat the same action.

- **Suspected root cause:**  
  Frontend state update issue. The ban action likely updates a banned users list or shows a toast, but the Users table is not re-rendered or filtered after the state change.

- **Proposed fix:**  
  After a successful ban action, update the users table state by removing the banned user or marking the user as banned. Also disable or change the Ban button after the action succeeds.

---

## Bug #6: Newly banned user is not added to the Banned Users list

- **Where:** Bans tab - Banned Users table

- **Steps to reproduce:**
  1. Open the live admin app.
  2. Go to the Users tab.
  3. Click Ban for user ID 101.
  4. Confirm that the success toast appears.
  5. Go to the Bans tab.
  6. Check whether user ID 101 appears in the Banned Users table.

- **Expected behavior:**  
  After banning user ID 101, the user should appear in the Banned Users table with the correct ID, username, reason, and banned timestamp.

- **Actual behavior:**  
  The Bans tab still only shows the existing banned user ID 901.  
  The newly banned user ID 101 is not added to the Banned Users table.

- **Screenshot:**  
  screenshots/BUG-006-banned-user-not-added-to-bans-list.png

- **Severity:** Medium

- **Severity reasoning:**  
  This is Medium because the ban action appears successful, but the ban state is not reflected in the Banned Users list. This can make admins unsure whether the user was actually banned.

- **Suspected root cause:**  
  Frontend state/data synchronization issue. The ban action likely updates a temporary message or local state, but the Bans table data source is not updated after the ban.

- **Proposed fix:**  
  After a successful ban action, update the banned users list state immediately or re-fetch the banned users data. Add a test to confirm the banned user appears in the Bans tab after the ban action.

---

## Bug #7: Helper text has low contrast and is hard to read

- **Where:** Section helper text under page titles, for example Users tab and Bans tab

- **Steps to reproduce:**
  1. Open the live admin app.
  2. Go to the Users tab.
  3. Look at the helper text under the User Management title.
  4. Go to the Bans tab.
  5. Look at the helper text under the Banned Users title.

- **Expected behavior:**  
  Helper text should be readable and have enough contrast against the white card background.

- **Actual behavior:**  
  The helper text is displayed in a very light gray color, which makes it difficult to read.

- **Screenshot:**  
  screenshots/BUG-007-low-contrast-helper-text.png

- **Severity:** Low

- **Severity reasoning:**  
  This is Low because it does not block the main functionality, but it affects readability and accessibility.

- **Suspected root cause:**  
  Frontend styling issue. The text color used for helper descriptions is too close to the white background.

- **Proposed fix:**  
  Increase the contrast of helper text by using a darker gray color that meets accessibility contrast recommendations. Also add a visual/accessibility review for secondary text styles.
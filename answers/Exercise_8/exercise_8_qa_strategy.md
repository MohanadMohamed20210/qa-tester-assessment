# QA Strategy Decisions

## Scenario 1: Greenfield automation for 400 Laravel endpoints

I would not start by automating the 400 endpoints one by one. This will take a long time, and many of the tests will probably end up being weak.

I would first look at the risky parts of the product. For example, login, permissions, wallet balance, sending gifts, and admin actions should be tested before normal read-only endpoints.

My first goal would be to build a small API smoke suite that can run in CI. It should cover login, current user, and one important business flow. After that, I would add more tests around the wallet and gift flow because mistakes there can affect coins and user balance.

For Laravel, I would also use PHPUnit or Pest for some backend logic. Some checks are better close to the code, especially validation rules, wallet calculation, and database changes.

For example, in the gift flow I would cover valid send gift, insufficient balance, invalid quantity, unauthorized request, and duplicate request with the same idempotency key.

I would also add schema checks for important responses, because frontend and mobile teams depend on the response structure.

My approach is to start small, but with useful tests. I prefer a smaller suite that checks real business behavior instead of a big suite that only checks status code 200.

---

## Scenario 2: Flaky test suite

If the test suite is flaky, I would not keep rerunning it until it passes. After some time, the team will stop trusting the result.

I would start with the tests that fail the most in CI. Usually, I first check if the test is waiting in a bad way, especially in UI tests. Fixed waits like Thread.sleep are one of the first things I would remove.

For UI automation, I would use explicit waits and improve the locators if they are weak. For API tests, I would make sure the test data is clear and not reused in a way that affects another test.

If a test is important but very unstable, I would move it out of the blocking pipeline for a short time until it is fixed. I would not delete it, but I also would not let it block every merge while we already know it is flaky.

The main thing for me is to make the CI result trusted again. A failed test should mean something. If the team starts ignoring red builds, the automation loses its value.

---

## Scenario 3: Release deadline with incomplete mobile regression

If mobile regression is not complete and the release is close, I would first check what changed in this release.

If the change touched login, wallet, payment, or a main user flow, I would not be comfortable releasing without testing these areas. These are not low-risk parts of the app.

If the missing coverage is only around small UI screens, then maybe the release can continue, but the risk should be clear to the team.

My minimum check before release would be the main user journey, login, and any money-related flow. I would also check at least one Android and one iOS device if the app supports both.

If the critical flow is not tested, I would say clearly that I do not recommend release yet. If the business still wants to release, I would suggest reducing the risk by using a small rollout or feature flag.

For me, QA should not just say pass or fail. I should explain what I tested, what is still not tested, and what risk the team is accepting.

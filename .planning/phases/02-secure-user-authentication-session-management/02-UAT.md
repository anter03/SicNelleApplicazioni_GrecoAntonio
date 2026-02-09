# Phase 2: User Acceptance Testing

## Test Plan

| Test ID | Description | Expected Result | Actual Result | Status |
|---|---|---|---|---|
| 2.1 | Register a new user. | User is created successfully. | | Pending |
| 2.2 | Log in with the new user. | User is logged in successfully. | | Pending |
| 2.3 | Check session cookie attributes. | `HttpOnly`, `Secure`, and `SameSite` flags are set. | | Pending |
| 2.4 | Test session fixation. | Old session is invalidated after login. | | Pending |
| 2.5 | Verify secure credential handling. | Passwords are not stored in memory as strings. | | Pending |

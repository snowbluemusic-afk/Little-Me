# Private presence relay

This is an optional, self-hosted service, not a deployed Little Me cloud account. It stores only each person's current home/away status and an update timestamp; unknown deletes the row. Status expires after 72 hours without a refresh. No location coordinates, history or personal names are accepted. Each person has a separate write key; a third shared key reads the two statuses. The server does not log requests. Do not expose it over plain HTTP.

## Setup
1. Use a trusted server or hosting account that you control. Generate three distinct secrets locally with `python -c "import secrets; print(secrets.token_urlsafe(32))"` (three times).
2. Set environment variables LITTLEME_SELF_TOKEN, LITTLEME_PARTNER_TOKEN, LITTLEME_READ_TOKEN to those secrets. Never commit them to GitHub. Set LITTLEME_DB to a persistent writable file.
3. Run `python relay.py` (defaults to localhost:8080), or build the supplied Dockerfile. Configure your own HTTPS reverse proxy with a valid certificate, persistent storage, and network/rate limits. Bind Docker's port to localhost behind the proxy.
4. In Little Me > Presence setup, each Android participant selects their own role, enters the HTTPS origin, shared read key and their own write key, and explicitly opts in. Never give one person the other's write key. At home, grant precise/background location and tap Set home here. Manual Home/Away controls can be used without location permissions. Geofence updates may take a few minutes; a stale state becomes unknown.
5. For an iPhone: with the iPhone owner's permission, create two personal Shortcuts automations (Arrive and Leave, using the Home location), choose Run Immediately if available, and use Get Contents of URL with POST to https://YOUR-HOST/v1/presence/partner, Authorization: Bearer YOUR-PARTNER-WRITE-KEY, JSON body state = home or away. Optionally add a daily automation to refresh the status using the owner's explicit choice, or run the shortcut manually. Do not send coordinates. Test both automations on the actual device; iOS versions and permissions differ.
6. To stop sharing, use the Android disconnect option or send state unknown from the iPhone, then disable/delete the automation. A network failure may prevent immediate remote deletion; the server operator can rotate that participant's write key and delete their database row. Rotate the shared read key if access must be revoked. A disconnected app must not treat an unreachable relay as a confirmed away state.

Run tests: `cd relay && python -m unittest -v test_relay`.

The Android app uses Google Play services geofencing; it works without the relay for that phone's local presence. The relay is needed for live cross-device sharing. An iPhone companion app is not included. No Life360 credentials or unsupported APIs are used.

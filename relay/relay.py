"""Little Me private home/away relay. Run behind an HTTPS reverse proxy."""
import hmac, json, os, sqlite3, time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import urlsplit

TTL = 72 * 3600
ROLES = ("self", "partner")
TOKENS = {role: os.environ.get("LITTLEME_" + role.upper() + "_TOKEN", "") for role in ROLES}
READ = os.environ.get("LITTLEME_READ_TOKEN", "")
DB = os.environ.get("LITTLEME_DB", "presence.sqlite3")

def valid_config():
    values = [READ, *TOKENS.values()]
    return all(len(x) >= 32 for x in values) and len(set(values)) == 3

def connect():
    conn = sqlite3.connect(DB, timeout=5)
    conn.execute("CREATE TABLE IF NOT EXISTS presence (role TEXT PRIMARY KEY, state TEXT NOT NULL, updated INTEGER NOT NULL)")
    return conn

def read_states(now=None):
    now = int(time.time()) if now is None else now
    result = dict.fromkeys(ROLES, "unknown")
    with connect() as db:
        for role, state, updated in db.execute("SELECT role,state,updated FROM presence"):
            if role in ROLES and 0 <= now-updated <= TTL:
                result[role] = state
    return result

def write_state(role, state, now=None):
    if role not in ROLES or state not in ("home", "away", "unknown"):
        raise ValueError("Invalid status")
    now = int(time.time()) if now is None else now
    with connect() as db:
        if state == "unknown":
            db.execute("DELETE FROM presence WHERE role=?", (role,))
        else:
            db.execute("INSERT INTO presence(role,state,updated) VALUES(?,?,?) ON CONFLICT(role) DO UPDATE SET state=excluded.state,updated=excluded.updated", (role,state,now))

class Handler(BaseHTTPRequestHandler):
    def log_message(self, *args):
        # Never log presence, authorization, or URLs.
        pass
    def reply(self, code, data):
        body=json.dumps(data,separators=(",",":")).encode()
        self.send_response(code)
        self.send_header("Content-Type","application/json")
        self.send_header("Cache-Control","no-store")
        self.send_header("Content-Length",str(len(body)))
        self.end_headers()
        self.wfile.write(body)
    def authorized(self, token):
        supplied=self.headers.get("Authorization","")
        return bool(token) and hmac.compare_digest(supplied.encode(),("Bearer "+token).encode())
    def do_GET(self):
        if self.path!="/v1/presence":
            return self.reply(404,{"error":"not found"})
        if not self.authorized(READ):
            return self.reply(401,{"error":"unauthorized"})
        self.reply(200,read_states())
    def do_POST(self):
        path=self.path
        role=next((r for r in ROLES if path=="/v1/presence/"+r),None)
        if role is None:
            return self.reply(404,{"error":"not found"})
        if not self.authorized(TOKENS[role]):
            return self.reply(401,{"error":"unauthorized"})
        try:
            size=int(self.headers.get("Content-Length","0"))
            if not 0<size<=256:
                raise ValueError()
            value=json.loads(self.rfile.read(size))
            if not isinstance(value,dict) or set(value)!={"state"} or value["state"] not in ("home","away","unknown"):
                raise ValueError()
            write_state(role,value["state"])
        except (ValueError,TypeError,KeyError):
            return self.reply(400,{"error":"invalid status"})
        self.reply(200,{"ok":True})

if __name__=="__main__":
    if not valid_config():
        raise SystemExit("Configure three distinct random tokens (at least 32 characters).")
    with connect(): pass
    ThreadingHTTPServer((os.environ.get("LITTLEME_BIND","127.0.0.1"),int(os.environ.get("PORT","8080"))),Handler).serve_forever()

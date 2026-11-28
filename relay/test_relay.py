import os, tempfile, unittest, threading, urllib.request, urllib.error, json
import relay
from http.server import ThreadingHTTPServer

class RelayTest(unittest.TestCase):
    def setUp(self):
        self.tmp=tempfile.TemporaryDirectory()
        relay.DB=os.path.join(self.tmp.name,"test.sqlite3")
        relay.READ="r"*40;relay.TOKENS={"self":"s"*40,"partner":"p"*40}
        self.server=ThreadingHTTPServer(("127.0.0.1",0),relay.Handler)
        self.thread=threading.Thread(target=self.server.serve_forever,daemon=True);self.thread.start()
        self.base="http://127.0.0.1:"+str(self.server.server_port)
    def tearDown(self):
        self.server.shutdown();self.server.server_close();self.thread.join();self.tmp.cleanup()
    def request(self,path,token,body=None):
        data=None if body is None else json.dumps(body).encode()
        req=urllib.request.Request(self.base+path,data=data,headers={"Authorization":"Bearer "+token,"Content-Type":"application/json"})
        try:
            with urllib.request.urlopen(req) as r: return r.status,json.load(r)
        except urllib.error.HTTPError as e:return e.code,json.load(e)
    def test_isolation_and_revocation(self):
        self.assertTrue(relay.valid_config())
        self.assertEqual(self.request("/v1/presence","bad")[0],401)
        self.assertEqual(self.request("/v1/presence/partner",relay.TOKENS["self"],{"state":"home"})[0],401)
        self.assertEqual(self.request("/v1/presence/self",relay.READ,{"state":"home"})[0],401)
        self.assertEqual(self.request("/v1/presence/self",relay.TOKENS["self"],{"state":"home","lat":1})[0],400)
        self.assertEqual(self.request("/v1/presence/self",relay.TOKENS["self"],{"state":"home"})[0],200)
        self.assertEqual(self.request("/v1/presence",relay.READ)[1],{"self":"home","partner":"unknown"})
        self.request("/v1/presence/self",relay.TOKENS["self"],{"state":"unknown"})
        self.assertEqual(self.request("/v1/presence",relay.READ)[1]["self"],"unknown")
    def test_expiry(self):
        relay.write_state("partner","away",100)
        self.assertEqual(relay.read_states(101)["partner"],"away")
        self.assertEqual(relay.read_states(101+relay.TTL)["partner"],"unknown")
if __name__=="__main__":unittest.main()

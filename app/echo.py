#!/usr/bin/python
import socket, traceback, time, struct
import sys

host = ''
port = 36500

if len(sys.argv) > 1:
    dstHost = sys.argv[1]
    dstPort = int(sys.argv[2])
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.settimeout(3)
    data = 'hello, WRTNode'
    s.sendto(data, (dstHost, dstPort))
    msg, addr = s.recvfrom(8192)
    print repr(msg)
else:
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.bind((host, port))
    while 1:
        try:
            message, address = s.recvfrom(8192)
            print "Get data from", address, repr(message)
            #pack data
            reply = struct.pack("!I", 0xaaaffa01);
            cmd = 1
            reply = reply + struct.pack("!H", cmd)
            model = "EM-2009"
            sn = "012457"
            datalen = 1 + len(model) + 1 + len(sn)
            reply = reply + struct.pack("!H", datalen)
            reply = reply + struct.pack("!B", len(model)) + model
            reply = reply + struct.pack("!B", len(sn)) + sn
            sum = 0x233
            reply = reply + struct.pack("!H", sum)
            s.sendto(reply, address)
        except (KeyboardInterrupt, SystemExit):
            raise
        except:
            traceback.print_exc()

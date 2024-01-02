import socket as s
import pickle
from PathCalculation import *


def client_sender_udp(_parent_router: NodeRouter):
    client_socket = s.socket(s.AF_INET, s.SOCK_DGRAM)
    while True:
        for child in _parent_router.neigh:
            _parent_router.msg.timestamp = dt.datetime.now().timestamp()
            message_to_send = pickle.dumps(_parent_router.msg)
            server_port = int(child.port)
            client_socket.sendto(message_to_send, (server_name, server_port))
        time.sleep(update_interval)
        _parent_router.msg.increment_sequence_number()

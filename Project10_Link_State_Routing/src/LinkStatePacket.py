import datetime as dt
from NodeRouter import NodeRouter


class LinkStatePacket:
    def __init__(self, sender: NodeRouter):
        self.port = sender.port
        self.name = sender.name
        self.neigh = sender.neigh
        self.seq_num = 0
        self.timestamp = dt.datetime.now().timestamp()
        self.last_sent_message = sender.name

    def increment_sequence_number(self):
        self.seq_num += 1

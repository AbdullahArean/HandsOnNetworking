from collections import defaultdict
from math import inf
from Neighbours import Neighbours
from typing import Dict, List, Any


class NodeRouter:
    def __init__(self, name, port, neighbours_list):
        self.name = name
        self.port = port
        self.neigh = neighbours_list
        self.msg = None
        self.preve_sent_msg_seq = defaultdict(int)
        self.global_time_st = defaultdict(float)
        self.global_routers = defaultdict(list)

    def neighbour_add(self, neighbour):
        self.neigh.append(neighbour)
        self.global_routers[self.name].append(neighbour)

    def mesg_set(self, msg):
        self.msg = msg

    def add_prev_seq(self, msg):
        self.preve_sent_msg_seq[msg.name] = msg.seq_num

    def chk_prev_seq(self, msg):
        return self.preve_sent_msg_seq[msg.name] != msg.seq_num

    def add_timestamp(self, msg):
        self.global_time_st[msg.name] = msg.timestamp

    def chk_timestamp(self, msg):
        return self.global_time_st[msg.name] != msg.timestamp

    def update_global_routers(self, msg):
        if len(self.global_routers[msg.name]) > 0:
            for neighbour in msg.neigh:
                present = False
                for present_neighbour in self.global_routers[msg.name]:
                    if present_neighbour.port == neighbour.port \
                            and present_neighbour.name == neighbour.name \
                            and present_neighbour.distance == neighbour.distance:
                        present = True
                if not present:
                    self.global_routers[msg.name].append(neighbour)
        else:
            for neighbour in msg.neigh:
                self.global_routers[msg.name].append(neighbour)

    def check_neighbour_alive(self, msg):
        for neighbour in msg.neigh:
            if neighbour.name == self.name:
                present = False
                for my_neighbour in self.neigh:
                    if my_neighbour.name == msg.name:
                        present = True
                if not present:
                    to_be_added = Neighbours(msg.name, msg.port, neighbour.distance)
                    self.neigh.append(to_be_added)
                    self.global_routers[self.name].append(to_be_added)

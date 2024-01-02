import sys
import threading
from Neighbours import Neighbours
from ClientReceiver import client_sender_udp
from ServerReceiverSender import *
from Variables import *

if len(sys.argv) == arg_num:
    f = open(sys.argv[file_name], "r")
    line_counter = 0
    number_of_neighbour = 0
    parent_router: NodeRouter
    list_file = []
    for line in f:
        list_file.append(line.split())
    for i in range(len(list_file)):
        # First line will always be Parent router
        if i == 0:
            parent_router = NodeRouter(list_file[i][name_router], list_file[i][parent_port], [])

        # Second line will always be the number of neigh
        elif i == 1:
            number_of_neighbour = list_file[i]  # TODO not using right now

        # From 3 onwards it will be the child routers
        elif i > 1:
            child_router = Neighbours(list_file[i][name_router], list_file[i][port_child], list_file[i][distance])
            parent_router.neighbour_add(child_router)
        line_counter += 1

    parent_router.mesg_set(LinkStatePacket(parent_router))
    client_thread_toreceive = threading.Thread(target=client_sender_udp, args=(parent_router,))
    server_thread_tosend = threading.Thread(target=server_receiver_sender_udp, args=(parent_router,))
    distance_calculation_thread = threading.Thread(target=calculate_paths_activator, args=(parent_router,))
    check_alive = threading.Thread(target=check_alive, args=(parent_router,))
    client_thread_toreceive.start()
    server_thread_tosend.start()
    distance_calculation_thread.start()
    check_alive.start()

import time
import sys
import os
import threading
from socket import *
import math
from PossiblePath import possible_path
from AdjacentNode import adjacent_node

SUSPEND_TIMEOUT = 3

router_id = str()
router_port = int()
router_filename = str()
router_neighbours = dict()
router_routes = dict()
lockthread = threading.Lock()

# Create a packet with the current distance vector information and send it to all neighbors
def pkt_send_creation(dest_id, cost):
    # Initialize the distance vector with the router's own ID
    distvector = str(router_id)

    # If a cost is given, append the neighbor's link cost to the distance vector
    if cost: 
        distvector += ' ' + str(router_neighbours[dest_id].linkCost)

    # Append the distances to all other nodes in the network to the distance vector
    distvector += '\n'
    for id, path in router_routes.items():
        if path.nxthop == dest_id:
            # If the node is unreachable via this neighbor, set the distance to infinity
            distvector += str(id) + " " + str(math.inf) + '\n'
        else:
            # Otherwise, include the distance via the next hop in the distance vector
            distvector += str(id) + " " + str(path.dis) + '\n'

    # Convert the distance vector to a byte string and return it
    return bytes(distvector, 'utf-8')

# Send distance vector information to all neighbors
def distancevectorshare(cost):
    # Create a UDP socket for sending packets
    sendSocket = socket(AF_INET, SOCK_DGRAM)

    # Acquire the lock to prevent other threads from accessing shared data
    lockthread.acquire()

    # Send a packet to each neighbor with the current distance vector information
    for id, neighbour in router_neighbours.items():
        sendSocket.sendto(pkt_send_creation(id, cost), ('localhost', neighbour.port))

    # Release the lock
    lockthread.release()

    # Close the socket
    sendSocket.close()

# Print the current distance vector table
def printdistancetable():
    # Initialize the first row of the table with node IDs
    string = '\t'
    for id in sorted(router_routes.keys()): 
        string += '\t' + id
    print(string)

    # Initialize the second row of the table with distances to each node
    string = router_id + '\t'
    for id in sorted(router_routes.keys()):
        string += '\t' + str("%.1f" % router_routes[id].dis)
    print(string)

    # Print the link costs and distances to each node via each neighbor
    for id in sorted(router_neighbours.keys()):
        string = id+'\t'+str(router_neighbours[id].linkCost)
        for key2 in sorted(router_neighbours[id].paths.keys()):
            string += '\t' + str("%.1f" % router_neighbours[id].paths[key2].dis)
        print(string)
    print('')
def checktimeout():
    while True:  # Run indefinitely until interrupted
        time.sleep(1)  # Wait for 1 second before checking for timeout
        for id, neighbour in router_neighbours.items():  # Loop through the router's neighbours
            s = socket(AF_INET, SOCK_DGRAM)  # Create a new UDP socket
            try:
                s.bind(('localhost', neighbour.port))  # Bind the socket to the neighbour's port
                s.close()  # Close the socket
                if router_neighbours[id].linkCost != math.inf:  # If the neighbour is not already disconnected
                    lockthread.acquire()  # Acquire a lock to avoid race conditions
                   
                    # Set the distance to the neighbour and the neighbour's link cost to infinity
                    router_routes[id].dis = math.inf
                    neighbour.linkCost = math.inf
                    neighbour.timeout = time.time()  # Set the neighbour's timeout to the current time
                   
                    # Set the distance to all routes that use the neighbour as the next hop to infinity
                    for key2, item2 in router_routes.items():
                        if item2.nxthop == id:
                            item2.dis = math.inf
                           
                    lockthread.release()  # Release the lock
                    distancevectorshare(False)  # Update the distance vectors of all neighbours
                    threading.Timer(SUSPEND_TIMEOUT, target=distancevectoralgorithm).start()  # Restart the distance vector algorithm after a delay
            except:
                pass  # Ignore any exceptions that occur while binding the socket
def threadlisten():
    # Create a UDP socket and bind it to the local address and router port
    socketlisten = socket(AF_INET, SOCK_DGRAM)
    socketlisten.bind(('localhost', router_port))

    # Continuously listen for incoming messages and process them
    while 1:
        # Receive a message from the socket
        message, socketAddress = socketlisten.recvfrom(2048)

        # Split the message into lines and extract the source router's ID
        lines = str(message)[2:len(str(message))-1].split('\\n')
        firstLine = lines[0].split()
        source = firstLine[0]

        # Update the source router's timeout value to indicate that it is still active
        router_neighbours[source].timeout = -1.0

        # If the first line contains a link cost, update the source router's link cost
        if len(firstLine) > 1:
            router_neighbours[source].linkCost = float(firstLine[1])
            router_neighbours[source].timeout = -1

        # Acquire a lock to synchronize access to shared data structures
        lockthread.acquire()

        # Iterate over the remaining lines to extract information about the available paths to other routers
        for i in range(1, len(lines)):
            if lines[i] == '':
                continue
            tokens = lines[i].split()
            newPath = possible_path(float(tokens[1]),'direct')

            # If a path to a new destination router is discovered, create a new node in the routing table
            if tokens[0] not in router_neighbours[source].paths:
                newNode(tokens[0])

            # If the discovered path is different from the existing path, update the routing table
            if not router_neighbours[source].paths[tokens[0]].equals(newPath):
                router_neighbours[source].paths[tokens[0]] = newPath

        # Start a new thread to run the distance vector algorithm
        threading.Thread(target=distancevectoralgorithm).start()

        # Release the lock to allow other threads to access the shared data structures
        lockthread.release()


def newNode(name):
    global router_neighbours

    # Create a new node in the routing table with an infinite cost and a direct link
    p = possible_path(math.inf, 'direct')
    router_routes[name] = p

    # Add the new node to the routing table of each neighbor
    for id, neighbour in router_neighbours.items():
        neighbour.paths[name] = p


def distancevectoralgorithm():
    global router_routes # accessing global variable

    isChanged = False # flag to keep track of whether the router_routes has changed or not

    lockthread.acquire() # acquiring the lock before accessing the shared variable
    for id, route in router_routes.items():
        m_list = list()
        if id == router_id:
            continue # skip current router

        # checking for timeouts and suspending the router for SUSPEND_TIMEOUT duration
        if id in router_neighbours:
            if time.time() > router_neighbours[id].timeout and time.time() < router_neighbours[id].timeout + SUSPEND_TIMEOUT:
                router_routes[id] = possible_path(math.inf, 'direct') # setting the route to infinity if the neighbour is timed out
                continue
            else:
                m_list.append(possible_path(router_neighbours[id].linkCost, 'direct')) # adding the direct neighbour cost

        # checking for all neighbours
        for id2, neighbour in router_neighbours.items():
            p = possible_path(router_neighbours[id2].linkCost + neighbour.paths[id].dis, id2) # calculating the path to the neighbour
            m_list.append(p)

        m = min(m_list, key = lambda x: x.dis) # selecting the minimum path

        # updating the router_routes if the path has changed
        if not router_routes[id].equals(possible_path(m.dis,m.nxthop)):
            router_routes[id] = possible_path(m.dis,m.nxthop)
            isChanged = True

    lockthread.release() # releasing the lock after accessing the shared variable

    # sharing the changes with the neighbours if the router_routes has changed
    if isChanged:
        distancevectorshare(False)

def UserPrompt():
    option = 0
    while(1):
        print('\n****ROUTER ' + router_id + '****\n')

        option = int(input('1: Display Costs of Reaching Other Routers.\n2: Display Distance Vector Table of The Router.\n3: Edit Cost of Link with adjacent_node.\n4: Exit\nPlease Enter Your Choice:(1/2/3/4): '))
        if option == 1:
            print('Destination\tNext Hop\tDistance')
            for id, route in sorted(router_routes.items()):
                if id != router_id:
                    print('     ' + id + '\t\t' + route.nxthop + '\t\t' + str("%.1f" % route.dis))
        elif option == 2:
            printdistancetable()
        elif option == 3:
            string = 'Neighbours:'
            for id in sorted(router_neighbours.keys()):
                string += ' ' + id
            print(string)
            toEdit = input('Enter which link to edit: ')
            newDistance = float(input('Enter new dis for ' + toEdit + ': '))
            router_neighbours[toEdit].linkCost = newDistance

            sendSocket = socket(AF_INET, SOCK_DGRAM)
            lockthread.acquire()
            sendSocket.sendto(pkt_send_creation(toEdit, True), ('localhost', router_neighbours[toEdit].port))
            lockthread.release()
            sendSocket.close()

            threading.Thread(target=distancevectoralgorithm).start()

        elif option == 4:
            os._exit(-1)
        else :
            continue;

# Check if the script is being run as the main program
if __name__ == '__main__':
    try:
        # Get the router filename from the command-line arguments
        router_filename = sys.argv[1]
        # Read the first line of the file to get the router ID and port number
        with open(router_filename, 'r') as f:
            first_line = f.readline()
            router_id, router_port = first_line.split()
            router_port = int(router_port)
    except ValueError or IndexError:
        # If the command-line arguments are incorrect, print an error message and exit
        print('Incorrect command-line arguments.\npython DistanceVectorRouting.py <filename>')
        exit(0)

    # Print the router ID to indicate that the script has started
    print("Router "+router_id)

    # Initialize the router's route to itself with a cost of 0
    router_routes[router_id] = possible_path(0, 'direct')

    # Read the rest of the file to determine the router's neighbors and their costs
    file = open(router_filename)
    lines = file.readlines()
    for i in range(2, len(lines)):
        tokens = lines[i].split()
        # Create a new adjacent node object for each neighbor
        router_neighbours[tokens[0]] = adjacent_node(float(tokens[1]), int(tokens[2]), -1)
        # Initialize the route to the neighbor with a direct link cost
        router_routes[tokens[0]] = possible_path(float(tokens[1]), 'direct')
    # Initialize the paths to all other nodes as infinite cost
    for id, neighbour in router_neighbours.items():  
        p = possible_path(math.inf, 'direct')
        for id2, neighbour2 in router_neighbours.items():
            neighbour.paths[id2] = p
        # Initialize the path to the router itself with a direct link cost of 0
        neighbour.paths[router_id] = possible_path(0, 'direct')

    # Start four threads for sending and receiving distance vectors, prompting the user for input, and checking for timeouts
    threading.Thread(target=distancevectorshare, kwargs={'cost': True}).start()  #temporary thread for sending DV
    threading.Thread(target=threadlisten).start()
    threading.Thread(target=UserPrompt).start()
    threading.Thread(target=checktimeout).start()
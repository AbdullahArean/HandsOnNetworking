# Link-State-Routing-Algorithm-Implemented
Welcome to the Link State Routing Protocol implementation project using Python. This project aims to demonstrate how to build a router node that can communicate with other router nodes, exchange information, and calculate the shortest path between routers using the Link State Routing protocol.

## Table of contents

- [Features](#features)
- [Installation](#installation)
- [Implementation](#implementation)
- [Results](#results)
- [Conclustion](#conclusion)


## Features
- Implements Link State Routing Protocol, which means every router has complete knowledge of the network topology.
- Uses Dijkstra’s shortest path algorithm to calculate the shortest path to every other router in the network.
- Allows random change of neighbour link cost which simulates a real-world network.
- Uses pickle for serialization and de-serialization of objects.
- Uses threading to run the routing protocol calculation periodically.
- Maintains a sequence number to keep track of the link-state packets.
- Uses a timestamp to keep track of the last time a router sent a link-state packet.
- Keeps track of the previous sequence number and timestamp of link-state packets received from neighbouring routers.
- Maintains a global routers dictionary to keep track of all routers in the network and their neighbours.
- Prints out the current state of the network topology periodically.

## Installation
In recent versions of Mac OS X, Python is pre-installed. However, for older versions, the latest version of Python can be downloaded from the official Python website. Similarly, in Linux, Python is usually pre-installed, but if it is not, the distribution's package manager can be used to install Python. For example, on Ubuntu and Debian, the command "sudo apt-get install python3" can be used. In case of Windows, the latest version of Python can be downloaded from the official Python website and installed on the system.

### Router Creation
To run router with your desired topology, make a configX.txt file with the following, goto src folder (cd src)
```
<router-name> <router-port>
<number-of-neighbour-router>
<neighbour-name> <link-cost> <neighbour-port>
......
.......

```
after that run the following command in a separate terminal tab to run that router and on previously specified port.
```
python3 LinkStateRouting.py configX.txt
```
Any number of router can be created.
### Testing With 6 Routers
For our testing purpose, created 6 routers, and wrote a script named "testlinux.py" to run all of them in linux and observe the routing algorithm.
```
cd Test
python3 testlinux.py
```
for windows machine run the following command,
```
cd Test
python3 testwindows.py
```
for mac machine run the following command,
```
cd Test
python3 testwindows.py
```
## Implementation
src contains all the classed and functions implemented in different python file. **LinkStateRouting.py** script that contains the implementation of a router simulation. It uses classes and functions from different python files to create Link State Routing Algorithm Simulation. Description about different classes and functions are given below:

- **NodeRouter class**: This class represents a router node in the network. It has attributes like **name**, **port**, **neigh** (list of neighbour nodes), **msg** (last received message), **preve_sent_msg_seq** (dictionary storing the previous sequence number of sent messages), **global_time_st** (dictionary storing the global time stamp of received messages), and **global_routers** (dictionary storing the global routers information). The class has methods like **neighbour_add** (to add a new neighbour node to the router), **mesg_set** (to set the last received message), **add_prev_seq** (to add the previous sequence number of sent messages to the dictionary), **chk_prev_seq** (to check if the received message's sequence number is not the same as the previous one), **add_timestamp** (to add the global time stamp of the received message to the dictionary), **chk_timestamp** (to check if the received message's time stamp is not the same as the global time stamp), **update_global_routers** (to update the global routers information with the received message's neighbour information), and **check_neighbour_alive** (to check if the received message's neighbour is still alive and add it to the current router's neighbour list if it is).

- **LinkStatePacket class**: This class represents the link state packet sent by a router to its neighbours. It has attributes like **port**,**name**, **neigh** (list of neighbours), **seq_num** (sequence number of the packet), **timestamp** (time stamp of the packet), and **last_send** (name of the last router that sent the packet). The class has a method **increment_sequence_number** (to increment the sequence number of the packet).

- **Neighbours class**: This class represents a neighbour node of a router. It has attributes like **name, port, and distance** (cost of the link between the two nodes).

- **Edge class**: This class represents an edge (link) between two nodes in the network. It has attributes like **start, end, and weight** (cost of the link).

- **Graph class**: This class represents the network topology as a graph. It has attributes like **global_routers** (dictionary of router nodes and their neighbours) and **graph** (dictionary of edges between the routers). The class has a method parse (to create the graph from the global routers information).

- **calculate_paths_activator function**: This function is used to periodically trigger the dijkstra_calculate_path function to calculate the least cost path to all nodes in the network. It also **randomly** changes the cost of one of the links in the network.
```
# select a this router name from the global_routers dictionary
        router_name = parent_router.name
        # select a random neighbour of the router
        neighbour_index = random.randint(0, len(parent_router.global_routers[router_name])-1)
        # change the cost of the neighbour's link
        new_cost = random.randint(1, 10)
        parent_router.global_routers[router_name][neighbour_index].distance = new_cost
```
- **dijkstra_calculate_path function**: This function calculates the least cost path to all nodes in the network using Dijkstra's algorithm. It uses a calculation_table dictionary to keep track of the least cost and visited status of each router node. It creates a new graph object from the parent router's global routers information and initializes the calculation table with initial values. It then iteratively selects the node with the least cost and updates its neighbours' least cost in the calculation table. Finally, it returns the least cost path to all nodes in the network.

```
def dijkstra_calculate_path():
    # Copying the parent router object
    _parent_router = parent_router

    # Indices for accessing calculation_table values
    weight = 0
    visited_status = 1
    parent_ = 2

    # Creating a new graph object from the parent router's global routers
    g = Graph(_parent_router.global_routers)

    # A dictionary to store the current least cost and visited status for each router
    calculation_table: Dict[Any, List[Union[float, bool]]] = {}

    # Initializing the calculation_table with initial values
    total_routers = 0
    for router in _parent_router.global_routers:
        if router != _parent_router.name:
            # filling all the table with name of router
            calculation_table[router] = [inf, False, None]
        else:
            # set the parent router's least cost to itself to 0
            calculation_table[router] = [0.0, True, None]
        total_routers += 1

    # Counter for counting number of routers visited so far
    counter = 0

    # Print the current router's name
    print(f'NodeRouter id:  {_parent_router.name}')

    # Set the current router to the parent router
    current_router = _parent_router.name

    # Initialize variables for storing the routers visited and the corresponding hops taken
    print_routers = _parent_router.name
    printing_list = []

    # While all routers haven't been visited
    while counter != total_routers-1:
        # For all edges from the current router
        for edge in g.graph[current_router]:
            # For each router in the calculation_table
            for node, weight_status in calculation_table.items():
                # If the router is the edge's end and hasn't been visited yet and has a higher cost than the current path
                if node == edge.end and not weight_status[visited_status] and calculation_table[node][weight] > calculation_table[current_router][weight] + float(edge.weight):
                    # Update the least cost for the router
                    calculation_table[node][weight] = calculation_table[current_router][weight] + float(edge.weight)
                    # Set the parent for the router to the current router
                    calculation_table[node][parent_] = edge.start

        # Find the router with the minimum least cost that hasn't been visited yet
        min_weight = inf
        min_node = ''
        for node, weight_status in calculation_table.items():
            if weight_status[weight] < min_weight and weight_status[visited_status] == False:
                min_node = node
                min_weight = weight_status[weight]

        # If a router was found
        if min_node != '':
            # Set the visited status of the router to True
            calculation_table[min_node][visited_status] = True
            # Set the current router to the newly found router
            current_router = min_node
            # Increment the counter
            counter += 1
            # Append the router to the list of visited routers
            printing_list.append(min_node)
            # Append the router to the string of routers visited so far
            print_routers = print_routers + min_node

    # Print the hops and the corresponding cost for each visited router
    for node in printing_list:
        hops = node
        current_parent = calculation_table[node][parent_]
        while current_parent is not None:
            hops = hops + current_parent
            current_parent = calculation_table[current_parent][parent_]
        print(f'Least cost path to router {node}:{hops[::-1]} and the cost is {calculation_table[node][weight]:.1f}')

```
- **udp_client function**: This function creates a **UDP** client socket and sends messages to its neighbors. It uses the _parent_router argument to get the list of its neighbors, sets the timestamp of the message, serializes it using the pickle module and sends it to each of its neighbors. It then sleeps for update_interval seconds and increments the sequence number of the message.

- **chk_prev_seq function**: This function takes a message and _parent_router as arguments and returns a boolean value indicating whether the sequence number of the message is greater than the previous sequence number for that message's name in the _parent_router. It is used to check if a message is newer than the previous message with the same name received by the router.

- **check_previous_sent_timestamp function**: This function takes a message and _parent_router as arguments and returns a boolean value indicating whether the timestamp of the message is greater than the previously received timestamp for that message's name in the _parent_router. It is used to check if a message is newer than the previously received message with the same name from a neighbor.

- **udp_server function**: This function creates a UDP server socket, binds to the router's port and waits for incoming messages. It then processes the received message by checking its timestamp, sequence number and previous sent message. It then forwards the message to its neighbors except the previous sender. It also updates the _parent_router with the received message's information.

- **check_neigh_alive function**: This function checks if any neighbor of the _parent_router has not sent a message for more than 3 seconds, in which case it removes that neighbor from the router's list of neighbors and from the _parent_router's global key-value dict of routers.

- **not_neigh function**: This function takes a router and _parent_router as arguments and returns a boolean value indicating whether the router is not a neighbor of the _parent_router.

- **check_if_non_neigh_alive function**: This function checks if any router which is not a neighbor of the _parent_router has not sent a message for more than 12 seconds, in which case it removes that router from the _parent_router's global key-value dict of routers.

- **check_alive function**: This function runs in a separate thread and periodically calls the check_neigh_alive and check_if_non_neigh_alive functions to check the liveness of its neighbors and non-neighbors respectively.

The main body of the code reads the input file and creates the NodeRouter object representing the parent router and Neighbours objects representing its neighbors. It then creates threads for the udp_client, udp_server, distance_calculation_activator and check_alive functions and starts them.
## Results

![All Router Live](https://github.com/AbdullahArean/Link-State-Routing-Algorithm-Implemented/blob/main/Screenshots/allrouterslive.png)
![One Router Live](https://github.com/AbdullahArean/Link-State-Routing-Algorithm-Implemented/blob/main/Screenshots/onerouterlive.png)

## Conclusion


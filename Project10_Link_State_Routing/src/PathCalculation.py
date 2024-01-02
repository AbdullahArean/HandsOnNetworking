from math import inf
from typing import Dict, List, Any, Union
import random
from Graph import Graph
from Alive import *
from NodeRouter import NodeRouter
from Variables import *


def calculate_paths_activator(parent_router: NodeRouter):
    while True:
        for key, value in parent_router.global_routers.items():
            if (key == parent_router.name):
                print(f"NodeRouter {key}:")
                for neighbour in value:
                    print(f"  - Name: {neighbour.name}, Port: {neighbour.port}, Distance: {neighbour.distance}")
        time.sleep(router_update_interval)
        dijkstra_calculate_path(parent_router)

        # select a this router name from the global_routers dictionary
        router_name = parent_router.name
        # select a random neighbour of the router
        neighbour_index = random.randint(0, len(parent_router.global_routers[router_name]) - 1)
        # change the cost of the neighbour's link
        new_cost = random.randint(1, 10)
        parent_router.global_routers[router_name][neighbour_index].distance = new_cost


def dijkstra_calculate_path(parent_router: NodeRouter):
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
    while counter != total_routers - 1:
        # For all edges from the current router
        for edge in g.graph[current_router]:
            # For each router in the calculation_table
            for node, weight_status in calculation_table.items():
                # If the router is the edge's end and hasn't been visited yet and has a higher cost than the current path
                if node == edge.end and not weight_status[visited_status] and calculation_table[node][weight] > \
                        calculation_table[current_router][weight] + float(edge.weight):
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

from Edge import Edge
from collections import defaultdict


class Graph:
    def __init__(self, global_routers):
        self.global_routers = global_routers
        self.graph = defaultdict(list)
        self.parse(self.global_routers)

    def parse(self, global_routers):
        for router, neigh in global_routers.items():
            parent = router
            for child in neigh:
                self.graph[parent].append(Edge(parent, child.name, child.distance))

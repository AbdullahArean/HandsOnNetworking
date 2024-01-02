class adjacent_node:
    def __init__(self, linkCost, port, timeout):
        self.linkCost = linkCost
        self.port = port
        self.timeout = timeout
        self.paths = dict()
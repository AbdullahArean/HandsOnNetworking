class possible_path:
    def __init__(self, dis, nxthop):
        self.dis = dis
        self.nxthop = nxthop

    def equals(self, path2):
        return self.dis == path2.dis and self.nxthop == path2.nxthop 
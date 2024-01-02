import time
import datetime as dt
from NodeRouter import NodeRouter
def check_neigh_alive(_parent_router: NodeRouter):
    not_alive_neigh_remove = None
    for neighbour in _parent_router.neigh:
        if dt.datetime.now().timestamp() - _parent_router.global_time_st[neighbour.name] > 3:
            # remove from LSA
            not_alive_neigh_remove = neighbour.name

            # remove from global key
            # remove from global values
    if not_alive_neigh_remove is not None:
        _parent_router.global_routers.pop(not_alive_neigh_remove, None)
        for neighbour in _parent_router.neigh:
            if not_alive_neigh_remove == neighbour.name:
                _parent_router.neigh.remove(neighbour)
                break


def not_neigh(router, _parent_router):
    for neighbour in _parent_router.neigh:
        if router == neighbour.name:
            return False
    return True


def check_if_non_neigh_alive(_parent_router: NodeRouter):
    
    not_alive_router_to_remove = None
    for router, all_neighbours in _parent_router.global_routers.items():
        if not_neigh(router, _parent_router) and router != _parent_router.name:
            if dt.datetime.now().timestamp() - _parent_router.global_time_st[router] > 12:
                
                not_alive_router_to_remove = router
    if not_alive_router_to_remove is not None:
        _parent_router.global_routers.pop(not_alive_router_to_remove, None)


def check_alive(_parent_router: NodeRouter):
    while True:
        time.sleep(3)
        check_neigh_alive(_parent_router)
        check_if_non_neigh_alive(_parent_router)

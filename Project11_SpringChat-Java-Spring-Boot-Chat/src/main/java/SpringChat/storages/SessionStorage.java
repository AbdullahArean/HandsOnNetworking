package SpringChat.storages;

import SpringChat.models.SessionModel;
import org.springframework.stereotype.Component;
import SpringChat.models.UserModel;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
@Component
public class SessionStorage {

    private final Map<String, SessionModel> map = new HashMap<>();
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public void deleteById(String id) {
        try {
            lock.writeLock().lock();
            id = id.toLowerCase();
            map.remove(id);
        } finally {
            lock.writeLock().unlock();
        }

    }

    public void save(SessionModel sessionModel) {
        try {
            lock.writeLock().lock();
            sessionModel.setId(sessionModel.getId().toLowerCase());
            map.put(sessionModel.getId(), sessionModel);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<SessionModel> findById(String id) {
        try {
            lock.readLock().lock();
            id = id.toLowerCase();
            if (map.containsKey(id)) {
                return Optional.of(map.get(id));
            } else {
                return Optional.empty();
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    public void deleteAll(List<SessionModel> list) {
        try {
            lock.writeLock().lock();
            list.stream().forEach(x -> map.remove(x.getId()));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<SessionModel> findExpired(long l) {
        try {
            lock.readLock().lock();
            List<SessionModel> list = new ArrayList<>();
            for (String id : map.keySet()) {
                if (map.get(id).getLastModified() < l) {
                    list.add(map.get(id));
                }
            }
            return list;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<SessionModel> findByUsername(String username) {
        try {
            lock.readLock().lock();
            List<SessionModel> list = new ArrayList<>();
            for (String id : map.keySet()) {
                UserModel userModel = map.get(id).getUserModel();
                if ((userModel != null) && (userModel.getUsername().equals(username))) {
                    list.add(map.get(id));
                }
            }
            return list;
        } finally {
            lock.readLock().unlock();
        }
    }
}

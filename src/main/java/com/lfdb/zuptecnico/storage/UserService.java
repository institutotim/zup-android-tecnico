package com.lfdb.zuptecnico.storage;

import com.lfdb.zuptecnico.entities.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Igor on 8/3/2015.
 */
public class UserService extends BaseService {
    public UserService(StorageServiceManager manager) {
        super(manager);
    }

    public void clear() {
        deleteObject("users");
    }

    public User getUser(int id) {
        User item = getObject("user_" + id, User.class);
        return item;
    }

    public void addUser(User user) {
        List<Integer> ids = getObjectList("users", Integer.class);
        if(ids == null)
            ids = new ArrayList<>();

        if(!ids.contains(user.id)) {
            ids.add(user.id);
            setList("users", ids);
        }

        saveUser(user);
    }

    public void saveUser(User user) {
        setObject("user_" + user.id, user);
    }
}

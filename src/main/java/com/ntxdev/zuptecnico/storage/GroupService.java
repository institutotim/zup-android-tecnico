package com.ntxdev.zuptecnico.storage;

import com.ntxdev.zuptecnico.entities.Group;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Igor on 8/3/2015.
 */
public class GroupService extends BaseService {
  public GroupService(StorageServiceManager manager) {
    super(manager);
  }

  public void clear() {
    deleteObject("groups");
    deleteObject("namespace_groups");
  }

  public Group getGroup(int id) {
    Group item = getObject("group_" + id, Group.class);
    return item;
  }

  public boolean hasGroup(int id) {
    List<Integer> ids = getObjectList("groups", Integer.class);
    if (ids == null) return false;

    return ids.contains(id);
  }

  public void addGroups(Group[] groups) {
    List<Integer> ids = getObjectList("groups", Integer.class);
    for (int i = 0; i < groups.length; i++) {
      if (ids.contains(groups[i].getId())) {
        continue;
      }
      saveGroup(groups[i]);
      ids.add(groups[i].getId());
    }

    setList("groups", ids);
  }

  public void addNamespaceGroups(Group[] groups) {
    List<Integer> ids = getObjectList("namespace_groups", Integer.class);
    for (int i = 0; i < groups.length; i++) {
      if (ids.contains(groups[i].getId())) {
        continue;
      }
      saveGroup(groups[i]);
      ids.add(groups[i].getId());
    }

    setList("namespace_groups", ids);
  }

  public void addGroup(Group group) {
    List<Integer> ids = getObjectList("groups", Integer.class);
    if (ids == null) ids = new ArrayList<>();

    if (!ids.contains(group.getId())) {
      ids.add(group.getId());
      setList("groups", ids);
    }

    saveGroup(group);
  }

  public void saveGroup(Group group) {
    setObject("group_" + group.getId(), group);
  }

  public List<Group> getGroups() {
    List<Group> groups = new ArrayList<Group>();
    List<Integer> ids = getObjectList("groups", Integer.class);
    for (Integer id : ids) {
      groups.add(getObject("group_" + id, Group.class));
    }
    return groups;
  }

  public List<Group> getNamespaceGroups() {
    List<Group> groups = new ArrayList<Group>();
    List<Integer> ids = getObjectList("namespace_groups", Integer.class);
    for (Integer id : ids) {
      groups.add(getObject("group_" + id, Group.class));
    }
    return groups;
  }
}

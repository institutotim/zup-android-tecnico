package com.ntxdev.zuptecnico.storage;

import com.ntxdev.zuptecnico.entities.Namespace;
import com.ntxdev.zuptecnico.entities.User;
import com.ntxdev.zuptecnico.entities.responses.NamespaceCollection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by WINDOWS on 31/08/2016.
 */
public class NamespaceService extends BaseService {
  public NamespaceService(StorageServiceManager manager) {
    super(manager);
  }

  public void clear() {
    deleteObject("namespaces");
  }

  public Namespace getNamespace(int id) {
    Namespace item = getObject("namespace_" + id, Namespace.class);
    return item;
  }

  public void addNamespace(Namespace namespace) {
    List<Integer> ids = getObjectList("namespaces", Integer.class);
    if (ids == null) ids = new ArrayList<>();

    if (!ids.contains(namespace.getId())) {
      ids.add(namespace.getId());
      setList("namespaces", ids);
    }

    saveNamespace(namespace);
  }

  public void setNamespaces(Namespace[] namespaces) {
    clear();
    if (namespaces == null) {
      return;
    }
    for (Namespace namespace : namespaces) {
      addNamespace(namespace);
    }
  }

  private void saveNamespace(Namespace namespace) {
    setObject("namespace_" + namespace.getId(), namespace);
  }

  public Namespace[] getNamespaces() {
    List<Integer> ids = getObjectList("namespaces", Integer.class);
    if (ids == null) {
      return null;
    }
    Namespace[] namespaces = new Namespace[ids.size()];
    for (int index = 0; index < ids.size(); index++) {
      namespaces[index] = getNamespace(ids.get(index));
    }
    return namespaces;
  }
}


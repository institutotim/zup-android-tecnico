package com.lfdb.zuptecnico.api;

import com.lfdb.zuptecnico.entities.Group;
import com.lfdb.zuptecnico.entities.User;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by igorlira on 8/23/15.
 */
public class ZupAccess {
  private Group[] groups;

  public ZupAccess(User user) {
    if (user == null) {
      this.groups = new Group[0];
    } else {
      this.groups = user.groups;
    }
  }

  public boolean canViewStep(int stepId, int flowId) {
    for (Group group : groups) {
      if (group.canViewStep(stepId, flowId)) {
        return true;
      }
    }
    return false;
  }

  public boolean canEditStep(int stepId, int flowId) {
    for (Group group : groups) {
      if (group.canEditStep(stepId, flowId)) {
        return true;
      }
    }
    return false;
  }

  public boolean canCreateReportItem() {
    for (Group group : groups) {
      if (group.canCreateReportItem()) {
        return true;
      }
    }

    return false;
  }

  public boolean canCreateReportItem(int categoryId) {
    for (Group group : groups) {
      if (group.canCreateReportItem(categoryId)) {
        return true;
      }
    }

    return false;
  }

  public boolean canEditReportItem(int categoryId) {
    for (Group group : groups) {
      if (group.canEditReportItem(categoryId)) {
        return true;
      }
    }

    return false;
  }

  public boolean canDeleteReportItem(int categoryId) {
    for (Group group : groups) {
      if (group.canDeleteReportItem(categoryId)) {
        return true;
      }
    }

    return false;
  }

  public boolean canCreateCommentOnReportItem(int categoryId) {
    for (Group group : groups) {
      if (group.canCreateCommentOnReportItem(categoryId)) {
        return true;
      }
    }

    return false;
  }

  public boolean canCreateInternalCommentOnReportItem(int categoryId) {
    for (Group group : groups) {
      if (group.canCreateInternalCommentOnReportItem(categoryId)) {
        return true;
      }
    }

    return false;
  }

  public boolean canEditInventoryCategory(int categoryId) {
    for (Group group : groups) {
      if (group.canEditInventoryCategory(categoryId)) {
        return true;
      }
    }

    return false;
  }

  public boolean canViewInventoryCategory(int categoryId) {
    for (Group group : groups) {
      if (group.canViewInventoryCategory(categoryId)) {
        return true;
      }
    }

    return false;
  }

  public boolean canViewInventoryItem(int categoryId) {
    for (Group group : groups) {
      if (group.canViewInventoryItem(categoryId)) {
        return true;
      }
    }

    return false;
  }

  public boolean canCreateInventoryItem(int categoryId) {
    for (Group group : groups) {
      if (group.canCreateInventoryItem(categoryId)) {
        return true;
      }
    }

    return false;
  }

  public boolean canCreateInventoryItem() {
    for (Group group : groups) {
      if (group.canCreateInventoryItem()) {
        return true;
      }
    }

    return false;
  }

  public boolean canEditInventoryItem(int categoryId) {
    for (Group group : groups) {
      if (group.canEditInventoryItem(categoryId)) {
        return true;
      }
    }

    return false;
  }

  public boolean canDeleteInventoryItem(int categoryId) {
    for (Group group : groups) {
      if (group.canDeleteInventoryItem(categoryId)) {
        return true;
      }
    }

    return false;
  }

  public boolean canDeleteCase(int id) {
    for (Group group : groups) {
      if (group.canDeleteCase(id)) {
        return true;
      }
    }

    return false;
  }

  public boolean canViewInventoryField(int categoryId, int fieldId) {
    for (Group group : groups) {
      if (group.canViewInventoryField(categoryId, fieldId)) {
        return true;
      }
    }

    return false;
  }

  public boolean canViewInventorySection(int categoryId, int sectionId) {
    for (Group group : groups) {
      if (group.canViewInventorySection(categoryId, sectionId)) {
        return true;
      }
    }

    return false;
  }

  public boolean canViewReportCategory(int categoryId) {
    for (Group group : groups) {
      if (group.canViewReportCategory(categoryId)) {
        return true;
      }
    }
    return false;
  }

  public boolean canViewCases() {
    for (Group group : groups) {
      if (group.canViewCases()) {
        return true;
      }
    }
    return false;
  }

  public boolean canViewIventoryItems() {
    for (Group group : groups) {
      if (group.canViewInventoryItems()) {
        return true;
      }
    }
    return false;
  }

  public boolean canViewReportItems() {
    for (Group group : groups) {
      if (group.canViewReports()) {
        return true;
      }
    }
    return false;
  }

  public boolean canForwardReportItems(int categoryId) {
    for (Group group : groups) {
      if (group.canForwardReportItem(categoryId)) {
        return true;
      }
    }
    return false;
  }

  public boolean canAlterReportItemStatus(int categoryId) {
    for (Group group : groups) {
      if (group.canAlterReportItemStatus(categoryId)) {
        return true;
      }
    }
    return false;
  }

  public Group getGroupAllowedToEditStep(int stepId, int flowId) {
    for (Group group : groups) {
      if (group.canEditStep(stepId, flowId)) {
        return group;
      }
    }
    return null;
  }

  public Integer[] getAllGroupIdsAllowedToView() {
    Set<Integer> ids = new HashSet<Integer>();
    for (Group group : groups) {
      ids.add(group.getId());
      ids.addAll(Arrays.asList(group.getPermissions().group_edit));
      ids.addAll(Arrays.asList(group.getPermissions().group_read_only));
    }
    Integer[] newIds = new Integer[ids.size()];
    return ids.toArray(newIds);
  }

  public boolean canViewAllGroups() {
    for (Group group : groups) {
      if (group.canViewAllGroups()) {
        return true;
      }
    }
    return false;
  }
}

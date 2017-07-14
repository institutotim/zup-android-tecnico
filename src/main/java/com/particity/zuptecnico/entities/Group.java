package com.particity.zuptecnico.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.particity.zuptecnico.util.Utilities;
import java.io.Serializable;

/**
 * Created by igorlira on 7/24/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true) public class Group implements Serializable {

  @JsonIgnoreProperties(ignoreUnknown = true) public static class Permissions
      implements Serializable {
    public boolean panel_access;
    public boolean create_reports_from_panel;
    public boolean users_full_access;
    public Integer[] users_edit;
    public boolean groups_full_access;
    public boolean reports_full_access;
    public boolean inventories_full_access;
    public boolean inventories_formulas_full_access;
    public Integer[] group_edit;
    public Integer[] group_read_only;
    public Integer[] reports_items_read_public;
    public Integer[] reports_items_read_private;
    public Integer[] reports_items_create;
    public Integer[] reports_items_edit;
    public Integer[] reports_items_delete;
    public Integer[] reports_items_forward;
    public Integer[] reports_items_create_internal_comment;
    public Integer[] reports_items_create_comment;
    public Integer[] reports_items_alter_status;
    public Integer[] reports_categories_edit;
    public Integer[] inventories_items_read_only;
    public Integer[] inventories_items_create;
    public Integer[] inventories_items_edit;
    public Integer[] inventories_items_delete;
    public Integer[] inventories_categories_edit;
    public Integer[] inventories_category_manage_triggers;
    public Integer[] inventory_fields_can_edit;
    public Integer[] inventory_fields_can_view;
    public Integer[] inventory_sections_can_edit;
    public Integer[] inventory_sections_can_view;
    public Integer[] flow_can_execute_all_steps;
    public Integer[] flow_can_delete_own_cases;
    public Integer[] flow_can_delete_all_cases;
    public Integer[] flow_can_view_all_steps;
    public Integer[] can_view_step;
    public Integer[] can_execute_step;
    public boolean manage_flows;
    public boolean manage_cases;
    public boolean manage_config;
    public boolean business_reports_edit;
    public Integer[] business_reports_view;
  }

  private int id;
  private String name;
  private Permissions permissions;

  public Group() {
  }

  public Group(int id, String name) {
    this.id = id;
    this.name = name;
  }

  @Override public boolean equals(Object o) {
    if (o == null || !(o instanceof Group)) {
      return false;
    }
    return ((Group) o).id == id;
  }

  @JsonProperty("id") public int getId() {
    return id;
  }

  @JsonProperty("id") public void setId(int id) {
    this.id = id;
  }

  @JsonProperty("name") public String getName() {
    return name;
  }

  @JsonProperty("name") public void setName(String name) {
    this.name = name;
  }

  @JsonProperty("permissions") public Permissions getPermissions() {
    return this.permissions;
  }

  @JsonProperty("permissions") public void setPermissions(Permissions permissions) {
    this.permissions = permissions;
  }

  public boolean canEditStep(int stepId, int flowId) {
    return permissions.manage_flows || Utilities.arrayContains(
        permissions.flow_can_execute_all_steps, flowId) || Utilities.arrayContains(
        permissions.can_execute_step, stepId);
  }

  public boolean canViewStep(int stepId, int flowId) {
    return canEditStep(stepId, flowId) || Utilities.arrayContains(
        permissions.flow_can_view_all_steps, flowId) || Utilities.arrayContains(
        permissions.can_view_step, stepId);
  }

  public boolean canDeleteCase(int id) {
    return permissions.manage_flows || Utilities.arrayContains(
        permissions.flow_can_delete_all_cases, id) || Utilities.arrayContains(
        permissions.flow_can_delete_own_cases, id);
  }

  public boolean canCreateReportItem() {
    return this.permissions.reports_full_access || !Utilities.arrayIsEmpty(
        permissions.reports_categories_edit) || !Utilities.arrayIsEmpty(
        permissions.reports_items_edit) || !Utilities.arrayIsEmpty(
        permissions.reports_items_create);
  }

  public boolean canCreateReportItem(int categoryId) {
    return canEditReportCategory(categoryId) || Utilities.arrayContains(
        permissions.reports_items_edit, categoryId) || Utilities.arrayContains(
        permissions.reports_items_create, categoryId);
  }

  public boolean canForwardReportItem(int categoryId) {
    return canEditReportItem(categoryId) || Utilities.arrayContains(
        permissions.reports_items_forward, categoryId);
  }

  public boolean canViewAllGroups() {
    return this.permissions.groups_full_access;
  }

  public boolean canEditReportItem(int categoryId) {
    return canEditReportCategory(categoryId) || Utilities.arrayContains(
        permissions.reports_items_edit, categoryId);
  }

  private boolean canEditReportCategory(int categoryId) {
    return this.permissions.reports_full_access || Utilities.arrayContains(
        permissions.reports_categories_edit, categoryId);
  }

  public boolean canViewCases() {
    return permissions.manage_flows
        || permissions.manage_cases
        || !Utilities.arrayIsEmpty(permissions.can_execute_step)
        || !Utilities.arrayIsEmpty(permissions.can_view_step)
        || !Utilities.arrayIsEmpty(permissions.flow_can_view_all_steps)
        || !Utilities.arrayIsEmpty(permissions.flow_can_delete_all_cases)
        || !Utilities.arrayIsEmpty(permissions.flow_can_delete_own_cases)
        || !Utilities.arrayIsEmpty(permissions.flow_can_execute_all_steps);
  }

  public boolean canAlterReportItemStatus(int categoryId) {
    return canEditReportItem(categoryId) || Utilities.arrayContains(
        permissions.reports_items_alter_status, categoryId);
  }

  public boolean canDeleteReportItem(int categoryId) {
    return canEditReportCategory(categoryId) || Utilities.arrayContains(
        permissions.reports_items_delete, categoryId);
  }

  public boolean canViewReportCategory(int categoryId) {
    return canViewReportItem(categoryId) || canEditReportItem(categoryId) || canCreateReportItem(
        categoryId) || canDeleteReportItem(categoryId);
  }

  public boolean canViewReports() {
    return canCreateReportItem()
        || !Utilities.arrayIsEmpty(permissions.reports_items_delete)
        || !Utilities.arrayIsEmpty(permissions.reports_items_edit)
        || !Utilities.arrayIsEmpty(permissions.reports_items_read_private)
        || !Utilities.arrayIsEmpty(permissions.reports_items_read_public)
        || !Utilities.arrayIsEmpty(permissions.reports_items_alter_status)
        || !Utilities.arrayIsEmpty(permissions.reports_items_create_comment)
        || !Utilities.arrayIsEmpty(permissions.reports_items_create_internal_comment)
        || !Utilities.arrayIsEmpty(permissions.reports_items_forward);
  }

  public boolean canViewReportItem(int categoryId) {
    return canEditReportItem(categoryId) || Utilities.arrayContains(
        permissions.reports_items_read_private, categoryId) || Utilities.arrayContains(
        permissions.reports_items_read_public, categoryId);
  }

  public boolean canCreateCommentOnReportItem(int categoryId) {
    return canEditReportItem(categoryId) || Utilities.arrayContains(
        permissions.reports_items_create_comment, categoryId);
  }

  public boolean canCreateInternalCommentOnReportItem(int categoryId) {
    return canEditReportItem(categoryId) || Utilities.arrayContains(
        permissions.reports_items_create_internal_comment, categoryId);
  }

  public boolean canEditInventoryCategory(int categoryId) {
    return this.permissions.inventories_full_access || Utilities.arrayContains(
        permissions.inventories_categories_edit, categoryId);
  }

  public boolean canEditInventoryCategory() {
    return this.permissions.inventories_full_access || !Utilities.arrayIsEmpty(
        permissions.inventories_categories_edit);
  }

  public boolean canViewInventoryCategory(int categoryId) {
    return canViewInventoryItem(categoryId) || canEditInventoryItem(categoryId);
  }

  public boolean canViewInventoryItems() {
    return canCreateInventoryItem()
        || !Utilities.arrayIsEmpty(permissions.inventories_items_delete)
        || !Utilities.arrayIsEmpty(permissions.inventories_items_edit)
        || !Utilities.arrayIsEmpty(permissions.inventories_items_read_only);
  }

  public boolean canViewInventoryItem(int categoryId) {
    return canCreateInventoryItem(categoryId)
        || canDeleteInventoryItem(categoryId)
        || Utilities.arrayContains(permissions.inventories_items_read_only, categoryId);
  }

  public boolean canCreateInventoryItem(int categoryId) {
    return canEditInventoryItem(categoryId) || Utilities.arrayContains(
        permissions.inventories_items_create, categoryId);
  }

  public boolean canCreateInventoryItem() {
    return !Utilities.arrayIsEmpty(permissions.inventories_items_create)
        || canEditInventoryCategory();
  }

  public boolean canEditInventoryItem(int categoryId) {
    return Utilities.arrayContains(permissions.inventories_items_edit, categoryId)
        || canEditInventoryCategory(categoryId);
  }

  public boolean canDeleteInventoryItem(int categoryId) {
    return Utilities.arrayContains(permissions.inventories_items_delete, categoryId)
        || canEditInventoryCategory(categoryId);
  }

  public boolean canViewInventoryField(int categoryId, int fieldId) {
    return Utilities.arrayContains(permissions.inventory_fields_can_view, fieldId)
        || Utilities.arrayContains(permissions.inventory_fields_can_edit, fieldId)
        || canEditInventoryItem(categoryId);
  }

  public boolean canViewInventorySection(int categoryId, int sectionId) {
    return Utilities.arrayContains(permissions.inventory_sections_can_view, sectionId)
        || Utilities.arrayContains(permissions.inventory_sections_can_edit, sectionId)
        || canEditInventoryItem(categoryId);
  }
}

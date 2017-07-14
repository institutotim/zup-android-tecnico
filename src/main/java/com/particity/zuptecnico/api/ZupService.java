package com.particity.zuptecnico.api;

import com.particity.zuptecnico.config.InternalConstants;
import com.particity.zuptecnico.entities.Message;
import com.particity.zuptecnico.entities.ReportNotificationCollection;
import com.particity.zuptecnico.entities.Session;
import com.particity.zuptecnico.entities.SingleGroup;
import com.particity.zuptecnico.entities.collections.CaseCollection;
import com.particity.zuptecnico.entities.collections.CaseHistoryCollection;
import com.particity.zuptecnico.entities.collections.FlowCollection;
import com.particity.zuptecnico.entities.collections.GroupCollection;
import com.particity.zuptecnico.entities.collections.InventoryCategoryCollection;
import com.particity.zuptecnico.entities.collections.InventoryItemCollection;
import com.particity.zuptecnico.entities.collections.ReportCategoryCollection;
import com.particity.zuptecnico.entities.collections.ReportHistoryItemCollection;
import com.particity.zuptecnico.entities.collections.ReportItemCollection;
import com.particity.zuptecnico.entities.collections.SingleCaseCollection;
import com.particity.zuptecnico.entities.collections.SingleInventoryCategoryCollection;
import com.particity.zuptecnico.entities.collections.SingleInventoryItemCollection;
import com.particity.zuptecnico.entities.collections.SingleReportItemCollection;
import com.particity.zuptecnico.entities.collections.SingleUserCollection;
import com.particity.zuptecnico.entities.collections.UserCollection;
import com.particity.zuptecnico.entities.collections.UserCreationResult;
import com.particity.zuptecnico.entities.requests.AssignReportToGroupRequest;
import com.particity.zuptecnico.entities.requests.ChangeCategoryReportRequest;
import com.particity.zuptecnico.entities.requests.CreateArbitraryReportItemRequest;
import com.particity.zuptecnico.entities.requests.CreateReportItemCommentRequest;
import com.particity.zuptecnico.entities.requests.CreateReportItemRequest;
import com.particity.zuptecnico.entities.requests.CreateUserRequest;
import com.particity.zuptecnico.entities.requests.PublishInventoryItemRequest;
import com.particity.zuptecnico.entities.requests.UpdateCaseStepRequest;
import com.particity.zuptecnico.entities.responses.CreateReportItemCommentResponse;
import com.particity.zuptecnico.entities.responses.DeleteInventoryItemResponse;
import com.particity.zuptecnico.entities.responses.EditInventoryItemResponse;
import com.particity.zuptecnico.entities.responses.NamespaceCollection;
import com.particity.zuptecnico.entities.responses.PositionValidationResponse;
import com.particity.zuptecnico.entities.responses.PublishInventoryItemResponse;

import java.util.Map;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.QueryMap;

public interface ZupService {
  @FormUrlEncoded @POST("/authenticate") Session authenticate(@Field("email") String email,
      @Field("password") String password);

  @FormUrlEncoded @POST("/authenticate") void authenticate(@Field("email") String email,
      @Field("password") String password, Callback<Session> callback);

  @FormUrlEncoded @PUT("/recover_password") void recoverPassword(@Field("email") String email,
      Callback<Message> callback);

  @GET("/users/{id}") SingleUserCollection retrieveUser(@Path("id") int id);

  @GET("/groups/{id}") SingleGroup retrieveGroup(@Path("id") int id);

  @Headers("use_user_namespace: true") @GET("/search/users") UserCollection retrieveUsersGroup(
      @Query("groups") int id, @Query("page") int page);

  @FormUrlEncoded @PUT("/reports/{reports_category_id}/items/{id}/assign")
  SingleReportItemCollection assignReportToUser(@Path("reports_category_id") int categoryId,
      @Path("id") int reportId, @Field("user_id") int userId);

  @GET("/groups?return_fields=id,name&ignore_namespaces=false")
  GroupCollection retrieveNamespaceGroups();

  @GET("/groups?return_fields=id,name&ignore_namespaces=true") GroupCollection retrieveGroups();

  @GET("/namespaces") NamespaceCollection getNamespaces();

  @GET("/users") UserCollection retrieveUsers(@Query("page") int page);

  @GET("/search/users") UserCollection searchUsers(@Query("name") String name,
      @Query("page") int page);

  @POST("/users") void createUser(@Body CreateUserRequest body,
      Callback<UserCreationResult> callback);

  @POST("/users") UserCreationResult createUser(@Body CreateUserRequest body);

  @PUT("/inventory/categories/{categoryId}/items/{item_id}?display_type=full&return_fields="
      + InternalConstants.INVENTORY_ITEM_RETURN_FIELDS) EditInventoryItemResponse editInventoryItem(
      @Path("categoryId") int categoryId, @Path("item_id") int itemId,
      @Body PublishInventoryItemRequest body);

  @POST("/inventory/categories/{categoryId}/items?display_type=full&return_fields="
      + InternalConstants.INVENTORY_ITEM_RETURN_FIELDS)
  PublishInventoryItemResponse publishInventoryItem(@Path("categoryId") int categoryId,
      @Body PublishInventoryItemRequest body);

  @DELETE("/inventory/categories/{categoryId}/items/{item_id}")
  DeleteInventoryItemResponse deleteInventoryItem(@Path("categoryId") int categoryId,
      @Path("item_id") int itemId);

  @DELETE("/cases/{id}") Response deleteCase(@Path("id") int id);

  @GET("/inventory/categories?display_type=full&return_fields="
      + InternalConstants.INVENTORY_CATEGORY_LISTING_RETURN_FIELDS)
  InventoryCategoryCollection getInventoryCategories();

  @GET("/inventory/categories/{categoryId}") SingleInventoryCategoryCollection getInventoryCategory(
      @Path("categoryId") int id);

  @GET("/search/inventory/items?return_fields="
      + InternalConstants.INVENTORY_ITEM_LISTING_RETURN_FIELDS) void searchInventoryItems(
      @Query("inventory_categories_ids") int inventory_category_ids, @Query("page") int page,
      @QueryMap Map<String, Object> options, Callback<InventoryItemCollection> cb);

  @GET("/search/inventory/items?return_fields="
      + InternalConstants.INVENTORY_ITEM_LISTING_RETURN_FIELDS) void searchInventoryItems(
      @Query("query") String query, @Query("page") int page, @QueryMap Map<String, Object> options,
      Callback<InventoryItemCollection> cb);

  @GET("/search/inventory/items?return_fields="
      + InternalConstants.INVENTORY_ITEM_LISTING_RETURN_FIELDS)
  InventoryItemCollection searchInventoryItems(@Query("page") int page,
      @Query("query") String query,
      @Query("inventory_categories_ids") int[] inventory_category_ids);

  @GET("/search/inventory/items?return_fields="
      + InternalConstants.INVENTORY_ITEM_LISTING_RETURN_FIELDS)
  InventoryItemCollection searchInventoryItems(@Query("page") int page,
      @Query("query") String query);

  @GET("/search/inventory/items?return_fields="
      + InternalConstants.INVENTORY_ITEM_LISTING_RETURN_FIELDS) void searchInventoryItems(
      @Query("page") int page, @QueryMap Map<String, Object> options,
      Callback<InventoryItemCollection> cb);

  @GET("/search/inventory/items?clusterize=true&return_fields="
      + InternalConstants.INVENTORY_ITEM_MAP_RETURN_FIELDS)
  InventoryItemCollection getInventoryItems(@Query("position[latitude]") double latitude,
      @Query("position[longitude]") double longitude, @Query("position[distance]") double radius,
      @Query("limit") int limit, @Query("zoom") int zoom);

  @GET("/inventory/categories/{categoryId}/items/{item_id}?display_type=full&return_fields="
      + InternalConstants.INVENTORY_ITEM_RETURN_FIELDS)
  SingleInventoryItemCollection getInventoryItem(@Path("categoryId") int categoryId,
      @Path("item_id") int itemId);

  @GET("/inventory/items/{item_id}?display_type=full&return_fields=title")
  SingleInventoryItemCollection getInventoryItemTitle(@Path("item_id") int itemId);

  @GET("/inventory/items/{item_id}?display_type=full&return_fields="
      + InternalConstants.INVENTORY_ITEM_RETURN_FIELDS)
  SingleInventoryItemCollection getInventoryItem(@Path("item_id") int itemId);

  @GET("/inventory/items/{item_id}?display_type=full&return_fields="
      + InternalConstants.INVENTORY_ITEM_RETURN_FIELDS) void getInventoryItem(
      @Path("item_id") int itemId, Callback<SingleInventoryItemCollection> cb);

  @PUT("/cases/{case_id}?display_type-full&return_fields="
      + InternalConstants.CASE_DETAILS_RETURN_FIELDS) SingleCaseCollection updateCaseStep(
      @Path("case_id") int caseId, @Body UpdateCaseStepRequest body);

  @GET("/cases/{item_id}/history") void retrieveCaseItemHistory(@Path("item_id") int itemId,
      Callback<CaseHistoryCollection> cb);

  @GET("/flows?display_type=full&return_fields=" + InternalConstants.FLOWS_RETURN_FIELDS)
  FlowCollection retrieveFlows();

  @GET("/cases?display_type=full&order=desc&sort=updated_at&return_fields="
      + InternalConstants.CASES_ADAPTER_RETURN_FIELDS) void retrieveCases(
      @Query("initialFlowId") int initialFlowId, @Query("page") int page,
      Callback<CaseCollection> caseCollection);

  @GET("/cases?display_type=full&order=desc&sort=updated_at&return_fields="
      + InternalConstants.CASES_ADAPTER_RETURN_FIELDS) void retrieveCases(@Query("page") int page,
      Callback<CaseCollection> caseCollection);

  @GET("/cases?display_type=full&order=desc&sort=updated_at&return_fields="
      + InternalConstants.CASES_ADAPTER_RETURN_FIELDS) void retrieveCases(
      @Query("initial_flow_id") int initialFlowId, @Query("page") int page,
      @Query("query") String query, Callback<CaseCollection> caseCollection);

  @GET("/cases?display_type=full&order=desc&sort=updated_at&return_fields="
      + InternalConstants.CASES_ADAPTER_RETURN_FIELDS) void retrieveCases(@Query("page") int page,
      @Query("query") String query, Callback<CaseCollection> caseCollection);

  @GET("/cases/{case_id}?display_type=full&return_fields="
      + InternalConstants.CASE_DETAILS_RETURN_FIELDS) void retrieveCase(@Path("case_id") int caseId,
      Callback<SingleCaseCollection> callback);

  @GET("/search/reports/items?return_fields=" + InternalConstants.REPORT_ITEM_LISTING_RETURN_FIELDS)
  void retrieveFilteredReportItems(@Query("page") int page, @QueryMap Map<String, Object> options,
      Callback<ReportItemCollection> cb);

  @GET("/search/reports/items?return_fields=" + InternalConstants.REPORT_ITEM_LISTING_RETURN_FIELDS)
  void retrieveReportItemsListing(@Query("page") int page, @QueryMap Map<String, Object> options,
      Callback<ReportItemCollection> cb);

  @GET("/search/reports/items?return_fields=" + InternalConstants.REPORT_ITEM_LISTING_RETURN_FIELDS)
  void retrieveReportItemsByAddressOrProtocol(@Query("page") int page, @Query("query") String query,
      Callback<ReportItemCollection> cb);

  @GET("/search/reports/items?return_fields=" + InternalConstants.REPORT_ITEM_LISTING_RETURN_FIELDS)
  void retrieveFilteredReportItemsByAddressOrProtocol(@Query("page") int page,
      @Query("query") String query, @QueryMap Map<String, Object> options,
      Callback<ReportItemCollection> cb);

  @GET("/search/reports/items?return_fields=" + InternalConstants.REPORT_ITEM_LISTING_RETURN_FIELDS)
  ReportItemCollection retrieveReportItemsByAddressOrProtocol(@Query("page") int page,
      @Query("query") String query);

  @GET("/search/reports/items?clusterize=true&return_fields="
      + InternalConstants.REPORT_ITEM_MAP_RETURN_FIELDS) ReportItemCollection retrieveReportItems(
      @Query("position[latitude]") float latitude, @Query("position[longitude]") float longitude,
      @Query("position[distance]") float distance, @Query("limit") int limit,
      @Query("zoom") int zoom);

  @GET("/reports/items/{item_id}/history?return_fields="
      + InternalConstants.REPORT_ITEM_HISTORY_RETURN_FIELDS) void retrieveReportItemHistory(
      @Path("item_id") int itemId, Callback<ReportHistoryItemCollection> cb);

  @GET("/reports/items/{item_id}/history?return_fields="
      + InternalConstants.REPORT_ITEM_HISTORY_RETURN_FIELDS)
  ReportHistoryItemCollection retrieveReportItemHistory(@Path("item_id") int itemId);

  @GET("/reports/categories/{category_id}/items/{item_id}/notifications/last?return_fields="
      + InternalConstants.REPORT_ITEM_NOTIFICATION_RETURN_FIELDS)
  void retrieveLastReportNotification(@Path("item_id") int itemId,
      @Path("category_id") int categoryId, Callback<ReportNotificationCollection> cb);

  @GET("/reports/categories/{category_id}/items/{item_id}/notifications/history?return_fields="
      + InternalConstants.REPORT_ITEM_NOTIFICATION_RETURN_FIELDS)
  void retrieveReportNotificationCollection(@Path("item_id") int itemId,
      @Path("category_id") int categoryId, Callback<ReportNotificationCollection> cb);

  @GET("/reports/categories?display_type=full&return_fields="
      + InternalConstants.REPORT_CATEGORY_RETURN_FIELDS)
  ReportCategoryCollection getReportCategories();

  @GET("/reports/items/{item_id}?return_fields="
      + InternalConstants.REPORT_ITEM_DETAILS_RETURN_FIELDS)
  SingleReportItemCollection retrieveReportItem(@Path("item_id") int itemId);

  @DELETE("/reports/items/{item_id}") Response deleteReportItem(@Path("item_id") int itemId);

  @POST("/reports/{item_id}/comments") CreateReportItemCommentResponse createReportItemComment(
      @Path("item_id") int itemId, @Body CreateReportItemCommentRequest request);

  @GET("/reports/items/{item_id}?return_fields="
      + InternalConstants.REPORT_ITEM_DETAILS_RETURN_FIELDS) void retrieveReportItem(
      @Path("item_id") int itemId, Callback<SingleReportItemCollection> cb);

  @POST("/reports/{categoryId}/items?return_fields="
      + InternalConstants.REPORT_ITEM_DETAILS_RETURN_FIELDS)
  SingleReportItemCollection createReportItem(@Path("categoryId") int categoryId,
      @Body CreateReportItemRequest body);

  @POST("/reports/{categoryId}/items?return_fields="
      + InternalConstants.REPORT_ITEM_DETAILS_RETURN_FIELDS)
  SingleReportItemCollection createReportItem(@Path("categoryId") int categoryId,
      @Body CreateArbitraryReportItemRequest body);

  @PUT("/reports/{categoryId}/items/{item_id}?return_fields="
      + InternalConstants.REPORT_ITEM_DETAILS_RETURN_FIELDS)
  SingleReportItemCollection updateReportItem(@Path("categoryId") int categoryId,
      @Path("item_id") int itemId, @Body CreateReportItemRequest body);

  @PUT("/reports/{categoryId}/items/{item_id}?return_fields="
      + InternalConstants.REPORT_ITEM_DETAILS_RETURN_FIELDS)
  SingleReportItemCollection updateReportItem(@Path("categoryId") int categoryId,
      @Path("item_id") int itemId, @Body CreateArbitraryReportItemRequest body);

  @PUT("/reports/{categoryId}/items/{item_id}/change_category?return_fields="
      + InternalConstants.REPORT_ITEM_DETAILS_RETURN_FIELDS)
  SingleReportItemCollection changeReportCategory(@Path("categoryId") int categoryId,
      @Path("item_id") int itemId, @Body ChangeCategoryReportRequest body);

  @FormUrlEncoded @PUT("/reports/{categoryId}/items/{item_id}/update_status?return_fields="
      + InternalConstants.REPORT_ITEM_DETAILS_RETURN_FIELDS)
  SingleReportItemCollection changeReportStatus(@Path("categoryId") int categoryId,
      @Path("item_id") int itemId, @Field("status_id") int statusId,
      @Field("case_conductor_id") int responsible);

  @FormUrlEncoded @PUT("/reports/{categoryId}/items/{item_id}/update_status?return_fields="
      + InternalConstants.REPORT_ITEM_DETAILS_RETURN_FIELDS)
  SingleReportItemCollection changeReportStatus(@Path("categoryId") int categoryId,
      @Path("item_id") int itemId, @Field("status_id") int statusId);

  @PUT("/reports/{categoryId}/items/{id}/forward?return_fields="
      + InternalConstants.REPORT_ITEM_DETAILS_RETURN_FIELDS)
  SingleReportItemCollection assignReportToGroup(@Path("categoryId") int categoryId,
      @Path("id") int itemId, @Body AssignReportToGroupRequest body);

  @POST("/users") SingleUserCollection createUser(@Query("email") String email,
      @Query("password") String password,
      @Query("password_confirmation") String password_confirmation, @Query("name") String name,
      @Query("phone") String phone, @Query("document") String document,
      @Query("address") String address, @Query("address_additional") String address_additional,
      @Query("postal_code") String postal_code, @Query("district") String district,
      @Query("city") String city);

  @GET("/utils/city-boundary/validate") PositionValidationResponse validatePosition(
      @Query("latitude") double latitude, @Query("longitude") double longitude);

  @FormUrlEncoded @PUT("/cases/{caseId}/finish") SingleCaseCollection finishCase(
      @Path("caseId") int caseId, @Field("resolution_state_id") int resolutionStateId);
}

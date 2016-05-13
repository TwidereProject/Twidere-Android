package org.mariotaku.microblog.library.statusnet.api;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.param.Path;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.microblog.library.statusnet.model.Group;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.Paging;
import org.mariotaku.microblog.library.twitter.model.ResponseList;
import org.mariotaku.microblog.library.twitter.model.Status;
import org.mariotaku.microblog.library.twitter.model.User;

/**
 * Created by mariotaku on 16/3/4.
 */
public interface GroupResources {

    @GET("/statusnet/groups/timeline/{group_id}.json")
    ResponseList<Status> getGroupStatuses(@Path("group_id") String groupId, @Query Paging paging) throws MicroBlogException;

    @GET("/statusnet/groups/timeline/{group_name}.json")
    ResponseList<Status> getGroupStatusesByName(@Path("group_name") String name, @Query Paging paging) throws MicroBlogException;

    @GET("/statusnet/groups/show.json")
    Group showGroup(@Query("group_id") String groupId) throws MicroBlogException;

    @GET("/statusnet/groups/show.json")
    Group showGroupByName(@Query("group_name") String groupName) throws MicroBlogException;

    @GET("/statusnet/groups/membership.json")
    ResponseList<User> getGroupMembers(@Query("group_id") String groupId, @Query Paging paging) throws MicroBlogException;

    @GET("/statusnet/groups/membership.json")
    ResponseList<User> getGroupMembersByName(@Query("group_name") String groupName, @Query Paging paging) throws MicroBlogException;

    @GET("/statusnet/groups/list.json")
    ResponseList<Group> getGroupsByScreenName(@Query("screen_name") String screenName) throws MicroBlogException;

    @GET("/statusnet/groups/list.json")
    ResponseList<Group> getGroups(@Query("id") String userId) throws MicroBlogException;

    @GET("/statusnet/groups/list_all.json")
    ResponseList<Group> getAllGroups(@Query Paging paging) throws MicroBlogException;

}

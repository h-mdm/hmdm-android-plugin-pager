package com.hmdm.pager.http;

import com.hmdm.pager.http.json.ServerResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ServerService {
    @GET( "{project}/rest/plugins/messaging/public/status/{id}/{status}" )
    Call<ServerResponse> updateMessageStatus(@Path("project") String project,
                                             @Path("id") int id,
                                             @Path("status") int status);

}

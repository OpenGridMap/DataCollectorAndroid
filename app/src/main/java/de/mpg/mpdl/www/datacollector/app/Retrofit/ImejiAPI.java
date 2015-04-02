package de.mpg.mpdl.www.datacollector.app.Retrofit;

import java.util.List;

import de.mpg.mpdl.www.datacollector.app.Model.DataItem;
import de.mpg.mpdl.www.datacollector.app.Model.User;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by allen on 01/04/15.
 */
public interface ImejiAPI {
    //You can use rx.java for sophisticated composition of requests
    //@GET("/users/{user}")
    //public Observable<User> fetchUser(@Path("user") String user);

    //or you can just get your model if you use json api
    @GET("/users/{user}")
    public User fetchUser(@Path("user") String user);

    //or if there are some special cases you can process your response manually
    //@GET("/users/{user}")
    //public Response fetchUsers(@Path("user") String user);

    //get all items
    //http://dev-faces.mpdl.mpg.de/imeji/rest/items
    @GET("/items")
    List<DataItem> getItems();

    //get one item by itemId
    //http://dev-faces.mpdl.mpg.de/imeji/rest/items/ju1rYDIm1EFE1f5
    @GET("/items/{itemId}")
    List<DataItem> getItemById(@Path("itemId") String itemId, Callback<Response> callback);
}

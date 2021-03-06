package de.mpg.mpdl.www.datacollector.app.ItemList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.melnykov.fab.FloatingActionButton;
import com.tuesda.walker.circlerefresh.CircleRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import de.mpg.mpdl.www.datacollector.app.Model.DataItem;
import de.mpg.mpdl.www.datacollector.app.Model.MetaDataLocal;
import de.mpg.mpdl.www.datacollector.app.R;
import de.mpg.mpdl.www.datacollector.app.Retrofit.MetaDataConverter;
import de.mpg.mpdl.www.datacollector.app.Retrofit.RetrofitClient;
import de.mpg.mpdl.www.datacollector.app.Workflow.UploadView.ReadyToUploadCollectionActivity;
import de.mpg.mpdl.www.datacollector.app.utils.DeviceStatus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * Created by allen on 02/04/15.
 */
public class ItemListFragment extends Fragment {
    /**
     * Encapsulates fetching the forecast and displaying it as a {@link android.widget.ListView} layout.
     */

    public static final String ARG_SECTION_NUMBER = "section_number";
    private ProgressDialog pDialog;
    public List<DataItem> dataList = new ArrayList<DataItem>();
    //public CustomListAdapter adapter;
    public  CustomSwipeAdapter adapter;
    //SwipeMenuListView listView;
    private ListView listView;
    private View rootView;
    private final String LOG_TAG = ItemListFragment.class.getSimpleName();
    private String collectionID = DeviceStatus.collectionID;
    private String username;
    private String password;
    private SharedPreferences mPrefs;
    private CircleRefreshLayout jellyLayout;
    private static Gson gson = new GsonBuilder()
            .serializeNulls()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    Callback<List<DataItem>> callback = new Callback<List<DataItem>>() {
        @Override
        public void success(List<DataItem> dataListFromServer, Response response) {
            //load all data from imeji
            //adapter =  new CustomListAdapter(getActivity(), dataList);
            List<DataItem> dataListLocal = new ArrayList<DataItem>();
            ActiveAndroid.beginTransaction();
            try {
                // here get the string of Metadata Json
                for (DataItem item : dataListFromServer) {
                    if (item.getCollectionId().equals(collectionID)) {
                        //convertMetaData(item);

                        MetaDataLocal metaDataLocal = MetaDataConverter.
                                metaDataToMetaDataLocal(item.getMetadata());
                        metaDataLocal.save();
                        item.setMetaDataLocal(metaDataLocal);
                        dataListLocal.add(item);
                        item.save();
                    }
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally{
                ActiveAndroid.endTransaction();
            }
            //Method 1 doesn't work :(
//            dataList = dataListLocal;
//            adapter.notifyDataSetChanged();

            //Method 2 dirty but works
            adapter =  new CustomSwipeAdapter(getActivity(), dataList);
            listView.setAdapter(adapter);


            jellyLayout.finishRefreshing();


            if (pDialog != null) {
                pDialog.hide();
            }
//            DeviceStatus.showSnackbar(rootView, "Data List is updated");


        }

        @Override
        public void failure(RetrofitError error) {
            Log.v(LOG_TAG, "get list failed");
            DeviceStatus.showSnackbar(rootView, "update data failed");

        }
    };




    Callback<Response> callbackDel = new Callback<Response>() {
        @Override
        public void success(Response response, Response response2) {
            List<DataItem> dataListLocal = new ArrayList<DataItem>();
            //dataList.remove();
            adapter =  new CustomSwipeAdapter(getActivity(), dataList);
            listView.setAdapter(adapter);
        }

        @Override
        public void failure(RetrofitError error) {
            Log.v(LOG_TAG, error.toString());
        }
    };


    public ItemListFragment() {
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
        mPrefs = getActivity().getSharedPreferences("myPref", 0);
        username = mPrefs.getString("username", "");
        password = mPrefs.getString("password", "");
    }

    @Override
    public void onStart() {
        super.onStart();
        updateDataItem();
    }

    @Override
    public void onResume(){
        super.onResume();
    }
    @Override
    public void onPause(){
        super.onPause();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        hidePDialog();
    }

    private void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_section_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateDataItem();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Loading...");
            pDialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        dataList = new Select()
                .from(DataItem.class)
                .where("isLocal != ?", 1)
                .execute();

        adapter =  new CustomSwipeAdapter(getActivity(), dataList);
        //adapter =  new CustomListAdapter(getActivity(), dataList);

        //TODO try to change the cell view
        rootView = inflater.inflate(R.layout.fragment_section_list, container, false);
        //rootView = inflater.inflate(R.layout.fragment_section_list_swipe, container, false);
        //delete = (Button) rootView.findViewById(R.id.delete);
        listView = (ListView) rootView.findViewById(R.id.item_list);
        //listView = (SwipeMenuListView) rootView.findViewById(R.id.listView);
        listView.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.attachToListView(listView);


        jellyLayout = (CircleRefreshLayout) rootView.findViewById(R.id.jelly_refresh);
        jellyLayout.setOnRefreshListener(
                new CircleRefreshLayout.OnCircleRefreshListener() {
                    @Override
                    public void refreshing() {
                        // do something when refresh starts
                        updateDataItem();
                    }

                    @Override
                    public void completeRefresh() {
                        // do something when refresh complete
                        //jellyLayout.finishRefreshing();
                    }
                });

        // set creator
        //listView.setMenuCreator(creator);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ReadyToUploadCollectionActivity.class);
                startActivity(intent);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                DataItem dataItem = (DataItem) adapter.getItem(position);

                Intent showDetailIntent = new Intent(getActivity(), DetailActivity.class);
                showDetailIntent.putExtra(Intent.EXTRA_TEXT, dataItem.getFilename());
                startActivity(showDetailIntent);
            }
        });

//        delete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //deleteDataItem();
//                showToast("delete from"+ LOG_TAG);
//            }
//        });
        //listView.setOnScrollListener();
        return rootView;
    }



    private void updateDataItem(){
        // Showing progress dialog before making http request
        RetrofitClient.getItems(callback, username, password);
    }

    private void deleteDataItem(String itemId){
        // Showing progress dialog before making http request
        RetrofitClient.deleteItem(itemId, callbackDel, username, password);
    }
}
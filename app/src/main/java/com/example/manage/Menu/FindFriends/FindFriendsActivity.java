package com.example.manage.Menu.FindFriends;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manage.Adapter.FindFriendsAdapter;
import com.example.manage.Helpers.FirebaseDatabaseReferences;
import com.example.manage.Module.Contacts;
import com.example.manage.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.util.Objects;

public class FindFriendsActivity extends AppCompatActivity {
    // TODO: Crashes when navigating back from the ProfileActivity

    private final FirebaseDatabaseReferences firebaseDatabaseReferences = new FirebaseDatabaseReferences();
    private RecyclerView rvFindFriends;
    private String selectedFilter = "name";
    private FindFriendsAdapter adapter;
    private String lastQueryText = "";
    private FirebaseRecyclerOptions<Contacts> options;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_firends);


        rvFindFriends = findViewById(R.id.rv_find_friends);
        rvFindFriends.setLayoutManager(new LinearLayoutManager(this));

        Toolbar mToolbar = findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        // Create the adapter here and set it to the RecyclerView
        options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(firebaseDatabaseReferences.getUsersRef(), Contacts.class)
                .build();
        adapter = new FindFriendsAdapter(options, selectedFilter);
        rvFindFriends.setAdapter(adapter);

        executeSearch(lastQueryText);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        } else {
            Log.e("FindFriendsActivity", "onStart: Adapter is null");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        } else {
            Log.e("FindFriendsActivity", "onStop: Adapter is null");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.startListening();
        } else {
            Log.e("FindFriendsActivity", "onResume: Adapter is null");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (adapter != null) {
            adapter.stopListening();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        customizeSearchView(searchView, searchItem);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_filter) {
            View menuItemView = findViewById(R.id.action_filter);
            showFilterPopup(menuItemView);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFilterPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.inflate(R.menu.menu_filter);

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_name) {
                selectedFilter = "name";
            } else if (item.getItemId() == R.id.action_username) {
                selectedFilter = "username";
            }

            executeSearch(lastQueryText);

            return true;
        });

        popup.show();
    }

    private void customizeSearchView(@NonNull SearchView searchView, MenuItem searchItem) {
        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_button);
        searchIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_IN);

        ImageView closeIcon = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        closeIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_IN);

        // Hide the close icon initially
        closeIcon.setVisibility(View.GONE);

        // Show the close icon when the SearchView is clicked
        searchView.setOnSearchClickListener(v -> closeIcon.setVisibility(View.VISIBLE));

        // Hide the close icon when the SearchView is closed
        searchView.setOnCloseListener(() -> {
            closeIcon.setVisibility(View.GONE);
            return false;
        });

        closeIcon.setOnClickListener(v -> {
            // Clear the search view query
            searchView.setQuery("", false);
            // Clear the search view focus
            searchView.clearFocus();

            searchView.setIconified(true);
            // Hide the close icon
            closeIcon.setVisibility(View.GONE);
            // Refresh the list
            executeSearch("");
            // Collapse the SearchView
            searchItem.collapseActionView();
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                lastQueryText = newText;
                executeSearch(newText);

                return true;
            }
        });
    }

    private void executeSearch(@NonNull String queryText) {
        if (!queryText.isEmpty()) {
            options = new FirebaseRecyclerOptions.Builder<Contacts>()
                    .setQuery(
                            firebaseDatabaseReferences.getUsersRef().orderByChild(selectedFilter)
                                    .startAt(queryText)
                                    .endAt(queryText + "\uf8ff"),
                            Contacts.class
                    )
                    .build();

        } else {
            // If query text is empty, reset the adapter to show the full list
            options = new FirebaseRecyclerOptions.Builder<Contacts>()
                    .setQuery(firebaseDatabaseReferences.getUsersRef(), Contacts.class)
                    .build();

        }
        if (adapter != null) {
            adapter.updateOptions(options);
        } else {
            adapter = new FindFriendsAdapter(options, selectedFilter);
            rvFindFriends.setAdapter(adapter);
        }
        if (adapter != null) {
            adapter.startListening();
            adapter.changeFilterType(selectedFilter);
        }
    }


}

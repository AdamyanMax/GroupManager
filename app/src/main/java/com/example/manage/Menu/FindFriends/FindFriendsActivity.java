package com.example.manage.Menu.FindFriends;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manage.Adapter.FindFriendsAdapter;
import com.example.manage.Contacts.Contacts;
import com.example.manage.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class FindFriendsActivity extends AppCompatActivity {

    private RecyclerView rvFindFriends;
    private DatabaseReference UsersReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_firends);

        UsersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        rvFindFriends = findViewById(R.id.rv_find_friends);
        rvFindFriends.setLayoutManager(new LinearLayoutManager(this));

        Toolbar mToolbar = findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        // Customize the search view
        customizeSearchView(searchView, searchItem);

        return true;
    }

    private void customizeSearchView(@NonNull SearchView searchView, MenuItem searchItem) {
        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_button);
        searchIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_IN);

        ImageView closeIcon = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        closeIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_IN);

        searchView.setOnSearchClickListener(v -> closeIcon.setVisibility(View.VISIBLE));

        searchView.setOnCloseListener(() -> {
            closeIcon.setVisibility(View.GONE);
            return false;
        });

        closeIcon.setOnClickListener(v -> {
            searchView.setQuery("", false);
            searchView.setIconified(true);
            searchView.clearFocus();

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
                FirebaseRecyclerOptions<Contacts> options;

                if (newText.isEmpty()) {
                    options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(UsersReference, Contacts.class).build();
                } else {
                    options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(UsersReference.orderByChild("name")
                            .startAt(newText).endAt(newText + "\uf8ff"), Contacts.class).build();
                }

                FindFriendsAdapter adapter = new FindFriendsAdapter(options);

                rvFindFriends.setAdapter(adapter);

                adapter.startListening();

                return true;
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(UsersReference, Contacts.class).build();

        FindFriendsAdapter adapter = new FindFriendsAdapter(options);

        rvFindFriends.setAdapter(adapter);

        adapter.startListening();
    }

}

package com.example.manage.Menu;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manage.R;

import java.util.Objects;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView rvFindFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_firends);

        rvFindFriends = findViewById(R.id.rv_find_friends);
        rvFindFriends.setLayoutManager(new LinearLayoutManager(this));

        mToolbar = findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");
    }
}
package com.example.manage;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.manage.Adapter.TabAccessorAdapter;
import com.example.manage.Authentication.LoginActivity;
import com.example.manage.Menu.FindFriends.FindFriendsActivity;
import com.example.manage.Menu.SettingsActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

// TODOs for the whole project
// TODO: Replace Activities with Fragments
// TODO: Replace ProgressDialog with ProgressBar
// TODO: Clicking on the user in the FindFriends tab for the first time will cause for the 1st person in the list profile to be opened
// TODO: When in gallery clicking the back button causes the app to crash.
// TODO: Concatenate all 4 RVs into a single Adapter
public class MainActivity extends AppCompatActivity {

    private final String[] titles = new String[]{"Chats", "Groups", "Contacts", "Requests"};

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        RootRef = FirebaseDatabase.getInstance().getReference();


        ViewPager2 mViewPager2 = findViewById(R.id.main_tabs_pager);
        TabAccessorAdapter mTabAccessorAdapter = new TabAccessorAdapter(this);
        mViewPager2.setAdapter(mTabAccessorAdapter);

        TabLayout mTabLayout = findViewById(R.id.main_tabs);
        new TabLayoutMediator(mTabLayout, mViewPager2, ((tab, position) -> tab.setText(titles[position]))).attach();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null) {
            sendUserToLoginActivity();
        } else {
            verifyUserExistence();
        }
        Toolbar mToolBar = findViewById(R.id.main_app_bar);
        setSupportActionBar(mToolBar);
    }


    private void verifyUserExistence() {
        String currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!(snapshot.child("name").exists())) {
                    sendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu_nav, menu);

        // Set the text color of all items in the menu to colorPrimary.
        // Just delete the for loop, if you're satisfied with the current color
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            SpannableString spannableString = new SpannableString(menuItem.getTitle().toString());
            spannableString.setSpan(new ForegroundColorSpan(getColor(R.color.dark_teal)), 0, spannableString.length(), 0);
            menuItem.setTitle(spannableString);
        }

        return true;
    }




    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_find_friends_option) {
            sendUserToFindFriendsActivity();
        }

        if (item.getItemId() == R.id.main_create_group_option) {
            requestNewGroup();
        }

        if (item.getItemId() == R.id.main_settings_option) {
            sendUserToSettingsActivity();
        }

        if (item.getItemId() == R.id.main_logout_option) {
            mAuth.signOut();
            sendUserToLoginActivity();
        }

        return true;
    }


    private void requestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);

        // Inflate the custom view and set it as the dialog content
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.alert_dialogue_new_group, null);
        final EditText etGroupName = view.findViewById(R.id.et_group_name);
        builder.setView(view);

        builder.setTitle(R.string.enter_group_details);

        builder.setPositiveButton(R.string.create, (dialog, which) -> {
            String groupName = etGroupName.getText().toString();

            if (TextUtils.isEmpty(groupName)) {
                Toast.makeText(MainActivity.this, R.string.please_provide_a_name_for_the_group, Toast.LENGTH_SHORT).show();
            } else {
                createNewGroup(groupName);
            }
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void createNewGroup(String groupName) {
        RootRef.child("Groups").child(groupName).setValue("").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(MainActivity.this, groupName + " " + getString(R.string.group_is_created), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void sendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void sendUserToFindFriendsActivity() {
        Intent findFriendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }
}
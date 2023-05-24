package com.example.manage;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.manage.Adapter.TabAccessorAdapter;
import com.example.manage.Authentication.AuthenticationActivity;
import com.example.manage.Helpers.FirebaseUtil;
import com.example.manage.Menu.FindFriends.FindFriendsActivity;
import com.example.manage.Menu.SettingsActivity;
import com.example.manage.ui.CreateGroupSheetDialogFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

// TODOs for the whole project
// TODO: Replace Activities with Fragments
// TODO: Clicking on the user in the FindFriends tab for the first time will cause for the 1st person in the list profile to be opened
// TODO: When in gallery clicking the back button causes the app to crash.
// TODO: Concatenate all 4 RVs into a single Adapter
// TODO: Add online check for all activities except for Login/Signup activity
public class MainActivity extends AppCompatActivity {

    private final String[] titles = new String[]{"Chats", "Groups", "Requests"};
    private final FirebaseUtil firebaseUtil = new FirebaseUtil();
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        ViewPager2 mViewPager2 = findViewById(R.id.main_tabs_pager);
        TabAccessorAdapter mTabAccessorAdapter = new TabAccessorAdapter(this);
        mViewPager2.setOffscreenPageLimit(3);
        mViewPager2.setAdapter(mTabAccessorAdapter);

        TabLayout mTabLayout = findViewById(R.id.main_tabs);
        new TabLayoutMediator(mTabLayout, mViewPager2, ((tab, position) -> tab.setText(titles[position]))).attach();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            sendUserToLoginActivity();
        } else {
            updateUserStatus("online");
            verifyUserExistence();
        }
        Toolbar mToolBar = findViewById(R.id.main_app_bar);
        setSupportActionBar(mToolBar);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            updateUserStatus("offline");
        }
    }

    private void verifyUserExistence() {
        String currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        firebaseUtil.getUsersRef().child(currentUserID).addValueEventListener(new ValueEventListener() {
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
            updateUserStatus("offline");
            mAuth.signOut();
            sendUserToLoginActivity();
        }

        return true;
    }


    private void requestNewGroup() {
        CreateGroupSheetDialogFragment bottomSheetDialogFragment = new CreateGroupSheetDialogFragment();
        bottomSheetDialogFragment.show(getSupportFragmentManager(), "myBottomSheet");
    }

//    private void createNewGroup(String groupName) {
//        firebaseUtil.getGroupsRef().child(groupName).setValue("").addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                Toast.makeText(MainActivity.this, groupName + " " + getString(R.string.group_is_created), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, AuthenticationActivity.class); // TODO: Change to AuthenticationActivity
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

    private void updateUserStatus(String state) {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd yyyy", Locale.getDefault());
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        String currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        firebaseUtil.getUsersRef().child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);

    }
}
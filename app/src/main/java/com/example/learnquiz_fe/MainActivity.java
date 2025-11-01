package com.example.learnquiz_fe;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.learnquiz_fe.ui.fragments.quiz.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // loads the XML

        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment selected = null;

            // Avoid reloading the same fragment if it's already selected
            if (id == bottomNavigation.getSelectedItemId() && getSupportFragmentManager().findFragmentById(R.id.fragment_container) != null) {
                return true; // Already on the selected fragment, do nothing.
            }

            if (id == R.id.nav_home) {
                selected = new HomeFragment();
            } else if (id == R.id.nav_my_quizzes) {
                Toast.makeText(this, "Navigating to my quizzies", Toast.LENGTH_SHORT).show();
//                selected = new QuizListFragment();
            } else if (id == R.id.nav_create_quiz) {
                Toast.makeText(this, "Navigating to create quiz", Toast.LENGTH_SHORT).show();
//                selected = new CreateQuizFragment();
            } else if (id == R.id.nav_profile) {
                Toast.makeText(this, "Navigating to my profile", Toast.LENGTH_SHORT).show();
//                selected = new ProfileFragment();
            }

            if (selected != null) {
                loadFragment(selected);
                return true;
            }
            return false;
        });

        // Load the default fragment only when the activity is first created
        if (savedInstanceState == null) {
            // This will trigger the listener and load the HomeFragment,
            // and the BottomNavigationView will automatically highlight the correct item.
            // So no need to manual handle it
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}

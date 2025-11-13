package com.example.learnquiz_fe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.learnquiz_fe.data.model.auth.AuthResponse;
import com.example.learnquiz_fe.data.network.RetrofitClient;
import com.example.learnquiz_fe.ui.activities.HomeActivity;
import com.example.learnquiz_fe.ui.fragments.payment.UpgradePremiumFragment;
import com.example.learnquiz_fe.ui.fragments.quiz.HomeFragment;
// THÊM IMPORT NÀY:
import com.example.learnquiz_fe.ui.fragments.feedback.QuizFeedbackFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private RetrofitClient retrofitClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // loads the XML

        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        retrofitClient = RetrofitClient.getInstance(this);

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
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_profile) {
                var userName = retrofitClient.getUsername();
                String msg = "Navigating to profile of " + (userName != null ? userName : "Guest");
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
//                selected = new ProfileFragment();

                // ===== BẠN ĐÃ SỬA Ở ĐÂY =====
            } else if (id == R.id.testing) {

                String testQuizId = "69041c9e2060f334a4daa331";

                selected = QuizFeedbackFragment.newInstance(testQuizId);
            }


            if (selected != null) {

                loadFragment(selected, true);
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

    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        var transaction = getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }
}
package com.example.learnquiz_fe.ui.fragments.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.learnquiz_fe.R;
//import com.example.learnquiz_fe.ui.activities.AccountSettingsActivity; // Sẽ tạo ở bước 5
import com.example.learnquiz_fe.data.network.RetrofitClient;
import com.example.learnquiz_fe.helpers.LoginPreferences;
import com.example.learnquiz_fe.ui.activities.LoginActivity; // Sử dụng để logout
import com.example.learnquiz_fe.ui.fragments.payment.UpgradePremiumFragment;
import com.example.learnquiz_fe.ui.viewmodel.LoginViewModel;
import com.google.android.material.button.MaterialButton;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private CircleImageView ivProfilePicture;
    private ImageView ivEditProfile;
    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private MaterialButton btnUpgradePremiumHeader; // MaterialButton
    private TextView tvQuizzesCreatedCount;
    private TextView tvQuizzesTakenCount;
    private TextView tvAvgScoreValue;

    private ConstraintLayout itemMyQuizzes;
    private ConstraintLayout itemQuizFolders;
    private ConstraintLayout itemQuizHistory;
    private ConstraintLayout itemAccountSettings;
    private ConstraintLayout itemUpgradePremium;
    private ConstraintLayout itemQuickTour;
    private ConstraintLayout itemNotifications;
    private ConstraintLayout itemLogout;
    private TextView tvAppVersion;
    private LoginViewModel loginViewModel;
    private LoginPreferences preferences;

    public ProfileFragment() {
        // Required empty public constructor
    }
    private void setupViewModel() {
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setupViewModel();
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        loadUserData(); // Tải dữ liệu người dùng (tạm thời hardcode)
        setupListeners();
    }

    private void initViews(View view) {
        ivProfilePicture = view.findViewById(R.id.iv_profile_picture);
        ivEditProfile = view.findViewById(R.id.iv_edit_profile);
        tvProfileName = view.findViewById(R.id.tv_profile_name);
        tvProfileEmail = view.findViewById(R.id.tv_profile_email);
        btnUpgradePremiumHeader = view.findViewById(R.id.btn_upgrade_premium_header);
        tvQuizzesCreatedCount = view.findViewById(R.id.tv_quizzes_created_count);
        tvQuizzesTakenCount = view.findViewById(R.id.tv_quizzes_taken_count);
        tvAvgScoreValue = view.findViewById(R.id.tv_avg_score_value);

        itemMyQuizzes = view.findViewById(R.id.item_my_quizzes);
        itemQuizFolders = view.findViewById(R.id.item_quiz_folders);
        itemQuizHistory = view.findViewById(R.id.item_quiz_history);
        itemAccountSettings = view.findViewById(R.id.item_account_settings);
        itemUpgradePremium = view.findViewById(R.id.item_upgrade_premium); // Dùng lại id từ header nếu muốn hành động giống nhau
        itemQuickTour = view.findViewById(R.id.item_quick_tour);
        itemNotifications = view.findViewById(R.id.item_notifications);
        itemLogout = view.findViewById(R.id.item_logout);
        tvAppVersion = view.findViewById(R.id.tv_app_version);

        // Check if user is premium to hide upgrade button if needed
        boolean isPremium = RetrofitClient.getInstance(getContext()).getIsPremium();
        if (isPremium) {
            btnUpgradePremiumHeader.setVisibility(View.GONE);
            itemUpgradePremium.setVisibility(View.GONE);
        } else {
            btnUpgradePremiumHeader.setVisibility(View.VISIBLE);
            itemUpgradePremium.setVisibility(View.VISIBLE);
        }
    }

    private void loadUserData() {
        // Tải ảnh profile, ví dụ dùng Glide
        // Glide.with(this).load("URL_TO_PROFILE_IMAGE").placeholder(R.drawable.ic_profile_placeholder).into(ivProfilePicture);
        // Hiện tại dùng placeholder mặc định

        var username = RetrofitClient.getInstance(null).getUsername();
        var email = RetrofitClient.getInstance(null).getEmail();

        tvProfileName.setText(username);
        tvProfileEmail.setText(email);

        // Dữ liệu thống kê giả định
        tvQuizzesCreatedCount.setText(String.valueOf(5));
        tvQuizzesTakenCount.setText(String.valueOf(23));
//        tvAvgScoreValue.setText(String.format(getString(R.string.average_score), 85)); // 85%

        tvAppVersion.setText(getString(R.string.app_version));
    }

    private void setupListeners() {
        ivEditProfile.setOnClickListener(v -> Toast.makeText(getContext(), "Edit Profile clicked!", Toast.LENGTH_SHORT).show());
        btnUpgradePremiumHeader.setOnClickListener(v -> {
            // Start upgrade fragment
            Fragment fragment = new UpgradePremiumFragment();

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        itemMyQuizzes.setOnClickListener(v -> Toast.makeText(getContext(), "My Quizzes clicked!", Toast.LENGTH_SHORT).show());
        itemQuizFolders.setOnClickListener(v -> Toast.makeText(getContext(), "Quiz Folders clicked!", Toast.LENGTH_SHORT).show());
        itemQuizHistory.setOnClickListener(v -> Toast.makeText(getContext(), "Quiz History clicked!", Toast.LENGTH_SHORT).show());

        itemAccountSettings.setOnClickListener(v -> Toast.makeText(getContext(), "Account Setting clicked!", Toast.LENGTH_SHORT).show());

        itemQuickTour.setOnClickListener(v -> Toast.makeText(getContext(), "Quick Tour clicked!", Toast.LENGTH_SHORT).show());
        itemNotifications.setOnClickListener(v -> Toast.makeText(getContext(), "Notifications clicked!", Toast.LENGTH_SHORT).show());

        itemLogout.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Logging out...", Toast.LENGTH_SHORT).show();
            loginViewModel.logout();
            // Clear preferences nếu cần
            preferences = new LoginPreferences(requireContext());
            preferences.clear();
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Xóa hết stack activity
                startActivity(intent);
                getActivity().finish();
            }
        });

        // Premium redirect
        btnUpgradePremiumHeader.setOnClickListener(v -> {
            Fragment fragment = new UpgradePremiumFragment();

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        itemUpgradePremium.setOnClickListener(v -> {
            Fragment fragment = new UpgradePremiumFragment();

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

    }
}
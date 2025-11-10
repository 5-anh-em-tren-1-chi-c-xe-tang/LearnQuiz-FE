package com.example.learnquiz_fe.ui.fragments.payment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.learnquiz_fe.R;
import com.example.learnquiz_fe.ui.activities.payment.PayActivity;

public class UpgradePremiumFragment extends Fragment {
    private ImageButton btnBack;
    private Button btnUpgrade;
    private RadioButton rbMonthly, rbYearly;
    private String planOption;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upgrade_premium, container, false);

        btnBack = view.findViewById(R.id.btnBack);
        btnUpgrade = view.findViewById(R.id.btnUpgrade);
        rbMonthly = view.findViewById(R.id.rbMonthly);
        rbYearly = view.findViewById(R.id.rbYearly);

        btnBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        btnUpgrade.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), PayActivity.class);
            if (planOption == null) planOption = "monthly";
            intent.putExtra("plan_option", planOption);
            startActivity(intent);
        });

        RadioGroup planGroup = view.findViewById(R.id.planGroup);

        planGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbMonthly) {
                planOption = "monthly";
            } else if (checkedId == R.id.rbYearly) {
                planOption = "yearly";
            }
        });

        rbMonthly.setChecked(true);

        return view;
    }
}


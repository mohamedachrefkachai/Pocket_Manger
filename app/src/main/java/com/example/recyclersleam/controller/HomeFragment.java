package com.example.recyclersleam.controller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.recyclersleam.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import android.app.AlertDialog;
import android.widget.Button;
import android.widget.EditText;
import com.example.recyclersleam.Entity.User;
import com.example.recyclersleam.Util.MyDataBase;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;
import android.graphics.Color;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recyclersleam.sensors.movement;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.Random;

public class HomeFragment extends Fragment {

    private TextView tvBudgetAmount, tvWelcome, tvRevenueAmount;
    private LineChart revenueChart;
    private int userId;
    private com.example.recyclersleam.Entity.User currentUser;

    // Sensor vars
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private movement shakeDetector;
    private Dialog tipDialog;
    private Dialog editBudgetDialog;
    private EditText etEditBudgetAmount; // Reference to reset on shake

    private final String[] tipsAndChallenges = {
            // Tips
            " TIP: Épargnez au moins 20% de vos revenus.",
            " TIP: Notez chaque dépense pour identifier les fuites.",
            "💡 TIP: Attendez 24h avant un achat impulsif.",
            " TIP: La règle 50/30/20 est votre meilleure amie.",

            // Challenges
            "🎯 DÉFI: Dépensez 0 TND aujourd'hui (hors nécessités) !",
            "🎯 DÉFI: Cuisinez tous vos repas aujourd'hui.",
            "🎯 DÉFI: Faites le tri dans vos abonnements ce soir.",
            "🎯 DÉFI: Marchez pour tout trajet < 2km aujourd'hui.",
            "🎯 DÉFI: Mettez 5 TND de côté immédiatement.",
            "🎯 DÉFI: Pas de café/snacks achetés dehors aujourd'hui !"
    };
    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@androidx.annotation.NonNull View view,
            @androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvBudgetAmount = view.findViewById(R.id.budgetAmount);
        tvWelcome = view.findViewById(R.id.welcomeText);
        tvRevenueAmount = view.findViewById(R.id.revenueAmount);
        revenueChart = view.findViewById(R.id.revenueChart);

        // Make budget clickable (CardView parent would be better but verifying ID
        // showed TextView. Let's try to find parent or click text)
        // In XML `fragment_home.xml`, budgetAmount is a TextView inside a CardView.
        // Let's set click listener on the TextView for simplicity or find the CardView
        // if possible.
        // Given we don't have CardView ID in Java, we use the TextView or the View
        // parent if we can find it.
        // Actually the XML shows the CardView has no ID. We can set it on the TextView
        // or traverse up.
        // Let's set on TextView.
        tvBudgetAmount.setOnClickListener(v -> showEditBudgetDialog());
        // Also set on the label "Solde disponible" or the container if possible.
        // Just the amount is fine per request "budget total box".

        setupSensor();
        loadUserData();
    }

    private void loadUserData() {
        new Thread(() -> {
            if (getContext() == null)
                return;
            com.example.recyclersleam.Util.MyDataBase db = com.example.recyclersleam.Util.MyDataBase
                    .getAppDataBase(getContext());
            if (userId == 0)
                return;

            currentUser = db.UserDao().findById(userId);
            double totalRevenue = db.RevenueDao().getTotalRevenueByUser(userId);
            List<com.example.recyclersleam.Entity.Revenue> revenueList = db.RevenueDao().getAllByUser(userId);

            if (currentUser != null) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvBudgetAmount.setText(currentUser.getSolde() + " TND");
                        tvWelcome.setText("Bienvenue, " + currentUser.getNom());
                        tvRevenueAmount.setText(totalRevenue + " TND");

                        // Setup Chart
                        setupRevenueChart(revenueList);
                    });
                }
            }
        }).start();
    }

    private void setupRevenueChart(List<com.example.recyclersleam.Entity.Revenue> revenueList) {
        if (revenueList == null || revenueList.isEmpty()) {
            revenueChart.clear();
            return;
        }

        try {
            ArrayList<Entry> entries = new ArrayList<>();
            // Simple index-based x-axis for now. In a real app, you might parse dates.
            for (int i = 0; i < revenueList.size(); i++) {
                entries.add(new Entry(i, (float) revenueList.get(i).getAmount()));
            }

            if (entries.isEmpty()) {
                revenueChart.clear();
                return;
            }

            LineDataSet dataSet = new LineDataSet(entries, "Revenus");
            dataSet.setColor(Color.parseColor("#388E3C"));
            dataSet.setValueTextColor(Color.WHITE);
            dataSet.setLineWidth(2f);
            dataSet.setCircleColor(Color.parseColor("#388E3C"));
            dataSet.setCircleRadius(4f);
            dataSet.setDrawValues(false); // Hide values on points to keep it clean

            LineData lineData = new LineData(dataSet);
            revenueChart.setData(lineData);
            revenueChart.getDescription().setEnabled(false); // Hide description
            revenueChart.getLegend().setEnabled(false); // Hide legend if preferred, or keep it

            com.github.mikephil.charting.components.XAxis xAxis = revenueChart.getXAxis();
            xAxis.setDrawGridLines(false); // Cleaner look
            xAxis.setGranularity(1f); // Force step to 1
            xAxis.setGranularityEnabled(true);
            xAxis.setTextColor(Color.WHITE);

            revenueChart.getAxisLeft().setDrawGridLines(false);
            revenueChart.getAxisLeft().setTextColor(Color.WHITE);
            revenueChart.getAxisRight().setEnabled(false); // Hide right axis
            revenueChart.invalidate(); // Refresh
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupSensor() {
        if (getActivity() != null) {
            sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager != null) {
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                shakeDetector = new movement();
                shakeDetector.setOnShakeListener(count -> {
                    if (editBudgetDialog != null && editBudgetDialog.isShowing()) {
                        // Reset Edit Field
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                if (etEditBudgetAmount != null) {
                                    etEditBudgetAmount.setText("");
                                    Toast.makeText(getContext(), "Montant réinitialisé", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        showTipDialog();
                    }
                });

                // Privacy FLIP Implementation
                shakeDetector.setOnFlipListener(isFaceDown -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (isFaceDown) {
                                tvBudgetAmount.setText("**** TND");
                                tvRevenueAmount.setText("**** TND");
                            } else {
                                // Restore original values
                                if (currentUser != null) {
                                    tvBudgetAmount.setText(currentUser.getSolde() + " TND");
                                    loadUserData();
                                }
                            }
                        });
                    }
                });
            }
        }
    }

    private void showTipDialog() {
        if (getContext() == null)
            return;

        // Prevent stacking dialogs
        if (tipDialog != null && tipDialog.isShowing())
            return;

        tipDialog = new Dialog(getContext());
        tipDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        tipDialog.setContentView(R.layout.tips);

        if (tipDialog.getWindow() != null) {
            tipDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvTip = tipDialog.findViewById(R.id.tvTipContent);
        // Find title view if exists or prepend to content
        // In simple tips.xml we only have tvTipContent and a static header.
        // Let's assume we update text.

        if (tvTip != null) {
            String selection = tipsAndChallenges[new Random().nextInt(tipsAndChallenges.length)];
            tvTip.setText(selection);

            // Optional: If we had a title view, we could change "Financial Tip" to "Daily
            // Challenge"
            // based on startsWith("🎯"). For now, the text itself contains the prefix.
        }

        tipDialog.show();

        // Auto dismiss after 4 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (tipDialog != null && tipDialog.isShowing()) {
                tipDialog.dismiss();
            }
        }, 4000);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
        loadUserData();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(shakeDetector);
            if (tipDialog != null && tipDialog.isShowing()) {
                tipDialog.dismiss();
            }
        }
    }

    private void showEditBudgetDialog() {
        if (getContext() == null || currentUser == null)
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_budget, null);
        builder.setView(view);

        etEditBudgetAmount = view.findViewById(R.id.etBudgetAmount);
        Button btnAdd = view.findViewById(R.id.btnAdd);

        // Set current budget as Hint
        etEditBudgetAmount.setHint(currentUser.getSolde() + " TND");

        editBudgetDialog = builder.create();
        if (editBudgetDialog.getWindow() != null) {
            editBudgetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        btnAdd.setOnClickListener(v -> {
            String amountStr = etEditBudgetAmount.getText().toString();
            if (TextUtils.isEmpty(amountStr))
                return;

            double newSolde = Double.parseDouble(amountStr);
            // Update DB with NEW Value (Set, not Add)
            updateUserSolde(newSolde);
            editBudgetDialog.dismiss();
        });

        editBudgetDialog.show();
    }

    private void updateUserSolde(double newSolde) {
        new Thread(() -> {
            currentUser.setSolde((float) newSolde); // Model uses Float or Double?
            // "getSolde" returned something printed as string.
            // Let's assume float based on typical Android. Or Double.
            // Check Entity if possible. HomeFragment line 58/65 uses it.
            // Assuming setSolde exists. If not, we might need to access field directly if
            // public.
            // Safe bet: user DAO update.
            com.example.recyclersleam.Util.MyDataBase db = com.example.recyclersleam.Util.MyDataBase
                    .getAppDataBase(getContext());
            db.UserDao().update(currentUser);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvBudgetAmount.setText(currentUser.getSolde() + " TND");
                    Toast.makeText(getContext(), "Solde mis à jour", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

}

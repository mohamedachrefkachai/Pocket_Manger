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
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;
import java.util.List;
import android.graphics.Color;

public class HomeFragment extends Fragment {

    private android.widget.TextView tvBudgetAmount, tvWelcome, tvRevenueAmount;
    private LineChart revenueChart;
    private int userId;
    private com.example.recyclersleam.Entity.User currentUser;

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

        loadUserData();
    }

    private void loadUserData() {
        new Thread(() -> {
            com.example.recyclersleam.Util.MyDataBase db = com.example.recyclersleam.Util.MyDataBase
                    .getAppDataBase(getContext());
            // If userId is not set, try to get from SharedPreferences or similar (omitted
            // for brevity, relying on Activity passing it)
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
        ArrayList<Entry> entries = new ArrayList<>();
        // Simple index-based x-axis for now. In a real app, you might parse dates.
        for (int i = 0; i < revenueList.size(); i++) {
            entries.add(new Entry(i, (float) revenueList.get(i).getAmount()));
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
    }
}

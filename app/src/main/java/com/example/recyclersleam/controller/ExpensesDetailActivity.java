package com.example.recyclersleam.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.recyclersleam.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

public class ExpensesDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses_detail);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        setupChart();
    }

    private void setupChart() {
        PieChart chart = findViewById(R.id.chartDetail);

        // Fake Data (same as dashboard but bigger view)
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(647f, ""));
        entries.add(new PieEntry(104f, ""));
        entries.add(new PieEntry(32f, ""));

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#6200EE")); // Purple Dark (Rent)
        colors.add(Color.parseColor("#03DAC6")); // Teal (Groceries)
        colors.add(Color.parseColor("#CF6679")); // Red (Sports)

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setDrawValues(false);

        PieData data = new PieData(dataSet);
        chart.setData(data);

        // Style
        chart.setDescription(null);
        chart.getLegend().setEnabled(false);
        chart.setHoleColor(Color.TRANSPARENT);
        chart.setHoleRadius(70f); // Bigger hole
        chart.setTransparentCircleRadius(0f);
        chart.setTouchEnabled(false); // Static display

        // Center Text
        chart.setCenterText("647.00 €\nRental");
        chart.setCenterTextColor(Color.WHITE);
        chart.setCenterTextSize(16f);

        chart.animateY(1000);
        chart.invalidate();
    }
}

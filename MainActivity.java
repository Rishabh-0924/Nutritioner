package eu.learning.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.math3.analysis.function.Add;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class MainActivity extends AppCompatActivity {
    private PieChart pieChart;
    private PieDataSet pieDataSet;
    private PieData pieData;
    private double defaultValue = 0.0d;
    private double TakeValue = 0.0d;
    private double completedValue = 0;
    private double AddValue = 0;
    AutoCompleteTextView autoCompleteTextViewFoodName;
    EditText editTextAmount;
    EditText TakeTextAmount;
    Button buttonCalculate;
    Button addCalculate;
    TextView textViewResult;
    TextView PieResult;
    Map<String, FoodData> foodDataMap = new HashMap<>();
    List<String> foodNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(getString(R.string.app_heading));
        autoCompleteTextViewFoodName = findViewById(R.id.autoCompleteTextViewFoodName);
        editTextAmount = findViewById(R.id.editTextAmount);
        TakeTextAmount = findViewById(R.id.TakeTextAmount);
        buttonCalculate = findViewById(R.id.buttonCalculate);
        textViewResult = findViewById(R.id.textViewResult);
        pieChart = findViewById(R.id.pieChart);
        addCalculate = findViewById(R.id.AddCalculate);
        PieResult = findViewById(R.id.PieResult);

        // Load Excel data
        loadExcelData();
        pieChart.setVisibility(View.GONE);
        addCalculate.setVisibility(View.GONE);
        TakeTextAmount.setVisibility(View.GONE);
        PieResult.setVisibility(View.GONE);
        TakeTextAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String input = s.toString();

                if (!input.isEmpty()) {
                    try {
                        defaultValue = Double.parseDouble(input);
                        TakeValue = defaultValue;
                        defaultValue -= completedValue;

                    } catch (NumberFormatException e) {

                    }
                } else {
                    defaultValue = 0.0d;
                }
                updatePieChart();
                if (defaultValue == 0.0d) {
                    pieChart.setVisibility(View.GONE);
                    PieResult.setVisibility(View.GONE);
                } else if (defaultValue <= 0.0d) {
                    pieChart.setVisibility(View.GONE);
                    PieResult.setVisibility(View.VISIBLE);
                } else {
                    pieChart.setVisibility(View.VISIBLE);
                    PieResult.setVisibility(View.GONE);
                }

            }
        });
        setupPieChart();
        addCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completedValue += AddValue;
                defaultValue -= AddValue;
                updatePieChart();
                if (defaultValue <= 0.0d) {
                    pieChart.setVisibility(View.GONE);
                    PieResult.setVisibility(View.VISIBLE);
                } else {
                    pieChart.setVisibility(View.VISIBLE);
                    PieResult.setVisibility(View.GONE);
                }
            }
        });
        // Set up the AutoCompleteTextView with the food names
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, foodNames);
        autoCompleteTextViewFoodName.setAdapter(adapter);

        buttonCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateIntake();
            }
        });

        Button buttonCalculate = findViewById(R.id.buttonCalculate);
        buttonCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateIntake();
            }
        });

    }

    private void loadExcelData() {
        try {
            // Access the Excel file from the assets folder
            InputStream inputStream = getAssets().open("food_data.xlsx");
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            // Iterate through the rows and load data into the map
            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue; // Skip header row
                String name = row.getCell(0).getStringCellValue();
                double calories = row.getCell(1).getNumericCellValue();
                double vitaminC = row.getCell(2).getNumericCellValue();
                foodDataMap.put(name.toLowerCase(), new FoodData(calories, vitaminC));
                foodNames.add(name); // Add the food name to the list for suggestions
            }
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this,
                    "Error loading Excel data: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void calculateIntake() {
        String foodName = autoCompleteTextViewFoodName.getText().toString().trim().toLowerCase();
        String amountStr = editTextAmount.getText().toString().trim();

        if (foodName.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter both food name and amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = 0;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }
        FoodData foodData = foodDataMap.get(foodName);

        if (foodData == null) {
            textViewResult.setText("Food item has negligible amount of VitaminC");
            return;
        }

        double totalCalories = (foodData.getCalories() / 100) * amount;
        double totalVitaminC = (foodData.getVitaminC() / 100) * amount;
        AddValue = totalVitaminC;

        TextView caloriesBox = findViewById(R.id.caloriesBox);
        TextView vitaminCBox = findViewById(R.id.vitaminCBox);
        LinearLayout resultBoxLayout = findViewById(R.id.resultBoxLayout);

        caloriesBox.setText(String.format("%.2f cal", totalCalories));
        vitaminCBox.setText(String.format("%.2f mg", totalVitaminC));

        resultBoxLayout.setVisibility(View.VISIBLE);
        textViewResult.setVisibility(View.GONE);
        addCalculate.setVisibility(View.VISIBLE);
        TakeTextAmount.setVisibility(View.VISIBLE);
    }

    public class MyValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            // Format the value to show 2 decimal places
            return String.format("%.2f", value);
        }
    }

    private void setupPieChart() {
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry((float) defaultValue, "Remaining"));
        pieEntries.add(new PieEntry((float) completedValue, "Completed"));

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.LTGRAY);
        colors.add(Color.GREEN);

        pieDataSet = new PieDataSet(pieEntries, "Progress");
        pieDataSet.setColors(colors);
        pieDataSet.setValueTextSize(12f);
        pieDataSet.setValueFormatter(new MyValueFormatter());
        pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.setCenterText(String.format("Progress\n%.2f mg", TakeValue));
        pieChart.setCenterTextSize(16f);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.animateY(1000);

        pieChart.invalidate();
    }

    private void updatePieChart() {
        pieChart.setCenterText(String.format("Progress\n%.2f mg", TakeValue));
        pieDataSet.getValues().clear();
        pieDataSet.addEntry(new PieEntry((float) defaultValue, "Remaining"));
        pieDataSet.addEntry(new PieEntry((float) (completedValue), "Completed"));

        pieChart.notifyDataSetChanged();
        pieChart.invalidate();
    }

    private static class FoodData {
        private final double calories;
        private final double vitaminC;

        public FoodData(double calories, double vitaminC) {
            this.calories = calories;
            this.vitaminC = vitaminC;
        }

        public double getCalories() {
            return calories;
        }

        public double getVitaminC() {
            return vitaminC;
        }
    }
}

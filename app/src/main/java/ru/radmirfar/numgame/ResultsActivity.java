package ru.radmirfar.numgame;

import static android.content.Intent.FLAG_ACTIVITY_NO_HISTORY;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class ResultsActivity extends AppCompatActivity {

    boolean timeLimited;
    String theme;
    int allNums;
    int correctNums;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_results);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        timeLimited = getIntent().getBooleanExtra("timeLimited", false);
        theme = getIntent().getStringExtra("theme");
        allNums = getIntent().getIntExtra("allNums", 0);
        correctNums = getIntent().getIntExtra("correctNums", 0);
        TextView resultsText = findViewById(R.id.resultsText);
        int incorrectNums = allNums - correctNums;
        PieChart pie = findViewById(R.id.pie); // находим диаграмму
        List<PieEntry> entries = new ArrayList<>(); // создаём и заполняем массив с данными
        entries.add(new PieEntry(correctNums, getString(R.string.correct_nums)));
        entries.add(new PieEntry(incorrectNums, getString(R.string.incorrect_nums)));
        PieDataSet set = new PieDataSet(entries, ""); // создаём набор значений из массива
        set.setColors(Color.GREEN, Color.RED); // цвета секторов
        PieData data = new PieData(set); // массив из массива
        data.setValueTextSize(16); // размер цифр
        data.setValueFormatter(new DefaultValueFormatter(0)); // убираем дробную часть
        pie.setData(data);
        pie.getLegend().setEnabled(false); // убираем легенду
        pie.getDescription().setEnabled(false); // и описание
        pie.setEntryLabelColor(Color.BLACK); // цвет подписей
        pie.setHoleColor(getColor(R.color.chalkboard)); // цвет дырки
        pie.setCenterText(theme);
        pie.setCenterTextColor(Color.WHITE); // цвет надписи внутри круга
        pie.setCenterTextSize(18); // и её размер
        //pie.setDrawHoleEnabled(false);
        pie.animateXY(1000, 1000);
        pie.invalidate();
        resultsText.setText(R.string.you_stink); // < 40%
        if (allNums != 0) {
            double percentage = (double) correctNums / allNums;
            if (percentage >= 0.6) resultsText.setText(R.string.good_work); // [60; 100]%
            else if (percentage >= 0.4) resultsText.setText(R.string.even_results); // [40; 60)%
        }
        findViewById(R.id.repeatTrainBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ResultsActivity.this, TrainActivity.class);
                intent.putExtra("timeLimited", timeLimited);
                intent.putExtra("theme", theme);
                intent.setFlags(FLAG_ACTIVITY_NO_HISTORY); // чтобы на activity нельзя было вернуться
                startActivity(intent);
            }
        });
    }
}
package ru.radmirfar.numgame;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.radmirfar.russian_numeral.Animacy;
import ru.radmirfar.russian_numeral.Case;
import ru.radmirfar.russian_numeral.Declension;
import ru.radmirfar.russian_numeral.DeclensionBuilder;
import ru.radmirfar.russian_numeral.Gender;
import ru.radmirfar.russian_numeral.Noun;
import ru.radmirfar.russian_numeral.RussianNumeral;
import ru.radmirfar.russian_numeral.Type;

public class StatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stats);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Noun numberNoun = new Noun(Gender.MASCULINE, Animacy.INANIMATE,
                getResources().getStringArray(R.array.num_sing),
                getResources().getStringArray(R.array.num_pl));
        Declension nom = new DeclensionBuilder(Case.NOMINATIVE).type(Type.CARDINAL).build();
        TextView statsText = findViewById(R.id.statsTextView);
        BarChart chart = findViewById(R.id.chart); // диаграмма
        DBHelper dbHelper = new DBHelper(StatsActivity.this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE,
                new String[]{DBHelper.COLUMN_THEME, DBHelper.COLUMN_ALL_NUMS, DBHelper.COLUMN_CORRECT_NUMS},
                null, null, null, null, "theme ASC");
        if (cursor.getCount() == 0) {
            statsText.setText(R.string.no_stats);
            chart.setVisibility(View.INVISIBLE);
            findViewById(R.id.clearStatsBtn).setVisibility(View.INVISIBLE);
        }
        else {
            cursor.moveToFirst();
            List<BarEntry> entries = new ArrayList<>(); // массив с данными
            for (int i = 0; i < cursor.getCount(); i++) { // заполняем массив данными из БД
                String theme = cursor.getString(0);
                int allNums = cursor.getInt(1), correctNums = cursor.getInt(2),
                        incorrectNums = allNums - correctNums;
                entries.add(new BarEntry(i, new float[]{correctNums, incorrectNums}, theme));
                cursor.moveToNext();
            }
            BarDataSet set = new BarDataSet(entries, ""); // создаём набор значений из массива
            set.setStackLabels(new String[]{getString(R.string.correct_nums), getString(R.string.incorrect_nums)}); // названия столбцов
            set.setColors(Color.GREEN, Color.RED); // цвета столбцов
            BarData data = new BarData(set); // один набор значений из другого
            data.setValueTextColor(Color.TRANSPARENT); // отключаем отображение меток, они налезают
            chart.setData(data);
            chart.getXAxis().setEnabled(false); // отключаем отображение оси абсцисс
            chart.getAxisRight().setEnabled(false); // и правой оси ординат
            chart.getAxisLeft().setTextColor(Color.WHITE); // цвет текста левой оси ординат
            chart.getAxisLeft().setTextSize(16); // и его размер
            chart.getLegend().setTextColor(Color.WHITE); // цвет текста легенды
            chart.getLegend().setTextSize(16); // и его размер
            chart.getDescription().setEnabled(false); // отключаем описание
            chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    String theme = (String) e.getData();
                    float[] vals = entries.get((int) e.getX()).getYVals();
                    int correct = (int) vals[0], incorrect = (int) vals[1];
                    // отображаем количество правильных и неправильных ответов, согласуя с сущ.
                    String correctNums = correct + " " +
                            RussianNumeral.getNumeralWithNoun(correct, numberNoun, nom)[1];
                    String incorrectNums = incorrect + " " +
                            RussianNumeral.getNumeralWithNoun(incorrect, numberNoun, nom)[1];
                    statsText.setText(getString(R.string.stats_details, theme, correctNums, incorrectNums));
                }
                @Override
                public void onNothingSelected() {
                    statsText.setText(R.string.click_to_view_stats);
                }
            });
            int animDuration = cursor.getCount() < 5 ? 1000 : 2000;
            chart.animateXY(animDuration, animDuration);
            chart.invalidate(); // выводим диаграмму
        }
        cursor.close();
        db.close();
        findViewById(R.id.clearStatsBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(StatsActivity.this);
                builder.setMessage(R.string.clear_stats_confirm);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        db.delete(DBHelper.TABLE, null, null);
                        finish();
                    }
                });
                builder.setNegativeButton(R.string.no, (dialogInterface, i) -> {});
                builder.create().show();
            }
        });
    }
}
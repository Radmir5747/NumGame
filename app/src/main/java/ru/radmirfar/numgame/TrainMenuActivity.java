package ru.radmirfar.numgame;

import static android.content.Intent.FLAG_ACTIVITY_NO_HISTORY;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TrainMenuActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_train_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Spinner spinner = findViewById(R.id.trainSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, getResources().getStringArray(R.array.train_options));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        RadioGroup radioGroup = findViewById(R.id.trainRadioGroup);
        findViewById(R.id.startTrainBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selected = radioGroup.getCheckedRadioButtonId();
                boolean timeLimited = selected == R.id.limitedTimeRadioBtn;
                String theme = adapter.getItem(spinner.getSelectedItemPosition());
                Intent intent = new Intent(TrainMenuActivity.this, TrainActivity.class);
                intent.putExtra("timeLimited", timeLimited);
                intent.putExtra("theme", theme);
                intent.setFlags(FLAG_ACTIVITY_NO_HISTORY); // чтобы на activity нельзя было вернуться
                startActivity(intent);
            }
        });
    }
}
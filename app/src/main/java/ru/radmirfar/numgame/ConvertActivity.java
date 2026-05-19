package ru.radmirfar.numgame;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.io.InputStream;

import ru.radmirfar.russian_numeral.*;

public class ConvertActivity extends AppCompatActivity {
    String webViewContents = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_convert);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        WebView webView = findViewById(R.id.resultsWebview);
        displayInWebView(webView, "", true); // загружаем пустой webview, чтобы не было мерцания
        RadioGroup radioGroup = findViewById(R.id.convertRadioGroup);
        CheckBox checkBox = findViewById(R.id.animacyCheckbox);
        EditText input = findViewById(R.id.inputNumberConvert);
        // при повороте экрана восстанавливаем содержимое
        if (savedInstanceState != null) {
            webViewContents = savedInstanceState.getString("webViewContents");
            if (webViewContents != null) displayInWebView(webView, webViewContents, false);
        }
        findViewById(R.id.convertNumBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewCompat.getWindowInsetsController(findViewById(R.id.main)).hide(WindowInsetsCompat.Type.ime());
                String inputText = input.getText().toString();
                int num;
                try {
                    num = Integer.parseInt(inputText);
                    if (num == Integer.MIN_VALUE) throw new Exception(); // ошибка библиотеки
                }
                catch (Exception e) {
                    webViewContents = getString(R.string.incorrect_data);
                    displayInWebView(webView, webViewContents, true);
                    return;
                }
                int selected = radioGroup.getCheckedRadioButtonId();
                DeclensionBuilder d = new DeclensionBuilder(Case.NOMINATIVE);
                Type type = Type.CARDINAL;
                Animacy animacy;
                if (selected == R.id.ordinalRadioBtn) type = Type.ORDINAL;
                else if (selected == R.id.collectivelRadioBtn) type = Type.COLLECTIVE;
                animacy = checkBox.isChecked() ? Animacy.ANIMATE : Animacy.INANIMATE;
                d.type(type).animacy(animacy);
                if (type == Type.COLLECTIVE && (num < 2 || num > 10)) { // собирательных числительных больше 10 и меньше 2 нет
                    webViewContents = getString(R.string.incorrect_data);
                    displayInWebView(webView, webViewContents, true);
                    return;
                }
                webViewContents = getTable(num, d.build());
                displayInWebView(webView, webViewContents, false);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("webViewContents", webViewContents);
    }

    /**
     * Выдаёт таблицу склонения в зависимости от числа
     * @param num число
     * @param baseDeclension грамматические характеристики (тип, одушевлённость)
     * @return таблица склонения
     */
    String getTable(int num, Declension baseDeclension) {
        String res = "";
        Helper.ParadigmType formType = Helper.getParadigmType(num, baseDeclension.getType());
        try {
            String[] forms = {};
            String paradigmSource = "";
            switch (formType) {
                case CASE: {
                    forms = new String[6];
                    for (Case gramCase : Case.values()) {
                        Declension d = new DeclensionBuilder(baseDeclension)
                                .gramCase(gramCase)
                                .build();
                        forms[gramCase.ordinal()] = RussianNumeral.getNumeral(num, d);
                    }
                    paradigmSource = "paradigms/paradigm_case.html";
                    break;
                }
                case CASE_GENDER_COUNT: {
                    forms = new String[6 * 4];
                    for (Case gramCase : Case.values()) {
                        for (Gender gender : Gender.values()) {
                            Declension d = new DeclensionBuilder(baseDeclension)
                                    .gramCase(gramCase)
                                    .gender(gender)
                                    .count(Count.SINGULAR)
                                    .build();
                            forms[gramCase.ordinal() * 4 + gender.ordinal()] = RussianNumeral.getNumeral(num, d);
                        }
                        Declension d = new DeclensionBuilder(baseDeclension)
                                .gramCase(gramCase)
                                .gender(Gender.MASCULINE)
                                .count(Count.PLURAL)
                                .build();
                        forms[gramCase.ordinal() * 4 + 3] = RussianNumeral.getNumeral(num, d);
                    }
                    paradigmSource = "paradigms/paradigm_case_gender_count.html";
                    break;
                }
                case CASE_COUNT: {
                    forms = new String[6 * 2];
                    for (Case gramCase : Case.values()) {
                        for (Count count : Count.values()) {
                            Declension d = new DeclensionBuilder(baseDeclension)
                                    .gramCase(gramCase)
                                    .count(count)
                                    .build();
                            forms[gramCase.ordinal() * 2 + count.ordinal()] = RussianNumeral.getNumeral(num, d);
                        }
                    }
                    paradigmSource = "paradigms/paradigm_case_count.html";
                    break;
                }
                case CASE_GENDER: {
                    forms = new String[6 * 3];
                    for (Case gramCase : Case.values()) {
                        for (Gender gender : Gender.values()) {
                            Declension d = new DeclensionBuilder(baseDeclension)
                                    .gramCase(gramCase)
                                    .gender(gender)
                                    .count(Count.SINGULAR)
                                    .build();
                            forms[gramCase.ordinal() * 3 + gender.ordinal()] = RussianNumeral.getNumeral(num, d);
                        }
                    }
                    paradigmSource = "paradigms/paradigm_case_gender.html";
                    break;
                }
            }
            try {
                InputStream inputStream = getAssets().open(paradigmSource);
                byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer);
                inputStream.close();
                String template = new String(buffer);
                res = String.format(template, forms);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        catch (Exception e) {
            res = "<p>" + getString(R.string.error_occurred) + " " + e.getMessage() + "</p>";
        }
        return res;
    }
    void displayInWebView(WebView webView, String data, boolean encloseInP) {
        if (encloseInP) data = "<html><head></head><link rel=\"stylesheet\" href=\"styles.css\"><body><p>" + data + "</p></body></html>";
        webView.loadDataWithBaseURL("file:///android_asset/paradigms/", data, "text/html", "ru_RU", null);
        if (webView.getVisibility() != View.VISIBLE) webView.setVisibility(View.VISIBLE);
    }
}
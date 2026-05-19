package ru.radmirfar.numgame;

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;

import java.util.ArrayList;

import ru.radmirfar.russian_numeral.*;

public class Helper {
    final static int CASE_COUNT = Case.values().length;
    final static int GENDER_COUNT = Gender.values().length;
    final static int COUNT_COUNT = Count.values().length;
    final static int ANIMACY_COUNT = Animacy.values().length;
    static DiffMatchPatch diffMatchPatch = new DiffMatchPatch();
    /**
     * <p>Выдаёт {@link ParadigmType парадигму склонения числительного} в зависимости от его типа и числа:</p>
     * <p>{@link ParadigmType#CASE} - изменение по падежам</p>
     * <p>{@link ParadigmType#CASE_GENDER_COUNT} изменение по падежам, родам и числам</p>
     * <p>{@link ParadigmType#CASE_COUNT} - изменение по падежам и числам</p>
     * <p>{@link ParadigmType#CASE_GENDER} - изменение по падежам и родам</p>
     * @param num число
     * @param type тип числительного (количественное, порядковое или собирательное)
     * @return тип парадигмы
     */
    static ParadigmType getParadigmType(int num, Type type) {
        if (type == Type.ORDINAL) return ParadigmType.CASE_GENDER_COUNT; // порядковые числительные склоняются по II парадигме
        else if (type == Type.CARDINAL) { // количественные числительные
            boolean negative = num < 0;
            if (negative) num *= -1; // чтобы корректно срабатывали следующие проверки
            if (num == 1000 || num == 1_000_000 || num == 1_000_000_000) return ParadigmType.CASE_COUNT; // 1K, 1m, 1M - по III парадигме
            else if (num != 11 && num != 12) { // числа 11-12 склоняются по I парадигме
                if (num % 10 == 1) return ParadigmType.CASE_GENDER_COUNT; // число 1 склоняется по II парадигме
                else if (num % 10 == 2) return ParadigmType.CASE_GENDER; // число 2 склоняется по IV парадигме
            }
        }
        return ParadigmType.CASE; // стандартная парадигма для собирательных и количественных числительных
    }

    /**
     * Сравнивает две строки с учётом е/ё, равноправных форм числительных и заглавных букв
     * @param s1 первая строка (пользовательский ввод)
     * @param s2 вторая строка (эталон)
     * @return true, если строки равны и false, если не равны
     */
    static boolean compareStrings(String s1, String s2) {
        s1 = prepareString(s1);
        s2 = s2.replace('ё', 'е'); // заменяем ё на е для простоты сравнивания
        return s1.equals(s2);
    }

    /**
     * Нормализует строку для дальнейшего сравнения с эталоном (заменяет ё на е, одни равноправные
     * формы на другие, приводит к нижнему регистру)
     * @param s строка, которую требуется привести к нормализованному виду
     * @return нормализованная строка
     */
    static String prepareString(String s) {
        return s.replace('ё', 'е').replace("нул", "нол")
                .replace("нолев", "нулев") // ошибка *нолевой
                .replace("восьмью", "восемью")
                .replace("тысячью", "тысячей").toLowerCase();
    }

    /**
     * Обёртка для получения списка диффов
     * @param s1 строка 1 (пользовательский ввод)
     * @param s2 строка 2 (правильно)
     * @return массив с диффами
     */
    static ArrayList<NumDiff> getDiff(String s1, String s2) {
        ArrayList<NumDiff> res = new ArrayList<>();
        s1 = prepareString(s1);
        s2 = s2.replace('ё', 'е'); // заменяем ё на е для простоты сравнивания
        var diffs = diffMatchPatch.diffMain(s1, s2);
        int i = 0;
        for (DiffMatchPatch.Diff diff : diffs) {
            if (diff.operation == DiffMatchPatch.Operation.INSERT) continue;
            NumDiff nd = new NumDiff(i, i + diff.text.length(), diff.operation);
            /*if (diff.operation == DiffMatchPatch.Operation.DELETE)*/ res.add(nd);
            i += diff.text.length();
        }
        return res;
    }

    /**
     * <p>Парадигмы склонения числительного</p>
     * <p>{@link #CASE} - изменение по падежам</p>
     * <p>{@link #CASE_GENDER_COUNT} изменение по падежам, родам и числам</p>
     * <p>{@link #CASE_COUNT} - изменение по падежам и числам</p>
     * <p>{@link #CASE_GENDER} - изменение по падежам и родам</p>
     */
    enum ParadigmType {
        /** Изменение по падежам */
        CASE,
        /** Изменение по падежам, родам и числам */
        CASE_GENDER_COUNT,
        /** Изменение по падежам и числам */
        CASE_COUNT,
        /** Изменение по падежам и родам */
        CASE_GENDER
    }
    static class NumDiff {
        public int start, finish;

        public NumDiff(int start, int finish, DiffMatchPatch.Operation operation) {
            this.start = start;
            this.finish = finish;
            this.operation = operation;
        }

        public DiffMatchPatch.Operation operation;

        @Override
        public String toString() {
            return "NumDiff{" +
                    "start=" + start +
                    ", finish=" + finish +
                    ", operation=" + operation +
                    '}';
        }
    }
}

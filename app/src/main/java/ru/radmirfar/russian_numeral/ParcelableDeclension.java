package ru.radmirfar.russian_numeral;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ParcelableDeclension extends Declension implements Parcelable {
    public ParcelableDeclension(Declension d) {
        super(d);
    }

    public static final Creator<ParcelableDeclension> CREATOR = new Creator<>() {
        @Override
        public ParcelableDeclension createFromParcel(Parcel in) {
            Gender gender = Gender.values()[in.readInt()];
            Case gramCase = Case.values()[in.readInt()];
            Count count = Count.values()[in.readInt()];
            Type type = Type.values()[in.readInt()];
            Animacy animacy = Animacy.values()[in.readInt()];
            return new ParcelableDeclension(new Declension(gender, gramCase, count, type, animacy));
        }

        @Override
        public ParcelableDeclension[] newArray(int size) {
            return new ParcelableDeclension[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeValue(this.gender);
        parcel.writeValue(this.gramCase);
        parcel.writeValue(this.count);
        parcel.writeValue(this.type);
        parcel.writeValue(this.animacy);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Declension that = (Declension) o;
        return gender == that.gender && gramCase == that.gramCase && count == that.count && type == that.type && animacy == that.animacy;
    }
    @Override
    public boolean adjCheck() {
        return super.adjCheck();
    }
}

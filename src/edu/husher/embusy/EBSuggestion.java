package edu.husher.embusy;

import android.os.Parcel;
import android.os.Parcelable;

public class EBSuggestion implements Parcelable {
	public String titlePattern;
	public String pattern;
	public int eta;

	public String getTitlePattern() {
		return titlePattern;
	}

	public void setTitlePattern(String titlePattern) {
		this.titlePattern = titlePattern;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public int getEta() {
		return eta;
	}

	public void setEta(int eta) {
		this.eta = eta;
	}

	public EBSuggestion(String titlePattern, String pattern, int eta) {
		this.titlePattern = titlePattern;
		this.pattern = pattern;
		this.eta = eta;
	}

	public static final Parcelable.Creator<EBSuggestion> CREATOR = new Parcelable.Creator<EBSuggestion>() {
		public EBSuggestion createFromParcel(Parcel in) {
			return new EBSuggestion(in);
		}

		public EBSuggestion[] newArray(int size) {
			return new EBSuggestion[size];
		}
	};

	private EBSuggestion(Parcel in) {
		titlePattern = in.readString();
		pattern = in.readString();
		eta = in.readInt();
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(titlePattern);
		dest.writeString(pattern);
		dest.writeInt(eta);
	}

}

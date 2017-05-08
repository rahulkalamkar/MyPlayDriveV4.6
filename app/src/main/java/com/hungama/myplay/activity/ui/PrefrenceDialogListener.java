package com.hungama.myplay.activity.ui;

import java.util.ArrayList;

import com.hungama.myplay.activity.data.dao.hungama.Era;
import com.hungama.myplay.activity.data.dao.hungama.Genre;
import com.hungama.myplay.activity.data.dao.hungama.Mood;
import com.hungama.myplay.activity.data.dao.hungama.Tempo;

public interface PrefrenceDialogListener {

	void onTempoEditDialog(ArrayList<Tempo> temp);

	void onEraEditDialog(Era era);

	void onLangaugeEditDialog(String mCategory);

	void onMoodEditDialog(Mood mood,int position);

	void onGenreEditDialog(Genre genre);
}

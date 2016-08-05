package net.bestidear.jettyinput.socket;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SharePreferences {

	private static final String NAME = "SHIFT";
	public static final String SHIFT_PARAM = "keycode_shift_left";

	SharedPreferences mySharedPreferences;
	SharedPreferences.Editor editor;

	public SharePreferences(Context context) {

		mySharedPreferences = context.getSharedPreferences(NAME,
				Activity.MODE_PRIVATE);
		// 获得SharedPreferences.Editor对象 （第二步)
		editor = mySharedPreferences.edit();
	}

	public void putShiftValue(String statue) {

		// 使用pubXxx方法保存数据(第三步)
		editor.putString(SHIFT_PARAM, statue);
		// 将数据保存在文件 中(第四步)
		editor.commit();
	}
	
	public String getShiftValue(){
		
		String str = mySharedPreferences.getString(SHIFT_PARAM, "false");
		return str;
	}
}

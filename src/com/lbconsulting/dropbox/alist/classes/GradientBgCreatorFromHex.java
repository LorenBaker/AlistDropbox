package com.lbconsulting.dropbox.alist.classes;

/*import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.View;*/

public class GradientBgCreatorFromHex {

	/*public static void gradientBgCreatorFromHex(View view, String bgColorHex, String gradColorHex) {

		ColorDefinitionResult bgColor = getArgbFromHexaString(bgColorHex);

		ColorDefinitionResult gradientColor = getArgbFromHexaString("1b6da7");
		CreateGradientBackground(view, bgColor, gradientColor);

	}

	public static void CreateGradientBackground(View view, ColorDefinitionResult bgColor,
			ColorDefinitionResult gradientColor) {

		int argbBgColor = Color.argb((int) bgColor.Alpha, bgColor.Red, bgColor.Green, bgColor.Blue);
		int argbGradient = Color.argb((int) gradientColor.Alpha, gradientColor.Red, gradientColor.Green,
				gradientColor.Blue);

		final Shader upperShader = new LinearGradient(0, 0, 0, 40, argbBgColor, argbGradient, Shader.TileMode.CLAMP);

		float[] roundedCorner = new float[] { 0, 0, 0, 0, 0, 0, 0, 0 };

		ShapeDrawable normal = new ShapeDrawable(new RoundRectShape(roundedCorner, null, null));
		normal.getPaint().setShader(upperShader);

		normal.setPadding(7, 3, 7, 0);
		StateListDrawable stateList = new StateListDrawable();
		stateList.addState(new int[] {}, normal);
		//view.setBackgroundDrawable(stateList);
		view.setBackground(stateList);
	}

	public static ColorDefinitionResult getArgbFromHexaString(String hexColorString) {
		ColorDefinitionResult colorDefinitionResult = new ColorDefinitionResult();
		if (hexColorString.length() == 6) {
			String redHex = hexColorString.substring(0, 2);
			String greenHex = hexColorString.substring(2, 4);
			String blueHex = hexColorString.substring(4, 6);
			colorDefinitionResult.Red = Integer.parseInt(redHex, 16);
			colorDefinitionResult.Green = Integer.parseInt(greenHex, 16);
			colorDefinitionResult.Blue = Integer.parseInt(blueHex, 16);
			colorDefinitionResult.Alpha = 255;

		}
		return colorDefinitionResult;
	}*/
}

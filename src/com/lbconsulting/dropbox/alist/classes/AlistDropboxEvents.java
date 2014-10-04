package com.lbconsulting.dropbox.alist.classes;

public class AlistDropboxEvents {

	public static class UpdateLists {

		private int mStyle;

		public UpdateLists(int style) {
			mStyle = style;
		}

		public int getStyle() {
			return mStyle;
		}

	}
}

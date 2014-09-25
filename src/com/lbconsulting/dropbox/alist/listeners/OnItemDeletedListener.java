package com.lbconsulting.dropbox.alist.listeners;

// Just a simple interface used by DeletableItemAdapter.
public interface OnItemDeletedListener<T> {
    public void onItemDeleted(T item);
}

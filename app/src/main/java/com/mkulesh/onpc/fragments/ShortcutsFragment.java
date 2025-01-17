/*
 * Enhanced Music Controller
 * Copyright (C) 2018-2023 by Mikhail Kulesh
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details. You should have received a copy of the GNU General
 * Public License along with this program.
 */

package com.mkulesh.onpc.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.mkulesh.onpc.R;
import com.mkulesh.onpc.config.CfgAppSettings;
import com.mkulesh.onpc.config.CfgFavoriteShortcuts;
import com.mkulesh.onpc.iscp.State;
import com.mkulesh.onpc.utils.Logging;
import com.mkulesh.onpc.utils.Utils;
import com.mobeta.android.dslv.DragSortListView;

import java.util.HashSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class ShortcutsFragment extends BaseFragment
{
    private static final String CLIPBOARD_LABEL = "com.mkulesh.onpc.clipboard";

    private DragSortListView listView;
    private ShortcutsListAdapter listViewAdapter;
    private CfgFavoriteShortcuts.Shortcut selectedItem = null;

    public ShortcutsFragment()
    {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        initializeFragment(inflater, container, R.layout.shortcuts_fragment, CfgAppSettings.Tabs.SHORTCUTS);
        listView = rootView.findViewById(R.id.shortcut_list);
        if (this.getContext() != null)
        {
            listViewAdapter = new ShortcutsListAdapter(this.getContext(), activity.getConfiguration().favoriteShortcuts);
            listView.setAdapter(listViewAdapter);
        }
        registerForContextMenu(listView);
        updateContent();
        return rootView;
    }

    @Override
    protected void updateStandbyView(@Nullable final State state)
    {
        updateList(state);
    }

    @Override
    protected void updateActiveView(@NonNull final State state, @NonNull final HashSet<State.ChangeType> eventChanges)
    {
        // Dismiss update if a data change due to receiver input is detected
        // In this case, the list will be apdated and drag is cancelled
        if (eventChanges.size() == State.ChangeType.values().length)
        {
            updateList(state);
        }
    }

    private void updateList(@Nullable final State state)
    {
        final TextView howto = rootView.findViewById(R.id.shortcut_howto);
        howto.setVisibility(View.GONE);
        listView.setVisibility(View.INVISIBLE);

        if (listViewAdapter == null || state == null)
        {
            return;
        }

        if (activity.getConfiguration().favoriteShortcuts.getShortcuts().isEmpty())
        {
            final String message = String.format(
                    activity.getResources().getString(R.string.favorite_shortcut_howto),
                    CfgAppSettings.getTabName(activity, CfgAppSettings.Tabs.MEDIA),
                    activity.getResources().getString(R.string.action_context_mobile),
                    activity.getResources().getString(R.string.favorite_shortcut_create));
            howto.setText(message);
            howto.setVisibility(View.VISIBLE);
            return;
        }

        listViewAdapter.setItems(activity.getConfiguration().favoriteShortcuts.getShortcuts());
        listView.setVisibility(View.VISIBLE);
        listView.setOnItemClickListener((adapterView, view, pos, l) ->
        {
            selectedItem = (CfgFavoriteShortcuts.Shortcut) listViewAdapter.getItem(pos);
            if (selectedItem != null && activity.isConnected())
            {
                activity.getStateManager().applyShortcut(activity, selectedItem);
                activity.setOpenedTab(CfgAppSettings.Tabs.MEDIA);
            }
        });
        listView.setDropListener((int from, int to) -> listViewAdapter.drop(from, to));
    }


    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        selectedItem = null;
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
        selectedItem = (CfgFavoriteShortcuts.Shortcut) listViewAdapter.getItem(acmi.position);
        if (selectedItem != null)
        {
            Logging.info(this, "Context menu: " + selectedItem);
            MenuInflater inflater = activity.getMenuInflater();
            inflater.inflate(R.menu.favorite_context_menu, menu);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item)
    {
        if (selectedItem != null)
        {
            Logging.info(this, "Context menu '" + item.getTitle() + "'; " + selectedItem);
            switch (item.getItemId())
            {
            case R.id.shortcut_menu_edit:
                editFavoriteShortcut(selectedItem);
                selectedItem = null;
                return true;
            case R.id.shortcut_menu_delete:
                activity.getConfiguration().favoriteShortcuts.deleteShortcut(selectedItem);
                updateContent();
                selectedItem = null;
                return true;
            case R.id.shortcut_menu_copy_to_clipboard:
                copyToClipboard(selectedItem);
                selectedItem = null;
                return true;
            }
        }
        selectedItem = null;
        return super.onContextItemSelected(item);
    }

    private void editFavoriteShortcut(@NonNull final CfgFavoriteShortcuts.Shortcut shortcut)
    {
        final FrameLayout frameView = new FrameLayout(activity);
        activity.getLayoutInflater().inflate(R.layout.dialog_favorite_shortcut_layout, frameView);

        final TextView path = frameView.findViewById(R.id.favorite_shortcut_path);
        path.setText(shortcut.getLabel(activity));

        final EditText alias = frameView.findViewById(R.id.favorite_shortcut_alias);
        alias.setText(shortcut.alias);

        final Drawable icon = Utils.getDrawable(activity, R.drawable.drawer_edit_item);
        Utils.setDrawableColorAttr(activity, icon, android.R.attr.textColorSecondary);
        final AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(R.string.favorite_shortcut_edit)
                .setIcon(icon)
                .setCancelable(false)
                .setView(frameView)
                .setNegativeButton(activity.getResources().getString(R.string.action_cancel), (dialog1, which) ->
                {
                    Utils.showSoftKeyboard(activity, alias, false);
                    dialog1.dismiss();
                })
                .setPositiveButton(activity.getResources().getString(R.string.action_ok), (dialog2, which) ->
                {
                    Utils.showSoftKeyboard(activity, alias, false);
                    activity.getConfiguration().favoriteShortcuts.updateShortcut(
                            shortcut, alias.getText().toString());
                    updateContent();
                    dialog2.dismiss();
                }).create();

        dialog.show();
        Utils.fixIconColor(dialog, android.R.attr.textColorSecondary);
    }

    private void copyToClipboard(CfgFavoriteShortcuts.Shortcut shortcut)
    {
        if (activity == null || !activity.isConnected())
        {
            return;
        }
        final String data = shortcut.toScript(activity, activity.getStateManager().getState());
        try
        {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) activity
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null)
            {
                android.content.ClipData clip = android.content.ClipData.newPlainText(CLIPBOARD_LABEL, data);
                clipboard.setPrimaryClip(clip);
            }
        }
        catch (Exception e)
        {
            // nothing to do
        }
    }
}

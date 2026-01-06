package com.example.recyclersleam.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recyclersleam.Entity.ShoppingItem;
import com.example.recyclersleam.R;
import com.example.recyclersleam.Util.MyDataBase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShoppingListFragment extends Fragment {

    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    private RecyclerView rvShoppingList;
    private TextView tvEmptyState;
    private FloatingActionButton fabMic, fabAdd;
    private ShoppingAdapter adapter;
    private MyDataBase db;
    private int userId;

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shopping_list, container, false);

        rvShoppingList = view.findViewById(R.id.rvShoppingList);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        fabMic = view.findViewById(R.id.fabMic);
        fabAdd = view.findViewById(R.id.fabAdd);

        db = MyDataBase.getAppDataBase(requireContext());

        setupRecyclerView();
        loadItems();

        fabMic.setOnClickListener(v -> speak());
        fabAdd.setOnClickListener(v -> showAddItemDialog());

        return view;
    }

    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Ajouter un article");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Ajouter", (dialog, which) -> {
            String text = input.getText().toString();
            if (!text.isEmpty()) {
                addItem(text);
            }
        });
        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void setupRecyclerView() {
        adapter = new ShoppingAdapter(new ShoppingAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(ShoppingItem item) {
                deleteItem(item);
            }

            @Override
            public void onCheckChange(ShoppingItem item, boolean isChecked) {
                updateItem(item, isChecked);
            }
        });
        rvShoppingList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvShoppingList.setAdapter(adapter);
    }

    private void loadItems() {
        new Thread(() -> {
            List<ShoppingItem> items = db.ShoppingDao().getAllForUser(userId);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    adapter.setList(items);
                    if (items.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        rvShoppingList.setVisibility(View.GONE);
                    } else {
                        tvEmptyState.setVisibility(View.GONE);
                        rvShoppingList.setVisibility(View.VISIBLE);
                    }
                });
            }
        }).start();
    }

    private void deleteItem(ShoppingItem item) {
        new Thread(() -> {
            db.ShoppingDao().delete(item);
            loadItems(); // Refresh
        }).start();
    }

    private void updateItem(ShoppingItem item, boolean isChecked) {
        item.setBought(isChecked);
        new Thread(() -> {
            db.ShoppingDao().update(item);
        }).start();
    }

    private void speak() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Dites ce que vous voulez acheter...");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Votre appareil ne supporte pas l'entrée vocale", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result != null && !result.isEmpty()) {
                    String spokeText = result.get(0);
                    addItem(spokeText);
                }
            }
        }
    }

    private void addItem(String text) {
        new Thread(() -> {
            ShoppingItem newItem = new ShoppingItem(userId, text, false);
            db.ShoppingDao().insert(newItem);
            loadItems();
        }).start();
    }
}

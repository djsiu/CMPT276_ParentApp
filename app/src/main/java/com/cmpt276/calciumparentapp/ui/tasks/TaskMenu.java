package com.cmpt276.calciumparentapp.ui.tasks;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cmpt276.calciumparentapp.R;
import com.cmpt276.calciumparentapp.model.manage.FamilyMembersManager;
import com.cmpt276.calciumparentapp.model.tasks.TaskManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 *  Activity for displaying the task list
 */
public class TaskMenu extends AppCompatActivity {
    TaskManager taskManager;
    TaskRecyclerViewAdapter adapter;
    FamilyMembersManager familyMembersManager;
    boolean hasFamilyMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_menu);

        taskManager = TaskManager.getInstance(this);

        //Adds back button in top left corner
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);

        familyMembersManager = FamilyMembersManager.getInstance(this);
        hasFamilyMembers = familyMembersManager.getFamilyMemberCount() > 0;

        if (hasFamilyMembers) {
            setupRecyclerView();
            setupAddTaskButton();
        } else {
            setupNoFamilyMembers();
        }
    }

    /*
     * ListViews use notifyDataSetChanged() anyway so this will have equal performance despite
     * the optimization suggestion
     *
     * DiffUtil is possibly a good way of optimizing RecyclerView without needing major retooling:
     * https://www.thedroidsonroids.com/blog/difference-between-listview-recyclerview
     */
    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();

        if (hasFamilyMembers) {
            adapter.notifyDataSetChanged();
        }
    }

    private void setupNoFamilyMembers() {
        TextView errorMsg = findViewById(R.id.task_menu_no_family_error);
        errorMsg.setVisibility(View.VISIBLE);
        FloatingActionButton addTaskBtn = findViewById(R.id.addTaskButton);
        addTaskBtn.setEnabled(false);
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.tasks_recycler_view);
        adapter = new TaskRecyclerViewAdapter(this, taskManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupAddTaskButton() {
        FloatingActionButton addTaskBtn = findViewById(R.id.addTaskButton);
        addTaskBtn.setOnClickListener(this::addTaskButtonOnClick);
    }

    private void addTaskButtonOnClick(View view) {
        FloatingActionButton addBtn = (FloatingActionButton) view;
        // check there is at least 1 family member
        if (familyMembersManager.getFamilyMemberCount() > 0) {
            Intent i = TaskConfigure.makeAddTaskIntent(this);
            startActivity(i);
        } else {
            displayNoFamilyMembersErrorToast();
        }
    }

    private void displayNoFamilyMembersErrorToast() {
        Toast toast = Toast.makeText(this,
                R.string.add_task_invalid_family_error_toast,
                Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     * Adds logic to action bar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Top left back arrow
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        // If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        return super.onOptionsItemSelected(item);
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, TaskMenu.class);
    }
}
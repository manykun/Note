package android.example.note;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xuexiang.xormlite.InternalDataBaseRepository;
import com.xuexiang.xormlite.db.DBService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NoteListFragment extends Fragment {

    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private DBService<NoteModel> mDBService;

    private List<NoteModel> allNotes;
    private List<NoteModel> filteredNotes;
    private List<String> categories;
    private ArrayAdapter<String> categoryAdapter;
    private Spinner spinnerCategoryFilter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDBService = InternalDataBaseRepository.getInstance().getDataBase(NoteModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notelistfragment, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        try {
            allNotes = mDBService.queryAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        Toolbar toolbar = view.findViewById(R.id.notelist_toolbar);
        toolbar.inflateMenu(R.menu.note_list);

        // 获取分类
        categories = new ArrayList<>();
        categories.add("All");
        categories.add("Not classified");
        try {
            allNotes = mDBService.queryAll();
            for (NoteModel note : allNotes) {
                if (!categories.contains(note.getTags()) && note.getTags() != null) {
                    categories.add(note.getTags());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        spinnerCategoryFilter = view.findViewById(R.id.spinnerCategoryFilter);
        categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoryFilter.setAdapter(categoryAdapter);


        // 监听分类选择
        spinnerCategoryFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterNotesByCategory(categories.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                filterNotesByCategory("All");
            }
        });

        return view;

    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterNotesByCategory(String category) {
        // 根据分类过滤笔记
        filteredNotes = new ArrayList<>();

        if (category.equals("All")) {
            filteredNotes.addAll(allNotes);
        } else if (category.equals("Not classified")) {
            for (NoteModel note : allNotes) {
                if (note.getTags() == null) {
                    filteredNotes.add(note);
                }
            }
        } else {
            for (NoteModel note : allNotes) {
                if (note.getTags() != null && note.getTags().equals(category)) {
                    filteredNotes.add(note);
                }
            }
        }

        noteAdapter = new NoteAdapter(filteredNotes);
        noteAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(noteAdapter);
    }
}

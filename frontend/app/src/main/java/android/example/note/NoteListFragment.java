package android.example.note;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xuexiang.xormlite.InternalDataBaseRepository;
import com.xuexiang.xormlite.db.DBService;

import java.sql.SQLException;
import java.util.List;

public class NoteListFragment extends Fragment {

    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private List<NoteModel> noteList;
    private DBService<NoteModel> mDBService;

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
            noteList = mDBService.queryAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        noteAdapter = new NoteAdapter(noteList);
        recyclerView.setAdapter(noteAdapter);

        Toolbar toolbar = view.findViewById(R.id.notelist_toolbar);
        toolbar.inflateMenu(R.menu.note_list);

//        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                if (item.getItemId() == R.id.action_photo) {
//                    showImageSourceDialog();
//                    return true;
//                } else if (item.getItemId() == R.id.action_record) {
//                    showRecordSourceDialog();
//                    return true;
//                } else {
//                    return false;
//                }
//            }
//        });


        return view;

    }
}

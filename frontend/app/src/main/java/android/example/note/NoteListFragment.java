package android.example.note;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class NoteListFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notelistfragment, container, false);

        Button button = view.findViewById(R.id.add_note_button);
        // 在第一个Fragment中创建一个Bundle对象并设置数据
        Bundle bundle = new Bundle();
        bundle.putInt("Note_id", Integer.parseInt("1"));

        Fragment noteaddFragment = new NoteAddFragment();
        noteaddFragment.setArguments(bundle);

        button.setOnClickListener(v -> {
             assert getFragmentManager() != null;
             getFragmentManager().beginTransaction()
                     .replace(R.id.fragment_container, noteaddFragment)
                     .addToBackStack(null)
                     .commit();


        });

        return view;

    }
}

package com.attendancetaker.fragments;

import static com.attendancetaker.MainActivity.goToLogin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.attendancetaker.R;
import com.example.attendancetaker.databinding.FragmentHistoryBinding;
import com.attendancetaker.AttendanceRecord;
import com.attendancetaker.Database;

import java.util.Vector;

public class HistoryFragment extends Fragment {

private FragmentHistoryBinding binding;
private LinearLayout _layout;
private View _root;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(Database.getActiveUser() == -1)
            goToLogin();
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        _root = binding.getRoot();

        return _root;
    }

    @Override
    public void onViewCreated(View view, Bundle saved_instance_state) {
        // Once view is done get layout from it
        super.onViewCreated(view, saved_instance_state);
        _layout = view.findViewById(R.id.historyLayout);

        new Database.UpdateHistory() {
            // Here this instances a new history query,
            // Over riding this method effectively subclasses
            // this task as a copy of itself + this method override
            @Override protected void onPostExecute(Vector<AttendanceRecord> cards) {
                inflateCardViewsAndAddToLayout(cards);
            }
        }.execute();
    }

    /**
     * This method inflates the card views using xml resources and
     * fills out relevant info before adding them to the layout
     * @param cards Vector of attendance information returned from query
     */
    private void inflateCardViewsAndAddToLayout(Vector<AttendanceRecord> cards) {
        for(AttendanceRecord rec : cards) {
            // Inflate our card and bind it to history fragment
            View card = getLayoutInflater().inflate(R.layout.history_card, _root.findViewById(R.id.navigations_history));

            // Gather card elements
            TextView course_info = card.findViewById(R.id.courseInfo);
            TextView date = card.findViewById(R.id.textViewDate);
            TextView time = card.findViewById(R.id.textViewTime);

            // get card view via TextView -> RelativeLayout -> CardView
            CardView card_view = (CardView) course_info.getParent().getParent();

            // Set text values
            course_info.setText(String.format("%d-%d", rec.course_number, rec.course_section));
            date.setText(rec.date.toString());
            time.setText(rec.time.toString().substring(0, 5));

            // Decide card color
            Color clr = Color.valueOf(ContextCompat.getColor(this.requireContext(),
                    R.color.black));
            if(rec.status != null) {
                switch(rec.status) {
                    case "Present":
                        clr = Color.valueOf(ContextCompat.getColor(this.requireContext(),
                                R.color.history_clr_present));
                        break;
                    case "Late":
                        clr = Color.valueOf(ContextCompat.getColor(this.requireContext(),
                                R.color.history_clr_late));
                        break;
                    case "Absent":
                        clr = Color.valueOf(ContextCompat.getColor(this.requireContext(),
                                R.color.history_clr_absent));
                        break;
                }
            }
            card_view.setCardBackgroundColor(clr.toArgb());
            // add the view to the layout
            _layout.addView(card);
        }
    }

@Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
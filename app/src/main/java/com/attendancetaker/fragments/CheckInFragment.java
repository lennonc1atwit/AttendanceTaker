package com.attendancetaker.fragments;

import static android.app.Activity.RESULT_OK;

import static com.attendancetaker.MainActivity.goToLogin;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.attendancetaker.databinding.FragmentCheckInBinding;
import com.attendancetaker.Database;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class CheckInFragment extends Fragment  {

private FragmentCheckInBinding binding;
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(Database.getActiveUser() == -1)
            goToLogin();
        binding = FragmentCheckInBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Get our inputs
        final EditText class_code_input = binding.editTextClassCode;

        // Set our OnClickListener for the submit button
        final Button submit_button = binding.buttonSubmit;
        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                int class_code = Integer.parseInt(class_code_input.getText().toString());
                // Here this instances a new check in task,
                // Over riding this method effectively subclasses
                // this task as a copy of itself + this method override
                new Database.CheckIn() {
                        @Override protected void onPostExecute(String result) {
                            super.onPostExecute(result);
                            if (result == "success") showCheckInSuccess();
                            else {
                                showCheckInError(result);
                            }
                        }
                }.execute(class_code);
            }
        });

        //Create a system activity which will end up opening the camera
        ActivityResultLauncher<Intent> activityResultLauncher;
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->
        {
            //Get the result of the activity and check its integrity
            if(result.getResultCode() == RESULT_OK && result.getData() != null){
                IntentResult intentResult = (IntentResult)
                        IntentIntegrator.parseActivityResult(result.getResultCode(), result.getData());
                //Parse and input class code
                String class_code = intentResult.getContents().replaceAll("[^0-9]", "");
                class_code_input.setText(class_code);
            }
            else {
                showCheckInError("Scan Failed!");
            }
        });

        // Set our OnClickListener for the scan button
        final Button open_camera = binding.buttonScanqr;
        open_camera.setOnClickListener(v -> {
            IntentIntegrator intentIntegrator = new IntentIntegrator(getActivity());
            intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
            intentIntegrator.setPrompt("Scan QR Code");
            intentIntegrator.setOrientationLocked(false);
            activityResultLauncher.launch(intentIntegrator.createScanIntent());
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Popout section
     *      these functions create pop out alerts to direct the users attention to an area or to
     *      restrict action until an action is taken by the user.
     */
    private void showCheckInError(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Error Recording Attendance");
        builder.setMessage(s);
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showCheckInSuccess() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Attendance Recorded");
        builder.setMessage("You're all set...");
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}


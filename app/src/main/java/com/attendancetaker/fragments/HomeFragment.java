package com.attendancetaker.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.attendancetaker.MainActivity;
import com.example.attendancetaker.R;
import com.attendancetaker.Database;
import com.example.attendancetaker.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

private FragmentHomeBinding _binding;
private View _root;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(Database.getActiveUser() != -1) {
            showLogoutPopup();
        }

        _binding = FragmentHomeBinding.inflate(inflater, container, false);
        _root = _binding.getRoot();

        // Login button callback
        final Button login_button = _binding.buttonLogin;
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText email =    _root.findViewById(R.id.editTextEmail);
                EditText password = _root.findViewById(R.id.editTextPasscode);
                // Here this instances a new log in task,
                // Over riding this method effectively subclasses
                // this task as a copy of itself + this method override
                new Database.Login() {
                    @Override
                    protected void onPostExecute(Integer result) {
                        super.onPostExecute(result);
                        if(result == -1)
                            showLoginErrorPopup();
                        else
                            MainActivity.goToCheckIn();
                    }
                }.execute(email.getText().toString(), password.getText().toString());
            }
        });

        // Register button callback
        final Button open_popup_button = _binding.buttonRegister;
        open_popup_button.setOnClickListener(this::showRegistrationPopup);

        return _root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }

    /**
     * Popout section
     *      these functions create pop out alerts to direct the users attention to an area or to
     *      restrict action until an action is taken by the user.
     */

    private void showLogoutPopup() {
        // Inflate view and get button
        final View logoutPopup = getLayoutInflater().inflate(R.layout.logout_popup, null);
        Button logout_button = logoutPopup.findViewById(R.id.buttonLogout);

        // Create pop up and display it
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setView(logoutPopup);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Logout User callback
        logout_button.setOnClickListener(view -> {
            Database.logoutActiveUser();
            EditText password_text = (EditText) _root.findViewById(R.id.editTextPasscode);
            password_text.setText("");
            dialog.dismiss();
        });
    }

    private void showLoginErrorPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Login Error!");
        builder.setMessage("Password or Email incorrect...");
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showRegistrationErrorPopup(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Registration Error!");
        builder.setMessage(s);
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void showRegistrationPopup(View view) {
        // Inflate view and gather info
        final View registrationPopup = getLayoutInflater().inflate(R.layout.register_popup, null);
        EditText f_name =       registrationPopup.findViewById(R.id.editTextRegisterName);
        EditText l_name =       registrationPopup.findViewById(R.id.editTextRegisterNameLast);
        EditText email =        registrationPopup.findViewById(R.id.editTextRegisterEmailAddress);
        EditText wit_id =       registrationPopup.findViewById(R.id.editTextRegisterWitID);
        EditText pass =         registrationPopup.findViewById(R.id.editTextNumberPassword);
        EditText pass_confirm = registrationPopup.findViewById(R.id.editTextNumberPasswordConfirm);

        // Create pop up and display it
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setView(registrationPopup);
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Send registration request
        Button butOne = registrationPopup.findViewById(R.id.buttonSendRegister);
        butOne.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v)
            {
                // Create our Async task and add a callback for when it finishes
                new Database.Register() {
                    @Override protected void onPostExecute(String s) {
                        super.onPostExecute(s);
                        if(s.equals("success")) dialog.dismiss();
                        else showRegistrationErrorPopup(s);
                    }
                }.execute(wit_id.getText().toString(),
                        email.getText().toString(),
                        pass.getText().toString(),
                        f_name.getText().toString(), l_name.getText().toString(),
                        pass_confirm.getText().toString());
            }
        });
    }
}
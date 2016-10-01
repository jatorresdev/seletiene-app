package com.example.aprendiz.salesapp.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.aprendiz.salesapp.MainActivity;
import com.example.aprendiz.salesapp.R;
import com.example.aprendiz.salesapp.clients.SalesAPI;
import com.example.aprendiz.salesapp.models.User;
import com.example.aprendiz.salesapp.models.UserData;
import com.example.aprendiz.salesapp.services.UserService;
import com.example.aprendiz.salesapp.utils.PrefUtils;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UpdateUserFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UpdateUserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UpdateUserFragment extends Fragment {
    private UserUpdateTask mUpdateTask = null;

    //UI references
    private EditText mNameView;
    private EditText mLastNameView;
    private EditText mCellphoneView;
    private EditText mTelephoneView;

    private View mProgressView;
    private View mUpdateFormView;

    private OnFragmentInteractionListener mListener;

    public UpdateUserFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update_user, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Gson gson = new Gson();
        final User user = gson.fromJson(((MainActivity) getActivity()).loggedInUserData, User.class);

        // Set up the login form.
        mNameView = (EditText) view.findViewById(R.id.name);
        mLastNameView = (EditText) view.findViewById(R.id.last_name);
        mCellphoneView = (EditText) view.findViewById(R.id.cellphone);
        mTelephoneView = (EditText) view.findViewById(R.id.telephone);

        mNameView.setText(user.getName());
        mLastNameView.setText(user.getLastName());
        mCellphoneView.setText(user.getCellphone());
        mTelephoneView.setText(user.getTelephone());

        Button mSignUpButton = (Button) view.findViewById(R.id.update_user);
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptUpdate(user);
            }
        });

        mUpdateFormView = view.findViewById(R.id.update_user_form);
        mProgressView = view.findViewById(R.id.update_user_progress);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * Logica de registro
     */
    private void attemptUpdate(User user) {
        // Reset errors.
        mNameView.setError(null);
        mLastNameView.setError(null);
        mCellphoneView.setError(null);
        mTelephoneView.setError(null);

        // Store values at the time of the login attempt.
        String name = mNameView.getText().toString();
        String lastName = mLastNameView.getText().toString();
        String cellphone = mCellphoneView.getText().toString();
        String telephone = mTelephoneView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid telephone, if the user entered one.
        if (!TextUtils.isEmpty(telephone) && !isNumberValid(telephone)) {
            mTelephoneView.setError(getString(R.string.register_error_invalid_telephone));
            focusView = mTelephoneView;
            cancel = true;
        }

        // Check for a valid cellphone, if the user entered one.
        if (TextUtils.isEmpty(cellphone)) {
            mCellphoneView.setError(getString(R.string.register_error_field_required));
            focusView = mCellphoneView;
            cancel = true;
        } else if (!isNumberValid(cellphone)) {
            mCellphoneView.setError(getString(R.string.register_error_invalid_cellphone));
            focusView = mCellphoneView;
            cancel = true;
        }

        // Check for a valid last name, if the user entered one.
        if (TextUtils.isEmpty(lastName)) {
            mLastNameView.setError(getString(R.string.register_error_field_required));
            focusView = mLastNameView;
            cancel = true;
        } else if (!isLastNameValid(lastName)) {
            mLastNameView.setError(getString(R.string.register_error_invalid_last_name));
            focusView = mLastNameView;
            cancel = true;
        }

        // Check for a valid name, if the user entered one.
        if (TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.register_error_field_required));
            focusView = mNameView;
            cancel = true;
        } else if (!isNameValid(name)) {
            mNameView.setError(getString(R.string.register_error_invalid_name));
            focusView = mNameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user register attempt.
            showProgress(true);
            mUpdateTask = new UserUpdateTask(name, lastName, cellphone, telephone);
            mUpdateTask.updateUser(user);
        }
    }

    private boolean isNameValid(String name) {
        return name.length() > 4;
    }

    private boolean isLastNameValid(String lastName) {
        return lastName.length() > 4;
    }

    private boolean isNumberValid(String number) {
        return number.matches("\\d+(?:\\.\\d+)?") && number.length() <= 10;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mUpdateFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mUpdateFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mUpdateFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mUpdateFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login task used to authenticate
     * the user.
     */
    public class UserUpdateTask {

        private final String mName;
        private final String mLastName;
        private final String mCellphone;
        private final String mTelephone;

        UserUpdateTask(String name, String lastName, String cellphone, String telephone) {
            mName = name;
            mLastName = lastName;
            mCellphone = cellphone;
            mTelephone = telephone;
        }

        public void updateUser(final User user) {
            user.setName(mName);
            user.setLastName(mLastName);
            user.setCellphone(mCellphone);
            user.setTelephone(mTelephone);

            UserService userUpdateService = SalesAPI.createService(UserService.class,
                    ((MainActivity) getActivity()).loggedInUserEmail, ((MainActivity) getActivity()).loggedInUserPassword);

            Call<UserData> callRegisterUser = userUpdateService.updateUser(user);
            callRegisterUser.enqueue(new Callback<UserData>() {
                @Override
                public void onResponse(Call<UserData> call, Response<UserData> response) {
                    showProgress(false);

                    if (response.isSuccessful()) {
                        User userUpdate = response.body().getData();

                        Gson gson = new Gson();
                        String userJson = gson.toJson(userUpdate);
                        PrefUtils.saveToPrefs(getActivity(), PrefUtils.PREFS_USER_KEY, userJson);

                        ((MainActivity) getActivity()).loadActivity();

                        Toast.makeText(getActivity(), "Información actualizada exitosamente "
                                + response.body().getData().getFullName(), Toast.LENGTH_LONG).show();

                    } else {
                        mUpdateTask = null;
                        Toast.makeText(getActivity(), "Ha ocurrido un error al intentar actualizar los datos", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<UserData> call, Throwable t) {
                    mUpdateTask = null;
                    showProgress(false);

                    Toast.makeText(getActivity(), "Ha ocurrido un error al intentar actualizar los datos", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
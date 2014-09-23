package fr.tvbarthel.apps.sayitfromthesky.fragments.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import butterknife.ButterKnife;
import fr.tvbarthel.apps.sayitfromthesky.R;
import fr.tvbarthel.apps.sayitfromthesky.helpers.ContentValuesHelper;
import fr.tvbarthel.apps.sayitfromthesky.models.Drawing;
import fr.tvbarthel.apps.sayitfromthesky.providers.contracts.DrawingContract;

/**
 * A simple {@link android.app.DialogFragment} to edit a {@link fr.tvbarthel.apps.sayitfromthesky.models.Drawing}.
 */
public class EditDrawingDialog extends DialogFragment {

    private static final String ARGS_DRAWING = "EditDrawingDialog.Args.Drawing";

    private Callback mCallback;
    private Drawing mDrawing;


    public static EditDrawingDialog newInstance(Drawing drawingToEdit) {
        final EditDrawingDialog dialog = new EditDrawingDialog();
        final Bundle arguments = new Bundle();
        arguments.putParcelable(ARGS_DRAWING, drawingToEdit);
        dialog.setArguments(arguments);
        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof Callback) {
            mCallback = (Callback) activity;
        } else {
            throw new ClassCastException(activity.toString()
                    + " must implement EditDrawingDialog.Callback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mDrawing = getArguments().getParcelable(ARGS_DRAWING);
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edit_drawing, null);
        final EditText editText = ButterKnife.findById(view, R.id.dialog_edit_drawing_title);
        editText.setText(mDrawing.getTitle());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.dialog_edit_drawing_title));
        builder.setView(view);
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                hideSoftKeyboard(editText.getWindowToken());
            }
        });
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String newTitle = editText.getText().toString();
                if (!newTitle.equals(mDrawing.getTitle())) {
                    final Drawing newDrawing = new Drawing(newTitle,
                            mDrawing.getCreationTimeInMillis(), mDrawing.getEncodedPolylines());
                    newDrawing.setId(mDrawing.getId());
                    final int rowUpdated = getActivity().getContentResolver().update(DrawingContract.getContentUri(newDrawing),
                            ContentValuesHelper.drawingToContentValues(newDrawing), null, null);
                    if (rowUpdated > 0) {
                        mCallback.onDrawingEdited(newDrawing);
                    }
                }
                hideSoftKeyboard(editText.getWindowToken());
            }
        });
        return builder.create();

    }

    /**
     * Close/hide the input method's soft input area
     *
     * @param windowToken Supplies the identifying token given to an input method when it was started, which allows it to perform this operation on itself.
     */
    private void hideSoftKeyboard(IBinder windowToken) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(windowToken, 0);
    }

    /**
     * Default Constructor.
     * <p/>
     * lint [ValidFragment]
     * http://developer.android.com/reference/android/app/Fragment.html#Fragment()
     * Every fragment must have an empty constructor, so it can be instantiated when restoring its activity's state.
     */
    public EditDrawingDialog() {
        super();
    }


    /**
     * An interface definition for a callback.
     */
    public interface Callback {
        /**
         * Called when the drawing has been edited.
         *
         * @param drawing the new {@link fr.tvbarthel.apps.sayitfromthesky.models.Drawing}.
         */
        void onDrawingEdited(Drawing drawing);
    }
}

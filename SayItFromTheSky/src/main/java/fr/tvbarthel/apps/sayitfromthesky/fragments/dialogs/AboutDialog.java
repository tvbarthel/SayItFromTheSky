package fr.tvbarthel.apps.sayitfromthesky.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.BuildConfig;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import fr.tvbarthel.apps.sayitfromthesky.R;

/**
 * A simple {@link android.app.DialogFragment} that displays the about section of this app.
 */
public class AboutDialog extends DialogFragment {


    @InjectView(R.id.dialog_about_version_name)
    TextView mVersionName;

    @InjectView(R.id.dialog_about_source_code)
    TextView mSourceCode;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_about, null);
        ButterKnife.inject(this, dialogView);

        setVersionName();

        mSourceCode.setMovementMethod(LinkMovementMethod.getInstance());
        Linkify.addLinks(mSourceCode, Linkify.WEB_URLS);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        builder.setView(dialogView);
        builder.setInverseBackgroundForced(true);

        return builder.create();
    }

    private void setVersionName() {
        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            String versionNameText = pInfo.versionName;
            if (BuildConfig.DEBUG) {
                versionNameText += ".dev";
            }
            mVersionName.setText(getString(R.string.dialog_about_version, versionNameText));
        } catch (PackageManager.NameNotFoundException e) {
            mVersionName.setText(getString(R.string.dialog_about_unknown_version));
        }
    }

    @OnClick(R.id.dialog_about_ok)
    void dismissDialog() {
        dismiss();
    }
}

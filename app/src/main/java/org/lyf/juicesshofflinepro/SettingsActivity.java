package org.lyf.juicesshofflinepro;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public final class SettingsActivity extends Activity {
    static final String PREFS = "settings";
    static final String KEY_HOOK_PRO_CHECK = "hook_pro_check";
    static final String KEY_HOOK_USER_SIGNATURE = "hook_user_signature";
    static final String KEY_HOOK_SESSION_EXPIRY = "hook_session_expiry";
    static final String KEY_HOOK_API_GATE = "hook_api_gate";
    static final String KEY_SESSION_YEARS = "session_years";

    private CheckBox proCheck;
    private CheckBox userSignature;
    private CheckBox sessionExpiry;
    private CheckBox apiGate;
    private EditText sessionYears;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        root.setPadding(pad, pad, pad, pad);
        scroll.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView title = new TextView(this);
        title.setText(R.string.app_name);
        title.setTextSize(22);
        title.setGravity(Gravity.START);
        root.addView(title, matchWrap());

        TextView hint = new TextView(this);
        hint.setText(R.string.settings_hint);
        hint.setPadding(0, dp(8), 0, dp(12));
        root.addView(hint, matchWrap());

        proCheck = checkBox(getString(R.string.hook_pro_check), prefs.getBoolean(KEY_HOOK_PRO_CHECK, true));
        userSignature = checkBox(getString(R.string.hook_user_signature), prefs.getBoolean(KEY_HOOK_USER_SIGNATURE, true));
        sessionExpiry = checkBox(getString(R.string.hook_session_expiry), prefs.getBoolean(KEY_HOOK_SESSION_EXPIRY, true));
        apiGate = checkBox(getString(R.string.hook_api_gate), prefs.getBoolean(KEY_HOOK_API_GATE, true));
        root.addView(proCheck, matchWrap());
        root.addView(userSignature, matchWrap());
        root.addView(sessionExpiry, matchWrap());
        root.addView(apiGate, matchWrap());

        TextView yearsLabel = new TextView(this);
        yearsLabel.setText(R.string.session_years);
        yearsLabel.setPadding(0, dp(12), 0, 0);
        root.addView(yearsLabel, matchWrap());

        sessionYears = new EditText(this);
        sessionYears.setSingleLine(true);
        sessionYears.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        sessionYears.setText(String.valueOf(prefs.getInt(KEY_SESSION_YEARS, 10)));
        root.addView(sessionYears, matchWrap());

        Button save = new Button(this);
        save.setText(R.string.save);
        save.setOnClickListener(v -> save());
        root.addView(save, matchWrap());

        Button defaults = new Button(this);
        defaults.setText(R.string.restore_defaults);
        defaults.setOnClickListener(v -> {
            proCheck.setChecked(true);
            userSignature.setChecked(true);
            sessionExpiry.setChecked(true);
            apiGate.setChecked(true);
            sessionYears.setText("10");
            save();
        });
        root.addView(defaults, matchWrap());

        setContentView(scroll);
    }

    private CheckBox checkBox(String text, boolean checked) {
        CheckBox cb = new CheckBox(this);
        cb.setText(text);
        cb.setChecked(checked);
        cb.setPadding(0, dp(4), 0, dp(4));
        return cb;
    }

    private void save() {
        int years;
        try {
            years = Integer.parseInt(sessionYears.getText().toString().trim());
        } catch (NumberFormatException e) {
            years = 10;
        }
        if (years < 1) years = 1;
        if (years > 100) years = 100;
        sessionYears.setText(String.valueOf(years));

        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_HOOK_PRO_CHECK, proCheck.isChecked())
                .putBoolean(KEY_HOOK_USER_SIGNATURE, userSignature.isChecked())
                .putBoolean(KEY_HOOK_SESSION_EXPIRY, sessionExpiry.isChecked())
                .putBoolean(KEY_HOOK_API_GATE, apiGate.isChecked())
                .putInt(KEY_SESSION_YEARS, years)
                .apply();

        Toast.makeText(this, R.string.saved_restart_juicessh, Toast.LENGTH_LONG).show();
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}

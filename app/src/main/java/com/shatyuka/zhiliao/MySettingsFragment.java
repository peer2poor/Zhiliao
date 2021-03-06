package com.shatyuka.zhiliao;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.io.File;
import java.util.Random;

public class MySettingsFragment extends PreferenceFragmentCompat {
    public static class DlgException extends RuntimeException {
    }

    public static class WarnIconDialogFragment extends DialogFragment {
        boolean result = true;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.dialog_warnicon)
                    .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            FragmentActivity activity = getActivity();
                            assert activity != null;
                            PackageManager p = activity.getPackageManager();
                            p.setComponentEnabledSetting(new ComponentName(activity.getPackageName(), "com.shatyuka.zhiliao.MainActivity"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                            throw new DlgException();
                        }
                    })
                    .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            result = false;
                            throw new DlgException();
                        }
                    }).setCancelable(false);
            return builder.create();
        }
    }

    static int version_click = 0;
    static int author_click = 0;

    private boolean isModuleActive() {
        Log.i("Zhiliao", "Not activated");
        return false;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        if (isModuleActive()) {
            Preference preference_status = findPreference("preference_status");
            assert preference_status != null;
            preference_status.setSummary(R.string.pref_status_on);
            preference_status.setIcon(R.drawable.ic_check);
        }

        Preference switch_hideicon = findPreference("switch_hideicon");
        assert switch_hideicon != null;
        switch_hideicon.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean value = (boolean) newValue;
                FragmentActivity activity = getActivity();
                assert activity != null;
                PackageManager p = activity.getPackageManager();
                if (value) {
                    WarnIconDialogFragment dlg = new WarnIconDialogFragment();
                    dlg.show(activity.getSupportFragmentManager(), null);
                    try {
                        Looper.loop();
                    } catch (DlgException e) {
                        return dlg.result;
                    }
                } else
                    p.setComponentEnabledSetting(new ComponentName(activity.getPackageName(), "com.shatyuka.zhiliao.MainActivity"), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                return true;
            }
        });

        Preference preference_settings = findPreference("preference_settings");
        assert preference_settings != null;
        preference_settings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent();
                intent.setClassName("com.zhihu.android", "com.zhihu.android.app.ui.activity.MainActivity");
                intent.putExtra("zhiliao_settings", true);
                startActivity(intent);
                return true;
            }
        });

        Preference preference_download = findPreference("preference_download");
        assert preference_download != null;
        preference_download.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                try {
                    String url = "https://www.zhihu.com/api/v4/sem/package/redirect?app_from=zhihuwap&utm_source=zhihu_default";
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    request.setDestinationInExternalFilesDir(getContext(), "", "zhihu.apk");
                    request.setVisibleInDownloadsUi(false);
                    BroadcastReceiver onComplete = new BroadcastReceiver() {
                        public void onReceive(Context context, Intent intent) {
                            Toast.makeText(context, "初始化成功！", Toast.LENGTH_SHORT).show();
                        }
                    };
                    getContext().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                    DownloadManager downloadManager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                    File file = new File(getContext().getExternalFilesDir("") + "/zhihu.apk");
                    if (file.exists())
                        file.delete();
                    downloadManager.enqueue(request);
                    Toast.makeText(getContext(), "下载中，请稍候！", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getContext(), "下载失败！", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        Preference preference_version = findPreference("preference_version");
        assert preference_version != null;
        preference_version.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                version_click++;
                if (version_click == 5) {
                    Toast.makeText(getContext(), "点我次数再多，更新也不会变快哦", Toast.LENGTH_SHORT).show();
                    version_click = 0;
                }
                return true;
            }
        });

        Preference preference_author = findPreference("preference_author");
        assert preference_author != null;
        preference_author.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                author_click++;
                if (author_click == 5) {
                    Toast.makeText(getContext(), getResources().getStringArray(R.array.click_author)[new Random().nextInt(4)], Toast.LENGTH_LONG).show();
                    author_click = 0;
                }
                return true;
            }
        });

        Preference preference_help = findPreference("preference_help");
        assert preference_help != null;
        preference_help.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Uri uri = Uri.parse("https://github.com/shatyuka/Zhiliao/wiki");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            }
        });

        Preference preference_channel = findPreference("preference_channel");
        assert preference_channel != null;
        preference_channel.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Uri uri = Uri.parse("https://t.me/zhiliao");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            }
        });

        Preference preference_telegram = findPreference("preference_telegram");
        assert preference_telegram != null;
        preference_telegram.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Uri uri = Uri.parse("https://t.me/joinchat/OibCWxbdCMkJ2fG8J1DpQQ");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            }
        });

        Preference preference_sourcecode = findPreference("preference_sourcecode");
        assert preference_sourcecode != null;
        preference_sourcecode.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Uri uri = Uri.parse("https://github.com/shatyuka/Zhiliao");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            }
        });

        Preference preference_donate = findPreference("preference_donate");
        assert preference_donate != null;
        preference_donate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.preferences_container, new PreferenceDonate.DonateFragment())
                        .commit();
                return true;
            }
        });
    }
}

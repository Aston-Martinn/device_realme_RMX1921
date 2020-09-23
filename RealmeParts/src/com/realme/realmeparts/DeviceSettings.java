/*
 * Copyright (C) 2018 The Asus-SDM660 Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.realme.realmeparts;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import android.content.Context;
import android.content.SharedPreferences;

import com.realme.realmeparts.kcal.KCalSettingsActivity;
import com.realme.realmeparts.preferences.CustomSeekBarPreference;
import com.realme.realmeparts.preferences.SecureSettingListPreference;
import com.realme.realmeparts.preferences.SecureSettingSwitchPreference;
import com.realme.realmeparts.preferences.VibratorStrengthPreference;
import com.realme.realmeparts.preferences.CallVibratorStrengthPreference;
import com.realme.realmeparts.preferences.NotifVibratorStrengthPreference;

import java.lang.Math.*;

public class DeviceSettings extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "RealmeParts";

    final static String PREF_TORCH_BRIGHTNESS = "torch_brightness";
    public static final String TORCH_1_BRIGHTNESS_PATH = "/sys/class/leds/led:torch_0/max_brightness";
    public static final String TORCH_2_BRIGHTNESS_PATH = "/sys/class/leds/led:torch_1/max_brightness";

    public static final String KEY_VIBSTRENGTH = "vib_strength";
    public static final String KEY_NOTIF_VIBSTRENGTH = "notif_vib_strength";
    public static final String KEY_CALL_VIBSTRENGTH = "call_vib_strength";
    public static final String CATEGORY_DISPLAY = "display";
    public static final String PREF_DEVICE_KCAL = "device_kcal";
    public static final String PREF_DEVICESETTINGS = "device_settings";
    private static final String EXTRA_CATEGORY = "extra";

    public static final String PREF_HEADPHONE_GAIN = "headphone_gain";
    public static final String HEADPHONE_GAIN_PATH = "/sys/kernel/sound_control/headphone_gain";
    public static final String PREF_MICROPHONE_GAIN = "microphone_gain";
    public static final String MICROPHONE_GAIN_PATH = "/sys/kernel/sound_control/mic_gain";
    public static final String PREF_EARPIECE_GAIN = "earpiece_gain";
    public static final String EARPIECE_GAIN_PATH = "/sys/kernel/sound_control/earpiece_gain";
    public static final String PREF_SPEAKER_GAIN = "speaker_gain";
    public static final String SPEAKER_GAIN_PATH = "/sys/kernel/sound_control/speaker_gain";

    private static final String DEVICE_SETTINGS_PACKAGE_NAME = "org.lineageos.settings.device";

    public static final int MIN_LED = 1;
    public static final int MAX_LED = 255;

    private CustomSeekBarPreference mTorchBrightness;
    private VibratorStrengthPreference mVibratorStrength;
    private NotifVibratorStrengthPreference mNotifVibratorStrength;
    private CallVibratorStrengthPreference mCallVibratorStrength;
    private Preference mKcal;
    private CustomSeekBarPreference mHeadphoneGain;
    private CustomSeekBarPreference mMicrophoneGain;
    private CustomSeekBarPreference mEarpieceGain;
    private CustomSeekBarPreference mSpeakerGain;

    private static Context mContext;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_realme_parts, rootKey);
        mContext = this.getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        String device = FileUtils.getStringProp("ro.build.product", "unknown");

        mTorchBrightness = (CustomSeekBarPreference) findPreference(PREF_TORCH_BRIGHTNESS);
        mTorchBrightness.setEnabled(FileUtils.fileWritable(TORCH_1_BRIGHTNESS_PATH) &&
                FileUtils.fileWritable(TORCH_2_BRIGHTNESS_PATH));
        mTorchBrightness.setOnPreferenceChangeListener(this);

        PreferenceCategory displayCategory = (PreferenceCategory) findPreference(CATEGORY_DISPLAY);

        mKcal = findPreference(PREF_DEVICE_KCAL);

        mKcal.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity().getApplicationContext(), KCalSettingsActivity.class);
            startActivity(intent);
            return true;
        });

        mVibratorStrength = (VibratorStrengthPreference) findPreference(KEY_VIBSTRENGTH);
        if (mVibratorStrength != null) {
            mVibratorStrength.setEnabled(VibratorStrengthPreference.isSupported());
        }

        mNotifVibratorStrength = (NotifVibratorStrengthPreference) findPreference(KEY_NOTIF_VIBSTRENGTH);
        if (mNotifVibratorStrength != null) {
            mNotifVibratorStrength.setEnabled(NotifVibratorStrengthPreference.isSupported());
        }

        mCallVibratorStrength = (CallVibratorStrengthPreference) findPreference(KEY_CALL_VIBSTRENGTH);
        if (mCallVibratorStrength != null) {
            mCallVibratorStrength.setEnabled(CallVibratorStrengthPreference.isSupported());
        }

        PreferenceCategory extraCategory = (PreferenceCategory) findPreference(EXTRA_CATEGORY);
        if (isAppNotInstalled(DEVICE_SETTINGS_PACKAGE_NAME)) {
            displayCategory.removePreference(findPreference(PREF_DEVICESETTINGS));
        }

        mHeadphoneGain = (CustomSeekBarPreference) findPreference(PREF_HEADPHONE_GAIN);
        mHeadphoneGain.setOnPreferenceChangeListener(this);

        mMicrophoneGain = (CustomSeekBarPreference) findPreference(PREF_MICROPHONE_GAIN);
        mMicrophoneGain.setOnPreferenceChangeListener(this);

        mEarpieceGain = (CustomSeekBarPreference) findPreference(PREF_EARPIECE_GAIN);
        mEarpieceGain.setOnPreferenceChangeListener(this);

        mSpeakerGain = (CustomSeekBarPreference) findPreference(PREF_SPEAKER_GAIN);
        mSpeakerGain.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        final String key = preference.getKey();
        switch (key) {
            case PREF_TORCH_BRIGHTNESS:
                FileUtils.setValue(TORCH_1_BRIGHTNESS_PATH, (int) value);
                FileUtils.setValue(TORCH_2_BRIGHTNESS_PATH, (int) value);
                break;

            case PREF_HEADPHONE_GAIN:
                FileUtils.setValue(HEADPHONE_GAIN_PATH, value + " " + value);
                break;

            case PREF_MICROPHONE_GAIN:
                FileUtils.setValue(MICROPHONE_GAIN_PATH, (int) value);
                break;

            case PREF_EARPIECE_GAIN:
                FileUtils.setValue(EARPIECE_GAIN_PATH, (int) value);
                break;

            case PREF_SPEAKER_GAIN:
                 FileUtils.setValue(SPEAKER_GAIN_PATH, (int) value);
                break;
            default:
                break;
        }
        return true;
    }

    private boolean isAppNotInstalled(String uri) {
        PackageManager packageManager = getContext().getPackageManager();
        try {
            packageManager.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
    }
}

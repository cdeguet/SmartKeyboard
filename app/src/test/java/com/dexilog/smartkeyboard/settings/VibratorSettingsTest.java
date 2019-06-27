package com.dexilog.smartkeyboard.settings;

import android.content.SharedPreferences;

import com.dexilog.smartkeyboard.settings.VibratorSettings;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class VibratorSettingsTest {
    @Mock
    SharedPreferences prefs;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test
    public void testEmptySettingsShouldGiveDefault() {
        VibratorSettings vibratorSettings = createSettings(-1, -1);
        assertEquals(15, vibratorSettings.getDurationMs());
    }

    @Test
    public void testConversionFromLegacySetting() {
        VibratorSettings vibratorSettings = createSettings(6, -1);
        assertEquals(40, vibratorSettings.getDurationMs());
    }

    @Test
    public void testNewSetting() {
        VibratorSettings vibratorSettings = createSettings(-1, 40);
        assertEquals(40, vibratorSettings.getDurationMs());
    }

    private VibratorSettings createSettings(int legacyDuration, int newDuration) {
        when(prefs.getInt("vibrator_duration", -1)).thenReturn(legacyDuration);
        when(prefs.getInt("vibrator_duration_ms", -1)).thenReturn(newDuration);
        SharedPreferences.Editor editor = mock(SharedPreferences.Editor.class);
        when(prefs.edit()).thenReturn(editor);
        return new VibratorSettings(prefs);
    }
}

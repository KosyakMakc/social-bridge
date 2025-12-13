package io.github.kosyakmakc.socialBridge.Localizations;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.kosyakmakc.socialBridge.SocialBridge;
import io.github.kosyakmakc.socialBridge.TestEnvironment.HeadlessMinecraftPlatform;
import io.github.kosyakmakc.socialBridge.TestEnvironment.ModuleForTest;
import io.github.kosyakmakc.socialBridge.TestEnvironment.TranslationSourceForTest;

public class LocalizationsTests {
    @Test
    public void createNew() throws SQLException, IOException {
        HeadlessMinecraftPlatform.Init();
        
        var module = new ModuleForTest();

        var english = new TranslationSourceForTest("en");
        english.Append("__Test__Create1", "test");
        english.Append("__Test__Create2", "test");
        english.Append("__Test__Create3", "test");

        var french = new TranslationSourceForTest("fr");
        french.Append("__Test__Create1", "test");
        french.Append("__Test__Create2", "test");
        french.Append("__Test__Create3", "test");

        module.addTranslation(english);
        module.addTranslation(french);

        SocialBridge.INSTANCE.getLocalizationService().restoreLocalizationsOfModule(module).join();
    }

    @Test
    public void checkCollision() throws SQLException, IOException {
        // simulate multiple starts with the same localizations
        HeadlessMinecraftPlatform.Init();
        
        var module = new ModuleForTest();

        var english = new TranslationSourceForTest("en");
        english.Append("__Test__Create1", "test");
        english.Append("__Test__Create2", "test");
        english.Append("__Test__Create3", "test");

        module.addTranslation(english);
        
        SocialBridge.INSTANCE.getLocalizationService().restoreLocalizationsOfModule(module).join();
        SocialBridge.INSTANCE.getLocalizationService().restoreLocalizationsOfModule(module).join();
    }

    @Test
    public void checkLanguageIsEmpty() throws SQLException, IOException {
        // simulate multiple starts with the same localizations
        HeadlessMinecraftPlatform.Init();
        
        var module = new ModuleForTest();

        var english = new TranslationSourceForTest("en");
        english.Append("", "test");

        module.addTranslation(english);
        
        Assertions.assertThrows(RuntimeException.class, () -> {
            SocialBridge.INSTANCE.getLocalizationService().restoreLocalizationsOfModule(module).join();
        });
    }

    @Test
    public void checkKeyIsEmpty() throws SQLException, IOException {
        // simulate multiple starts with the same localizations (restart server)
        HeadlessMinecraftPlatform.Init();
        
        var module = new ModuleForTest();

        var noLanguage = new TranslationSourceForTest("");
        noLanguage.Append("__Test__Create1", "test");
        noLanguage.Append("__Test__Create2", "test");
        noLanguage.Append("__Test__Create3", "test");

        module.addTranslation(noLanguage);
        
        Assertions.assertThrows(RuntimeException.class, () -> {
            SocialBridge.INSTANCE.getLocalizationService().restoreLocalizationsOfModule(module).join();
        });
    }
}

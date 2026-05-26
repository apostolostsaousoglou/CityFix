package com.citydamage.app;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class LanguageManager {

    private static final LanguageManager INSTANCE = new LanguageManager();
    private final BooleanProperty isGreek = new SimpleBooleanProperty(true);

    private LanguageManager() {}

    public static LanguageManager getInstance() {
        return INSTANCE;
    }

    public BooleanProperty isGreekProperty() {
        return isGreek;
    }

    public boolean isGreek() {
        return isGreek.get();
    }

    public void setGreek(boolean value) {
        isGreek.set(value);
    }

    public void toggle() {
        isGreek.set(!isGreek.get());
    }
}

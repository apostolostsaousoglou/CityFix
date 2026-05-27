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

    // ── Nav strings ───────────────────────────────────────────────────────────

    public String nav_home()    { return isGreek() ? "Αρχική"   : "Home"; }
    public String nav_reports() { return isGreek() ? "Αναφορές" : "Reports"; }
    public String nav_useful()  { return isGreek() ? "Χρήσιμα"  : "Useful"; }
    public String nav_login()   { return isGreek() ? "Είσοδος"  : "Login"; }

    // ── Hero strings ──────────────────────────────────────────────────────────

    public String hero_title()    { return isGreek() ? "Αναφορά Βλάβης" : "Report City Damage"; }
    public String hero_subtitle() { return isGreek()
        ? "Βοήθησε να βελτιωθεί η πόλη σου αναφέροντας προβλήματα υποδομών"
        : "Help improve your city by reporting infrastructure issues"; }
    public String hero_cta()      { return isGreek() ? "Αναφορά Προβλήματος" : "Report a Problem"; }

    // ── How It Works section ──────────────────────────────────────────────────

    public String how_title()   { return isGreek() ? "Πώς Λειτουργεί;" : "How Does It Work?"; }

    public String step1_title() { return isGreek() ? "Εντόπισε το Πρόβλημα" : "Locate the Problem"; }
    public String step1_desc()  { return isGreek()
        ? "Βγάλε μια φωτογραφία ή περιέγραψε το ζήτημα."
        : "Take a photo or describe the issue."; }

    public String step2_title() { return isGreek() ? "Υποβολή Αναφοράς" : "Submit a Report"; }
    public String step2_desc()  { return isGreek()
        ? "Συμπλήρωσε τα στοιχεία και στείλε την αναφορά σου."
        : "Fill in the details and send your report."; }
}

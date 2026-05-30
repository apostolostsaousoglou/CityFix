package com.citydamage.app;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Singleton language manager. isGreek=true means Greek UI, false means English.
 */
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

    // ── Text helpers ──────────────────────────────────────────────────────────

    public String nav_home()      { return isGreek() ? "Αρχική"          : "Home"; }
    public String nav_reports()   { return isGreek() ? "Αναφορές"        : "Reports"; }
    public String nav_useful()    { return isGreek() ? "Χρήσιμα"         : "Useful"; }
    public String nav_login()     { return isGreek() ? "Είσοδος/Εγγραφή" : "Login / Register"; }

    public String hero_title()    { return isGreek() ? "Είδες κάτι; Ανέφερε το." : "Saw something? Report it."; }
    public String hero_subtitle() { return isGreek() ? "Κάνε την πόλη σου πιο ασφαλή, με ένα κλικ!" : "Make your city safer — with one click!"; }
    public String hero_cta()      { return isGreek() ? "Αναφορά Προβλήματος" : "Report a Problem"; }

    public String how_title()     { return isGreek() ? "Πώς Λειτουργεί;" : "How Does It Work?"; }

    public String step1_title()   { return isGreek() ? "Εντόπισε το Πρόβλημα" : "Locate the Problem"; }
    public String step1_desc()    { return isGreek() ? "Βγάλε μια φωτογραφία ή περιέγραψε το ζήτημα." : "Take a photo or describe the issue."; }

    public String step2_title()   { return isGreek() ? "Υποβολή Αναφοράς" : "Submit a Report"; }
    public String step2_desc()    { return isGreek() ? "Συμπλήρωσε τα στοιχεία και στείλε την αναφορά σου." : "Fill in the details and send your report."; }

    public String step3_title()   { return isGreek() ? "Παρακολούθησε την Εξέλιξη" : "Track Progress"; }
    public String step3_desc()    { return isGreek() ? "Λάβε ενημερώσεις για την πορεία της επίλυσης." : "Get updates on the resolution progress."; }

    public String footer()        { return "© 2025 City Damage Reporter | Keeping Our City Safe"; }
}

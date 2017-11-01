package net.tfobz.vokabeltrainer;

import android.content.Context;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import net.tfobz.vokabeltrainer.model.Fach;
import net.tfobz.vokabeltrainer.model.Karte;
import net.tfobz.vokabeltrainer.model.Lernkartei;
import net.tfobz.vokabeltrainer.model.VokabeltrainerDB;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class VokabeltrainerDBTest {

    public static final String TAG = VokabeltrainerDBTest.class.getCanonicalName();

    private static Context context = null;

    @BeforeClass
    public static void holeContext() {
        context = InstrumentationRegistry.getTargetContext();
    }
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("net.tfobz.vokabeltrainer", appContext.getPackageName());
    }
    @Test
    public void getLernkarteiTest() {
        VokabeltrainerDB.getInstance(context).loeschenTabellen();
        VokabeltrainerDB.getInstance(context).erstellenTabellen();
        VokabeltrainerDB.getInstance(context).hinzufuegenTestdaten();
        Lernkartei l = VokabeltrainerDB.getInstance(context).getLernkartei(1);
        assertNotNull(l);
        assertEquals(l.getBeschreibung(),"Vokabeltrainer Deutsch Englisch");
        assertEquals(l.getWortEinsBeschreibung(),"Deutsch");
        assertEquals(l.getWortZweiBeschreibung(),"Englisch");
        assertEquals(l.getNummer(),1);
        assertEquals(l.getGrossKleinschreibung(),false);
        assertEquals(l.getRichtung(),true);
        l = VokabeltrainerDB.getInstance(context).getLernkartei(2);
        assertNotNull(l);
        assertEquals(l.getBeschreibung(),"Vokabeltrainer Deutsch Italienisch");
        assertEquals(l.getWortEinsBeschreibung(),"Deutsch");
        assertEquals(l.getWortZweiBeschreibung(),"Italienisch");
        assertEquals(l.getNummer(),2);
        assertEquals(l.getGrossKleinschreibung(),true);
        assertEquals(l.getRichtung(),false);
        l = VokabeltrainerDB.getInstance(context).getLernkartei(3);
        assertNotNull(l);
        assertEquals(l.getBeschreibung(),"Vokabeltrainer Deutsch Französisch");
        assertEquals(l.getWortEinsBeschreibung(),"Deutsch");
        assertEquals(l.getWortZweiBeschreibung(),"Französisch");
        assertEquals(l.getNummer(),3);
        assertEquals(l.getGrossKleinschreibung(),true);
        assertEquals(l.getRichtung(),false);
        assertNull(VokabeltrainerDB.getInstance(context).getLernkartei(4));
    }
    @Test
    public void getLernkarteienTest() {
        VokabeltrainerDB.getInstance(context).loeschenTabellen();
        VokabeltrainerDB.getInstance(context).erstellenTabellen();
        assertEquals(0, VokabeltrainerDB.getInstance(context).getLernkarteien().size());
        VokabeltrainerDB.getInstance(context).hinzufuegenTestdaten();
        assertEquals(3, VokabeltrainerDB.getInstance(context).getLernkarteien().size());
    }
    @Test
    public void getFaecherTest() {
        VokabeltrainerDB.getInstance(context).loeschenTabellen();
        VokabeltrainerDB.getInstance(context).erstellenTabellen();
        VokabeltrainerDB.getInstance(context).hinzufuegenTestdaten();
        assertEquals(null, VokabeltrainerDB.getInstance(context).getFaecher(0));
        assertEquals(1, VokabeltrainerDB.getInstance(context).getFaecher(1).size());
        assertEquals(0, VokabeltrainerDB.getInstance(context).getFaecher(3).size());
    }
    @Test
    public void getFachTest() {
        VokabeltrainerDB.getInstance(context).loeschenTabellen();
        VokabeltrainerDB.getInstance(context).erstellenTabellen();
        VokabeltrainerDB.getInstance(context).hinzufuegenTestdaten();
        assertNull(VokabeltrainerDB.getInstance(context).getFach(1, 3));
        Fach f = VokabeltrainerDB.getInstance(context).getFach(1, 1);
        assertEquals(1, f.getNummer());
        assertEquals("Fach 1", f.getBeschreibung());
        assertEquals(0, f.getErinnerungsIntervall());
        assertEquals(VokabeltrainerDB.convertToString(new Date()),
                VokabeltrainerDB.convertToString(f.getGelerntAm()));
    }
  @Test
  public void getFachNummerFachTest() {
    VokabeltrainerDB.getInstance(context).loeschenTabellen();
    VokabeltrainerDB.getInstance(context).erstellenTabellen();
    VokabeltrainerDB.getInstance(context).hinzufuegenTestdaten();
    assertNull(VokabeltrainerDB.getInstance(context).getFach(3));
    Fach f = VokabeltrainerDB.getInstance(context).getFach(1);
    assertEquals(1, f.getNummer());
    assertEquals("Fach 1", f.getBeschreibung());
    assertEquals(0, f.getErinnerungsIntervall());
    assertEquals(VokabeltrainerDB.convertToString(new Date()),
            VokabeltrainerDB.convertToString(f.getGelerntAm()));
  }
  @Test
  public void getKarteTest() {
    VokabeltrainerDB.getInstance(context).loeschenTabellen();
    VokabeltrainerDB.getInstance(context).erstellenTabellen();
    VokabeltrainerDB.getInstance(context).hinzufuegenTestdaten();
    assertNull(VokabeltrainerDB.getInstance(context).getZufaelligeKarte(1, 3));
    Fach f = VokabeltrainerDB.getInstance(context).getFach(1, 1);
    f.setGelerntAm(VokabeltrainerDB.getDateOneDayBeforeToday());
    assertEquals(0, VokabeltrainerDB.getInstance(context).aendernFach(f));
    Karte k = VokabeltrainerDB.getInstance(context).getZufaelligeKarte(1, 1);
    assertNotNull(k);
    assertEquals("Haus", k.getWortEins());
    assertEquals("house", k.getWortZwei());
    f = VokabeltrainerDB.getInstance(context).getFach(1, 1);
    assertEquals(VokabeltrainerDB.convertToString(
            VokabeltrainerDB.getDateOneDayBeforeToday()), f.getGelerntAmString());
  }
  @Test
  public void setKarteRichtigTest() {
    VokabeltrainerDB.getInstance(context).loeschenTabellen();
    VokabeltrainerDB.getInstance(context).erstellenTabellen();
    VokabeltrainerDB.getInstance(context).hinzufuegenTestdaten();
    Karte k = VokabeltrainerDB.getInstance(context).getZufaelligeKarte(1, 1);
    assertEquals(-2, VokabeltrainerDB.getInstance(context).setKarteRichtig(k));
    Fach f = new Fach();
    assertEquals(0, VokabeltrainerDB.getInstance(context).hinzufuegenFach(1,f));
    f = VokabeltrainerDB.getInstance(context).getFach(3);
    assertEquals(0, VokabeltrainerDB.getInstance(context).setKarteRichtig(k));
    assertEquals(null, VokabeltrainerDB.getInstance(context).getZufaelligeKarte(1, 1));
    assertEquals(k, VokabeltrainerDB.getInstance(context).getZufaelligeKarte(1, 3));
  }
  @Test
  public void setKarteRichtigTest1() {
    VokabeltrainerDB.getInstance(context).loeschenTabellen();
    VokabeltrainerDB.getInstance(context).erstellenTabellen();
    VokabeltrainerDB.getInstance(context).hinzufuegenTestdaten();
    Fach f = VokabeltrainerDB.getInstance(context).getFach(1);
    f.setGelerntAm(VokabeltrainerDB.getDateOneDayBeforeToday());
    VokabeltrainerDB.getInstance(context).aendernFach(f);
    f = VokabeltrainerDB.getInstance(context).getFach(1);
    assertEquals(VokabeltrainerDB.getDateOneDayBeforeToday(), f.getGelerntAm());
    Fach f1 = new Fach();
    assertEquals(0,VokabeltrainerDB.getInstance(context).hinzufuegenFach(1, f1));
    Karte k = VokabeltrainerDB.getInstance(context).getZufaelligeKarte(1, 1);
    assertEquals(0, VokabeltrainerDB.getInstance(context).setKarteRichtig(k));
    f = VokabeltrainerDB.getInstance(context).getFach(1);
    assertEquals(VokabeltrainerDB.getActualDate(), f.getGelerntAm());
  }
  @Test
  public void setKarteFalschTest() {
    VokabeltrainerDB.getInstance(context).loeschenTabellen();
    VokabeltrainerDB.getInstance(context).erstellenTabellen();
    VokabeltrainerDB.getInstance(context).hinzufuegenTestdaten();
    Karte k = VokabeltrainerDB.getInstance(context).getZufaelligeKarte(1, 1);
    Fach f = new Fach();
    assertEquals(0, VokabeltrainerDB.getInstance(context).hinzufuegenFach(1,f));
    f = VokabeltrainerDB.getInstance(context).getFach(3);
    assertEquals(0, VokabeltrainerDB.getInstance(context).setKarteRichtig(k));
    assertEquals(null, VokabeltrainerDB.getInstance(context).getZufaelligeKarte(1, 1));
    assertEquals(k, VokabeltrainerDB.getInstance(context).getZufaelligeKarte(1, 3));
    assertEquals(0, VokabeltrainerDB.getInstance(context).setKarteFalsch(k));
    assertEquals(k, VokabeltrainerDB.getInstance(context).getZufaelligeKarte(1, 1));
    assertEquals(null, VokabeltrainerDB.getInstance(context).getZufaelligeKarte(1, 3));
  }
  @Test
  public void setKarteFalschTest1() {
    VokabeltrainerDB.getInstance(context).loeschenTabellen();
    VokabeltrainerDB.getInstance(context).erstellenTabellen();
    VokabeltrainerDB.getInstance(context).hinzufuegenTestdaten();
    Fach f = VokabeltrainerDB.getInstance(context).getFach(1);
    f.setGelerntAm(VokabeltrainerDB.getDateOneDayBeforeToday());
    VokabeltrainerDB.getInstance(context).aendernFach(f);
    f = VokabeltrainerDB.getInstance(context).getFach(1);
    assertEquals(
            VokabeltrainerDB.getDateOneDayBeforeToday(), f.getGelerntAm());
    Karte k = VokabeltrainerDB.getInstance(context).getZufaelligeKarte(1, 1);
    assertEquals(0, VokabeltrainerDB.getInstance(context).setKarteFalsch(k));
    f = VokabeltrainerDB.getInstance(context).getFach(1);
    assertEquals(
            VokabeltrainerDB.getActualDate(), f.getGelerntAm());
  }

  @Test
  public void hinzufuegenFachTest() {
    VokabeltrainerDB.getInstance(context).loeschenTabellen();
    VokabeltrainerDB.getInstance(context).erstellenTabellen();
    VokabeltrainerDB.getInstance(context).hinzufuegenTestdaten();
    Fach f = new Fach();
    assertEquals(0, VokabeltrainerDB.getInstance(context).hinzufuegenFach(1, f));
    assertEquals(3, f.getNummer());
    f = VokabeltrainerDB.getInstance(context).getFach(3);
    assertEquals("Fach 2", f.getBeschreibung());
    assertEquals(1, f.getErinnerungsIntervall());
    assertEquals(VokabeltrainerDB.getActualDate(), f.getGelerntAm());
    assertEquals(-1, VokabeltrainerDB.getInstance(context).hinzufuegenFach(1, f));
    f = new Fach();
    f.setGelerntAm(VokabeltrainerDB.getDateOneDayBeforeToday());
    assertEquals(0, VokabeltrainerDB.getInstance(context).hinzufuegenFach(1, f));
    assertEquals(-1, VokabeltrainerDB.getInstance(context).hinzufuegenFach(10, f));
    f = VokabeltrainerDB.getInstance(context).getFach(4);
    assertEquals(
            VokabeltrainerDB.convertToString(VokabeltrainerDB.getDateOneDayBeforeToday()),
            VokabeltrainerDB.convertToString(f.getGelerntAm()));
  }
  @Test
  public void aendernFachTest() {
    VokabeltrainerDB.getInstance(context).loeschenTabellen();
    VokabeltrainerDB.getInstance(context).erstellenTabellen();
    VokabeltrainerDB.getInstance(context).hinzufuegenTestdaten();
    Fach f = VokabeltrainerDB.getInstance(context).getFach(1, 1);
    f.setGelerntAm(VokabeltrainerDB.getDateOneDayBeforeToday());
    f.setBeschreibung("x");
    f.setErinnerungsIntervall(3);
    assertEquals(0, VokabeltrainerDB.getInstance(context).aendernFach(f));
    f = VokabeltrainerDB.getInstance(context).getFach(1, 1);
    assertEquals("x", f.getBeschreibung());
    assertEquals(3, f.getErinnerungsIntervall());
    assertEquals(VokabeltrainerDB.getDateOneDayBeforeToday(), f.getGelerntAm());
    f.setBeschreibung(null);
    assertEquals(-2, VokabeltrainerDB.getInstance(context).aendernFach(f));
  }
  @Test
  public void hinzufuegenLernkarteiTest() {
    VokabeltrainerDB.getInstance(context).loeschenTabellen();
    VokabeltrainerDB.getInstance(context).erstellenTabellen();
    VokabeltrainerDB.getInstance(context).hinzufuegenTestdaten();
    Lernkartei l = new Lernkartei();
    assertEquals(-2, VokabeltrainerDB.getInstance(context).hinzufuegenLernkartei(l));
    assertEquals(3, l.getFehler().size());
    l.setBeschreibung("b");
    l.setWortEinsBeschreibung("wb1");
    l.setWortZweiBeschreibung("wb2");
    assertEquals(0, VokabeltrainerDB.getInstance(context).hinzufuegenLernkartei(l));
    assertEquals(4, l.getNummer());
    Lernkartei l1 = VokabeltrainerDB.getInstance(context).getLernkartei(4);
    assertEquals(l, l1);
    l = new Lernkartei();
    l.setBeschreibung("b");
    l.setWortEinsBeschreibung("wb1");
    l.setWortZweiBeschreibung("wb2");
    assertEquals(-2, VokabeltrainerDB.getInstance(context).hinzufuegenLernkartei(l));
    assertEquals(1, l.getFehler().size());
  }
  @Test
  public void aendernLernkarteiTest() {
    VokabeltrainerDB.getInstance(context).loeschenTabellen();
    VokabeltrainerDB.getInstance(context).erstellenTabellen();
    VokabeltrainerDB.getInstance(context).hinzufuegenTestdaten();
    Lernkartei l = VokabeltrainerDB.getInstance(context).getLernkartei(1);
    l.setBeschreibung(null);
    assertEquals(-2, VokabeltrainerDB.getInstance(context).aendernLernkartei(l));
    Lernkartei l1 = VokabeltrainerDB.getInstance(context).getLernkartei(2);
    l.setBeschreibung(l1.getBeschreibung());
    assertEquals(-2, VokabeltrainerDB.getInstance(context).aendernLernkartei(l));
    l.setBeschreibung("b2");
    l.setWortEinsBeschreibung("w1");
    l.setWortZweiBeschreibung("w2");
    assertEquals(0, VokabeltrainerDB.getInstance(context).aendernLernkartei(l));
    l1 = VokabeltrainerDB.getInstance(context).getLernkartei(1);
    assertEquals(l, l1);
  }
  @Test
  public void loeschenLernkarteiTest() {
    VokabeltrainerDB.getInstance(context).loeschenTabellen();
    VokabeltrainerDB.getInstance(context).erstellenTabellen();
    VokabeltrainerDB.getInstance(context).hinzufuegenTestdaten();
    assertEquals(-1, VokabeltrainerDB.getInstance(context).loeschenLernkartei(-1));
    assertNotNull(VokabeltrainerDB.getInstance(context).getFach(1));
    assertEquals(0, VokabeltrainerDB.getInstance(context).loeschenLernkartei(1));
    assertNull(VokabeltrainerDB.getInstance(context).getFach(1));
  }
  @Test
  public void hinzufuegenKarteTest() {
    VokabeltrainerDB.getInstance(context).loeschenTabellen();
    VokabeltrainerDB.getInstance(context).erstellenTabellen();
    VokabeltrainerDB.getInstance(context).hinzufuegenTestdaten();
    Karte k = new Karte();
    assertEquals(-2, VokabeltrainerDB.getInstance(context).hinzufuegenKarte(1, k));
    k.setWortEins("Haus");
    k.setWortZwei("House");
    assertEquals(-5, VokabeltrainerDB.getInstance(context).hinzufuegenKarte(1, k));
    k.setWortEins("w1");
    k.setWortZwei("w1");
    assertEquals(-3, VokabeltrainerDB.getInstance(context).hinzufuegenKarte(10, k));
    assertEquals(-4, VokabeltrainerDB.getInstance(context).hinzufuegenKarte(3, k));
    assertEquals(0, VokabeltrainerDB.getInstance(context).hinzufuegenKarte(2, k));
    assertEquals(k, VokabeltrainerDB.getInstance(context).getZufaelligeKarte(2, 2));
  }
  @Test
  public void aendernKarteTest() {
    VokabeltrainerDB.getInstance(context).loeschenTabellen();
    VokabeltrainerDB.getInstance(context).erstellenTabellen();
    VokabeltrainerDB.getInstance(context).hinzufuegenTestdaten();
    Karte k = VokabeltrainerDB.getInstance(context).getZufaelligeKarte(1,1);
    assertEquals(1, k.getNummer());
    k.setWortEins(null);
    assertEquals(-2, VokabeltrainerDB.getInstance(context).aendernKarte(k));
    Karte k1 = new Karte();
    k1.setWortEins("w1");
    k1.setWortZwei("w2");
    assertEquals(0, VokabeltrainerDB.getInstance(context).hinzufuegenKarte(1, k1));
    k.setWortEins("w1");
    k.setWortZwei("w2");
    assertEquals(-4, VokabeltrainerDB.getInstance(context).aendernKarte(k));
    k.setWortEins("w11");
    k.setWortZwei("w21");
    assertEquals(0, VokabeltrainerDB.getInstance(context).aendernKarte(k));
    assertEquals(k,VokabeltrainerDB.getInstance(context).getKarte(k.getNummer()));
  }
  @Test
  public void loeschenKarteTest() {
    VokabeltrainerDB.getInstance(context).loeschenTabellen();
    VokabeltrainerDB.getInstance(context).erstellenTabellen();
    VokabeltrainerDB.getInstance(context).hinzufuegenTestdaten();
    assertEquals(-1, VokabeltrainerDB.getInstance(context).loeschenKarte(10));
    assertEquals(0, VokabeltrainerDB.getInstance(context).loeschenKarte(1));
    assertNull(VokabeltrainerDB.getInstance(context).getZufaelligeKarte(1, 1));
  }
  @Test
  public void loeschenAlleFaecherTest() {
    VokabeltrainerDB.getInstance(context).loeschenTabellen();
    VokabeltrainerDB.getInstance(context).erstellenTabellen();
    VokabeltrainerDB.getInstance(context).hinzufuegenTestdaten();
    assertEquals(-1, VokabeltrainerDB.getInstance(context).loeschenAlleFaecher(10));
    assertEquals(0, VokabeltrainerDB.getInstance(context).loeschenAlleFaecher(1));
    assertEquals(0, VokabeltrainerDB.getInstance(context).getFaecher(1).size());
    assertNull(VokabeltrainerDB.getInstance(context).getZufaelligeKarte(1,1));
  }
  @Test
  public void getLernkarteienErinnerungTest() {
    VokabeltrainerDB.getInstance(context).loeschenTabellen();
    VokabeltrainerDB.getInstance(context).erstellenTabellen();
    VokabeltrainerDB.getInstance(context).hinzufuegenTestdaten();
    assertEquals(0, VokabeltrainerDB.getInstance(context).getLernkarteienErinnerung().size());
    Fach f = VokabeltrainerDB.getInstance(context).getFach(1);
    f.setGelerntAm(VokabeltrainerDB.getDateOneDayBeforeToday());
    f.setErinnerungsIntervall(2);
    VokabeltrainerDB.getInstance(context).aendernFach(f);
    assertEquals(0, VokabeltrainerDB.getInstance(context).getLernkarteienErinnerung().size());
    f.setErinnerungsIntervall(0);
    VokabeltrainerDB.getInstance(context).aendernFach(f);
    assertEquals(0, VokabeltrainerDB.getInstance(context).getLernkarteienErinnerung().size());
    f.setErinnerungsIntervall(1);
    VokabeltrainerDB.getInstance(context).aendernFach(f);
    assertEquals(1, VokabeltrainerDB.getInstance(context).getLernkarteienErinnerung().size());
    assertEquals(VokabeltrainerDB.getInstance(context).getLernkarteienErinnerung().get(0),
            VokabeltrainerDB.getInstance(context).getLernkartei(1));
  }
  @Test
  public void getFaecherErinnerungTest() {
    VokabeltrainerDB.getInstance(context).loeschenTabellen();
    VokabeltrainerDB.getInstance(context).erstellenTabellen();
    VokabeltrainerDB.getInstance(context).hinzufuegenTestdaten();
    assertNull(VokabeltrainerDB.getInstance(context).getFaecherErinnerung(10));
    assertEquals(0, VokabeltrainerDB.getInstance(context).getFaecherErinnerung(1).size());
    Fach f = VokabeltrainerDB.getInstance(context).getFach(1);
    f.setGelerntAm(VokabeltrainerDB.getDateOneDayBeforeToday());
    f.setErinnerungsIntervall(1);
    VokabeltrainerDB.getInstance(context).aendernFach(f);
    assertEquals(1, VokabeltrainerDB.getInstance(context).getFaecherErinnerung(1).size());
    assertEquals(VokabeltrainerDB.getInstance(context).getFaecherErinnerung(1).get(0),
            VokabeltrainerDB.getInstance(context).getFach(1));
  }
  @Test
  public void getKartenTest() {
    VokabeltrainerDB.getInstance(context).loeschenTabellen();
    VokabeltrainerDB.getInstance(context).erstellenTabellen();
    VokabeltrainerDB.getInstance(context).hinzufuegenTestdaten();
    assertNull(VokabeltrainerDB.getInstance(context).getKarten(10));
    assertEquals(0, VokabeltrainerDB.getInstance(context).getKarten(2).size());
    assertEquals(1, VokabeltrainerDB.getInstance(context).getKarten(1).size());
  }
  @Test
  public void getStandardLernkarteiTest() {
    VokabeltrainerDB.getInstance(context).loeschenTabellen();
    VokabeltrainerDB.getInstance(context).erstellenTabellen();
    VokabeltrainerDB.getInstance(context).hinzufuegenTestdaten();
    assertNull(VokabeltrainerDB.getInstance(context).getEinstellungenStandardLernkartei());
    assertEquals(0, VokabeltrainerDB.getInstance(context).setEinstellungenStandardLernkartei(1));
    assertEquals(1, VokabeltrainerDB.getInstance(context).getEinstellungenStandardLernkartei().getNummer());
  }
  @Test
  public void setStandardLernkarteiTest() {
    VokabeltrainerDB.getInstance(context).loeschenTabellen();
    VokabeltrainerDB.getInstance(context).erstellenTabellen();
    VokabeltrainerDB.getInstance(context).hinzufuegenTestdaten();
    assertEquals(-1, VokabeltrainerDB.getInstance(context).setEinstellungenStandardLernkartei(10));
    assertEquals(0, VokabeltrainerDB.getInstance(context).setEinstellungenStandardLernkartei(1));
    assertEquals(1, VokabeltrainerDB.getInstance(context).getEinstellungenStandardLernkartei().getNummer());
    assertEquals(0, VokabeltrainerDB.getInstance(context).setEinstellungenStandardLernkartei(-1));
    assertNull(VokabeltrainerDB.getInstance(context).getEinstellungenStandardLernkartei());
  }
  @Test
  public void setStandardLernkarteienMitErinnerungTest() {
    VokabeltrainerDB.getInstance(context).loeschenTabellen();
    VokabeltrainerDB.getInstance(context).erstellenTabellen();
    VokabeltrainerDB.getInstance(context).hinzufuegenTestdaten();
    assertEquals(0, VokabeltrainerDB.getInstance(context).setEinstellungenLernkarteienMitErinnerung(true));
    assertTrue(VokabeltrainerDB.getInstance(context).getEinstellungenLernkarteienMitErinnerung());
    assertEquals(0, VokabeltrainerDB.getInstance(context).setEinstellungenLernkarteienMitErinnerung(false));
    assertFalse(VokabeltrainerDB.getInstance(context).getEinstellungenLernkarteienMitErinnerung());
  }
  @Test
  public void getErinnerungFaelligTest() {
    Fach f = new Fach();
    f.setErinnerungsIntervall(1);
    f.setGelerntAm(new Date());
    assertEquals(false, f.getErinnerungFaellig());
    f.setErinnerungsIntervall(0);
    assertEquals(false, f.getErinnerungFaellig());
    f.setErinnerungsIntervall(1);
    f.setGelerntAm(VokabeltrainerDB.getDateOneDayBeforeToday());
    assertEquals(true, f.getErinnerungFaellig());
  }
}

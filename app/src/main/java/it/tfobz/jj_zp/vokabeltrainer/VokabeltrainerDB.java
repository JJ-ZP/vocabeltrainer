package it.tfobz.jj_zp.vokabeltrainer;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

/**
 * Hilfsklasse zum Zugriff auf eine SQLite-Datenbank. Diese Klasse stellt keine Import- und
 * Export-Funktionen bereit
 * @author Michael
 */public class VokabeltrainerDB extends SQLiteOpenHelper
{
  private static final String TAG = VokabeltrainerDB.class.getName();

  private static final String DB_NAME = "lernkarteien.db";
  private static final int DB_VERSION = 4;

  protected static final String CREATE_EINSTELLUNGEN = "CREATE TABLE einstellungen( "
          + "  elnummerstandard INTEGER, "
          + "  elernkarteienmiterinnerung BOOLEAN DEFAULT 0, "
          + "  FOREIGN KEY (elnummerstandard) REFERENCES lernkarteien(lnummer) "
          + "    ON DELETE SET NULL ON UPDATE CASCADE " + "  );";
  protected static final String CREATE_LERNKARTEIEN = "CREATE TABLE lernkarteien( "
          + "  lnummer INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
          + "  lbeschreibung VARCHAR(500) NOT NULL UNIQUE, "
          + "  lworteinsbeschreibung VARCHAR(100) NOT NULL, "
          + "  lwortzweibeschreibung VARCHAR(100) NOT NULL, "
          + "  lrichtung BOOLEAN DEFAULT 1 NOT NULL, "
          + "  lgrosskleinschreibung BOOLEAN DEFAULT 0 NOT NULL " + "  );";
  protected static final String CREATE_FAECHER = "CREATE TABLE faecher( "
          + "  fnummer INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
          + "  fbeschreibung VARCHAR(500), "
          + "  ferinnerung INTEGER DEFAULT 1, "
          + "  fgelerntam INTEGER, "
          + "  lnummer INTEGER NOT NULL, "
          + "  FOREIGN KEY (lnummer) REFERENCES lernkarteien(lnummer) "
          + "    ON DELETE CASCADE ON UPDATE CASCADE " + "  );";
  protected static final String CREATE_KARTEN = "CREATE TABLE karten( "
          + "  knummer INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
          + "  kworteins VARCHAR(100) NOT NULL, "
          + "  kwortzwei VARCHAR(100) NOT NULL,"
          + "  kgrosskleinschreibung BOOLEAN DEFAULT 0 NOT NULL, "
          + "  fnummer INTEGER NOT NULL, "
          + "  FOREIGN KEY (fnummer) REFERENCES faecher(fnummer) "
          + "    ON DELETE CASCADE ON UPDATE CASCADE " + "  );";

  protected static final String DROP_EINSTELLUNGEN = "DROP TABLE einstellungen;";
  protected static final String DROP_LERNKARTEIEN = "DROP TABLE lernkarteien;";
  protected static final String DROP_FAECHER = "DROP TABLE faecher;";
  protected static final String DROP_KARTEN = "DROP TABLE karten;";

  protected static final String INSERT_LERNKARTEI1 = "INSERT INTO lernkarteien(lbeschreibung, lworteinsbeschreibung, lwortzweibeschreibung, lrichtung, lgrosskleinschreibung) "
          + "  VALUES('Vokabeltrainer Deutsch Englisch','Deutsch','Englisch',1,0);";
  protected static final String INSERT_LERNKARTEI2 = "INSERT INTO lernkarteien(lbeschreibung, lworteinsbeschreibung, lwortzweibeschreibung, lrichtung, lgrosskleinschreibung) "
          + "  VALUES('Vokabeltrainer Deutsch Italienisch','Deutsch','Italienisch',0,1);";
  protected static final String INSERT_LERNKARTEI3 = "INSERT INTO lernkarteien(lbeschreibung, lworteinsbeschreibung, lwortzweibeschreibung, lrichtung, lgrosskleinschreibung) "
          + "  VALUES('Vokabeltrainer Deutsch Französisch','Deutsch','Französisch',0,1);";
  protected static final String INSERT_FACH11 = "INSERT INTO faecher(fbeschreibung, ferinnerung, fgelerntam, lnummer) "
          + "  VALUES('Fach 1',0,'" + new Date().getTime() + "', 1);";
  protected static final String INSERT_FACH21 = "INSERT INTO faecher(fbeschreibung, ferinnerung, fgelerntam, lnummer) "
          + "  VALUES('Fach 1',1,'" + new Date().getTime() + "', 2);";
  protected static final String INSERT_KARTE111 = "INSERT INTO karten(kworteins, kwortzwei, fnummer) "
          + "  VALUES('Haus','house',1);";

  protected static final String INIT_EINSTELLUNGEN = "INSERT INTO einstellungen(elnummerstandard, elernkarteienmiterinnerung) "
        + "  VALUES (NULL, 0);";

  private static VokabeltrainerDB instanz;

    private VokabeltrainerDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static VokabeltrainerDB getInstance(Context context) {
      VokabeltrainerDB ret = null;
      if (instanz == null) {
        instanz = new VokabeltrainerDB(context);
        // Schaltet die Fremdschlüsselunterstützung in SQLite ein
        instanz.getReadableDatabase().execSQL("PRAGMA foreign_keys = 'ON'");
      }
      ret = instanz;
      return ret;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL(CREATE_LERNKARTEIEN);
      db.execSQL(CREATE_FAECHER);
      db.execSQL(CREATE_KARTEN);
      db.execSQL(CREATE_EINSTELLUNGEN);
      db.execSQL(INIT_EINSTELLUNGEN);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      loeschenTabellen();
      onCreate(db);
    }


    //--------------------------------------> OWN

    public ArrayList<Karte> getAllKarten(int lernkarteinummer){
      ArrayList<Karte> karten = new ArrayList<Karte>();
      for (Fach f : getFaecher(lernkarteinummer)) {
        karten.addAll(getKarten(f.getNummer()));
      }
      return karten;
    }

    public ArrayList<Karte> getAllZuLernendeKarten(int lernkarteinummer){
      ArrayList<Karte> karten = new ArrayList<Karte>();
      for (Fach f : getFaecherErinnerung(lernkarteinummer)) {
        karten.addAll(getKarten(f.getNummer()));
      }
      return karten;
    }

    public int setGelerntFach(int fachnummer){
      String sql =
              "UPDATE faecher " +
                      "  SET fgelerntam =  " + new Date().getTime() +
                      "  WHERE fnummer = " + fachnummer + ";";
      SQLiteStatement stmt = getReadableDatabase().compileStatement(sql);
      return stmt.executeUpdateDelete();
    }

    //--------------------------------------> END

    public void erstellenTabellen() {
      SQLiteDatabase db = getReadableDatabase();
      db.execSQL(CREATE_LERNKARTEIEN);
      db.execSQL(CREATE_FAECHER);
      db.execSQL(CREATE_KARTEN);
      db.execSQL(CREATE_EINSTELLUNGEN);
      db.execSQL(INIT_EINSTELLUNGEN);
    }
    public void loeschenTabellen() {
      SQLiteDatabase db = getReadableDatabase();
      db.execSQL(DROP_KARTEN);
      db.execSQL(DROP_FAECHER);
      db.execSQL(DROP_LERNKARTEIEN);
      db.execSQL(DROP_EINSTELLUNGEN);
    }
    public void hinzufuegenTestdaten() {
      SQLiteDatabase db = getReadableDatabase();
      db.execSQL(INSERT_LERNKARTEI1);
      db.execSQL(INSERT_LERNKARTEI2);
      db.execSQL(INSERT_LERNKARTEI3);
      db.execSQL(INSERT_FACH11);
      db.execSQL(INSERT_FACH21);
      db.execSQL(INSERT_KARTE111);
    }

    /**
     * Holt die Lernkartei welche die übergebene Nummer hat aus der Datenbank
     * @param nummerLernkartei
     * @return null falls Lernkartei nicht gefunden werden kann
     */
    public Lernkartei getLernkartei(int nummerLernkartei) {
      Lernkartei ret = null;
      Cursor c = null;
      try {
        String sql = "SELECT lnummer, lbeschreibung, lworteinsbeschreibung, lwortzweibeschreibung, lrichtung, lgrosskleinschreibung "
                + "  FROM lernkarteien"
                + "  WHERE lnummer = "
                + nummerLernkartei
                + ";";
        c = getReadableDatabase().rawQuery(sql, null);
        if (c.moveToNext()) {
          int nummer = c.getInt(0);
          String beschreibung = c.getString(1);
          String wortEinsBeschreibung = c.getString(2);
          String wortZweiBeschreibung = c.getString(3);
          boolean richtung = c.getInt(4) == 0 ? false : true;
          boolean grossKleinschreibung = c.getInt(5) == 0 ? false : true;
          ret = new Lernkartei(nummer, beschreibung, wortEinsBeschreibung,
                  wortZweiBeschreibung, richtung, grossKleinschreibung);
        }
      } catch (SQLException e) {
        ret = null;
      } finally {
        try {
          c.close();
        } catch (Exception e) {
          ;
        }
      }
      return ret;
    }

    /**
     * Liefert eine Liste aller Lernkarteien
     * @return null falls ein Datenbankfehler aufgetreten ist<br>
     *         eine leere ArrayList falls keine Lernkarteien zu finden sind
     */
    public List<Lernkartei> getLernkarteien() {
      ArrayList<Lernkartei> ret = new ArrayList();
      Cursor c = null;
      try {
        String sql =
                "SELECT lnummer, lbeschreibung, lworteinsbeschreibung, lwortzweibeschreibung, " +
                        "  lrichtung, lgrosskleinschreibung " +
                        "  FROM lernkarteien;";
        c = getReadableDatabase().rawQuery(sql, null);
        while (c.moveToNext()) {
          int nummer = c.getInt(0);
          String beschreibung = c.getString(1);
          String wortEinsBeschreibung = c.getString(2);
          String wortZweiBeschreibung = c.getString(3);
          boolean richtung = c.getInt(4) == 0 ? false : true;
          boolean grossKleinschreibung = c.getInt(5) == 0 ? false : true;
          Lernkartei l = new Lernkartei(nummer, beschreibung,
                  wortEinsBeschreibung, wortZweiBeschreibung, richtung,
                  grossKleinschreibung);
          ret.add(l);
        }
      } catch (SQLException e) {
        ret = null;
      } finally {
        try {
          c.close();
        } catch (Exception e) {
          ;
        }
      }
      return ret;
    }

    /**
     * Liefert zur übergebenen Lernkartei die Fächer
     * @return null falls die Lernkartei nicht existiert oder Datenbankfehler
     * aufgetreten ist<br>
     * eine leere ArrayList wenn keine Fächer zur Lernkartei existieren
     */
    public List<Fach> getFaecher(int nummerLernkartei) {
      ArrayList<Fach> ret = null;
      Cursor c = null;
      try {
        String sql =
                "SELECT * " +
                        "  FROM lernkarteien " +
                        "  WHERE lnummer = " + nummerLernkartei + ";";
        c = getReadableDatabase().rawQuery(sql, null);
        if (c.moveToNext()) {
          ret = new ArrayList();
          sql = "SELECT fnummer, fbeschreibung, ferinnerung, fgelerntam, lnummer " +
                  "  FROM faecher " +
                  "  WHERE lnummer = " + nummerLernkartei +
                  "  ORDER BY fnummer;";
          c = getReadableDatabase().rawQuery(sql, null);
          while (c.moveToNext()) {
            int nummer = c.getInt(0);
            String beschreibung = c.getString(1);
            int erinnerung = c.getInt(2);
            Date gelerntAm = new Date(c.getLong(3));
            Fach f = new Fach(nummer, beschreibung, erinnerung, gelerntAm);
            ret.add(f);
          }
        }
      } catch (SQLException e) {
        ret = null;
      } finally {
        try {
          c.close();
        } catch (Exception e) {
          ;
        }
      }
      return ret;
    }

    /**
     * Sucht das Fach mit der übergebenen Nummer. Dabei muss das Fach zur
     * Lernkartei mit der übergebenen Nummer gehören
     * @param nummerLernkartei
     * @param nummerFach
     * @return null falls Fach oder Lernkartei nicht vorhanden sind oder Datenbankfehler aufgetreten ist
     */
    public Fach getFach(int nummerLernkartei, int nummerFach) {
      Fach ret = null;
      Cursor c = null;
      try {
        String sql =
                "SELECT fnummer, fbeschreibung, ferinnerung, fgelerntam, lnummer " +
                        "  FROM faecher " +
                        "  WHERE fnummer = " + nummerFach + " " +
                        "    AND lnummer = " + nummerLernkartei+ ";";
        c = getReadableDatabase().rawQuery(sql, null);
        if (c.moveToNext()) {
          int nummer = c.getInt(0);
          String beschreibung = c.getString(1);
          int erinnerung = c.getInt(2);
          Date gelerntAm = new Date(c.getLong(3));
          ret = new Fach(nummer, beschreibung, erinnerung, gelerntAm);
        }
      } catch (SQLException e) {
        ret = null;
      } finally {
        try {
          c.close();
        } catch (Exception e) {
          ;
        }
      }
      return ret;
    }

    /**
     * Sucht das Fach mit der übergebenen Nummer
     * @param nummerFach
     * @return null falls Fach nicht vorhanden sind oder Datenbankfehler aufgetreten ist
     */
    public Fach getFach(int nummerFach) {
      Fach ret = null;
      Cursor c = null;
      try {
        String sql =
                "SELECT fnummer, fbeschreibung, ferinnerung, fgelerntam " +
                        "  FROM faecher " +
                        "  WHERE fnummer = " + nummerFach + ";";
        c = getReadableDatabase().rawQuery(sql, null);
        if (c.moveToNext()) {
          int nummer = c.getInt(0);
          String beschreibung = c.getString(1);
          int erinnerung = c.getInt(2);
          Date gelerntAm = new Date(c.getLong(3));
          ret = new Fach(nummer, beschreibung, erinnerung, gelerntAm);
        }
      } catch (SQLException e) {
        ret = null;
      } finally {
        try {
          c.close();
        } catch (Exception e) {
          ;
        }
      }
      return ret;
    }

    /**
     * Liefert eine zufällige Karte aus dem übergebenen Fach der übergebenen Lernkartei. Es wird
     * nicht abgespeichert, dass in diesem Fach heute gelernt wurde. Das Fach muss zur aktuellen
     * Lernkartei gehören
     * @param nummerLernkartei
     * @param nummerFach
     * @return null falls das Fach und/oder Lernkartei nicht existiert oder
     * wenn es keine Karte in diesem Fach gibt oder wenn ein Datenbankfehler auftritt
     */
    public Karte getZufaelligeKarte(int nummerLernkartei, int nummerFach) {
      Karte ret = null;
      Cursor c = null;
      try {
        String sql =
                "SELECT k.knummer, k.kworteins, k.kwortzwei, l.lrichtung, l.lgrosskleinschreibung " +
                        "  FROM karten k, faecher f, lernkarteien l " +
                        "  WHERE k.fnummer = f.fnummer " +
                        "    AND l.lnummer = f.lnummer " +
                        "    AND f.fnummer = " + nummerFach + " " +
                        "    AND f.lnummer = " + nummerLernkartei + " " +
                        "  ORDER BY RANDOM() " +
                        "  LIMIT 1;";
        c = getReadableDatabase().rawQuery(sql, null);
        if (c.moveToNext()) {
          int nummer = c.getInt(0);
          String wortEins = c.getString(1);
          String wortZwei = c.getString(2);
          boolean richtung = c.getInt(3) == 0 ? false : true;
          boolean grossKleinschreibung = c.getInt(3) == 0 ? false : true;
          ret = new Karte(nummer, wortEins, wortZwei, richtung, grossKleinschreibung);
        }
      } catch (SQLException e) {
        Log.e("LLOG", e.getMessage());
        ret = null;
      } finally {
        try {
          c.close();
        } catch (Exception e) {
          ;
        }
      }
      return ret;
    }

    /**
     * Liefert die Karte mit der übergebenen Nummer
     * @param nummerKarte
     * @return null falls Datenbankfehler aufgetreten ist oder die
     * Karte nicht gefunden werden konnte
     */
    public Karte getKarte(int nummerKarte) {
      Karte ret = null;
      Cursor c = null;
      try {
        String sql =
                "SELECT k.knummer, k.kworteins, k.kwortzwei, l.lrichtung, l.lgrosskleinschreibung " +
                        "  FROM karten k, faecher f, lernkarteien l " +
                        "  WHERE k.fnummer = f.fnummer AND " +
                        "    f.lnummer = l.lnummer AND " +
                        "    k.knummer = " + nummerKarte + ";";
        c = getReadableDatabase().rawQuery(sql, null);
        if (c.moveToNext()) {
          int nummer = c.getInt(0);
          String wortEins = c.getString(1);
          String wortZwei = c.getString(2);
          boolean richtung = c.getInt(3) == 0 ? false : true;
          boolean grossKleinschreibung = c.getInt(4) == 0 ? false : true;
          ret = new Karte(nummer, wortEins, wortZwei, richtung, grossKleinschreibung);
        }
      } catch (SQLException e) {
        ret = null;
      } finally {
        try {
          c.close();
        } catch (Exception e) {
          ;
        }
      }
      return ret;
    }

    /**
     * Schiebt eine Karte ein Fach nach hinten und merkt sich, dass in der Lernkartei
     * gelernt wurde
     * @param karte
     * @return 0 falls erfolgreich<br>
     * -1 falls Karte nicht abgespeichert oder Datenbankfehler aufgetreten ist<br>
     * -2 falls kein Fach existiert hinter dem Fach aus dem die Karte stammt
     */
    public int setKarteRichtig(Karte karte) {
        int ret = -1;
        Cursor c = null;
        SQLiteStatement stmt = null;
        try {
            if (karte != null && karte.getNummer() != -1) {
                String sql =
                        "SELECT fnummer " +
                        "  FROM faecher f2 " +
                        "  WHERE fnummer > " +
                        "    (SELECT f1. fnummer " +
                        "      FROM karten k1, faecher f1 " +
                        "      WHERE k1.fnummer = f1.fnummer AND " +
                        "        f2.lnummer = f1.lnummer AND " +
                        "        knummer = " + karte.getNummer() + ") " +
                        "  ORDER BY fnummer;";
                c = getReadableDatabase().rawQuery(sql, null);
                if (!c.moveToNext())
                    // Es existiert noch kein Fach hinter dem Fach in dem Karte steckt
                    ret = -2;
                else {
                    int fachNummer = c.getInt(0);
                    /*
                    sql =
                            "UPDATE faecher " +
                            "  SET fgelerntam =  " + new Date().getTime() +
                            "  WHERE fnummer = " +
                            "    (SELECT fnummer " +
                            "      FROM karten " +
                            "      WHERE knummer = " + karte.getNummer() + ");";
                    stmt = getReadableDatabase().compileStatement(sql);
                    if (stmt.executeUpdateDelete() == 1) {
                    */

                    sql =   "UPDATE karten " +
                            "  SET fnummer = " + fachNummer +
                            "  WHERE knummer = " + karte.getNummer() + ";";
                    stmt = getReadableDatabase().compileStatement(sql);
                    if (stmt.executeUpdateDelete() == 1)
                        ret = 0;
                    //}
                }
            }
        } catch (SQLException e) {
            ret = -1;
        } finally {
            try {
                c.close();
            } catch (Exception e) {
                ;
            }
            try {
                stmt.close();
            } catch (Exception e) {
                ;
            }
        }
        return ret;
    }

    /**
     * Schiebt die Karte ins erste Fach der Lernkartei und merkt sich, dass in der Lernkartei
     * gelernt wurde
     * @param karte
     * @return 0 falls erfolgreich<br>
     * -1 falls Karte noch nicht abgespeichert wurde oder Datenbankfehler aufgetreten ist
     */
    public int setKarteFalsch(Karte karte) {
        int ret = -1;
        Cursor c = null;
        SQLiteStatement stmt = null;
        try {
            if (karte != null && karte.getNummer() != -1) {
                String sql =
                        "SELECT fnummer " +
                        "  FROM faecher " +
                        "  WHERE lnummer = " +
                        "    (SELECT f.lnummer " +
                        "      FROM karten k, faecher f " +
                        "      WHERE k.fnummer = f.fnummer AND " +
                        "        knummer = " + karte.getNummer() + ") " +
                        "  ORDER BY fnummer;";
                c = getReadableDatabase().rawQuery(sql, null);
                c.moveToNext();
                int fachNummer = c.getInt(0);
                sql =
                        "UPDATE karten " +
                        "  SET fnummer = " + fachNummer +
                        "  WHERE knummer = " + karte.getNummer() + ";";
                stmt = getReadableDatabase().compileStatement(sql);
                //OWN
                if (stmt.executeUpdateDelete() == 1)
                  ret = 0;

                /*
                if (stmt.executeUpdateDelete() == 1) {
                    sql =
                            "UPDATE faecher " +
                            "  SET fgelerntam = " + new Date().getTime() +
                            "  WHERE fnummer = " +
                            "    (SELECT fnummer " +
                            "      FROM karten " +
                            "      WHERE knummer = " + karte.getNummer() + ");";
                    stmt = getReadableDatabase().compileStatement(sql);
                    if (stmt.executeUpdateDelete() == 1)
                        ret = 0;
                }
                */
            }
        } catch (SQLException e) {
            ret = -1;
        } finally {
            try {
                c.close();
            } catch (Exception e) {
                ;
            }
            try {
                stmt.close();
            } catch (Exception e) {
                ;
            }
        }
        return ret;
    }

    /**
     * Fügt neues Fach zur aktuellen Lernkartei am Ende hinzu. Im Fach wird die Nummer gesetzt,
     * welche vom Datenbanksystem vergeben wird. Im Fach wird gelerntAm auf das heutige
     * Datum gesetzt falls im übergebenen Objekt gelerntAm nicht gesetzt wurde. Wurde die
     * Fachbeschreibung nicht gesetzt, so wird diese auf "Fach <Nummer in Lernkartei>" gesetzt
     * @param nummerLernkartei
     * @param fach
     * @return -1 falls Lernkartei nicht existiert oder falls Fach schon in Datebank existiert
     */
    public int hinzufuegenFach(int nummerLernkartei, Fach fach) {
        int ret = -1;
        SQLiteStatement stmt = null;
        try {
            if (fach != null && fach.getNummer() == -1 && getLernkartei(nummerLernkartei) != null) {
                String sqlBeschreibung = null;
                if (fach.getBeschreibung() == null || fach.getBeschreibung().length() == 0) {
                    sqlBeschreibung = "'Fach " + (int)(getFaecher(nummerLernkartei).size() + 1) + "'";
                } else
                    sqlBeschreibung = "'" + fach.getBeschreibung() + "'";
                long sqlGelerntAm = 0;
                if (fach.getGelerntAm() == null)
                    sqlGelerntAm = new Date().getTime();
                else
                    sqlGelerntAm = fach.getGelerntAm().getTime();
                String sql =
                        "INSERT INTO faecher(fbeschreibung, ferinnerung, fgelerntam, lnummer) " +
                        "  VALUES(" + sqlBeschreibung + ", " + fach.getErinnerungsIntervall() +
                        "    , " + sqlGelerntAm + ", " + nummerLernkartei + ");";
                stmt = getReadableDatabase().compileStatement(sql);
                int ret1 = (int)stmt.executeInsert();
                if (ret1 == 0)
                    ret = -1;
                else {
                    fach.nummer = ret1;
                    ret = 0;
                }
            }
        } catch (SQLException e) {
            ret = -1;
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
                ;
            }
        }
        return ret;
    }

    /**
     * Ändert die Inhalte eines bestehenden Faches. Dabei können nur die Beschreibung,
     * die Erinnerung und gelerntAm geändert werden
     * @param fach
     * @return -1 falls Fach noch nicht in Datenbank exisitert oder Datenbankfehler<br>
     * -2 falls Validierungsfehler aufgetreten ist
     * aufgetreten ist
     */
    public int aendernFach(Fach fach) {
        int ret = -1;
        SQLiteStatement stmt = null;
        try {
            if (fach != null && fach.getNummer() != -1) {
                fach.validiere();
                if (fach.getFehler() != null)
                    ret = -2;
                else {
                    String sqlBeschreibung = null;
                    if (fach.getBeschreibung() == null || fach.getBeschreibung().length() == 0)
                        sqlBeschreibung = "fbeschreibung = NULL";
                    else
                        sqlBeschreibung = "fbeschreibung = '" + fach.getBeschreibung() + "'";
                    String sqlGelerntAm = null;
                    if (fach.getGelerntAm() == null)
                        sqlGelerntAm = "fgelerntam = NULL";
                    else
                        sqlGelerntAm = "fgelerntam = " + fach.getGelerntAm().getTime();
                    String sql =
                            "UPDATE faecher " +
                            "  SET " + sqlBeschreibung + ", " + sqlGelerntAm + ", " +
                            "    ferinnerung = " + fach.getErinnerungsIntervall() +
                            "  WHERE fnummer = " + fach.getNummer() + ";";
                    stmt = getReadableDatabase().compileStatement(sql);
                    if (stmt.executeUpdateDelete() == 0)
                        ret = -1;
                    else
                        ret = 0;
                }
            }
        } catch (SQLException e) {
            ret = -1;
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
                ;
            }
        }
        return ret;
    }

    /**
     * Fügt neue Lernkartei in die Datenbank ein. Die Lernkartei erhält die Nummer,
     * welche diese in der Datenbank hat
     * @param lernkartei
     * @return -1 falls Datenbankfehler aufgetreten ist<br>
     * -2 falls Validierungsfehler aufgetreten ist oder falls Lernkartei mit
     * dieser Beschreibung bereits existiert. In diesem Fall wird in Beschreibung eine
     * entsprechende Fehlermeldung gesetzt
     */
    public int hinzufuegenLernkartei(Lernkartei lernkartei) {
        int ret = -1;
        SQLiteStatement stmt = null;
        try {
            if (lernkartei != null && lernkartei.getNummer() == -1) {
                lernkartei.validiere();
                if (lernkartei.getFehler() != null)
                    ret = -2;
                else {
                    String sql =
                            "INSERT INTO lernkarteien(lbeschreibung, lworteinsbeschreibung, " +
                            "  lwortzweibeschreibung, lrichtung, lgrosskleinschreibung) " +
                            "  VALUES('" + lernkartei.getBeschreibung() + "', '" +
                            lernkartei.getWortEinsBeschreibung() + "', '" +
                            lernkartei.getWortZweiBeschreibung() + "', " +
                            (lernkartei.getRichtung() ? 1 : 0) + ", " +
                            (lernkartei.getGrossKleinschreibung() ? 1 : 0) + ");";
                    stmt = getReadableDatabase().compileStatement(sql);
                    lernkartei.nummer = (int)stmt.executeInsert();
                    ret = 0;
                }
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("2067")) {
                if (lernkartei.fehler == null)
                    lernkartei.fehler = new Hashtable<String, String>();
                lernkartei.fehler.put("beschreibung",
                        "Es existiert bereits Lernkartei mit gleicher Beschreibung");
                ret = -2;
            } else {
                Log.e("LLOG", e.getMessage());
                ret = -1;
            }
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
                ;
            }
        }
        return ret;
    }

    /**
     * Ändert eine bestehende Lernkartei in der Datenbank
     * @param lernkartei
     * @return -1 falls Lernkartei nicht in Datenbank existiert oder Datenbankfehler
     * aufgetreten ist<br>
     * -2 falls Validierungsfehler aufgetreten ist und falls Lernkartei mit dieser
     * Beschreibung schon existiert. Im letzteren Fall wird für die Beschreibung eine
     * entsprechende Fehlermeldung gesetzt
     */
    public  int aendernLernkartei(Lernkartei lernkartei) {
        int ret = -1;
        SQLiteStatement stmt = null;
        try {
            if (lernkartei != null && lernkartei.getNummer() != -1) {
                lernkartei.validiere();
                if (lernkartei.getFehler() != null)
                    ret = -2;
                else {
                    String sql =
                            "UPDATE lernkarteien " +
                            "  SET lbeschreibung = '" + lernkartei.getBeschreibung() + "', " +
                            "    lworteinsbeschreibung = '" + lernkartei.getWortEinsBeschreibung() + "', " +
                            "    lwortzweibeschreibung = '" + lernkartei.getWortZweiBeschreibung() + "', " +
                            "    lrichtung = " + (lernkartei.getRichtung() ? 1 : 0) + ", " +
                            "    lgrosskleinschreibung = " + (lernkartei.getGrossKleinschreibung() ? 1 : 0) +
                            "  WHERE lnummer = " + lernkartei.getNummer() + ";";
                    stmt = getReadableDatabase().compileStatement(sql);
                    if (stmt.executeUpdateDelete() == 0)
                        ret = -1;
                    else
                        ret = 0;
                }
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("2067")) {
                if (lernkartei.fehler == null)
                    lernkartei.fehler = new Hashtable<String, String>();
                lernkartei.fehler.put("beschreibung",
                        "Es existiert bereits Lernkartei mit gleicher Beschreibung");
                ret = -2;
            } else {
                ret = -1;
            }
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
                ;
            }
        }
        return ret;
    }

    /**
     * Löscht die übergebene Lernkartei aus der Datenbank. Dabei werden
     * auch alle Fächer und Karten die zu dieser Lernkartei gehören mit
     * gelöscht
     * @param nummerLernkartei
     * @return -1 falls Lernkartei nicht gefunden wurde oder ein Datenbankfehler
     * aufgetreten ist
     */
    public int loeschenLernkartei(int nummerLernkartei) {

        for(Karte k : getAllKarten(nummerLernkartei)){
          loeschenKarte(k.getNummer());
        }

        loeschenAlleFaecher(nummerLernkartei);

        int ret = -1;
        SQLiteStatement stmt = null;
        try {
            String sql =
                    "DELETE FROM lernkarteien " +
                    "  WHERE lnummer = " + nummerLernkartei + ";";
            stmt = getReadableDatabase().compileStatement(sql);
            if (stmt.executeUpdateDelete() == 0)
                ret = -1;
            else {
                ret = 0;
            }
        } catch (SQLException e) {
            ret = -1;
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
                ;
            }
        }
        return ret;
    }

  /**
   * Fügt eine neue Karte in das erste Fach der übergebenen Lernkartei ein. Die
   * Karte erhält beim Einfügen die Nummer welche diese in der Datenbank hat
   * @param nummerLernkartei
   * @param karte
   * @return -1 wenn Datenbankfehler aufgetreten ist<br>
   * -2 falls Karte nicht vollständig ist<br>
   * -3 falls Lernkartei nicht existiert<br>
   * -4 falls kein Fach in der Lernkartei existiert<br>
   * -5 falls dieselbe Karte in der Lernkartei in irgendeinem Fach schon existiert<br>
   */
  public int hinzufuegenKarte(int nummerLernkartei, Karte karte) {
    int ret = -1;
    SQLiteStatement stmt = null;
    Cursor c = null;
    if (karte != null && karte.getNummer() == -1) {
      karte.validiere();
      if (karte.getFehler() != null)
        ret = -2;
      else {
        List<Fach> faecher = getFaecher(nummerLernkartei);
        if (faecher == null)
          ret = -3;
        else
        if (faecher.size() == 0)
          ret = -4;
        else {
          try {
            String sql =
                    "SELECT lgrosskleinschreibung " +
                    "  FROM lernkarteien " +
                    "  WHERE lnummer = " + nummerLernkartei + ";";
            c = getReadableDatabase().rawQuery(sql, null);
            c.moveToNext();
            if (c.getInt(0) == 1)
              sql =
                      "SELECT COUNT(*) " +
                      "  FROM karten k, faecher f, lernkarteien l " +
                      "  WHERE k.fnummer = f.fnummer AND " +
                      "    f.lnummer = " + nummerLernkartei + " AND " +
                      "    kworteins = '" + karte.getWortEins() + "' AND " +
                      "    kwortzwei = '" + karte.getWortZwei() + "';";
            else
              sql =
                      "SELECT COUNT(*) " +
                      "  FROM karten k, faecher f, lernkarteien l " +
                      "  WHERE k.fnummer = f.fnummer AND " +
                      "    f.lnummer = " + nummerLernkartei + " AND " +
                      "    LOWER(kworteins) = '" + karte.getWortEins().toLowerCase() + "' AND " +
                      "    LOWER(kwortzwei) = '" + karte.getWortZwei().toLowerCase() + "';";
            c.close();
            c = getReadableDatabase().rawQuery(sql, null);
            c.moveToNext();
            if (c.getInt(0) != 0)
              ret = -5;
            else {
              sql =
                      "INSERT INTO karten(kworteins, kwortzwei, fnummer, kgrosskleinschreibung) " +
                      "  VALUES('" + karte.getWortEins() + "', '" + karte.getWortZwei() + "', " +
                      faecher.get(0).getNummer() + ", " + (karte.getGrossKleinschreibung() ? 1 : 0) + ");";
              stmt = getReadableDatabase().compileStatement(sql);
              int nummer = (int)stmt.executeInsert();
              if (nummer == 0)
                ret = -1;
              else {
                karte.nummer = nummer;
                ret = 0;
              }
            }
          } catch (SQLException e) {
            Log.e("LLOG", e.getMessage());
            ret = -1;
          } finally {
            try {
              c.close();
            } catch (Exception e) {
              ;
            }
            try {
              stmt.close();
            } catch (Exception e) {
              ;
            }
          }
        }
      }
    }
    return ret;
  }

  /**
   * Ändert von einer bestehenden Karte die Wörter. Andere Eigenschaften der
   * Karte können nicht geändert werden. Die Karte verbleibt in demselben Fach
   * @param karte
   * @return -1 falls Karte nicht in Datenbank ist oder Datenbankfehler aufgetreten ist<br>
   * -2 falls Karte nicht vollständig ist<br>
   * -4 falls Karte mit demselben Inhalten in irgendeinem Fach bereits vorhanden ist
   */
  public int aendernKarte(Karte karte) {
    int ret = -1;
    SQLiteStatement stmt = null;
    Cursor c = null;
    if (karte != null && karte.getNummer() != -1) {
      karte.validiere();
      if (karte.getFehler() != null)
        ret = -2;
      else {
        try {
          String sql = null;
          if (karte.grossKleinschreibung)
            sql =
                    "SELECT COUNT(*) " +
                    "  FROM karten k1, faecher f1, faecher f2, karten k2 " +
                    "  WHERE k1.fnummer = f1.fnummer AND " +
                    "    k1.kworteins = '" + karte.getWortEins() + "' AND " +
                    "    k1.kwortzwei = '" + karte.getWortZwei() + "' AND " +
                    "    k1.knummer <> " + karte.getNummer() + " AND " +
                    "    f1.lnummer = f2.lnummer AND " +
                    "    f2.fnummer = k2.knummer AND " +
                    "    k2.knummer = " + karte.getNummer() + ";";
          else
            sql =
                    "SELECT COUNT(*) " +
                    "  FROM karten k1, faecher f1, faecher f2, karten k2 " +
                    "  WHERE k1.fnummer = f1.fnummer AND " +
                    "    LOWER(k1.kworteins) = '" + karte.getWortEins().toLowerCase() + "' AND " +
                    "    LOWER(k1.kwortzwei) = '" + karte.getWortZwei().toLowerCase() + "' AND " +
                    "    k1.knummer <> " + karte.getNummer() + " AND " +
                    "    f1.lnummer = f2.lnummer AND " +
                    "    f2.fnummer = k2.knummer AND " +
                    "    k2.knummer = " + karte.getNummer() + ";";
          c = getReadableDatabase().rawQuery(sql, null);
          c.moveToNext();
          if (c.getInt(0) != 0)
            ret = -4;
          else {
            sql =
                    "UPDATE karten " +
                    "  SET kworteins = '" + karte.getWortEins() + "', " +
                    "    kwortzwei = '" + karte.getWortZwei() + "', " +
                    "    kgrosskleinschreibung = '" + (karte.getGrossKleinschreibung()? 1 : 0) + "' " +
                    "  WHERE knummer = " + karte.getNummer() + ";";
            stmt = getReadableDatabase().compileStatement(sql);
            if (stmt.executeUpdateDelete() == 0)
              ret = -1;
            else {
              ret = 0;
            }
          }
        } catch (SQLException e) {
          ret = -1;
        } finally {
          try {
            c.close();
          } catch (Exception e) {
            ;
          }
          try {
            stmt.close();
          } catch (Exception e) {
            ;
          }
        }
      }
    }
    return ret;
  }

  /**
   * Löscht die Karte mit der übergebenen Nummer aus der Datenbank
   * @param nummerKarte
   * @return -1 falls Datenbankfehler aufgetreten ist oder Karte nicht
   * gefunden wurde
   */
  public int loeschenKarte(int nummerKarte) {
    int ret = -1;
    SQLiteStatement stmt = null;
    try {
      String sql =
              "DELETE FROM karten " +
              "  WHERE knummer = " + nummerKarte + ";";
      stmt = getReadableDatabase().compileStatement(sql);
      if (stmt.executeUpdateDelete() == 0)
        ret = -1;
      else {
        ret = 0;
      }
    } catch (SQLException e) {
      ret = -1;
    } finally {
      try {
        stmt.close();
      } catch (Exception e) {
        ;
      }
    }
    return ret;
  }

  /**
   * Löscht aus übergebenen Lernkartei alle Fächer und somit
   * alle Karten die den Fächern zugeordnet sind
   * @param nummerLernkartei
   * @return -1 falls Datenbankfehler aufgetreten ist oder Lernkartei nicht
   * gefunden wurde
   */
  public int loeschenAlleFaecher(int nummerLernkartei) {
    int ret = -1;
    SQLiteStatement stmt = null;
    try {
      if (!(getLernkartei(nummerLernkartei) == null)) {
        String sql =
                "DELETE FROM faecher " +
                "  WHERE lnummer = " + nummerLernkartei + ";";
        stmt = getReadableDatabase().compileStatement(sql);
        stmt.executeUpdateDelete();
        ret = 0;
      }
    } catch (SQLException e) {
      ret = -1;
    } finally {
      try {
        stmt.close();
      } catch (Exception e) {
        ;
      }
    }
    return ret;
  }

  /**
   * Liefert eine Liste aller Lernkarteien in denen Fächer existieren deren
   * Erinnerung abgelaufen ist. Eine Erinnerung läuft ab, wenn die Differenz zwischen
   * dem heutigen Datum und dem Datum an dem zuletzt gelernt wurde größer oder gleich der
   * Erinnerung ist. Auch werden alle Lernkarteien zurück geliefert
   * die Fächer enthalten in denen noch nie gelernt wurde. Ist das Erinnerungsintervall 0 so
   * werden die Fächer nicht berücksichtigt
   * @return null falls ein Datenbankfehler aufgetreten ist<br>
   * eine leere ArrayList falls keine Lernkarteien zu finden sind
   */
  public List<Lernkartei> getLernkarteienErinnerung() {
    ArrayList<Lernkartei> ret = new ArrayList();
    Cursor c = null;
    try {
      // HINWEIS: strftime('%s','now') holt sich die Sekunden die seit dem 1.1.70 vergangen sind
      String sql =
              "SELECT l.lnummer, l.lbeschreibung, " +
              "  l.lworteinsbeschreibung, l.lwortzweibeschreibung, " +
              "  l.lrichtung, l.lgrosskleinschreibung " +
              "  FROM lernkarteien l, faecher f " +
              "  WHERE l.lnummer = f.lnummer AND " +
              "    (f.fgelerntam IS NULL OR " +
              "    ferinnerung <> 0 AND " +
              "    (strftime('%s','now') - fgelerntam / 1000) / (24 * 60 * 60) >= ferinnerung) " +
              "  ORDER BY l.lnummer;";
      c = getReadableDatabase().rawQuery(sql, null);
      while (c.moveToNext()) {
        int nummer = c.getInt(0);
        String beschreibung = c.getString(1);
        String wortEinsBeschreibung = c.getString(2);
        String wortZweiBeschreibung = c.getString(3);
        boolean richtung = c.getInt(4) == 1 ? true : false;
        boolean grossKleinschreibung = c.getInt(5) == 1 ? true : false;
        Lernkartei l = new Lernkartei(nummer, beschreibung, wortEinsBeschreibung,
                wortZweiBeschreibung, richtung, grossKleinschreibung);
        ret.add(l);
      }
    } catch (SQLException e) {
      ret = null;
    } finally {
      try {
        c.close();
      } catch (Exception e) {
        ;
      }
    }
    return ret;
  }

  /**
   * Liefert zur übergebenen Lernkartei die Fächer bei denen die Erinnerung abgelaufen
   * ist
   * @return null falls Lernkartei nicht existiert oder Datenbankfehler aufgetreten
   * ist<br>
   * eine leere ArrayList wenn keine Fächer zur Lernkartei existieren bei denen die
   * Erinnerung abgelaufen ist
   */
  public List<Fach> getFaecherErinnerung(int nummerLernkartei) {
    ArrayList<Fach> ret = null;
    if (getLernkartei(nummerLernkartei) != null) {
      ret = new ArrayList();
      Cursor c = null;
      try {
        String sql =
                "SELECT f.fnummer, f.fbeschreibung, f.ferinnerung, " +
                "  f.fgelerntam, f.lnummer " +
                "  FROM faecher f " +
                "  WHERE f.lnummer = " + nummerLernkartei + " AND " +
                "    (f.fgelerntam IS NULL OR " +
                "    f.ferinnerung <> 0 AND " +
                "    (strftime('%s','now') - fgelerntam / 1000) / (24 * 60 * 60) >= ferinnerung) " +
                "  ORDER BY f.fnummer;";
        c = getReadableDatabase().rawQuery(sql, null);
        while (c.moveToNext()) {
          int nummer = c.getInt(0);
          String beschreibung = c.getString(1);
          int erinnerung = c.getInt(2);
          Date gelerntAm = new Date(c.getLong(3));
          Fach f = new Fach(nummer, beschreibung, erinnerung, gelerntAm);
          ret.add(f);
        }
      } catch (SQLException e) {
        ret = null;
      } finally {
        try {
          c.close();
        } catch (Exception e) {
          ;
        }
      }
    }
    return ret;
  }

    /**
     * Holt die Karten des übergebenen Faches
     * @param nummerFach
     * @return null falls Fach nicht vorhanden oder Datenbankfehler aufgetreten ist
     * oder eine leere ArrayList wenn das Fach keine Karten hat
     */
    public ArrayList<Karte> getKarten(int nummerFach) {
      ArrayList<Karte> ret = null;
      if (getFach(nummerFach) != null) {
        Cursor c = null;
        try {
          String sql =
                  "SELECT k.knummer, kworteins, kwortzwei, l.lrichtung, k.kgrosskleinschreibung " +
                  "  FROM karten k, faecher f, lernkarteien l " +
                  "  WHERE k.fnummer = f.fnummer AND " +
                  "    f.lnummer = l.lnummer AND " +
                  "    k.fnummer = " + nummerFach + ";";
          c = getReadableDatabase().rawQuery(sql, null);
          ret = new ArrayList();
          while (c.moveToNext()) {
            int nummer = c.getInt(0);
            String wortEins = c.getString(1);
            String wortZwei = c.getString(2);
            boolean richtung = c.getInt(3) == 1 ? true : false;
            boolean grossKleinschreibung = c.getInt(4) == 1 ? true : false;
            Karte k = new Karte(nummer, wortEins, wortZwei, richtung, grossKleinschreibung);
            ret.add(k);
          }
        } catch (SQLException e) {
          ret = null;
        } finally {
          try {
            c.close();
          } catch (SQLException e) {
            ;
          }
        }
      }
      return ret;
    }

    /**
     * Wenn eine Lernkartei als Standard festgelegt wurde, dann wird
     * diese zurück geliefert
     * @return Lernkartei die als Standardlernkartei gesetzt wurde<br>
     * null falls Datenbankfehler aufgetreten ist oder noch keine
     * Lernkartei als Standard gesetzt wurde
     */
    public Lernkartei getEinstellungenStandardLernkartei() {
      Lernkartei ret = null;
      Cursor c = null;
      try {
        String sql =
                "SELECT l.lnummer, l.lbeschreibung, l.lworteinsbeschreibung, l.lwortzweibeschreibung, " +
                "    l.lrichtung, l.lgrosskleinschreibung " +
                "  FROM einstellungen e, lernkarteien l " +
                "  WHERE e.elnummerstandard = l.lnummer;";
        c = getReadableDatabase().rawQuery(sql, null);
        if (c.moveToNext()) {
          int nummer = c.getInt(0);
          String beschreibung = c.getString(1);
          String wortEinsBeschreibung = c.getString(2);
          String wortZweiBeschreibung = c.getString(3);
          boolean richtung = c.getInt(4) == 1 ? true : false;
          boolean grossKleinschreibung = c.getInt(5) == 1 ? true : false;
          ret = new Lernkartei(nummer, beschreibung, wortEinsBeschreibung,
                      wortZweiBeschreibung, richtung, grossKleinschreibung);
        }
      } catch (SQLException e) {
        ret = null;
      } finally {
        try { c.close(); } catch (Exception e) { ; }
      }
    return ret;
    }

  /**
   * Setzt die übergebene Lernkartei als Standardlernkartei, welche beim
   * Öffnen verwendet wird. Wird der Methode -1 übergeben, so wird der Eintrag
   * für Standardlernkartei gelöscht
   * @param nummerLernkartei
   * @return -1 falls Lernkartei nicht gefunden wurde oder Datenbankfehler
   * aufgetreten ist
   */
  public int setEinstellungenStandardLernkartei(int nummerLernkartei) {
    int ret = -1;
    SQLiteStatement stmt = null;
    try {
      String sql = null;
      if (nummerLernkartei == -1)
        sql =
                "UPDATE einstellungen " +
                "  SET elnummerstandard = null;";
      else
        sql =
                "UPDATE einstellungen " +
                "  SET elnummerstandard = " + nummerLernkartei + ";";
      stmt = getReadableDatabase().compileStatement(sql);
      if (stmt.executeUpdateDelete() == 1) {
        ret = 0;
      }
    } catch (SQLException e) {
      ret = -1;
    } finally {
      try { stmt.close(); } catch (Exception e) { ; }
    }
    return ret;
  }

  /**
   * Legt fest, dass Benutzer nur in Lernkarteien mit Erinnerung lernen möchte
   * @param lernkarteienMitErinnerung
   * @return -1 falls Datenbankfehler aufgetreten ist
   */
  public int setEinstellungenLernkarteienMitErinnerung(boolean lernkarteienMitErinnerung) {
    int ret = -1;
    SQLiteStatement stmt = null;
    try {
      String sql =
              "UPDATE einstellungen " +
              "  SET elernkarteienmiterinnerung = " + (lernkarteienMitErinnerung ? 1 : 0)+ ";";
      stmt = getReadableDatabase().compileStatement(sql);
      if (stmt.executeUpdateDelete() == 1) {
        ret = 0;
      }
    } catch (SQLException e) {
      ret = -1;
    } finally {
      try { stmt.close(); } catch (Exception e) { ; }
    }
    return ret;
  }

  /**
   * Liefert zurück ob der Benutzer nur in Lernkarteien mit abgelaufener Erinnerung
   * lernen möchte
   * @return false falls noch keine Einstellung getroffen wurde
   */
  public boolean getEinstellungenLernkarteienMitErinnerung() {
    boolean ret = false;
    Cursor c = null;
    try {
      String sql =
              "SELECT elernkarteienmiterinnerung " +
              "  FROM einstellungen;";
      c = getReadableDatabase().rawQuery(sql, null);
      if (c.moveToNext())
        ret = c.getInt(0) == 1 ? true : false;
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try { c.close(); } catch (Exception e) { ; }
    }
    return ret;
  }

  /*
   * Hilfsmethoden zur Datumsmanipulation
   */
  public static Date getActualDate() {
    Calendar calGelerntAm = Calendar.getInstance();
    calGelerntAm.set(Calendar.HOUR, 0);
    calGelerntAm.set(Calendar.MINUTE, 0);
    calGelerntAm.set(Calendar.SECOND, 0);
    calGelerntAm.set(Calendar.MILLISECOND, 0);
    return calGelerntAm.getTime();
  }
  public static Date getDateOneDayBeforeToday() {
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.add(Calendar.DAY_OF_WEEK, -1);
    cal.set(Calendar.MILLISECOND, 0);
    cal.set(Calendar.HOUR, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    return cal.getTime();
  }
  public static String convertToString(Date date) {
    String ret = null;
    if (date == null)
      ret = "NULL";
    else
      ret = new SimpleDateFormat("yyyy-MM-dd").format(date);
    return ret;
  }
}

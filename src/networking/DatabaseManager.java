package networking;

import main.Capteur;
import main.TypeFluide;

import java.sql.*;
import java.util.*;

public class DatabaseManager {
    private static NavigableMap<String, Capteur> list;
    private static String databaseName = "jdbc:mysql://localhost:3306/capteurs_database";
    private static String user = "root";
    private static String pass = "";

    public static void initList(NavigableMap<String, Capteur> list){
        DatabaseManager.list = list;
    }

    public static void loadCapteurs(NavigableSet<Capteur> list){
        try {
            Connection con = DriverManager.getConnection(databaseName, user, pass);
            Statement stmt = con.createStatement();
            try{
                ResultSet rs = stmt.executeQuery("SELECT * FROM `Capteur`");

                while(rs.next()){
                    Capteur toAdd = new Capteur(
                            rs.getString("NomCapteur"),
                            rs.getString("BatimentCapteur"),
                            rs.getInt("EtageCapteur"),
                            rs.getString("LocalisationCapteur"),
                            TypeFluide.valueOf(rs.getString("TypeCapteur")),
                            rs.getFloat("ValeurCapteur"),
                            rs.getFloat("SeuilMinCapteur"),
                            rs.getFloat("SeuilMaxCapteur"),
                            false
                    );
                    list.add(toAdd);
                    DatabaseManager.list.put(toAdd.getNom(), toAdd);
                }
            }finally{
                stmt.close();
                con.close();
            }
        }catch (SQLException ex){
            treatException(ex);
        }
    }

    public static void addCapteur(Capteur capteur){
        try {
            Connection con = DriverManager.getConnection(databaseName, user, pass);
            Statement stmt = con.createStatement();
            try{
                stmt.execute(createQueryAddCapteur(capteur));
            }finally{
                stmt.close();
                con.close();
            }
        }catch (SQLException ex){
            treatException(ex);
        }
    }

    public static void addValeur(float valeur, Capteur capteur){
        try{
            Connection con = DriverManager.getConnection(databaseName, user, pass);
            Statement stmt = con.createStatement();
            try{
                stmt.execute("INSERT INTO `Valeur` (`ValeurPrise`, `CapteurCorr`) VALUE ('"+valeur+"', '"+capteur.getNom()+"');");
                stmt.execute("UPDATE `Capteur` SET `ValeurCapteur` = '"+valeur+"' WHERE `Capteur`.`NomCapteur` = '"+capteur.getNom()+"';");
            }finally{
                stmt.close();
                con.close();
            }
        }catch(SQLException ex){
            treatException(ex);
        }
    }

    public static NavigableSet<Capteur> getCapteurs(String type){
        NavigableSet<Capteur> ret = new TreeSet<>();
        try{
            Connection con = DriverManager.getConnection(databaseName, user, pass);
            Statement stmt = con.createStatement();
            try{
                ResultSet rs = stmt.executeQuery("SELECT `NomCapteur` FROM `Capteur` WHERE `TypeCapteur` = '"+type+"';");

                while (rs.next()){
                    String currentNom = rs.getString("NomCapteur");
                    Capteur currentCapteur = list.get(currentNom);
                    ret.add(currentCapteur);
                }
            }finally{
                stmt.close();
                con.close();
            }
        }catch(SQLException ex){
            treatException(ex);
        }
        return ret;
    }

    public static NavigableMap<String, Float> getValeursCapteur(Capteur capteur, String dateMin, String dateMax){
        NavigableMap<String, Float> ret = new TreeMap<>();
        try{
            Connection con = DriverManager.getConnection(databaseName, user, pass);
            Statement stmt = con.createStatement();
            try{
                String query = "SELECT `ValeurPrise`, `DateValeur` FROM `Valeur` WHERE `CapteurCorr` = '"+capteur.getNom()+"';";
                ResultSet rs = stmt.executeQuery(query);

                while (rs.next()){
                    Float currentValeur = rs.getFloat("ValeurPrise");
                    String currentDate = getDate(rs.getString("DateValeur"));
                    if (currentDate.compareTo(dateMin) >= 0 && currentDate.compareTo(dateMax) <= 0)
                        ret.put(currentDate, currentValeur);
                }
            }finally {
                stmt.close();
                con.close();
            }
        }catch(SQLException ex){
            treatException(ex);
        }
        return ret;
    }

    /**
     * @return - return the list of all the times recorded (without doubles)
     */
    public static List<String> getTimes(List<Capteur> capteurs){
        NavigableSet<String> temp = new TreeSet<>();
        if (capteurs.size() == 0)    return new ArrayList<>();
        try{
            Connection con = DriverManager.getConnection(databaseName, user, pass);
            Statement stmt = con.createStatement();
            try{
                String query = "SELECT `DateValeur` FROM `Valeur` WHERE `CapteurCorr` = '"+capteurs.get(0).getNom()+"'";
                for (int i = 1 ; i < capteurs.size() ; i++){
                    query += " || `CapteurCorr` = '"+capteurs.get(i).getNom()+"'";
                }
                query+=";";
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()){
                    temp.add(getDate(rs.getString("DateValeur")));
                }
            }finally{
                stmt.close();
                con.close();
            }
        }catch (SQLException ex){
            treatException(ex);
        }
        return new ArrayList<>(temp);
    }

    public static void setSeuils(Capteur capteur, float seuilMin, float seuilMax){
        try{
            Connection con = DriverManager.getConnection(databaseName, user, pass);
            Statement stmt = con.createStatement();
            try{
                stmt.execute("UPDATE `Capteur` SET `SeuilMinCapteur` = '"+seuilMin+"', `SeuilMaxCapteur` = '"+seuilMax+"' WHERE `Capteur`.`NomCapteur` = '"+capteur.getNom()+"'");
            }finally{
                stmt.close();
                con.close();
            }
        }catch (SQLException ex){
            treatException(ex);
        }
    }

    private static String getDate(String date){
        String[] datetime = date.split(" ");
        String time = datetime[1];
        int lastSec = Integer.valueOf(time.substring(7, 8));
        if (lastSec > 5)   lastSec = 5;
        else lastSec = 0;
        String ret = time.substring(0, 7) + lastSec;
        return ret;
    }

    private static String createQueryAddCapteur(Capteur capteur){
        String ret = "INSERT INTO `Capteur` (`NomCapteur`, `BatimentCapteur`, `EtageCapteur`, `LocalisationCapteur`, `SeuilMinCapteur`, `SeuilMaxCapteur`, `ValeurCapteur`, `TypeCapteur`) VALUES (";
        ret += "'"+capteur.getNom()+"', ";
        ret += "'"+capteur.getBatiment()+"', ";
        ret += "'"+capteur.getEtage()+"', ";
        ret += "'"+capteur.getLocalisation()+"', ";
        ret += "'"+capteur.getSeuilMin()+"', ";
        ret += "'"+capteur.getSeuilMax()+"', ";
        ret += "NULL, ";
        ret += "'"+capteur.getType()+"');";
        return ret;
    }

    private static void treatException(SQLException ex){
        System.out.println("SQLException: " + ex.getMessage());
        System.out.println("SQLState: " + ex.getSQLState());
        System.out.println("VendorError: " + ex.getErrorCode());
    }
}

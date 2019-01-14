package modeles;

import main.Capteur;
import main.TypeFluide;

import java.util.List;

import javax.swing.table.AbstractTableModel;

public class ModeleTableau extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    private List<Capteur> capteurs;

    public ModeleTableau(List<Capteur> capteur) {
        this.capteurs=capteur;
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public int getRowCount() {
        return capteurs.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Capteur capteur;
        try {
            capteur = capteurs.get(rowIndex);
        }catch(NullPointerException | IndexOutOfBoundsException e){
            return null;
        }
        switch(columnIndex)
        {
            case 0:return capteur.getNom();
            case 1:return capteur.getType();
            case 2:return capteur.getLocalisation();
            case 3:return capteur.getValeurCourante();
            default:return null;
        }
    }

    public Class<?> getColumnClass(int columnIndex){
        switch(columnIndex){
            case 0: return String.class;
            case 1: return TypeFluide.class;
            case 2: return String.class;
            case 3: return float.class;
            default: return null;
        }
    }

    public String getColumnName(int columnIndex) {
        switch(columnIndex)
        {
            case 0: return "Nom capteur";
            case 1: return "Type";
            case 2: return "Localisation";
            case 3: return "Valeur";
            default: return null;
        }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public Capteur getCapteur(int rowIndex) {
        return capteurs.get(rowIndex);
    }

    public void add(Capteur capteur){
        int index = capteurs.size();
        capteurs.add(capteur);
        fireTableRowsInserted(index, index);
    }

    public void remove(Capteur capteur){
        int index = capteurs.indexOf(capteur);
        capteurs.remove(capteur);
        fireTableRowsDeleted(index, index);
    }

    public void update(Capteur capteur){
        int index = capteurs.indexOf(capteur);
        fireTableRowsUpdated(index, index);
    }

}
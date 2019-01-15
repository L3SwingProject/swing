package modeles;

import main.Capteur;
import main.TypeFluide;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class ModeleTableau extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    private List<Capteur> list;
    private List<Capteur> capteurs;

    public ModeleTableau(List<Capteur> capteur) {
        this.list=capteur;
        this.capteurs = new ArrayList<>(list);
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
        list.add(capteur);
        fireTableRowsInserted(index, index);
    }

    public void remove(Capteur capteur){
        int index = capteurs.indexOf(capteur);
        capteurs.remove(capteur);
        list.remove(capteur);
        fireTableRowsDeleted(index, index);
    }

    public void update(Capteur capteur){
        int index = capteurs.indexOf(capteur);
        if (index != -1) {
            Capteur toSet = capteurs.get(index);
            toSet.update(capteur.getValeurCourante());
            toSet.setSeuilMin(capteur.getSeuilMin());
            toSet.setSeuilMax(capteur.getSeuilMax());
            fireTableRowsUpdated(index, index);
        }
    }

    public void filtrer(TypeFluide type, String batiment){
        List<Capteur> newList = new ArrayList<>();
        int indexDeleted = capteurs.size()-1;
        if (type == null)   newList.addAll(list);
        else{
            for (Capteur capteur : list){
                if (type.equals(capteur.getType())){
                    newList.add(capteur);
                }
            }
        }
        List<Capteur> finalList = new ArrayList<>();
        if (batiment == null)   finalList.addAll(newList);
        else{
            for (Capteur capteur : newList){
                if (batiment.equals(capteur.getBatiment())){
                    finalList.add(capteur);
                }
            }
        }
        capteurs = finalList;
        int indexAdded = capteurs.size()-1;
        fireTableRowsDeleted(0, indexDeleted);
        fireTableRowsInserted(0, indexAdded);
    }

}
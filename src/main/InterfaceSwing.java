package main;

import modeles.ModeleArbre;
import modeles.ModeleTableau;
import networking.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InterfaceSwing {
    private static NavigableSet<Capteur> list = new TreeSet<>();
    private static NavigableMap<String, Capteur> keyList = new TreeMap<>();
    private static int port = 0;
    private static volatile Boolean portEntered = false;

    /* Composants graphiques */
    private static JFrame fenetre = new JFrame();
    private static JSplitPane layout = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

    private static JSplitPane tableTree = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

    /* Partie tableau temps reel */
    private static JSplitPane splitTableau;
    private static JPanel panelFiltres;
    private static JScrollPane panelTableau;
    private static JTable table;
    private static ModeleTableau tableModel;
    private static JComboBox<String> filtreType;
    private static JComboBox<String> filtreBatiment;
    private static JButton submitFiltres = new JButton("Appliquer");
    private static JButton resetFiltres = new JButton("Reinitialiser");
    private static TypeFluide typeChoisi = null;
    private static String batimentChoisi = null;

    /* Partie arbre gestion */
    private static JSplitPane treePane;
    private static JTree jModTree;
    private static JPanel jPanelInfo = new JPanel();
    private static JLabel jLNom = new JLabel();
    private static JLabel jLLoc = new JLabel();
    private static JLabel jLType = new JLabel();
    private static JLabel jLSeuilMax = new JLabel();
    private static JLabel jLSeuilMin = new JLabel();
    private static JLabel jLEspace = new JLabel();
    private static JLabel jLModif = new JLabel();
    private static JLabel labelMax = new JLabel("Seuil Max :", JLabel.LEFT);
    private static JLabel labelMin = new JLabel("Seuil Min :", JLabel.LEFT);
    private static JSpinner spinMax = new JSpinner();
    private static JSpinner spinMin = new JSpinner();
    private static JButton submitSeuils = new JButton("Modifier");
    private static JButton resetSeuils = new JButton("Reinitialiser");
    private static Capteur selected;

    /* Partie courbes */
    private static JSplitPane courbesGestionPanel;
    private static ChartPanel courbesPanel;
    private static JFreeChart courbes;
    private static JPanel gestion = new JPanel();
    private static JComboBox<String> typeCourbes = new JComboBox<>();
    private static JComboBox<Capteur> capteursCourbe1 = new JComboBox<>();
    private static JComboBox<Capteur> capteursCourbe2 = new JComboBox<>();
    private static JComboBox<Capteur> capteursCourbe3 = new JComboBox<>();
    private static JSpinner dateDebutCourbes;
    private static JSpinner dateFinCourbes;
    private static JLabel[] labels = new JLabel[6];
    private static JButton submitCourbes = new JButton("Afficher");
    private static NavigableSet<Capteur> capteursTyped;

    /**
     * main function : build, print graphic componant and animate them communicating with somulators/databse.
     * @param args - none
     */
    public static void main(String[] args){
        buildInterface();
        new Thread(() -> {
            fenetre.setVisible(true);
            while (!portEntered){
                String portStr = JOptionPane.showInputDialog(fenetre, "Port serveur :", "PORT", port);
                try{
                    port = Integer.valueOf(portStr);
                }catch(NumberFormatException e){
                    port = 0;
                }
                portEntered = (port > 0);
            }
        }).start();
        while(!portEntered);
        Serveur.listenSimul(list, keyList, port);
    }

    public static void setModeleArbre(){
        jModTree.setModel(new ModeleArbre(list));
        for (int i=0;i<jModTree.getRowCount();i++)
            jModTree.expandRow(i);
    }

    private static void buildInterface(){
        DatabaseManager.initList(keyList);
        DatabaseManager.loadCapteurs(list);

        buildTreePanel();
        buildTablePanel();
        buildCourbesGestionPanel();

        tableTree.setLeftComponent(treePane);
        tableTree.setRightComponent(splitTableau);
        tableTree.setDividerSize(5);
        tableTree.setDividerLocation(475);
        layout.setTopComponent(tableTree);
        layout.setBottomComponent(courbesGestionPanel);
        layout.setDividerSize(5);
        layout.setDividerLocation(400);
        fenetre.add(layout);
        fenetre.setSize(2500,4000);

        fenetre.addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        portEntered = true; //to be safe
                        Serveur.exit();
                    }
                }
        );
    }

    private static void buildCourbesGestionPanel(){
        /* Components initialization */
        /* Form part */
        createForm();

        /* Chart part */
        courbesPanel = new ChartPanel(courbes);

        courbesGestionPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        courbesGestionPanel.setLeftComponent(gestion);
        courbesGestionPanel.setRightComponent(courbesPanel);
        //fenetre.add(courbesGestionPanel);
        courbesGestionPanel.setDividerSize(0);

        /* actions */
        typeCourbes.addActionListener( al -> initCapteurBoxContent(typeCourbes.getSelectedIndex() != 0) );
        capteursCourbe1.addActionListener(
                al -> {
                    if (capteursCourbe1.getItemCount() > 0) updateDates();
                    if (capteursCourbe1.getSelectedIndex() != 0 && capteursCourbe1.getItemCount() > 0){
                        capteursCourbe2.setEnabled(true);
                        submitCourbes.setEnabled(true);
                    }else{
                        capteursCourbe2.setEnabled(false);
                        submitCourbes.setEnabled(false);
                    }
                }
        );
        capteursCourbe2.addActionListener(
                al -> {
                    if (capteursCourbe2.getItemCount() > 0) updateDates();
                    if (capteursCourbe2.getSelectedIndex() != 0 && capteursCourbe2.getItemCount() > 0){
                        capteursCourbe3.setEnabled(true);
                    }else{
                        capteursCourbe3.setEnabled(false);
                    }
                }
        );
        capteursCourbe3.addActionListener(al -> {
            if (capteursCourbe3.getItemCount() > 0) updateDates();
        });
        dateDebutCourbes.addChangeListener(
                al -> {
                    if (dateDebutCourbes.getValue().equals(dateFinCourbes.getValue())){
                        dateDebutCourbes.setValue(dateDebutCourbes.getPreviousValue());
                    }
                }
        );
        dateFinCourbes.addChangeListener(
                al -> {
                    if (dateFinCourbes.getValue().equals(dateDebutCourbes.getValue())){
                        dateFinCourbes.setValue(dateFinCourbes.getNextValue());
                    }
                }
        );
        submitCourbes.addActionListener(
                al -> {
                    courbes = createChart(createCategoryDataset());
                    courbesPanel = new ChartPanel(courbes);
                    courbesGestionPanel.setRightComponent(courbesPanel);
                }
        );
    }

    private static void createForm(){
        typeCourbes.addItem("Selectionner");
        for (TypeFluide type : TypeFluide.values()){
            typeCourbes.addItem(type.toString());
        }

        //init components
        //JComboBoxes
        capteursCourbe1.addItem(new Capteur("Selectionner", null, 0, null, null, 0, 0, 0, false));
        capteursCourbe2.addItem(new Capteur("Selectionner", null, 0, null, null, 0, 0, 0, false));
        capteursCourbe3.addItem(new Capteur("Selectionner", null, 0, null, null, 0, 0, 0, false));


        capteursCourbe1.setEnabled(false);
        capteursCourbe2.setEnabled(false);
        capteursCourbe3.setEnabled(false);
        submitCourbes.setEnabled(false);

        //JSpinners
        dateDebutCourbes = new JSpinner();
        dateFinCourbes = new JSpinner();
        dateDebutCourbes.setEnabled(false);
        dateFinCourbes.setEnabled(false);

        dateFinCourbes.setValue(dateDebutCourbes.getNextValue());

        //labels
        labels[0] = new JLabel("Type : ", JLabel.LEFT);
        labels[1] = new JLabel("Capteur 1 : ", JLabel.LEFT);
        labels[2] = new JLabel("Capteur 2 : ", JLabel.LEFT);
        labels[3] = new JLabel("Capteur 3 : ", JLabel.LEFT);
        labels[4] = new JLabel("Debut : ", JLabel.LEFT);
        labels[5] = new JLabel("Fin : ", JLabel.LEFT);

        /*add components to panel*/
        GroupLayout layout = new GroupLayout(gestion);
        gestion.setLayout(layout);
        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();

        hGroup.addGroup(layout.createParallelGroup()
                .addComponent(labels[0])
                .addComponent(labels[1])
                .addComponent(labels[2])
                .addComponent(labels[3])
                .addComponent(labels[4])
                .addComponent(labels[5])
        );

        hGroup.addGroup(layout.createParallelGroup()
                .addComponent(typeCourbes)
                .addComponent(capteursCourbe1)
                .addComponent(capteursCourbe2)
                .addComponent(capteursCourbe3)
                .addComponent(dateDebutCourbes)
                .addComponent(dateFinCourbes)
                .addComponent(submitCourbes)
        );

        layout.setHorizontalGroup(hGroup);

        GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(labels[0])
                .addComponent(typeCourbes)
        );

        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(labels[1])
                .addComponent(capteursCourbe1)
        );

        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(labels[2])
                .addComponent(capteursCourbe2)
        );

        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(labels[3])
                .addComponent(capteursCourbe3)
        );

        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(labels[4])
                .addComponent(dateDebutCourbes)
        );

        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(labels[5])
                .addComponent(dateFinCourbes)
        );

        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(submitCourbes)
        );

        layout.setVerticalGroup(vGroup);
    }

    private static void buildTreePanel(){
        jModTree = new JTree(new ModeleArbre(list));
        //virer la root
        jModTree.setRootVisible(false);

        // ouvrir les branches
        for (int i=0;i<jModTree.getRowCount();i++)
            jModTree.expandRow(i);

        //virer les icone
        DefaultTreeCellRenderer iconeTree = new  DefaultTreeCellRenderer();
        iconeTree.setClosedIcon(null);
        iconeTree.setOpenIcon(null);
        iconeTree.setLeafIcon(null);
        jModTree.setCellRenderer(iconeTree);

        //afficher infos + modification seuils
        //initialisation
        jLNom.setHorizontalAlignment(JLabel.LEFT);
        jLLoc.setHorizontalAlignment(JLabel.LEFT);
        jLModif.setHorizontalAlignment(JLabel.LEFT);
        jLSeuilMin.setHorizontalAlignment(JLabel.LEFT);
        jLSeuilMax.setHorizontalAlignment(JLabel.LEFT);
        labelMax.setVisible(false);
        labelMin.setVisible(false);
        spinMin.setVisible(false);
        spinMax.setVisible(false);
        submitSeuils.setVisible(false);
        resetSeuils.setVisible(false);

        //ajouter au layout
        GroupLayout layout = new GroupLayout(jPanelInfo);
        jPanelInfo.setLayout(layout);
        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();

        hGroup.addGroup(layout.createParallelGroup()
                .addComponent(jLNom)
                .addComponent(jLLoc)
                .addComponent(jLType)
                .addComponent(jLSeuilMax)
                .addComponent(jLSeuilMin)
                .addComponent(jLEspace)
                .addComponent(jLModif)
                .addComponent(labelMin)
                .addComponent(labelMax)
                .addComponent(submitSeuils)
                .addComponent(resetSeuils)
        );

        hGroup.addGroup(layout.createParallelGroup()
                .addComponent(spinMin)
                .addComponent(spinMax)
        );

        layout.setHorizontalGroup(hGroup);

        GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(jLNom)
        );

        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(jLLoc)
        );

        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(jLType)
        );

        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(jLSeuilMin)
        );

        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(jLSeuilMax)
        );

        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(jLEspace)
        );

        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(jLModif)
        );

        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(labelMin)
                .addComponent(spinMin)
        );

        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(labelMax)
                .addComponent(spinMax)
        );

        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(submitSeuils)
        );

        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(resetSeuils)
        );

        layout.setVerticalGroup(vGroup);

        //info qd on clique
        jModTree.addTreeSelectionListener(event -> {
            Object noeud = jModTree.getLastSelectedPathComponent();
            if( noeud != null && noeud.getClass().equals(Capteur.class)){
                Float mins = -10000f;
                Float maxs = 10000f;
                Float pas = 0.5f;
                Capteur temp = (Capteur) noeud;
                selected = keyList.get(temp.getNom());
                jLNom.setText("Nom : " + selected.getNom());
                jLLoc.setText("Localisation : "+ selected.getLieu());
                jLType.setText("Type : "+ selected.getType());
                jLSeuilMin.setText("Seuil minimum : "+ selected.getSeuilMin());
                jLSeuilMax.setText("Seuil maximum : "+ selected.getSeuilMax());
                jLEspace.setText(" ");
                jLModif.setText("Modification des seuils :");
                labelMin.setVisible(true);
                labelMax.setVisible(true);
                spinMin.setVisible(true);
                spinMax.setVisible(true);
                submitSeuils.setVisible(true);
                resetSeuils.setVisible(true);
                spinMin.setModel(new SpinnerNumberModel((Float)selected.getSeuilMin(), mins, maxs, pas));
                spinMax.setModel(new SpinnerNumberModel((Float)selected.getSeuilMax(), mins, maxs, pas));
            }else{
                jLNom.setText("");
                jLLoc.setText("");
                jLType.setText("");
                jLSeuilMin.setText("");
                jLSeuilMax.setText("");
                jLEspace.setText("");
                jLModif.setText("");
                labelMin.setVisible(false);
                labelMax.setVisible(false);
                spinMin.setVisible(false);
                spinMax.setVisible(false);
                submitSeuils.setVisible(false);
                resetSeuils.setVisible(false);
            }
        });

        spinMin.addChangeListener(
                al -> {
                    Float valueMin = (Float)spinMin.getValue();
                    Float valueMax = (Float)spinMax.getValue();
                    if (valueMin.compareTo(valueMax) >= 0){
                        spinMin.setValue(spinMin.getPreviousValue());
                    }
                }
        );

        spinMax.addChangeListener(
                al -> {
                    Float valueMin = (Float)spinMin.getValue();
                    Float valueMax = (Float)spinMax.getValue();
                    if (valueMax.compareTo(valueMin) <= 0){
                        spinMax.setValue(spinMax.getNextValue());
                    }
                }
        );

        resetSeuils.addActionListener(
                al -> {
                    selected.initSeuil();
                    DatabaseManager.setSeuils(selected, selected.getSeuilMin(), selected.getSeuilMax());
                    jLSeuilMin.setText("Seuil minimum : "+ selected.getSeuilMin());
                    jLSeuilMax.setText("Seuil maximum : "+ selected.getSeuilMax());
                    capteurUpdate(selected);
                }
        );

        submitSeuils.addActionListener(
                al -> {
                    Float valueMin = (Float)spinMin.getValue();
                    Float valueMax = (Float)spinMax.getValue();
                    selected.setSeuilMin(valueMin);
                    selected.setSeuilMax(valueMax);
                    DatabaseManager.setSeuils(selected, valueMin, valueMax);
                    jLSeuilMin.setText("Seuil minimum : "+ selected.getSeuilMin());
                    jLSeuilMax.setText("Seuil maximum : "+ selected.getSeuilMax());
                    capteurUpdate(selected);
                }
        );

        treePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(jModTree), jPanelInfo);
        treePane.setDividerLocation(200);
        treePane.setDividerSize(5);



        //ajout des listener sur les spinners/boutons
    }

    private static void buildTablePanel(){
        //Set comboboxes
        filtreType = new JComboBox<>();
        filtreBatiment = new JComboBox<>();
        filtreType.addItem("Type");
        for (TypeFluide type : TypeFluide.values()){
            filtreType.addItem(type.toString());
        }
        resetLieuxFiltres();


        filtreType.setSize(filtreType.getWidth(), submitCourbes.getHeight());
        filtreBatiment.setSize(filtreBatiment.getWidth(), submitCourbes.getHeight());
        submitFiltres.setPreferredSize(filtreType.getSize());
        resetFiltres.setPreferredSize(filtreType.getSize());

        //add to layout
        panelFiltres = new JPanel();
        JPanel centerBox = new JPanel();
        GroupLayout layoutFiltres = new GroupLayout(centerBox);
        centerBox.setLayout(layoutFiltres);

        GroupLayout.SequentialGroup hGroup = layoutFiltres.createSequentialGroup();
        hGroup.addGroup(layoutFiltres.createParallelGroup()
                .addComponent(filtreType)
                .addComponent(submitFiltres)
        );
        hGroup.addGroup(layoutFiltres.createParallelGroup()
                .addComponent(filtreBatiment)
                .addComponent(resetFiltres)
        );

        layoutFiltres.setHorizontalGroup(hGroup);

        GroupLayout.SequentialGroup vGroup = layoutFiltres.createSequentialGroup();
        vGroup.addGroup(layoutFiltres.createParallelGroup()
                .addComponent(filtreType)
                .addComponent(filtreBatiment)
        );

        vGroup.addGroup(layoutFiltres.createParallelGroup()
                .addComponent(submitFiltres)
                .addComponent(resetFiltres)
        );

        layoutFiltres.setVerticalGroup(vGroup);


        //Set components position
        panelFiltres.setLayout(new BoxLayout(panelFiltres, BoxLayout.X_AXIS));
        panelFiltres.add(Box.createHorizontalGlue());
        panelFiltres.add(centerBox);
        panelFiltres.add(Box.createHorizontalGlue());

        //set table
        tableModel = new ModeleTableau(new ArrayList<>());
        table = new JTable(tableModel);
        panelTableau = new JScrollPane(table);
        TableColumnModel modeleColonne = table.getColumnModel();
        for(int i=0;i<4;i++) {
            modeleColonne.getColumn(i).setCellRenderer(new CellRenderer());
        }
        JTableHeader header = new JTableHeader(modeleColonne);
        header.setReorderingAllowed(false);
        table.setTableHeader(header);

        //set splitpane
        splitTableau = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitTableau.setTopComponent(panelFiltres);
        splitTableau.setBottomComponent(panelTableau);
        splitTableau.setDividerLocation(submitCourbes.getHeight()*2);
        splitTableau.setDividerSize(0);
        splitTableau.setDividerLocation(75);

        //set actions
        filtreType.addActionListener(
                l -> {
                    if (filtreType.getSelectedIndex() == 0){
                        typeChoisi = null;
                    }else{
                        typeChoisi = TypeFluide.valueOf(filtreType.getSelectedItem().toString());
                    }
                    resetLieuxFiltres();
                }
        );

        filtreBatiment.addActionListener(
                l -> {
                    if (filtreBatiment.getItemCount() == 0 || filtreBatiment.getSelectedIndex() == 0){
                        batimentChoisi = null;
                    }else{
                        batimentChoisi = filtreBatiment.getSelectedItem().toString();
                    }
                }
        );

        submitFiltres.addActionListener(
                l -> tableModel.filtrer(typeChoisi, batimentChoisi)
        );

        resetFiltres.addActionListener(
                l -> tableModel.filtrer(null, null)
        );
    }

    public static void addCapteur(Capteur capteur){
        if (list.contains(capteur)){
            keyList.get(capteur.getNom()).connect();
        }else {
            addCapteurMemory(capteur);
            DatabaseManager.addCapteur(capteur);
        }
        tableModel.add(capteur);
    }

    public static void addCapteurMemory(Capteur capteur){
        list.add(capteur);
        keyList.put(capteur.getNom(), capteur);
        setModeleArbre();
    }

    public static void removeCapteurFromTable(Capteur capteur){
        tableModel.remove(capteur);
    }

    public static void capteurUpdate(Capteur capteur){
        tableModel.update(capteur);
    }

    public static void resetLieuxFiltres(){
        if (filtreBatiment.getItemCount() != 0) {
            filtreBatiment.removeAllItems();
        }
        filtreBatiment.addItem("Localisation");
        NavigableSet<String> lieux = new TreeSet<>();
        for (Capteur capteur : list){
            if (typeChoisi == null) {
                if (capteur.estConnect()) {
                    lieux.add(capteur.getBatiment());
                }
            }else{
                if (capteur.estConnect() && typeChoisi == capteur.getType()){
                    lieux.add(capteur.getBatiment());
                }
            }
        }
        for (String s : lieux){
            filtreBatiment.addItem(s);
        }
    }

    /**
     * Precondition : at least one captor chosen.
     */
    private static CategoryDataset createCategoryDataset(){
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Capteur capteurChoisi2 = (Capteur)capteursCourbe2.getSelectedItem();
        Capteur capteurChoisi3 = (Capteur)capteursCourbe3.getSelectedItem();
        updateDataset((Capteur)capteursCourbe1.getSelectedItem(), dataset);
        if (!capteurChoisi2.toString().equals("Selectionner"))   updateDataset(capteurChoisi2, dataset);
        if (!capteurChoisi3.toString().equals("Selectionner"))   updateDataset(capteurChoisi3, dataset);
        return dataset;
    }

    private static void updateDataset(Capteur capteur, DefaultCategoryDataset dataset){
        NavigableMap<String, Float> valeursCapteur;
        Lock l = new ReentrantLock();
        l.lock();
        try{
            valeursCapteur = DatabaseManager.getValeursCapteur(capteur, dateDebutCourbes.getValue().toString(), dateFinCourbes.getValue().toString());
        }finally{
            l.unlock();
        }

        for (Map.Entry<String, Float> entry : valeursCapteur.entrySet()){
            String dateCapteurCourant = entry.getKey();
            dataset.addValue(entry.getValue(), capteur, dateCapteurCourant);
        }
    }

    private static JFreeChart createChart(CategoryDataset d){
        JFreeChart chart = ChartFactory.createLineChart("Evolution des capteurs", "Secondes", "Valeur Capteurs", d, PlotOrientation.VERTICAL, true, true, false);
        return chart;
    }

    private static void initCapteurBoxContent(boolean isTypeDefined){
        capteursCourbe1.removeAllItems();
        capteursCourbe1.addItem(new Capteur("Selectionner", null, 0, null, null, 0, 0, 0, false));
        capteursCourbe2.removeAllItems();
        capteursCourbe2.addItem(new Capteur("Selectionner", null, 0, null, null, 0, 0, 0, false));
        capteursCourbe3.removeAllItems();
        capteursCourbe3.addItem(new Capteur("Selectionner", null, 0, null, null, 0, 0, 0, false));
        capteursCourbe1.setEnabled(false);
        capteursCourbe2.setEnabled(false);
        capteursCourbe3.setEnabled(false);
        dateDebutCourbes.setEnabled(false);
        dateFinCourbes.setEnabled(false);
        submitCourbes.setEnabled(false);
        if (isTypeDefined){
            Lock l = new ReentrantLock();
            l.lock();
            try{
                capteursTyped = DatabaseManager.getCapteurs(Objects.requireNonNull(typeCourbes.getSelectedItem().toString()));
            }finally{
                l.unlock();
            }
            for (Capteur capteur : capteursTyped){
                capteursCourbe1.addItem(capteur);
                capteursCourbe2.addItem(capteur);
                capteursCourbe3.addItem(capteur);
            }
            capteursCourbe1.setEnabled(true);
        }
    }

    private static void updateDates(){
        List<String> times;
        List<Capteur> capteurs = new ArrayList<>();
        if (!capteursCourbe1.getSelectedItem().equals("Selectionner"))  capteurs.add((Capteur)capteursCourbe1.getSelectedItem());
        if (!capteursCourbe2.getSelectedItem().equals("Selectionner"))  capteurs.add((Capteur)capteursCourbe2.getSelectedItem());
        if (!capteursCourbe3.getSelectedItem().equals("Selectionner"))  capteurs.add((Capteur)capteursCourbe3.getSelectedItem());
        Lock l = new ReentrantLock();
        l.lock();
        try{
            times = DatabaseManager.getTimes(capteurs);
        }finally{
            l.unlock();
        }
        if (!times.isEmpty()) {
            dateDebutCourbes.setModel(new SpinnerListModel(times));
            dateFinCourbes.setModel(new SpinnerListModel(times));
            dateFinCourbes.setValue(dateFinCourbes.getNextValue());
            dateDebutCourbes.setEnabled(true);
            dateFinCourbes.setEnabled(true);
        }
    }

    private static class CellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        private final Color rouge = new Color(247, 72, 75);

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Capteur c = ((ModeleTableau)table.getModel()).getCapteur(row);
            setText(String.valueOf(value));
            setHorizontalAlignment(JLabel.LEFT);
            if (c.valeurExtSeuil()) {
                this.setBackground(rouge);
            }
            else {
                this.setBackground(Color.WHITE);
            }
            return this;
        }
    }
}
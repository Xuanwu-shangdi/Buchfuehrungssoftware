/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application.menu.datei;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

import application.Kontenverwaltung;
import application.menu.analyse.EroeffnungsbilanzEinsehenController;
import io.IOManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import konten.Abschlusskonto;
import konten.Bestandskonto;
import konten.Erfolgskonto;
import konten.Konto;
import konten.Steuerkonto;
import utility.alertDialog.AlertDialogFrame;

/**
 * BilanzErstellen dient f�r das Erstellen einer Bilanz und damit einer Datei. Es k�nnen Eingaben �ber Best�nde vom letzten Jahr get�tigt werden. Neue Konten k�nnen hinzugef�gt werden.
 *
 */
public class BilanzErstellenController implements Initializable {

	private static final String STANDARD_PATH = System.getProperty("user.home")
			+ "\\AppData\\Roaming\\BuF�-HWRVersion\\";
	@FXML
	private TextField textfieldBilanzname;
	@FXML
	private TextField textfieldKontenname;
	@FXML
	private TextField textfieldKuerzel;
	@FXML
	private Button buttonAddKonto;
	@FXML
	private Button buttonAB;
	@FXML
	private TextField textfieldAB;
	@FXML
	private RadioButton radioAktivkonto;
	@FXML
	private RadioButton radioPassivkonto;
	@FXML
	private RadioButton radioErtragskonto;
	@FXML
	private RadioButton radioAufwandskonto;
	@FXML
	private DatePicker datepickerGJBeginn;
	@FXML
	private TableView<Konto> tableKonto;
	@FXML
	private TableColumn<Konto, String> columnKonto;
	@FXML
	private TableColumn<Konto, String> columnBeschreibung;
	@FXML
	private RadioButton radioBestandskonto;
	@FXML
	private RadioButton radioErfolgskonto;
	@FXML
	private Button buttonBilanzErstellen;
	@FXML
	private CheckBox checkboxProduzierendesU;
	@FXML
	private ComboBox<String> verrechnungskonto;

	private ArrayList<Konto> kontenListe;

	private Kontenverwaltung neueBilanz;

	private boolean bilanzHinzugefuegt;

	private Bestandskonto bank, kasse, verb, ford, dar, bga, ek;

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		bilanzHinzugefuegt = false;
		kontenListe = new ArrayList<>();
		ToggleGroup group1 = new ToggleGroup();
		ToggleGroup group2 = new ToggleGroup();
		ToggleGroup group3 = new ToggleGroup();

		radioBestandskonto.setToggleGroup(group1);
		radioErfolgskonto.setToggleGroup(group1);
		radioErtragskonto.setToggleGroup(group2);
		radioAufwandskonto.setToggleGroup(group2);
		radioAktivkonto.setToggleGroup(group3);
		radioPassivkonto.setToggleGroup(group3);
		radioBestandskonto.setOnAction(e -> {
			radioAufwandskonto.setDisable(!(radioErfolgskonto.isSelected()));
			radioErtragskonto.setDisable(!(radioErfolgskonto.isSelected()));
			radioAktivkonto.setDisable(!(radioBestandskonto.isSelected()));
			radioPassivkonto.setDisable(!(radioBestandskonto.isSelected()));
			textfieldAB.setDisable(!(radioBestandskonto.isSelected()));
			verrechnungskonto.setDisable(!(radioErfolgskonto.isSelected()));
		});
		radioErfolgskonto.setOnAction(e -> {
			radioAufwandskonto.setDisable(!(radioErfolgskonto.isSelected()));
			radioErtragskonto.setDisable(!(radioErfolgskonto.isSelected()));
			radioAktivkonto.setDisable(!(radioBestandskonto.isSelected()));
			radioPassivkonto.setDisable(!(radioBestandskonto.isSelected()));
			textfieldAB.setDisable(radioErfolgskonto.isSelected());
			verrechnungskonto.setDisable(!(radioErfolgskonto.isSelected()));
		});

		ContextMenu contextMenu = new ContextMenu();
		MenuItem item1 = new MenuItem("Bearbeiten");
		MenuItem item2 = new MenuItem("L�schen");
		item1.setOnAction(ev -> {
			bearbeiteKonto(tableKonto.getSelectionModel().getSelectedItem());
		});
		item2.setOnAction(ev -> {
			loescheKonto(tableKonto.getSelectionModel().getSelectedItems());
		});
		contextMenu.getItems().addAll(item1, item2);
		tableKonto.setContextMenu(contextMenu);

		LocalDate ld = LocalDate.of(LocalDate.now().getYear(), 1, 1);
		datepickerGJBeginn.setValue(ld);

		standardkontenHinzufuegen();
	}

	/**
	 * <i><b>Ereignisbehandlung: Hinzuf�gen eines Kontos</b></i><br>
	 * <br>
	 * Es wird auf fehlerhafte Nutzereingaben gepr�ft. Ein Konto wird nach Angaben des Nutzer erstellt.<br>
	 * 
	 * @param event
	 * 			- Nutzeraktion
	 */
	@FXML
	private void handle_KontoHinzufuegen(ActionEvent event) {
		String fehlermeldung = "";
		if (textfieldKuerzel.getText().length() > 6 || textfieldKuerzel.getText().length() == 0) {
			fehlermeldung += "- Das K�rzel ist bez�glich seiner L�nge ung�ltig\n";
		}
		if (textfieldKontenname.getText().length() == 0) {
			fehlermeldung += "- Keinen Kontonamen angegeben\n";
		}
		if (radioBestandskonto.isSelected()
				&& (textfieldAB.getText().length() == 0 || !isStringANumber(textfieldAB.getText()))) {
			fehlermeldung += "- Bitte geben Sie einen g�ltigen Anfangsbestand f�r das Konto an\n";
		}
		if (radioErfolgskonto.isSelected() && verrechnungskonto.getSelectionModel().isEmpty()) {
			fehlermeldung += "- Bitte geben Sie ein Verrechnungskonto f�r das Konto an\n";
		}
		// Fehler�berpr�fung abgeschlossen
		if (fehlermeldung.equals("")) {
			if (radioBestandskonto.isSelected()) {
				Bestandskonto newBKonto = new Bestandskonto(textfieldKontenname.getText(), textfieldKuerzel.getText(),
						"SBK", Double.parseDouble(textfieldAB.getText()), radioAktivkonto.isSelected());
				kontenListe.add(newBKonto);
			} else if (radioErfolgskonto.isSelected()) {
				Erfolgskonto newEKonto = new Erfolgskonto(textfieldKontenname.getText(), textfieldKuerzel.getText(),
						verrechnungskonto.getValue(), radioErtragskonto.isSelected());
				kontenListe.add(newEKonto);
			}
			tabelleAktualisieren();
			new AlertDialogFrame().showConfirmDialog("\"" + textfieldKontenname.getText() + "\" hinzugef�gt!",
					"Das Konto wurde erfolgreich angelegt.", "Ok", AlertDialogFrame.INFORMATION_TYPE);
			textfieldKontenname.setText("");
			textfieldAB.setText("");
			textfieldKuerzel.setText("");
		} else {
			new AlertDialogFrame().showConfirmDialog(
					"Das Konto \"" + textfieldKontenname.getText()
							+ "\" konnte aus folgenden Gr�nden nicht hinzugef�gt werden:",
					fehlermeldung, "Ok", AlertDialogFrame.WARNING_TYPE);
		}

	}

	/**
	 * <i><b>Ereignisbehandlung: Bilanz erstellen</b></i><br>
	 * <br>
	 * Die neue Bilanz wird nach Angaben des Nutzers erstellt. Es wird gepr�ft, ob der Name der Bilanz bereits vergeben ist. <br>
	 * 
	 * @param event
	 * 			- Nutzeraktion
	 */
	@FXML
	private void handleBilanzErstellen(ActionEvent event) {
		if (checkboxProduzierendesU.isSelected()) {
			Konto fe = new Bestandskonto("Fertige Erzeugnisse", "FE", "BV", 0, true);
			Konto ue = new Bestandskonto("Unfertige Erzeugnisse", "UE", "BV", 0, true);
			Konto bv = new Bestandskonto("Bestandsver�nderungen", "BV", "GuV", 0, true);
			kontenListe.add(fe);
			kontenListe.add(ue);
			kontenListe.add(bv);
		}
		neueBilanz = new Kontenverwaltung(new File(STANDARD_PATH + textfieldBilanzname.getText() + ".bil"), kontenListe,
				datepickerGJBeginn.getValue());
		File standardPath = new File(STANDARD_PATH);
		if (!standardPath.exists()) {
			standardPath.mkdirs();
		}
		boolean ersetzeDatei = true;
		if (textfieldBilanzname.getText().length() == 0) {
			new AlertDialogFrame().showConfirmDialog("Bilanz konnte nicht angelegt werden",
					"Bitte geben sie einen Bilanznamen an, der ebenfalls als Dateinamen dienen wird.", "Ok",
					AlertDialogFrame.ERROR_TYPE);
			return;
		}
		if (neueBilanz.getSpeicherort().exists()) {
			ersetzeDatei = new AlertDialogFrame().showChoiseDialog(
					"Die Datei " + textfieldBilanzname.getText() + ".bil existiert bereits",
					"Soll die Datei mit der neuen Bilanz �berschrieben werden?", "Ok", "Abbrechen",
					AlertDialogFrame.QUESTION_TYPE);
		}
		boolean isBilanzAusgeglichen = eroeffnungsbilanzAnzeigen();
		if (ersetzeDatei && isBilanzAusgeglichen) {
			boolean erfolgreich = IOManager.saveFile(neueBilanz.getKonten(), neueBilanz.getFaelle(),
					neueBilanz.getSpeicherort(), neueBilanz.getGeschaeftsjahrBeginn());
			if (!erfolgreich) {
				new AlertDialogFrame().showConfirmDialog("Bilanz konnte nicht angelegt werden",
						"M�glicherweise ist der angegebene Dateiname (Bilanzname) nicht g�ltig.", "Ok",
						AlertDialogFrame.ERROR_TYPE);
			} else {
				bilanzHinzugefuegt = true;
			}
		} else if (!isBilanzAusgeglichen) {
			new AlertDialogFrame().showConfirmDialog("Warnung: BIlanz ist nicht ausgeglichen!",
					"Bitte bearbeiten Sie die Anfangsbest�nde der Konten so, dass die Bilanz auf der Aktivseite den gleichen Betrag wie auf der Passivseite besitzt.",
					"Ok", AlertDialogFrame.ERROR_TYPE);
		}
		if (bilanzHinzugefuegt) {
			Stage stage = (Stage) buttonBilanzErstellen.getScene().getWindow();
			stage.close();
		}

	}
	/**
	 * <i><b>Anzeigen der Er�ffnungsbilanz</b></i><br>
	 * <br>
	 * Es wird eine FXML-Datei geladen, mit der die Er�ffnungsbilanz eingesehen wird. Die Er�ffnungsbilanz ist Abh�ngig von den Eingaben des Nutzers. <br>
	 * 
	 * @return ob die Er�ffnungsbilanz ausgeglichen ist
	 */
	private boolean eroeffnungsbilanzAnzeigen() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("application/menu/analyse/EroeffnungsbilanzEinsehen.fxml"));
			Scene scene = new Scene(loader.load());
			Stage stage = new Stage();
			EroeffnungsbilanzEinsehenController controller = loader.getController();
			stage.setResizable(false);
			stage.setScene(scene);
			controller.setKonten(kontenListe);
			stage.show();
			return controller.isBilanzAusgeglichen();
		} catch (IOException e) {
			new AlertDialogFrame().showConfirmDialog("Interner Fehler",
					"Men�punkt \"Diagramme berechnen\" konnte nicht durchgef�hrt werden.", "Ok",
					AlertDialogFrame.ERROR_TYPE);
			e.printStackTrace();
		}
		return false;

	}
	/**
	 * <i><b>Ereignisbehandlung: Anfangsbest�nde �ndern</b></i><br>
	 * <br>
	 * Es wird eine FXML-Datei geladen. Nutzereingaben (ge�nderte Anfangsbest�nde) aus dem separatem Fenster werden den Konten �bergeben. <br>
	 * 
	 * @param event
	 * 			- Nutzeraktion
	 */
	@FXML
	private void handleABAendern(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("AB.fxml"));
			Scene scene = new Scene(loader.load());
			ABController controller = loader.getController();
			controller.setTextfieldBankValue(bank.getAnfangsbestand());
			controller.setTextfieldKasseValue(kasse.getAnfangsbestand());
			controller.setTextfieldVerbValue(verb.getAnfangsbestand());
			controller.setTextfieldFordValue(ford.getAnfangsbestand());
			controller.setTextfieldDarValue(dar.getAnfangsbestand());
			controller.setTextfieldBGAValue(bga.getAnfangsbestand());
			controller.setTextfieldEKValue(ek.getAnfangsbestand());
			Stage abStage = new Stage();
			abStage.setScene(scene);
			abStage.showAndWait();
			bank.setAnfangsbestand(controller.getTextfieldBankValue());
			kasse.setAnfangsbestand(controller.getTextfieldKasseValue());
			verb.setAnfangsbestand(controller.getTextfieldVerbValue());
			ford.setAnfangsbestand(controller.getTextfieldFordValue());
			dar.setAnfangsbestand(controller.getTextfieldDarValue());
			bga.setAnfangsbestand(controller.getTextfieldBGAValue());
			ek.setAnfangsbestand(controller.getTextfieldEKValue());
			tabelleAktualisieren();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * <i><b>Hinzuf�gen von Standardkonten</b></i><br>
	 * <br>
	 * Es werden Standardkonten erstellt und ausgegeben. Es gibt die M�glichkeit Konten aus einem produzierendem Unternehmen zu �bernehmen. <br>
	 * 
	 */
	private void standardkontenHinzufuegen() {
		bank = new Bestandskonto("Bank", "Bank", "SBK", 0, true);
		bga = new Bestandskonto("B�ro- und Gesch�ftsausstattung", "BGA", "SBK", 0, true);
		kasse = new Bestandskonto("Kasse", "Kasse", "SBK", 0, true);
		verb = new Bestandskonto("Verbindlichkeiten a.L.L.", "Verb", "SBK", 0, false);
		dar = new Bestandskonto("Darlehen", "Dar", "SBK", 0, false);
		ek = new Bestandskonto("Eigenkapital", "EK", "SBK", 0, false);
		ford = new Bestandskonto("Forderungen a.L.L.", "Ford", "SBK", 0, true);
		Konto ust = new Steuerkonto("Umsatzsteuer", "USt", "SBK");
		Konto vorst = new Steuerkonto("Vorsteuer", "Vorst", "SBK");
		Konto uerl = new Erfolgskonto("Umsatzerl�se", "UErl", "GuV", true);
		Konto privat = new Erfolgskonto("Privat", "Privat", "EK", true);
		Konto efpz = new Erfolgskonto("Entnahme f. priv. Zwecke", "EfpZ", "Privat", true);
		Konto guv = new Abschlusskonto("Gewinn- und Verlustkonto", "GuV", "EK");
		Konto sbk = new Abschlusskonto("Schlussbilanzkonto", "SBK", "");

		kontenListe.add(bga);
		kontenListe.add(bank);
		kontenListe.add(kasse);
		kontenListe.add(ford);
		kontenListe.add(ek);
		kontenListe.add(verb);
		kontenListe.add(dar);
		kontenListe.add(ust);
		kontenListe.add(vorst);
		kontenListe.add(uerl);
		kontenListe.add(efpz);
		kontenListe.add(privat);
		kontenListe.add(guv);
		kontenListe.add(sbk);

		if (checkboxProduzierendesU.isSelected()) {
			Konto fe = new Bestandskonto("Fertige Erzeugnisse", "FE", "BV", 0, true);
			Konto ue = new Bestandskonto("Unfertige Erzeugnisse", "UE", "BV", 0, true);
			Konto bv = new Bestandskonto("Bestandsver�nderungen", "BV", "GuV", 0, true);
			kontenListe.add(fe);
			kontenListe.add(ue);
			kontenListe.add(bv);
		}
		tabelleAktualisieren();
	}

	/**
	 * <i><b>Aktualisierung der Tabelle</b></i><br>
	 * <br>
	 * Die Tabelle wird mit akuellen Werten der Kontenliste aktualisiert. <br>
	 * 
	 */
	private void tabelleAktualisieren() {
		tableKonto.getItems().clear();
		ObservableList<Konto> obsList = FXCollections.observableArrayList(kontenListe);
		tableKonto.setItems(obsList);
		columnKonto.setCellValueFactory(new PropertyValueFactory<Konto, String>("titel"));
		columnBeschreibung.setCellValueFactory(new PropertyValueFactory<Konto, String>("beschreibung"));
		ArrayList<String> kontenKuerzel = new ArrayList<>();
		for (Konto konto : kontenListe) {
			kontenKuerzel.add(konto.getKuerzel());
		}
		verrechnungskonto.setItems(FXCollections.observableArrayList(kontenKuerzel));
	}

	/**
	 * <i><b>L�schen von Konten</b></i><br>
	 * <br>
	 * Es werden ausgew�hlte Konten aus der Kontenliste entfernt. <br>
	 * 
	 * @param selectedKonten
	 * 			- ausgew�hlte Konten, die gel�scht werden sollen
	 */
	private void loescheKonto(ObservableList<Konto> selectedKonten) {

		System.out.println("L�schen....");
		for (int i = 0; i < selectedKonten.size(); i++) {
			if (kontenListe.contains(selectedKonten.get(i)) && selectedKonten.get(i).getKontoart() != 3
					&& selectedKonten.get(i).getKontoart() != 4) {
				kontenListe.remove(selectedKonten.get(i));
			}
		}
		tabelleAktualisieren();
	}
	/**
	 * <i><b>Bearbeitung eines Kontos</b></i><br>
	 * <br>
	 * Es wird eine FXML-Datei geladen. Nutzereingaben aus dem separatem Fenster werden den Konten �bergeben. <br>
	 * 
	 * @param selectedKonto
	 * 			- ausgew�hltes Konto, welches bearbeitet werden soll
	 */
	private void bearbeiteKonto(Konto selectedKonto) {
		if (selectedKonto.getKontoart() != 3 && selectedKonto.getKontoart() != 4) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("KontoBearbeiten.fxml"));
				Scene scene = new Scene(loader.load());
				KontoBearbeitenController controller = loader.getController();
				ArrayList<String> kontenKuerzel = new ArrayList<>();
				for (Konto konto : kontenListe) {
					kontenKuerzel.add(konto.getKuerzel());
				}
				controller.setChangeKonto(selectedKonto, FXCollections.observableArrayList(kontenKuerzel));

				Stage stage = new Stage();
				stage.setScene(scene);
				stage.showAndWait();
				tabelleAktualisieren();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * <i><b>�berpr�fung auf Nummer</b></i><br>
	 * <br>
	 * Es wird �berpr�ft, ob der Text eine Nummer ist. <br>
	 * 
	 * @param text
	 * 			- �bergebener, zu verarbeitender Text
	 * @return ob es eine Nummer ist
	 */
	private boolean isStringANumber(String text) {
		try {
			Double.parseDouble(text);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;

	}

	public Kontenverwaltung getNeueBilanz() {
		return neueBilanz;
	}

	public boolean isNeueBilanzErstellt() {
		return bilanzHinzugefuegt;
	}

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;

import application.menu.analyse.DiagrammErstellen;
import application.menu.analyse.EroeffnungsbilanzEinsehenController;
import application.menu.bearbeiten.KontenverwaltungAnzeigenController;
import application.menu.bearbeiten.NeuerBSController;
import application.menu.bearbeiten.NeuerGFController;
import application.menu.bearbeiten.UebersichtanzeigenController;
import application.menu.datei.BilanzErstellenController;
import geschaeftsfall.Buchungssatz;
import io.DataStorage;
import io.IOManager;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import konten.Bestandskonto;
import konten.Erfolgskonto;
import konten.Konto;
import utility.Collection.Tuple;
import utility.alertDialog.AlertDialogFrame;

/**
 * FXML Controller class
 *
 */
public class GUIController implements Initializable {

	@FXML
	private GridPane t1_A, t1_P;
	@FXML
	private VBox t2, t3, t4Container;
	@FXML
	private Menu menuBearbeiten, menuAnalyse;
	@FXML
	private MenuItem menuitemSpeichern, menuitemJAB, menuitemAddGF, menuitemAddBS;
	@FXML
	private Separator vSeparator;
	@FXML
	private HBox abschlusskontoContainer, chartContainer1, chartContainer2, charContainer2, chartContainer3;

	private GridPane t2_Ertragskonten;
	private GridPane t2_Aufwandskonten;
	private GridPane t3_Steuerkonten;
	private int count_t2_Ertragskonten, count_t2_Aufwandskonten, count_t3_Steuerkonten, count_t1_A, count_t1_P;
	private Kontenverwaltung kontenverwaltung;

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {

		t2_Aufwandskonten = new GridPane();
		t2_Ertragskonten = new GridPane();
		t3_Steuerkonten = new GridPane();
		kontenverwaltung = new Kontenverwaltung();

		t2.getChildren().addAll(t2_Ertragskonten, t2_Aufwandskonten);
		t3.getChildren().add(t3_Steuerkonten);
		GridPane[] gpList = new GridPane[] { t1_A, t1_P, t2_Ertragskonten, t2_Aufwandskonten, t3_Steuerkonten };
		for (int i = 0; i < gpList.length; i++) {
			gpList[i].setVgap(10);
			gpList[i].setHgap(40);
			gpList[i].setPadding(new Insets(10));
		}
		enableMenuBar(false);
		t1_A.heightProperty().addListener(e -> {
			if (t1_A.getHeight() > t1_P.getHeight())
				vSeparator.setPrefHeight(t1_A.getHeight());
		});
		t1_P.heightProperty().addListener(e -> {
			if (t1_P.getHeight() > t1_A.getHeight())
				vSeparator.setPrefHeight(t1_P.getHeight());
		});
	}

	/**
	 * <i><b>Setzen vom Layout</b></i><br>
	 * <br>
	 * Es wird das Layout f�r die Hauptoberfl�che modifiziert. <br>
	 * 
	 */
	private void initalHeadings() {
		count_t2_Aufwandskonten = 4;
		count_t2_Ertragskonten = 4;
		count_t3_Steuerkonten = 5;
		count_t1_A = 2;
		count_t1_P = 2;
		Label labelErtrag = new Label("\tErtragskonten");
		labelErtrag.setFont(Font.font("System", FontWeight.BOLD, 18));
		Label labelAktiva = new Label("\tAktiva");
		labelAktiva.setFont(Font.font("System", FontWeight.BOLD, 18));
		Label labelPassiva = new Label("\tPassiva");
		labelPassiva.setFont(Font.font("System", FontWeight.BOLD, 18));
		Label labelAufwand = new Label("\tAufwandskonten");
		labelAufwand.setFont(Font.font("System", FontWeight.BOLD, 18));
		Label labelSteuerkonto = new Label("\tSteuerkonten");
		labelSteuerkonto.setFont(Font.font("System", FontWeight.BOLD, 18));
		t2_Ertragskonten.add(labelErtrag, 0, 0, 5, 1);
		t2_Aufwandskonten.add(labelAufwand, 0, 0, 5, 1);
		t3_Steuerkonten.add(labelSteuerkonto, 0, 0, 5, 1);
		t1_A.add(labelAktiva, 0, 0, 2, 1);
		t1_P.add(labelPassiva, 0, 0, 2, 1);

	}

	public GridPane getT1_A() {
		return t1_A;
	}

	public GridPane getT1_P() {
		return t1_P;
	}

	/**
	 * <i><b>Ereignisbehandlung: Bilanz erstellen</b></i><br>
	 * <br>
	 * Aus einem Ereignis heraus wird eine FXML-Datei geladen und ein Controller
	 * dieser erstellt. Erstellte Daten in dem Controller werden der
	 * Kontenverwaltung �bergeben. <br>
	 * 
	 * @param event
	 *            - Nutzeraktion
	 */
	@FXML
	private void handle_Datei_NeueBilanzErstellen(ActionEvent event) {
		try {
			System.out.println(getClass().getResource(""));
			FXMLLoader loader = new FXMLLoader(getClass().getResource("menu/datei/BilanzErstellen.fxml"));
			Scene scene = new Scene(loader.load());
			BilanzErstellenController controller = loader.getController();
			Stage bilanzErstellenStage = new Stage();
			bilanzErstellenStage.setScene(scene);
			bilanzErstellenStage.setTitle("Neue Bilanz erstellen");
			bilanzErstellenStage.setResizable(false);
			bilanzErstellenStage.showAndWait();
			if (controller.isNeueBilanzErstellt()) {
				kontenverwaltung = controller.getNeueBilanz();
				ladeKonten(true);
				enableMenuBar(true);
			}
		} catch (IOException e) {
			new AlertDialogFrame().showConfirmDialog("Interner Fehler",
					"Men�punkt \"Neue Bilanz erstellen\" konnte nicht durchgef�hrt werden.", "Ok",
					AlertDialogFrame.ERROR_TYPE);
			e.printStackTrace();
		} catch (NullPointerException e) {
			// Wenn das Fenster geschlossen wird, dann wird eine
			// NullPointerException geworfen. Exception wird ignoriert.
		}

	}

	/**
	 * <i><b>Laden der Konten</b></i><br>
	 * <br>
	 * Es werden alle Konten auf die Hauptoberfl�che geladen. Im Falle eines
	 * Jahresabschlusses werden Diagramme ausgegeben. <br>
	 * 
	 * @param neueBilanz
	 *            - wenn eine neue Bilanz erstellt wurde
	 */
	private void ladeKonten(boolean neueBilanz) {
		GridPane[] gpList = new GridPane[] { t1_A, t1_P, t2_Ertragskonten, t2_Aufwandskonten, t3_Steuerkonten };
		for (int i = 0; i < gpList.length; i++) {
			gpList[i].getChildren().clear();
		}
		abschlusskontoContainer.getChildren().clear();
		initalHeadings();
		Iterator<Konto> it = kontenverwaltung.getKontenIterator();
		while (it.hasNext()) {
			Konto konto = it.next();
			switch (konto.getKontoart()) {
			case (1):
				Bestandskonto bkonto = (Bestandskonto) konto;
				bkonto.confirmAB();
				if (!neueBilanz) {
					bkonto.newContainer();
				}
				if (bkonto.isAktivkonto()) {
					t1_A.add(bkonto.getGUIComponents(), count_t1_A % 2, count_t1_A / 2);
					count_t1_A++;
				} else {
					t1_P.add(bkonto.getGUIComponents(), count_t1_P % 2, count_t1_P / 2);
					count_t1_P++;
				}
				break;
			case (2):
				Erfolgskonto ekonto = (Erfolgskonto) konto;
				if (!neueBilanz) {
					ekonto.newContainer();
				}
				if (ekonto.isErtragskonto()) {
					t2_Ertragskonten.add(ekonto.getGUIComponents(), count_t2_Ertragskonten % 4,
							count_t2_Ertragskonten / 4);
					count_t2_Ertragskonten++;
				} else {
					t2_Aufwandskonten.add(ekonto.getGUIComponents(), count_t2_Aufwandskonten % 4,
							count_t2_Aufwandskonten / 4);
					count_t2_Aufwandskonten++;
				}
				break;
			case (3):
				if (!neueBilanz) {
					konto.newContainer();
				}
				t3_Steuerkonten.add(konto.getGUIComponents(), count_t3_Steuerkonten % 4, count_t3_Steuerkonten / 4);
				count_t3_Steuerkonten++;
				break;
			case (4):
				if (!neueBilanz) {
					konto.newContainer();
				}
				abschlusskontoContainer.getChildren().add(konto.getGUIComponents());
				break;
			}
		}
		if (kontenverwaltung.getKonten().get("SBK").getBilanzwert() != -1) {
			DiagrammErstellen dec = new DiagrammErstellen(kontenverwaltung.getKontenArraylist());
			chartContainer1.getChildren().add(dec.getPieChart_BestandAlt());
			chartContainer1.getChildren().add(dec.getPieChart_BestandNeu());
			if (!kontenverwaltung.isAlleErfolgskontenMitBilanzwertNull()) {
				chartContainer2.getChildren().add(dec.getPieChart_Ertrag());
				chartContainer2.getChildren().add(dec.getPieChart_Aufwand());
				chartContainer3.getChildren().add(dec.getBarChart_GuV());
			}

			menuitemJAB.setDisable(true);
			menuitemAddGF.setDisable(true);
			menuitemAddBS.setDisable(true);
		}
	}

	/**
	 * <i><b>Ereignisbehandlung: Datei �ffnen</b></i><br>
	 * <br>
	 * Aus einem Ereignis heraus wird eine bestehende Datei ge�ffnet und der
	 * Kontenverwatung komplett �bergeben <br>
	 * 
	 * @param event
	 *            - Nutzeraktion
	 */
	@FXML
	// Typ DataStorage als R�ckgabewert, damit bestehende F�lle und Konten auf
	// konten und faelle geschrieben werden k�nnen
	private void handle_Datei_Oeffnen(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("�ffne Bilanzdatei");
		fileChooser.setInitialDirectory(
				new File(System.getProperty("user.home") + "\\AppData\\Roaming\\BuF�-HWRVersion\\"));
		File file = fileChooser.showOpenDialog(new Stage());
		DataStorage myStorage = null;
		if (file != null) {
			try {
				myStorage = io.IOManager.readFile(file);
				kontenverwaltung = new Kontenverwaltung(file, myStorage.getKonten(), myStorage.getFaelle(),
						myStorage.getGeschaeftsjahrBeginn());
				enableMenuBar(true);
				ladeKonten(false);
			} catch (IOException e) {
				e.printStackTrace();
				new AlertDialogFrame().showConfirmDialog("Die ausgew�hlte Datei konnte nicht ge�ffnet werden",
						"Die Datei ist nicht kompatibel mit dieser Version!", "Ok", AlertDialogFrame.ERROR_TYPE);
			}
		}
	}

	/**
	 * <i><b>Ereignisbehandlung: Datei Speichern</b></i><br>
	 * <br>
	 * Aus einem Ereignis heraus wird die Datei gespeichert. <br>
	 * 
	 * @param event
	 *            - Nutzeraktion
	 */
	@FXML
	private void handle_Datei_Speichern(ActionEvent event) {
		boolean erfolgreich = IOManager.saveFile(kontenverwaltung.getKonten(), kontenverwaltung.getFaelle(),
				kontenverwaltung.getSpeicherort(), kontenverwaltung.getGeschaeftsjahrBeginn());
		if (erfolgreich) {
			new AlertDialogFrame().showConfirmDialog("Die Bilanz wurde erfolgreich gespeichert",
					"Die Bilanz wurde unter der Datei " + kontenverwaltung.getSpeicherort().getName() + " gespeichert.",
					"Ok", AlertDialogFrame.INFORMATION_TYPE);
		} else {
			new AlertDialogFrame().showConfirmDialog("Speichern nicht erfolgreich",
					"Beim Speichern der Bilanz ist ein Fehler aufgetreten. Die Bilanz konnte nicht gespeichert werden",
					"Ok", AlertDialogFrame.ERROR_TYPE);
		}
	}

	@FXML
	private void handle_Datei_Beenden(ActionEvent event) {
		System.exit(0);
	}

	/**
	 * <i><b>Ereignisbehandlung: Konto Hinzuf�gen</b></i><br>
	 * <br>
	 * Aus einem Ereignis wird eine FXML-Datei geladen und aus dem Controller
	 * dieser FXML-Datei wird die neue Kontenliste �bergeben. <br>
	 * 
	 * @param event
	 *            - Nutzeraktion
	 */
	@FXML
	private void handle_Bearbeiten_Kontenverwaltung(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("menu/bearbeiten/KontenverwaltungAnzeigen.fxml"));
			Scene scene = new Scene(loader.load());
			Stage KontoverwaltungAnzeigen = new Stage();
			KontenverwaltungAnzeigenController controller = loader.getController();
			KontoverwaltungAnzeigen.setResizable(false);
			KontoverwaltungAnzeigen.setScene(scene);
			controller.setKonten(kontenverwaltung.getKonten());
			KontoverwaltungAnzeigen.setTitle(kontenverwaltung.getSpeicherort().getName());
			KontoverwaltungAnzeigen.showAndWait();

			// Ebene 1: neu erstellte Konten werden der Kontenverwaltung
			// �bergeben
			if (controller.isKontenErstellt()) {
				for (Konto konto : controller.getNeueKonten()) {
					kontenverwaltung.addKonto(konto);
					System.out.println(kontenverwaltung.getKonten().size());
				}
				ladeKonten(false);
			}

		} catch (IOException e) {
			new AlertDialogFrame().showConfirmDialog("Interner Fehler",
					"Men�punkt \"Kontoverwaltung\" konnte nicht durchgef�hrt werden.", "Ok",
					AlertDialogFrame.ERROR_TYPE);
			e.printStackTrace();
		}
	}

	/**
	 * <i><b>Ereignisbehandlung: �bersicht aller Gesch�ftsf�lle
	 * �ffnen</b></i><br>
	 * <br>
	 * Aus einem Ereignis heraus wird eine FXML-Datei geladen. Dem Controller
	 * dieser FXML-Datei werden alle Gesch�ftf�lle �bergeben. <br>
	 * 
	 * @param event
	 *            - Nutzeraktion
	 */
	@FXML
	private void handle_Bearbeiten_GF_Uebersicht(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("menu/bearbeiten/UebersichtAnzeigen.fxml"));
			Scene scene = new Scene(loader.load());
			UebersichtanzeigenController controller = loader.getController();
			controller.setFaelle(kontenverwaltung.getFaelle());

			Stage bilanzErstellenStage = new Stage();
			bilanzErstellenStage.setResizable(false);
			bilanzErstellenStage.setScene(scene);
			bilanzErstellenStage.setTitle(kontenverwaltung.getSpeicherort().getName());
			bilanzErstellenStage.showAndWait();
		} catch (IOException e) {
			new AlertDialogFrame().showConfirmDialog("Interner Fehler",
					"Men�punkt \"�bersicht anzeigen\" konnte nicht durchgef�hrt werden.", "Ok",
					AlertDialogFrame.ERROR_TYPE);
			e.printStackTrace();
		}
	}

	/**
	 * <i><b>Ereignisbehandlung: Hinzuf�gen eines neuen
	 * Gesch�ftsfalles</b></i><br>
	 * <br>
	 * Aus einem Ereignis heraus wird eine FXML-Datei geladen. Aus dem
	 * Controller dieser FXML-Datei werden die neuen Gesch�ftsf�lle der
	 * Kontenverwaltung �bergeben. <br>
	 * 
	 * @param event
	 *            - Nutzeraktion
	 */
	@FXML
	private void handle_Bearbeiten_GF_neuerGF(ActionEvent event) {
		try {
			System.out.println(getClass().getResource(""));
			FXMLLoader loader = new FXMLLoader(getClass().getResource("menu/bearbeiten/NeuerGF.fxml"));
			Scene scene = new Scene(loader.load());
			NeuerGFController controller = loader.getController();
			Stage stage = new Stage();
			stage.setResizable(false);
			stage.setTitle("Neuen Gesch�ftsfall erstellen");
			stage.setScene(scene);
			stage.showAndWait();
			if (!controller.isCloseButtonPressed()) {
				kontenverwaltung
						.addGeschaeftsfall(controller.getGeschaeftsfall(kontenverwaltung.getFaelle().size() + 1));
			}

		} catch (IOException e) {
			new AlertDialogFrame().showConfirmDialog("Interner Fehler",
					"Men�punkt \"Neuen Gesch�ftsfall hinzuf�gen\" konnte nicht durchgef�hrt werden.", "Ok",
					AlertDialogFrame.ERROR_TYPE);
			e.printStackTrace();
		}
	}

	/**
	 * <i><b>Ereignisbehandlung: Hinzuf�gen eines Buchungssatzes</b></i><br>
	 * <br>
	 * Aus einem Ereignis heraus wird eine FXML-Datei geladen. Aus dem
	 * Controller dieser FXML-Datei werden die neuen Buchungss�tze der
	 * Kontenverwaltung �bergeben. <br>
	 * 
	 * @param event
	 *            - Nutzeraktion
	 */
	@FXML
	private void handle_Bearbeiten_GF_BSEintragen(ActionEvent event) {
		try {
			System.out.println(getClass().getResource(""));
			FXMLLoader loader = new FXMLLoader(getClass().getResource("menu/bearbeiten/NeuerBS.fxml"));
			Scene scene = new Scene(loader.load());
			NeuerBSController controller = loader.getController();
			controller.setParameter(kontenverwaltung.getKonten(), kontenverwaltung.getFaelle());
			Stage stage = new Stage();
			stage.setResizable(false);
			stage.setTitle("Buchungssatz erstellen");
			stage.setScene(scene);
			stage.showAndWait();
			for (Tuple<Integer, ArrayList<Buchungssatz>> gf : controller.getNeueBuchungssaetze()) {
				kontenverwaltung.addBuchungssatz(kontenverwaltung.getFaelle().get(gf.getX()), gf.getY());
			}
		} catch (IOException e) {
			new AlertDialogFrame().showConfirmDialog("Interner Fehler",
					"Men�punkt \"Buchungssatz einem Gesch�ftsfall hinzuf�gen\" konnte nicht durchgef�hrt werden.", "Ok",
					AlertDialogFrame.ERROR_TYPE);
			e.printStackTrace();
		}
	}

	/**
	 * <i><b>Ereignisbehandlung: Einsehen der Er�ffnungsbilanz</b></i><br>
	 * <br>
	 * Aus einem Ereignis heraus wird eine FXML-Datei geladen. Dem Controller
	 * dieser FXML-Datei werden alle Konten �bergeben. <br>
	 * 
	 * @param event
	 *            - Nutzeraktion
	 */
	@FXML
	private void handle_Analyse_EBEinsehen(ActionEvent event) {
		try {
			System.out.println(getClass().getResource(""));
			FXMLLoader loader = new FXMLLoader(getClass().getResource("menu/analyse/EroeffnungsbilanzEinsehen.fxml"));
			Scene scene = new Scene(loader.load());
			Stage BEEinsehenStage = new Stage();
			EroeffnungsbilanzEinsehenController controller = loader.getController();
			BEEinsehenStage.setScene(scene);
			BEEinsehenStage.setResizable(false);

			controller.setKonten(kontenverwaltung.getKontenArraylist());
			BEEinsehenStage.setTitle(kontenverwaltung.getSpeicherort().getName());

			BEEinsehenStage.show();

		} catch (IOException e) {
			new AlertDialogFrame().showConfirmDialog("Interner Fehler",
					"Men�punkt \"Diagramme berechnen\" konnte nicht durchgef�hrt werden.", "Ok",
					AlertDialogFrame.ERROR_TYPE);
			e.printStackTrace();
		}
	}

	/**
	 * <i><b>Ereignisbehandlung: Erstellen einer Schlussbilanz</b></i><br>
	 * <br>
	 * Aus einem Ereignis heraus werden alle Konten der Kontenverwaltung
	 * saldiert. <br>
	 * 
	 * @param event
	 *            - Nutzeraktion
	 */
	@FXML
	private void handle_Analyse_SBErstellen(ActionEvent event) {
		kontenverwaltung.kontensaldierung();
		ladeKonten(false);
	}

	/**
	 * <i><b>Ereignisbehandlung: Anzeigen der Produktinformationen</b></i><br>
	 * <br>
	 * Aus einem Ereignis heraus wird die Produktinformation ge�ffnet in einem
	 * neuen Fenster. <br>
	 * 
	 * @param event
	 *            - Nutzeraktion
	 */
	@FXML
	private void handle_Hilfe_Produktinformationen(ActionEvent event) {
		new AlertDialogFrame().showConfirmDialog("Produktinformationen - Release-Version: v1.0 / 29.08.2017",
				"Die Software wurde im Laufe des Projekts f�r das Modul \"Objektorientierte Programmierung\" erstellt.\n\n"
						+ "(Matrikelnummer) Entwickler der Buchf�hrungssoftware:\n" + "(652059) Sophie-Louise Schmidt\n"
						+ "(674216) Tanja Manlik\n" + "(658024) Emil Tenbieg\n" + "(691071) Marc Tukendorf\n",
				"Ok", AlertDialogFrame.INFORMATION_TYPE);
	}

	/**
	 * <i><b>Ereignisbehandlung: Anzeigen des Handbuches</b></i><br>
	 * <br>
	 * Aus einem Ereignis heraus wird das Handbuch in einer PDF ge�ffnet. <br>
	 * 
	 * @param event
	 *            - Nutzeraktion
	 */
	@FXML
	private void handle_Hilfe_Handbuch(ActionEvent event) {
		try {
			if (Desktop.isDesktopSupported()) {
				File file = new File(System.getProperty("user.home")
						+ "\\AppData\\Roaming\\BuF�-HWRVersion\\data\\Nutzerhandbuch.pdf");
				file.mkdirs();
				file.delete();
				if (!file.exists()) {
					InputStream inputStream = ClassLoader.getSystemClassLoader()
							.getResourceAsStream("application/Nutzerhandbuch.pdf");
					OutputStream outputStream = new FileOutputStream(file);
					byte[] buffer = new byte[1024];
					int length;
					while ((length = inputStream.read(buffer)) > 0) {
						outputStream.write(buffer, 0, length);
					}
					outputStream.close();
					inputStream.close();
				}
				Desktop.getDesktop().open(file);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * <i><b>Ereignisbehandlung: Anzeigen des FAQs</b></i><br>
	 * <br>
	 * Aus einem Ereignis heraus wird die FAQ in einer PDF ge�ffnet. <br>
	 * 
	 * @param event
	 *            - Nutzeraktion
	 */
	@FXML
	private void handle_Hilfe_FAQ(ActionEvent event) {
		try {
			if (Desktop.isDesktopSupported()) {
				File file = new File(
						System.getProperty("user.home") + "\\AppData\\Roaming\\BuF�-HWRVersion\\data\\FAQ.pdf");
				file.mkdirs();
				file.delete();
				if (!file.exists()) {
					InputStream inputStream = ClassLoader.getSystemClassLoader()
							.getResourceAsStream("application/FAQ.pdf");
					OutputStream outputStream = new FileOutputStream(file);
					byte[] buffer = new byte[1024];
					int length;
					while ((length = inputStream.read(buffer)) > 0) {
						outputStream.write(buffer, 0, length);
					}
					outputStream.close();
					inputStream.close();
				}
				Desktop.getDesktop().open(file);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * <i><b>MenuBar aktivieren</b></i><br>
	 * <br>
	 * Aktiviert Items in der MenuBar. <br>
	 * 
	 * @param enable
	 *            - Ob aktiviert, oder nicht
	 */
	private void enableMenuBar(boolean enable) {
		menuBearbeiten.setDisable(!enable);
		menuAnalyse.setDisable(!enable);
		menuitemSpeichern.setDisable(!enable);
		menuitemJAB.setDisable(!enable);
		menuitemAddGF.setDisable(!enable);
		menuitemAddBS.setDisable(!enable);
	}

}

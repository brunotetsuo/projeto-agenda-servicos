package gui;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import db.DbIntegrityException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Utils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.entities.Client;
import model.services.ClientService;

public class FinancialController implements Initializable, DataChangeListener {

	private ClientService service;

	@FXML
	private TableView<Client> tableViewClient;

	@FXML
	private TableColumn<Client, String> tableColumnName;
	
	@FXML
	private TableColumn<Client, String> tableColumnPhone;
	
	@FXML
	private TableColumn<Client, Date> tableColumnScheduled;
	
	@FXML
	private TableColumn<Client, String> tableColumnTypeService;
	
	@FXML
	private TableColumn<Client, Double> tableColumnPrice;

	@FXML
	private TableColumn<Client, Client> tableColumnEDIT;

	@FXML
	private TableColumn<Client, Client> tableColumnREMOVE;

	@FXML
	private Button btNew;
		
	@FXML
	private Button btDay;
	
	@FXML
	private Button btFindPrice;
	
	@FXML
	private TextField txtFindPrice;
	
	@FXML
	private Label labelResultDay;
	
	@FXML
	private Label labelResultMonth;
	
	@FXML
	private Label labelResultFind;

	private ObservableList<Client> obsList;

	@FXML
	public void onBtNewAction(ActionEvent event) {
		Stage parentStage = Utils.currentStage(event);
		Client obj = new Client();
		createDialogForm(obj, "/gui/ClientForm.fxml", parentStage);
	}
	
	@SuppressWarnings("deprecation")
	@FXML
	public void onBtFindPriceAction() throws ParseException {
		List<Client> list = service.findAll();
		List<Client> res = new ArrayList<>();
 		for (Client c : list) {
 			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			Date d = sdf.parse(txtFindPrice.getText());
			
			if (c.getScheduled().getDate() == d.getDate() && c.getScheduled().getMonth() == d.getMonth()
					&& c.getScheduled().getYear() == d.getYear()) {
				res.add(c);
			}
		}
 		double sum = 0.0;
 		for (Client cl : res) {
 			sum = sum + cl.getPrice();
 		}
 		labelResultFind.setText(String.format("%.2f", sum));		
	}
	
	@SuppressWarnings("deprecation")
	@FXML
	public void onBtDayAction() {
		List<Client> list = service.findAll();
		List<Client> res = new ArrayList<>();
 		for (Client c : list) {
			Date d = new Date();
			if (c.getScheduled().getDay() == d.getDay()) {
				res.add(c);
			}
		}
		obsList = FXCollections.observableArrayList(res);
		tableViewClient.setItems(obsList);
		initEditButtons();
		initRemoveButtons();
	}
	
	@SuppressWarnings("deprecation")
	@FXML
	public void resultDay() {
		List<Client> list = service.findAll();
		List<Client> res = new ArrayList<>();
 		for (Client c : list) {
			Date d = new Date();
			if (c.getScheduled().getDay() == d.getDay()) {
				res.add(c);
			}
		}
 		double sum = 0.0;
		for (Client cl : res) {
			sum = sum + cl.getPrice();
		}
		labelResultDay.setText(String.format("%.2f", sum));
	}
	
	@SuppressWarnings("deprecation")
	@FXML
	public void resultMonth() {
		List<Client> list = service.findAll();
		List<Client> res = new ArrayList<>();
 		for (Client c : list) {
			Date d = new Date();
			if (c.getScheduled().getMonth() == d.getMonth()) {
				res.add(c);
			}
		}
 		double sum = 0.0;
		for (Client cl : res) {
			sum = sum + cl.getPrice();
		}
		labelResultMonth.setText(String.format("%.2f", sum));
	}

	public void setClientService(ClientService service) {
		this.service = service;
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNodes();
	}

	private void initializeNodes() {
		tableColumnName.setCellValueFactory(new PropertyValueFactory<>("name"));
		tableColumnPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
		tableColumnScheduled.setCellValueFactory(new PropertyValueFactory<>("scheduled"));
		Utils.formatTableColumnDate(tableColumnScheduled, "dd/MM/yyyy HH:mm");
		tableColumnTypeService.setCellValueFactory(new PropertyValueFactory<>("typeService"));
		tableColumnPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
		Utils.formatTableColumnDouble(tableColumnPrice, 2);				
	}

	public void updateTableView() {
		if (service == null) {
			throw new IllegalStateException("Service was null");
		}
		List<Client> list = service.findAll();
		obsList = FXCollections.observableArrayList(list);
		tableViewClient.setItems(obsList);
		initEditButtons();
		initRemoveButtons();
		resultDay();
		resultMonth();
	}

	private void createDialogForm(Client obj, String absoluteName, Stage parentStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(absoluteName));
			Pane pane = loader.load();

			ClientFormController controller = loader.getController();
			controller.setClient(obj);
			controller.setServices(new ClientService());
			controller.subscribeDataChangeListener(this);
			controller.updateFormData();

			Stage dialogStage = new Stage();
			dialogStage.setTitle("Enter Client data");
			dialogStage.setScene(new Scene(pane));
			dialogStage.setResizable(false);
			dialogStage.initOwner(parentStage);
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
			Alerts.showAlert("IO Exception", "Error loading view", e.getMessage(), AlertType.ERROR);
		}
	}

	@Override
	public void onDataChanged() {
		updateTableView();
	}

	private void initEditButtons() {
		tableColumnEDIT.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumnEDIT.setCellFactory(param -> new TableCell<Client, Client>() {
			private final Button button = new Button("Editar");

			@Override
			protected void updateItem(Client obj, boolean empty) {
				super.updateItem(obj, empty);
				if (obj == null) {
					setGraphic(null);
					return;
				}
				setGraphic(button);
				button.setOnAction(
						event -> createDialogForm(obj, "/gui/ClientForm.fxml", Utils.currentStage(event)));
			}
		});
	}

	private void initRemoveButtons() {
		tableColumnREMOVE.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumnREMOVE.setCellFactory(param -> new TableCell<Client, Client>() {
			private final Button button = new Button("Deletar");

			@Override
			protected void updateItem(Client obj, boolean empty) {
				super.updateItem(obj, empty);
				if (obj == null) {
					setGraphic(null);
					return;
				}
				setGraphic(button);
				button.setOnAction(event -> removeEntity(obj));
			}
		});
	}

	private void removeEntity(Client obj) {
		Optional<ButtonType> result = Alerts.showConfirmation("Confirmation", "Are you sure to delete?");
		
		if (result.get() == ButtonType.OK) {
			if (service == null) {
				throw new IllegalStateException("Service was null");
			}
			try {
				service.remove(obj);
				updateTableView();
			}
			catch (DbIntegrityException e) {
				Alerts.showAlert("Error removing object", null, e.getMessage(), AlertType.ERROR);
			}
		}
	}
}
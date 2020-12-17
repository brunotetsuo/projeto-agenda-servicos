package gui;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.entities.Client;
import model.exceptions.ValidationException;
import model.services.ClientService;

public class ClientFormController implements Initializable {

	private Client entity;

	private ClientService service;

	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();

	@FXML
	private TextField txtName;

	@FXML
	private TextField txtPhone;

	@FXML
	private TextField txtScheduled;
	
	@FXML
	private TextField txtTypeService;

	@FXML
	private TextField txtPrice;

	@FXML
	private Label labelErrorName;

	@FXML
	private Label labelErrorPhone;

	@FXML
	private Label labelErrorScheduled;
	
	@FXML
	private Label labelErrorTypeService;

	@FXML
	private Label labelErrorPrice;

	@FXML
	private Button btSave;

	@FXML
	private Button btCancel;

	public void setClient(Client entity) {
		this.entity = entity;
	}

	public void setServices(ClientService service) {
		this.service = service;
	}

	public void subscribeDataChangeListener(DataChangeListener listener) {
		dataChangeListeners.add(listener);
	}

	@FXML
	public void onBtSaveAction(ActionEvent event) throws ParseException {
		if (entity == null) {
			throw new IllegalStateException("Entity was null");
		}
		if (service == null) {
			throw new IllegalStateException("Service was null");
		}
		try {
			entity = getFormData();
			service.saveOrUpdate(entity);
			notifyDataChangeListeners();
			Utils.currentStage(event).close();
		} catch (ValidationException e) {
			setErrorMessages(e.getErrors());
		} catch (DbException e) {
			Alerts.showAlert("Error saving object", null, e.getMessage(), AlertType.ERROR);
		}

	}

	private void notifyDataChangeListeners() {
		for (DataChangeListener listener : dataChangeListeners) {
			listener.onDataChanged();
		}
	}

	private Client getFormData() throws ParseException {
		Client obj = new Client();

		ValidationException exception = new ValidationException("Validation error");

		if (txtName.getText() == null || txtName.getText().trim().equals("")) {
			exception.addError("name", "Field can't be empty");
		}
		obj.setName(txtName.getText());
		
		if (txtPhone.getText() == null || txtPhone.getText().trim().equals("")) {
			exception.addError("phone", "Field can't be empty");
		}
		obj.setPhone(txtPhone.getText());
		
		if (txtScheduled.getText() == null) {
			exception.addError("scheduled", "Field can't be empty");
		}
		else {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			Date date = sdf.parse(txtScheduled.getText());
			obj.setScheduled(date);
		}
		
		if (txtTypeService.getText() == null || txtTypeService.getText().trim().equals("")) {
			exception.addError("typeService", "Field can't be empty");
		}
		obj.setTypeService(txtTypeService.getText());
		
		if (txtPrice.getText() == null || txtPrice.getText().trim().equals("")) {
			exception.addError("price", "Field can't be empty");
		}
		obj.setPrice(Utils.tryParseToDouble(txtPrice.getText()));
		
		if (exception.getErrors().size() > 0) {
			throw exception;
		}

		return obj;
	}

	@FXML
	public void onBtCancelAction(ActionEvent event) {
		Utils.currentStage(event).close();
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNodes();
	}

	private void initializeNodes() {
		Constraints.setTextFieldMaxLength(txtName, 70);
		Constraints.setTextFieldDouble(txtPrice);
		Constraints.setTextFieldInteger(txtPhone);
		Constraints.setTextFieldMaxLength(txtPhone, 11);
		Constraints.setTextFieldMaxLength(txtScheduled, 16);
		Constraints.setTextFieldMaxLength(txtTypeService, 70);
	}

	public void updateFormData() {
		if (entity == null) {
			throw new IllegalStateException("Entity was null");
		}
		txtName.setText(entity.getName());
		txtPhone.setText(entity.getPhone());
		Locale.setDefault(Locale.US);
		txtPrice.setText(String.format("%.2f", entity.getPrice()));
		if (entity.getScheduled() != null) {
		txtScheduled.setText(entity.getScheduled().toString());
		}
		txtTypeService.setText(entity.getTypeService());
	}

	private void setErrorMessages(Map<String, String> errors) {
		Set<String> fields = errors.keySet();

		labelErrorName.setText((fields.contains("name") ? errors.get("name") : ""));
		labelErrorPhone.setText((fields.contains("phone") ? errors.get("phone") : ""));
		labelErrorScheduled.setText((fields.contains("scheduled") ? errors.get("scheduled") : ""));
		labelErrorPrice.setText((fields.contains("price") ? errors.get("price") : ""));
		labelErrorTypeService.setText((fields.contains("typeService") ? errors.get("typeService") : ""));
	}
}
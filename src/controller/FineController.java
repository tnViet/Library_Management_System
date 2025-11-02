package controller;

import dao.FineDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Fine;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

public class FineController implements Initializable {

    @FXML private TableView<Fine> fineTable;
    @FXML private TableColumn<Fine, Integer> colId;
    @FXML private TableColumn<Fine, Integer> colLoanId;
    @FXML private TableColumn<Fine, Double> colAmount;
    @FXML private TableColumn<Fine, Boolean> colPaid;
    @FXML private TableColumn<Fine, LocalDate> colFineDate;

    @FXML private TextField txtLoanId;
    @FXML private TextField txtAmount;
    @FXML private CheckBox chkPaid;
    @FXML private DatePicker dpFineDate;
    @FXML private TextField txtSearch;

    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;

    private FineDAO fineDAO;
    private ObservableList<Fine> fineList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fineDAO = new FineDAO();
        fineList = FXCollections.observableArrayList();

        // Thiết lập các cột trong bảng
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colLoanId.setCellValueFactory(new PropertyValueFactory<>("loanId"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colPaid.setCellValueFactory(new PropertyValueFactory<>("paid"));
        colFineDate.setCellValueFactory(new PropertyValueFactory<>("fineDate"));

        // Custom cell factory cho cột Paid để hiển thị đẹp hơn
        colPaid.setCellFactory(column -> new TableCell<Fine, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Đã thanh toán" : "Chưa thanh toán");
                    setStyle(item ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        });

        // Load dữ liệu ban đầu
        loadFines();

        // Lắng nghe sự kiện chọn hàng trong bảng
        fineTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        showFineDetails(newSelection);
                    }
                }
        );

        // Thiết lập giá trị mặc định cho DatePicker
        dpFineDate.setValue(LocalDate.now());
    }

    private void loadFines() {
        try {
            fineList.clear();
            fineList.addAll(fineDAO.getAllFines());
            fineTable.setItems(fineList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách phạt: " + e.getMessage());
        }
    }

    private void showFineDetails(Fine fine) {
        txtLoanId.setText(String.valueOf(fine.getLoanId()));
        txtAmount.setText(String.valueOf(fine.getAmount()));
        chkPaid.setSelected(fine.isPaid());
        dpFineDate.setValue(fine.getFineDate());
    }

    @FXML
    private void handleAdd() {
        if (validateInput()) {
            try {
                Fine fine = new Fine(
                        Integer.parseInt(txtLoanId.getText()),
                        Double.parseDouble(txtAmount.getText()),
                        chkPaid.isSelected(),
                        dpFineDate.getValue()
                );

                fineDAO.addFine(fine);
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm phạt mới thành công!");
                loadFines();
                clearFields();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm phạt: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleUpdate() {
        Fine selectedFine = fineTable.getSelectionModel().getSelectedItem();
        if (selectedFine == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một phạt để cập nhật!");
            return;
        }

        if (validateInput()) {
            try {
                selectedFine.setLoanId(Integer.parseInt(txtLoanId.getText()));
                selectedFine.setAmount(Double.parseDouble(txtAmount.getText()));
                selectedFine.setPaid(chkPaid.isSelected());
                selectedFine.setFineDate(dpFineDate.getValue());

                fineDAO.updateFine(selectedFine);
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật phạt thành công!");
                loadFines();
                clearFields();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật phạt: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDelete() {
        Fine selectedFine = fineTable.getSelectionModel().getSelectedItem();
        if (selectedFine == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một phạt để xóa!");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận");
        confirmAlert.setHeaderText("Xóa phạt");
        confirmAlert.setContentText("Bạn có chắc chắn muốn xóa phạt này?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                fineDAO.deleteFine(selectedFine.getId());
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xóa phạt thành công!");
                loadFines();
                clearFields();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa phạt: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear() {
        clearFields();
        fineTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleSearch() {
        String searchText = txtSearch.getText().trim();
        if (searchText.isEmpty()) {
            loadFines();
            return;
        }

        try {
            int loanId = Integer.parseInt(searchText);
            fineList.clear();
            fineList.addAll(fineDAO.getFinesByLoanId(loanId));
            fineTable.setItems(fineList);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập số Loan ID hợp lệ!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tìm kiếm: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        if (txtLoanId.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập Loan ID!");
            return false;
        }
        if (txtAmount.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập số tiền phạt!");
            return false;
        }
        if (dpFineDate.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn ngày phạt!");
            return false;
        }

        try {
            Integer.parseInt(txtLoanId.getText().trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Loan ID phải là số nguyên!");
            return false;
        }

        try {
            double amount = Double.parseDouble(txtAmount.getText().trim());
            if (amount <= 0) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Số tiền phạt phải lớn hơn 0!");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Số tiền phạt không hợp lệ!");
            return false;
        }

        return true;
    }

    private void clearFields() {
        txtLoanId.clear();
        txtAmount.clear();
        chkPaid.setSelected(false);
        dpFineDate.setValue(LocalDate.now());
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
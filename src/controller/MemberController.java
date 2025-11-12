package controller;

import dao.MemberDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Member;

import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class MemberController implements Initializable {

    @FXML private TableView<Member> memberTable;
    @FXML private TableColumn<Member, Integer> colId;
    @FXML private TableColumn<Member, String> colName;
    @FXML private TableColumn<Member, String> colEmail;
    @FXML private TableColumn<Member, String> colPhone;
    @FXML private TableColumn<Member, String> colAddress;

    @FXML private TextField txtName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private TextArea txtAddress;
    @FXML private TextField txtSearch;

    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;

    private MemberDAO memberDAO;
    private ObservableList<Member> memberList;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(0|\\+84)[0-9]{9,10}$"
    );

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        memberDAO = new MemberDAO();
        memberList = FXCollections.observableArrayList();

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));

        // Custom cell factory cho cột Email (hiển thị với icon)
        colEmail.setCellFactory(column -> new TableCell<Member, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #1976D2;");
                }
            }
        });

        // Load dữ liệu ban đầu
        loadMembers();

        // Lắng nghe sự kiện chọn hàng trong bảng
        memberTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        showMemberDetails(newSelection);
                    }
                }
        );

        // Giới hạn số ký tự cho phone
        txtPhone.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.length() > 15) {
                txtPhone.setText(oldValue);
            }
        });
    }

    private void loadMembers() {
        try {
            memberList.clear();
            memberList.addAll(memberDAO.getAllMembers());
            memberTable.setItems(memberList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách thành viên: " + e.getMessage());
        }
    }

    private void showMemberDetails(Member member) {
        txtName.setText(member.getName());
        txtEmail.setText(member.getEmail());
        txtPhone.setText(member.getPhone());
        txtAddress.setText(member.getAddress());
    }

    @FXML
    private void handleAdd() {
        if (validateInput()) {
            try {
                // Kiểm tra email đã tồn tại chưa
                if (memberDAO.isEmailExists(txtEmail.getText().trim())) {
                    showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Email này đã được đăng ký!");
                    return;
                }

                Member member = new Member(
                        txtName.getText().trim(),
                        txtEmail.getText().trim().toLowerCase(),
                        txtPhone.getText().trim(),
                        txtAddress.getText().trim()
                );

                memberDAO.addMember(member);
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm thành viên mới thành công!");
                loadMembers();
                clearFields();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm thành viên: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleUpdate() {
        Member selectedMember = memberTable.getSelectionModel().getSelectedItem();
        if (selectedMember == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một thành viên để cập nhật!");
            return;
        }

        if (validateInput()) {
            try {
                String newEmail = txtEmail.getText().trim().toLowerCase();
                if (!newEmail.equals(selectedMember.getEmail()) &&
                        memberDAO.isEmailExists(newEmail)) {
                    showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Email này đã được đăng ký!");
                    return;
                }

                selectedMember.setName(txtName.getText().trim());
                selectedMember.setEmail(newEmail);
                selectedMember.setPhone(txtPhone.getText().trim());
                selectedMember.setAddress(txtAddress.getText().trim());

                memberDAO.updateMember(selectedMember);
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật thành viên thành công!");
                loadMembers();
                clearFields();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật thành viên: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDelete() {
        Member selectedMember = memberTable.getSelectionModel().getSelectedItem();
        if (selectedMember == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một thành viên để xóa!");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận");
        confirmAlert.setHeaderText("Xóa thành viên");
        confirmAlert.setContentText("Bạn có chắc chắn muốn xóa thành viên: " + selectedMember.getName() + "?\n" +
                "Lưu ý: Tất cả phiếu mượn của thành viên này cũng sẽ bị xóa!");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                memberDAO.deleteMember(selectedMember.getId());
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xóa thành viên thành công!");
                loadMembers();
                clearFields();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa thành viên: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear() {
        clearFields();
        memberTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleSearch() {
        String searchText = txtSearch.getText().trim();
        if (searchText.isEmpty()) {
            loadMembers();
            return;
        }

        try {
            memberList.clear();
            memberList.addAll(memberDAO.searchMembers(searchText));
            memberTable.setItems(memberList);

            if (memberList.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Không tìm thấy thành viên nào!");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tìm kiếm: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        // Kiểm tra tên
        if (txtName.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập họ tên!");
            txtName.requestFocus();
            return false;
        }
        if (txtName.getText().trim().length() < 2) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Họ tên phải có ít nhất 2 ký tự!");
            txtName.requestFocus();
            return false;
        }

        // Kiểm tra email
        if (txtEmail.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập email!");
            txtEmail.requestFocus();
            return false;
        }
        if (!EMAIL_PATTERN.matcher(txtEmail.getText().trim()).matches()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Email không hợp lệ!\nVí dụ: example@email.com");
            txtEmail.requestFocus();
            return false;
        }

        // Kiểm tra số điện thoại
        if (txtPhone.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập số điện thoại!");
            txtPhone.requestFocus();
            return false;
        }
        if (!PHONE_PATTERN.matcher(txtPhone.getText().trim()).matches()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Số điện thoại không hợp lệ!\nVí dụ: 0912345678 hoặc +84912345678");
            txtPhone.requestFocus();
            return false;
        }

        // Kiểm tra địa chỉ
        if (txtAddress.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập địa chỉ!");
            txtAddress.requestFocus();
            return false;
        }

        return true;
    }

    private void clearFields() {
        txtName.clear();
        txtEmail.clear();
        txtPhone.clear();
        txtAddress.clear();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
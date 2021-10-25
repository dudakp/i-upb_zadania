package sk.dudak.upb.ecdc.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sk.dudak.upb.ecdc.service.CryptService;
import sk.dudak.upb.ecdc.service.SecureKeyService;
import sk.dudak.upb.ecdc.service.impl.CryptServiceImpl;
import sk.dudak.upb.ecdc.service.impl.SecureKeyServiceImpl;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

public class MainController {

    private CryptService cryptService;

    private SecureKeyService secureKeyService;

    @FXML
    public Button btnEncrypt;

    @FXML
    public Button btnDecrypt;

    @FXML
    public Button fileSelect;

    @FXML
    public Button loadEncrypted;

    @FXML
    public Label statusLabel;

    @FXML
    public Label dcStatusLabel;

    private File fileToEncrypt;
    private File encryptedFile;


    public MainController() {
    }

    @FXML
    private void initialize() throws IOException, ClassNotFoundException {
        this.cryptService = new CryptServiceImpl();
        this.secureKeyService = new SecureKeyServiceImpl(cryptService);
    }

    public void handleBtnEncrypt(ActionEvent action) throws Exception {
        if (this.fileToEncrypt != null) {
            long time = this.cryptService.encryptFile(this.fileToEncrypt, this.secureKeyService.getAesSymetricKey(), this.secureKeyService.getPublicKey(), this.secureKeyService.getIv(), this.statusLabel);
        } else {
            System.out.println("No file chosen!");
        }
    }

    public void handleBtnDecrypt(ActionEvent action) throws InvalidAlgorithmParameterException, IOException, InvalidKeyException {
        if (this.encryptedFile != null) {
            long time = this.cryptService.decryptFile(this.encryptedFile, this.secureKeyService.getPrivateKey(), this.secureKeyService.getIv(), this.dcStatusLabel);
        } else {
            System.out.println("No file chosen!");
        }
    }

    public void handleChooseFile(ActionEvent action) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        this.fileToEncrypt = fileChooser.showOpenDialog(new Stage());
    }

    public void handleLoadEncrypted(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        this.encryptedFile = fileChooser.showOpenDialog(new Stage());
    }
}

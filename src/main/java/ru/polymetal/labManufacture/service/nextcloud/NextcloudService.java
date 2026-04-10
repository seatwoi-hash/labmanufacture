package ru.polymetal.labManufacture.service.nextcloud;

import java.io.IOException;

public interface NextcloudService {

    public void uploadFile(String remotePath, byte[] content) throws IOException;

    public void deleteFile(String remotePath) throws IOException;

    byte[] downloadFile(String remotePath) throws IOException;

    public boolean fileExists(String fileName) throws IOException;

}

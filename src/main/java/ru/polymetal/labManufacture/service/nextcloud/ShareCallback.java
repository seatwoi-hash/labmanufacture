package ru.polymetal.labManufacture.service.nextcloud;

import java.io.IOException;

public interface ShareCallback {
    void onShareCreated(String shareUrl, boolean isPdf) throws IOException;

}

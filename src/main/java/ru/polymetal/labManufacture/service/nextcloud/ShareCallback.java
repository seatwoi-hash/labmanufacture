package ru.polymetal.labManufacture.service.nextcloud;

import java.io.IOException;

/**
 * Контракт сервиса ShareCallback.
 *
 * @author Tatarinov Anton
 */
public interface ShareCallback {
    void onShareCreated(String shareUrl, boolean isPdf) throws IOException;

}

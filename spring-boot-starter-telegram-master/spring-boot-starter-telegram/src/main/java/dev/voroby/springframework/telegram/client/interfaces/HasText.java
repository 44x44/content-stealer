package dev.voroby.springframework.telegram.client.interfaces;

import dev.voroby.springframework.telegram.client.TdApi;

public interface HasText {
    TdApi.FormattedText getText();
    void setText(TdApi.FormattedText text);
}

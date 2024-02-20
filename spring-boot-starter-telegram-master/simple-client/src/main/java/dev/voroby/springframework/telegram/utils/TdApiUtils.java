package dev.voroby.springframework.telegram.utils;

import dev.voroby.springframework.telegram.client.TdApi;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Objects;

public class TdApiUtils {
    public static void removeAllLinks(TdApi.FormattedText text) {
        removeAllLinks(text, 0);
    }

    public static void removeAllLinks(TdApi.FormattedText text, int i) {
        if (ArrayUtils.isEmpty(text.entities)) {
            return;
        }
        TdApi.TextEntity textEntity = text.entities[i];
        if (textEntity.type instanceof TdApi.TextEntityTypeTextUrl textUrl && textUrl.url.matches("https://t\\.me/.*")) {
            text.text = text.text.substring(0, textEntity.offset) +
                (text.text.length() > textEntity.offset + textEntity.length ?
                    text.text.substring(textEntity.offset + textEntity.length) : "");
            text.entities = ArrayUtils.remove(text.entities, 0);
        } else {
            i++;
        }
        removeAllLinks(text, i);
    }

    public static void addLink(TdApi.FormattedText text, String caption, String link) {
        if (text == null || caption == null || link == null) return;
        TdApi.TextEntity textEntity = new TdApi.TextEntity(text.text.length(), caption.length(), new TdApi.TextEntityTypeTextUrl(link));
        text.text += "\n" + caption;
        text.entities = text.entities == null ? new TdApi.TextEntity[]{textEntity} : ArrayUtils.add(text.entities, textEntity);
    }
}

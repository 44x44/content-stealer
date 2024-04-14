package dev.voroby.springframework.telegram.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageIds {
    private String ids;

    public void addId(String id) {
        if (this.ids == null) this.ids = id;
        else this.ids += ";" + id;
    }
}

package leaguehub.leaguehubbackend.entity.constant;

import lombok.Getter;

@Getter
public enum GlobalConstant {
    NO_DATA("NO_DATA");

    private final String data;

    GlobalConstant(String data) {
        this.data = data;
    }
}

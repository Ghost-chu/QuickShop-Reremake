package org.maxgamer.quickshop.util.holder;

import lombok.Getter;
import lombok.Setter;

public class Result {

    public static final Result SUCCESS = new Result() {
        @Override
        public String getMessage() {
            return "";
        }

        @Override
        public void setMessage(String message) {
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public void setResult(boolean result) {
        }
    };
    @Setter
    private boolean result = false;
    @Setter
    @Getter
    private String message;


    public Result() {
    }

    public Result(String message) {
        result = false;
        this.message = message;
    }

    public boolean isSuccess() {
        return result;
    }
}

package com.yunxin.protocol;

import lombok.Data;

@Data
public class LoginResponse {
    private String randKey;
    private String sessionKey;
}

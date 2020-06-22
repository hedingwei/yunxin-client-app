package com.yunxin.protocol;

import lombok.Data;

@Data
public class LoginRequest {
    private String deviceId;
    private String randKey;
}

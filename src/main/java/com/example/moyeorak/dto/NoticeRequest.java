package com.example.moyeorak.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeRequest {
    private String title;
    private String content;
    private Long regionId;
}

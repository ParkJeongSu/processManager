package kr.co.aim.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ChartPointDto {
    private String time;  // X축: "10:15"
    private double value; // Y축: 15.5
}

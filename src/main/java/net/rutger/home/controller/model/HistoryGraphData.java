package net.rutger.home.controller.model;

import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@ToString
public class HistoryGraphData {
    List<String> labels = new ArrayList<>();
    List<Double> precipitation = new ArrayList<>();
    List<Double> makkink = new ArrayList<>();
    List<Integer> duration = new ArrayList<>();

}

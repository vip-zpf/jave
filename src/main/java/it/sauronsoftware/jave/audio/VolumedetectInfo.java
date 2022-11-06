package it.sauronsoftware.jave.audio;

import lombok.Data;

import java.util.Map;

@Data
public class VolumedetectInfo {
    private String meanVolume;
    private String maxVolume;
    private Map<String, String> histogramMap;
}

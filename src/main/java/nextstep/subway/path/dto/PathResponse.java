package nextstep.subway.path.dto;

import nextstep.subway.line.domain.Distance;
import nextstep.subway.station.dto.StationResponse;

import java.util.List;

public class PathResponse {
    private List<StationResponse> stations;
    private double distance;

    public PathResponse() {
    }

    public PathResponse(List<StationResponse> stations, double distance) {
        this.stations = stations;
        this.distance = distance;
    }

    public List<StationResponse> getStations() {
        return stations;
    }

    public double getDistance() {
        return distance;
    }
}

package nextstep.subway.path.application;

import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.path.domain.PathFinder;
import nextstep.subway.path.dto.PathRequest;
import nextstep.subway.path.dto.PathResponse;
import nextstep.subway.station.application.StationService;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.dto.StationResponses;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class PathService {
    private LineRepository lineRepository;
    private StationService stationService;

    public PathService(LineRepository lineRepository, StationService stationService) {
        this.lineRepository = lineRepository;
        this.stationService = stationService;
    }

    public PathResponse findShortestPath(PathRequest pathRequest) {
        Station source = stationService.findById(pathRequest.getSource());
        Station target = stationService.findById(pathRequest.getTarget());
        PathFinder pathFinder = new PathFinder(lineRepository.findAll());
        StationResponses stationResponses = new StationResponses(pathFinder.findPathStations(source, target));
        BigDecimal shortestPathLength = pathFinder.findPathLength(source, target);
        return new PathResponse(stationResponses.getList(), shortestPathLength.doubleValue());
    }
}

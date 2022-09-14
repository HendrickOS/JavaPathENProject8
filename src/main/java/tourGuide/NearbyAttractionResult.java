package tourGuide;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;

import java.util.List;

public class NearbyAttractionResult {

    VisitedLocation visitedLocation;
    List<Attraction> attractionList;
    List<Double> distances;
    List<Integer> rewards;

}

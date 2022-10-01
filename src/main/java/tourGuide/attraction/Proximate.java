package tourGuide.attraction;

import gpsUtil.location.Attraction;

public class Proximate implements Comparable<Proximate> {
        Attraction attraction;
        Double distance;

    @Override
    public int compareTo(Proximate o) {
        return distance.compareTo(o.distance);
    }

    public Attraction getAttraction() {
        return attraction;
    }

    public void setAttraction(Attraction attraction) {
        this.attraction = attraction;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }
}

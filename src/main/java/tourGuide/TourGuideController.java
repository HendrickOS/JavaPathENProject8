package tourGuide;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.jsoniter.annotation.JsonObject;
import gpsUtil.location.Attraction;
import jdk.nashorn.api.scripting.JSObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jsoniter.output.JsonStream;

import gpsUtil.location.VisitedLocation;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tripPricer.Provider;

@RestController
public class TourGuideController {

	@Autowired
	TourGuideService tourGuideService;
	
    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }
    
    @RequestMapping("/getLocation") 
    public String getLocation(@RequestParam String userName) {
    	VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
		return JsonStream.serialize(visitedLocation.location);
    }

    @RequestMapping("/getNearbyAttractions") 
    public String getNearbyAttractions(@RequestParam String userName) {
    	VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
    	return JsonStream.serialize(tourGuideService.getNearByAttractions(visitedLocation));
    }

    @RequestMapping("/getNearbyAttractionsEdit")
    public String getNearbyAttractionsEdit(@RequestParam String userName) {
        NearbyAttractionResult result = new NearbyAttractionResult();

        VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));

        List<Attraction> fiveClosestAttractions = tourGuideService.getFiveClosestAttractions(visitedLocation);

        List<Double> distances = tourGuideService.getDistancesUserAttractions(fiveClosestAttractions, getUser(userName));

        List<Integer> rewards = tourGuideService.getRewardPointsList(fiveClosestAttractions, getUser(userName));

        result.visitedLocation = visitedLocation;
        result.attractionList = fiveClosestAttractions;
        result.distances = distances;
        result.rewards = rewards;

        return JsonStream.serialize(result);
    }
    
    @RequestMapping("/getRewards") 
    public String getRewards(@RequestParam String userName) {
    	return JsonStream.serialize(tourGuideService.getUserRewards(getUser(userName)));
    }
    
    @RequestMapping(value = "/getAllCurrentLocations", produces = { "application/json" })
    public String getAllCurrentLocations() {

        List<User> usersList = tourGuideService.getAllUsers();

        StringBuilder result = new StringBuilder();
        result.append(" {\n");
        result.append(" ");
        for (User user : usersList){

            result.append(user.getUserId());
            result.append(" : ");

            int sizeOfVisitedLocationsList = user.getVisitedLocations().size()-1;
            List<VisitedLocation> visitedLocationsList = user.getVisitedLocations();
            VisitedLocation lastVisitedLocation = visitedLocationsList.get(sizeOfVisitedLocationsList);
            result.append(JsonStream.serialize(lastVisitedLocation.location));
            result.append(", \n ");
        }
        result.append("}");
        return result.toString();
    }
    
    @RequestMapping("/getTripDeals")
    public String getTripDeals(@RequestParam String userName) {
    	List<Provider> providers = tourGuideService.getTripDeals(getUser(userName));
    	return JsonStream.serialize(providers);
    }
    
    private User getUser(String userName) {
    	return tourGuideService.getUser(userName);
    }
   

}
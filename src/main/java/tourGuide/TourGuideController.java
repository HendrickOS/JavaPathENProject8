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
    
    //  TODO: Change this method to no longer return a List of Attractions.
 	//  Instead: Get the closest five tourist attractions to the user - no matter how far away they are.
 	//  Return a new JSON object that contains:
    	// Name of Tourist attraction, 
        // Tourist attractions lat/long, 
        // The user's location lat/long, 
        // The distance in miles between the user's location and each of the attractions.
        // The reward points for visiting each Attraction.
        //    Note: Attraction reward points can be gathered from RewardsCentral
    @RequestMapping("/getNearbyAttractions") 
    public String getNearbyAttractions(@RequestParam String userName) {
    	VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
    	return JsonStream.serialize(tourGuideService.getNearByAttractions(visitedLocation));
    }

    @RequestMapping("/getNearbyAttractionsEdit")
    public String getNearbyAttractionsEdit(@RequestParam String userName) {
        NearbyAttractionResult result = new NearbyAttractionResult();

        /* User's Location */
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));

        /* Five Closest Attraction */
        List<Attraction> fiveClosestAttractions = tourGuideService.getFiveClosestAttractions(visitedLocation);

        /* Distance in miles between the user's location and each of the attractions. */
        List<Double> distances = tourGuideService.getDistancesUserAttractions(fiveClosestAttractions, getUser(userName));

        /* Reward points for visiting each Attraction. */
        List<Integer> rewards = tourGuideService.getRewardPointsList(fiveClosestAttractions, getUser(userName));

        result.visitedLocation = visitedLocation;
        result.attractionList = fiveClosestAttractions;
        result.distances = distances;
        result.rewards = rewards;

        return JsonStream.serialize(result);
    }

    /* Pour tester la liste obtenue */
//    @RequestMapping("/getFiveClosestAttractions")
//    public List<Attraction> getFiveAttraction(@RequestParam String userName){
//
//        VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
//
//        return tourGuideService.getFiveClosestAttractions(visitedLocation);
//    }
    
    @RequestMapping("/getRewards") 
    public String getRewards(@RequestParam String userName) {
    	return JsonStream.serialize(tourGuideService.getUserRewards(getUser(userName)));
    }
    
    @RequestMapping(value = "/getAllCurrentLocations", produces = { "application/json" })
    public String getAllCurrentLocations() {
    	// TODO: Get a list of every user's most recent location as JSON
    	//- Note: does not use gpsUtil to query for their current location, 
    	//        but rather gathers the user's current location from their stored location history.
    	//
    	// Return object should be the just a JSON mapping of userId to Locations similar to:
    	//     {
    	//        "019b04a9-067a-4c76-8817-ee75088c3822": {"longitude":-48.188821,"latitude":74.84371} 
    	//        ...
    	//     }

        /* Liste des users */
        List<User> usersList = tourGuideService.getAllUsers();

        /* User's Location History */
        StringBuilder result = new StringBuilder();
        result.append(" {\n");
        result.append(" ");
        for (User user : usersList){

            result.append(user.getUserId());
            result.append(" : ");

            int sizeOfVisitedLocationsList = user.getVisitedLocations().size()-1; /* Dernier élément d'une liste : list.size()-1 */
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
package tourGuide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.attraction.Proximate;
import tourGuide.helper.InternalTestHelper;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final GpsUtil gpsUtil;
	private final RewardsService rewardsService;
	private final TripPricer tripPricer = new TripPricer();
	public final Tracker tracker;
	boolean testMode = true;


	public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
		this.gpsUtil = gpsUtil;
		this.rewardsService = rewardsService;
		
		if(testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}
	
	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}
	
	public VisitedLocation getUserLocation(User user) {
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ?
			user.getLastVisitedLocation() :
			trackUserLocation(user);
		return visitedLocation;
	}
	
	public User getUser(String userName) {
		return internalUserMap.get(userName);
	}
	
	public List<User> getAllUsers() {
		return internalUserMap.values().stream().collect(Collectors.toList());
	}
	
	public void addUser(User user) {
		if(!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}
	
	public List<Provider> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(), 
				user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}
	
	public VisitedLocation trackUserLocation(User user) {
		VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
		user.addToVisitedLocations(visitedLocation);
		rewardsService.calculateRewards(user);
		System.out.println(user.getUserName());

		// ~Solution : Peut-être appelé la méhtode MultiThreading ici
//		List<User> allUsers = new ArrayList<>();
////		allUsers = this.getAllUsers();
//		allUsers.add(user);
//
//		try {
//			rewardsService.calculateRewardsMultiThreading(allUsers, 500);
//		} catch (InterruptedException e){
//			throw new RuntimeException(e);
//		};

		return visitedLocation;
	}

	// New method using MultiThreading
	public void trackUserLocationMultiThreading(List<User> users, int threadNumber) throws InterruptedException {
		List<Thread> threads = new ArrayList<>();
		for (int i=0; i<threadNumber; i++){
			int subListSize = users.size()/threadNumber;
			List<User> tmp = new ArrayList<>();
			tmp.addAll(users.subList(i*subListSize, (i+1)*subListSize));
			Thread myThread = new Thread() {
				List<User> users;
				@Override
				public void run() {
					for (User user: users) {
						trackUserLocation(user);
					}
				}
				public Thread init(List<User> users){
					this.users = users;
					return this;
				}
			}.init(tmp);
			threads.add(myThread);
		}
		for (Thread thread : threads) {
			thread.start();
		}
		for (Thread thread : threads) {
			thread.join();
		}
	}

	public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
		List<Attraction> nearbyAttractions = new ArrayList<>();
		for(Attraction attraction : gpsUtil.getAttractions()) {
			if(rewardsService.isWithinAttractionProximity(attraction, visitedLocation.location)) {
				nearbyAttractions.add(attraction);
			}
		}
		return nearbyAttractions;
	}

	/*New Method*/
	public List<Attraction> getFiveClosestAttractions(VisitedLocation visitedLocation) {
		List<Attraction> fiveClosestAttractions = new ArrayList<>();
		List<Proximate> attractionsList = new ArrayList<>();
		int maxClosestAttraction = 5;
		Proximate proximateAttraction = new Proximate();


		for (Attraction attraction : gpsUtil.getAttractions()){
			proximateAttraction.setAttraction(attraction);
			proximateAttraction.setDistance(rewardsService.getDistance(attraction, visitedLocation.location));
			attractionsList.add(proximateAttraction);
		}

		Collections.sort(attractionsList);

		for (int i=0; i<maxClosestAttraction;i++) {
			fiveClosestAttractions.add(attractionsList.get(i).getAttraction());
		}

		return fiveClosestAttractions;
	}
	/* Distance in miles between the user's location and each of the attractions. */
	public List<Double> getDistancesUserAttractions (List<Attraction> attractions, User user) {
		List<Double> distancesUserAttractions = new ArrayList<>();
		for (Attraction attraction : attractions) {
			Location location = new Location(attraction.latitude, attraction.longitude);
			distancesUserAttractions.add(rewardsService.getDistance(getUserLocation(user).location, location));
		}
		return distancesUserAttractions;
	}

	/* Reward points for visiting each Attraction. */
	public final RewardCentral rewardCentral = new RewardCentral();
	public List<Integer> getRewardPointsList (List<Attraction> attractions, User user) {
		List<Integer> rewardPointsList = new ArrayList<>();
		for (Attraction attraction : attractions) {
			rewardPointsList.add(rewardCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId()));
		}
		return rewardPointsList;
	}
	
	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() { 
		      public void run() {
		        tracker.stopTracking();
		      } 
		    }); 
	}
	
	/**********************************************************************************
	 * 
	 * Methods Below: For Internal Testing
	 * 
	 **********************************************************************************/
	private static final String tripPricerApiKey = "test-server-api-key";
	// Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
	private final Map<String, User> internalUserMap = new HashMap<>();
	private void initializeInternalUsers() {
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);
			
			internalUserMap.put(userName, user);
		});
		logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}
	
	private void generateUserLocationHistory(User user) {
		IntStream.range(0, 3).forEach(i-> {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}
	
	private double generateRandomLongitude() {
		double leftLimit = -180;
	    double rightLimit = 180;
	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}
	
	private double generateRandomLatitude() {
		double leftLimit = -85.05112878;
	    double rightLimit = 85.05112878;
	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}
	
	private Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
	    return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}
	
}

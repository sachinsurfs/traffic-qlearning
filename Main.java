import interfaces.Car;
import interfaces.LearningModule;
import interfaces.RoadMap;
import interfaces.TrafficLight;
import utils.Coords;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main (String[] args) {
        // Inputting arguments, or defaulting to
        // reward 1 and intensity 0.25

        Integer rewardFunction;
        Double trafficIntensity;
        int defaultRewardFunction = 1;
        double defaultTrafficIntensity = 0;
        if (args.length == 2) {
            try {
                rewardFunction = Integer.parseInt(args[0]);
                trafficIntensity = Double.parseDouble(args[1]);
            } catch (Exception e) {
                rewardFunction = defaultRewardFunction;
                trafficIntensity = defaultTrafficIntensity;
                System.err.println("Defaulting to " +
                        "reward " + defaultRewardFunction +
                        " intensity " + defaultTrafficIntensity);
            }
        } else {
            rewardFunction = defaultRewardFunction;
            trafficIntensity = defaultTrafficIntensity;
        }

        final int TRAINING_TIME = 250000;
        //final int TRAINING_TIME = 0;
        final int TRAINING_INTENSITY_INTERVAL = TRAINING_TIME/10; //Change 5 to 4 , to skip 1.0 training intensity

        
        //Simulation parameters
        final int TIMESTEP_INTERVAL = 50;
        final int SIMULATION_TIME = 5000 ;
        final int TESTING_INTENSITY_INTERVAL = SIMULATION_TIME/10;

        final int STEP_TIME = 50; 
        
        //Graphics and runtime parameters
        boolean graphicalOutput = true;
        boolean consoleOutput = false;
        boolean output = graphicalOutput || consoleOutput;
        int score = 0;

        //output parameters
        long iterations = 0;
        long totalCars = 0;
        long totalCarsStopped = 0;
        int maxCarsStopped = 0;

        //Initialise map, list of cars currently on map, and list of
        //trafficlights
        RoadMap map = new RoadMapImpl();
        List<Car> cars = new ArrayList<Car>();
        List<TrafficLight> trafficLights =
                new ArrayList<TrafficLight>();
        trafficLights.add(
                new TrafficLightImpl(new Coords(20, 20),false));
        trafficLights.add(
                new TrafficLightImpl(new Coords(20, 40),true));
        trafficLights.add(
                new TrafficLightImpl(new Coords(40, 20),true));
        trafficLights.add(
                new TrafficLightImpl(new Coords(40, 40),false));

        //Set actionposition based on arg1
        int actionPosition = 1000;
        switch (rewardFunction) {
            case 1:
                actionPosition = 1000;
                break;
            case 2:
            case 3:
                actionPosition = 100000;
                break;
            }
        LearningModule learningModule = new LearningModuleImpl(actionPosition);
        Viewer v = graphicalOutput ? new Viewer() : null;

        //Simulation tests
        int currentTimeStep;


        //Basic logic for each time step
        // - change traffic lights if required - call a function from
        //   'learning' class to do this
        // - move cars in their current direction by velocity (modify
        //   velocity if necessary - using CarAI)
        // - spawn cars at extremities
        // - Now that we have the new state, update the qvalue for the
        //  previous s,a pair

        //TRAINING TIME
        int timeRan = 0;
        for (timeRan = 0; timeRan < TRAINING_TIME; timeRan++) {

            if(timeRan % TESTING_INTENSITY_INTERVAL ==0) {
              trafficIntensity +=0.1;
              //System.out.println(trafficIntensity);
            }
            //Params required to learn
            RoadMap currentState = map.copyMap();
            currentState.addCars(cars);
            List<Boolean> switchedLights;
            List<Integer> states = new ArrayList<Integer>();
            List<Integer> nextStates = new ArrayList<Integer>();
            List<Integer> rewards = new ArrayList<Integer>();

            //Save the states of each traffic light before updating
            for (TrafficLight light: trafficLights) {
                switch (rewardFunction) {
                    case 1:
                        states.add(currentState.stateCode(light));
                        break;
                    case 2:
                        states.add(currentState.stateCode2(light));
                        break;
                    case 3:
                        states.add(currentState.stateCode3(light, cars));
                        break;
                }
            }

            //Use the learned values to update the traffic lights
            switchedLights = learningModule.updateTrafficLights(
                    currentState, trafficLights, timeRan
            );

            //copy updated state of map
            RoadMap nextState = currentState.copyMap();

            //Move cars currently on map
            List<Car> carsToRemove = new ArrayList<Car>();
            for (Car car : cars) {
                car.move(
                        currentState.getClosestTrafficLight(
                                car, trafficLights),
                        nextState);
                int x=car.getCoords().getX(), y=car.getCoords().getY();
                if (x<0 || x>=60 || y<0 || y>=60) {
                     carsToRemove.add(car);
                }
            }
            cars.removeAll(carsToRemove);

            //Spawn cars onto map extremities
            for (Coords roadEntrance : map.getRoadEntrances()) {
                if (
                    Math.random() <= trafficIntensity &&
                    !currentState.carAt(roadEntrance)
                ) {
                    Car c = new CarImpl(
                            new Coords(roadEntrance),
                            map.getStartingVelocity(roadEntrance)
                    );
                    cars.add(c);
                    totalCars ++;
                }
            }
            nextState.addCars(cars);

            //Update statistics
            iterations++;
            int localCarsStopped = 0;
            for (Car car : cars) {
                int dx = car.getVelocity().getXSpeed();
                int dy = car.getVelocity().getYSpeed();
                if (dx == 0 && dy == 0) {
                    localCarsStopped++;
                }
            }
            totalCarsStopped += localCarsStopped;

            if (localCarsStopped > maxCarsStopped) {
                maxCarsStopped = localCarsStopped;
            }

            // Updates q-values
            //calculate reward and state code for each traffic light
            for (TrafficLight light : trafficLights) {
                switch (rewardFunction) {
                    case 1:
                        rewards.add(learningModule.reward(
                                nextState.stateCode(light)));
                        nextStates.add(nextState.stateCode(light));
                        break;
                    case 2:
                        rewards.add(learningModule.reward2(
                                nextState.stateCode2(light)));
                        nextStates.add(nextState.stateCode2(light));
                        break;
                    case 3:
                        rewards.add(learningModule.reward3(
                                nextState.stateCode3(light, cars)));
                        nextStates.add(nextState.stateCode3(light, cars));
                        break;
                }

            }


            learningModule.learn(
                states, switchedLights, rewards, nextStates,
                trafficLights
            );
          }

          trafficIntensity =0.0;
          cars.clear();

          //Simulation time
                   
          for (timeRan = 0; timeRan < SIMULATION_TIME; timeRan++) {
              if(timeRan % TESTING_INTENSITY_INTERVAL ==0) {
                trafficIntensity +=0.1;
                totalCarsStopped = 0;
                //Clearing all cars after  every intensity interval
                cars.clear();

              }
              //Params required to learn
              RoadMap currentState = map.copyMap();
              currentState.addCars(cars);
              List<Boolean> switchedLights;
              List<Integer> states = new ArrayList<Integer>();
              List<Integer> nextStates = new ArrayList<Integer>();
              List<Integer> rewards = new ArrayList<Integer>();

              //Save the states of each traffic light before updating
              for (TrafficLight light: trafficLights) {
                  switch (rewardFunction) {
                      case 1:
                          states.add(currentState.stateCode(light));
                          break;
                      case 2:
                          states.add(currentState.stateCode2(light));
                          break;
                      case 3:
                          states.add(currentState.stateCode3(light, cars));
                          break;
                  }
              }

              //Use the learned values to update the traffic lights
              switchedLights = learningModule.updateTrafficLights(
                      currentState, trafficLights, timeRan
              );

              //copy updated state of map
              RoadMap nextState = currentState.copyMap();

              //Move cars currently on map
              List<Car> carsToRemove = new ArrayList<Car>();
              for (Car car : cars) {
                  car.move(
                          currentState.getClosestTrafficLight(
                                  car, trafficLights),
                          nextState);
                  int x=car.getCoords().getX(), y=car.getCoords().getY();
                  if (x<0 || x>=60 || y<0 || y>=60) {
                       carsToRemove.add(car);
                  }
              }
              cars.removeAll(carsToRemove);

              //Spawn cars onto map extremities
              for (Coords roadEntrance : map.getRoadEntrances()) {
                  if (
                      Math.random() <= trafficIntensity &&
                      !currentState.carAt(roadEntrance)
                  ) {
                      Car c = new CarImpl(
                              new Coords(roadEntrance),
                              map.getStartingVelocity(roadEntrance)
                      );
                      cars.add(c);
                      totalCars ++;
                  }
              }
              nextState.addCars(cars);

              //Update statistics
              iterations++;
              int localCarsStopped = 0;
              for (Car car : cars) {
                  int dx = car.getVelocity().getXSpeed();
                  int dy = car.getVelocity().getYSpeed();
                  if (dx == 0 && dy == 0) {
                      localCarsStopped++;
                  }
              }
              totalCarsStopped += localCarsStopped;


              if (localCarsStopped > maxCarsStopped) {
                  maxCarsStopped = localCarsStopped;
              }
        	  
              
              if (graphicalOutput) {
                  v.view(map, cars, trafficLights);
              }
              if (consoleOutput) {
                  map.print(cars, trafficLights);
              }
              if (output) {
                  try {
                      Thread.sleep(STEP_TIME);
                  } catch (Exception ignored) {}
              }
              for (Car c : cars) {
              int dx = c.getVelocity().getXSpeed();
              int dy = c.getVelocity().getYSpeed();
                  score += dx==0&&dy==0 ? -1 : 0;
              }

          if(timeRan % TIMESTEP_INTERVAL ==0) {
            System.out.println(trafficIntensity+","+((float)totalCarsStopped)/TESTING_INTENSITY_INTERVAL +","+ timeRan% TESTING_INTENSITY_INTERVAL );
        
          }
                
          }
          
          
          
    }

}
